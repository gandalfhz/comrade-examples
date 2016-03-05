(ns comrade-examples.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [clojure.string :as s]
            [comrade-examples.store :as store]
            [comrade-examples.handler :refer :all]))

(defn make-request [target-app method uri cookie & data]
  (let
    [string-data (if-not (nil? data) (json/generate-string (first data)))]
    (-> (mock/request method uri)
        (as-> req (if-not (nil? cookie) (mock/header req "cookie" cookie) req))
        (as-> req (if-not (nil? data) (mock/body req string-data) req))
        (as-> req (if-not (nil? data) (mock/content-type req "application/json") req))
        (target-app))))

(defn parse [response]
  {:status (:status response)
   :body (if
           (s/starts-with? (get-in response [:headers "Content-Type"] "") "application/json")
           (json/parse-string (:body response) true)
           (:body response))})

(deftest test-app
  (testing "examples"
    (let
      [admin-login-response
       (make-request app :post "/api/login" nil {:username "admin" :password "admin-password"})
       admin-cookie
       (first (get-in admin-login-response [:headers "Set-Cookie"]))
       user-a-login-response
       (make-request app :post "/api/login" nil {:username "user-a" :password "user-password-a"})
       user-a-cookie
       (first (get-in user-a-login-response [:headers "Set-Cookie"]))
       user-b-login-response
       (make-request app :post "/api/login" nil {:username "user-b" :password "user-password-b"})
       user-b-cookie
       (first (get-in user-b-login-response [:headers "Set-Cookie"]))]

      (is (= (parse (make-request app :get "/api/user/data" user-a-cookie))
             {:status 200 :body []}))
      (is (= (parse (make-request app :get "/api/user/data" user-b-cookie))
             {:status 200 :body []}))

      (is (= (parse (make-request app :post "/api/user/data/key1" user-a-cookie {:value "data1"}))
             {:status 200 :body {:status "ok"}}))
      (is (= (parse (make-request app :post "/api/user/data/key2" user-a-cookie {:value "data2"}))
             {:status 200 :body {:status "ok"}}))
      (is (= (parse (make-request app :post "/api/user/data/key3" user-b-cookie {:value "data3"}))
             {:status 200 :body {:status "ok"}}))

      (is (= (parse (make-request app :get "/api/user/data" user-a-cookie))
             {:status 200 :body ["key1" "key2"]}))
      (is (= (parse (make-request app :get "/api/user/data" user-b-cookie))
             {:status 200 :body ["key3"]}))

      (is (= (parse (make-request app :get "/api/user/data/key1" user-a-cookie))
             {:status 200 :body "data1"}))
      (is (= (parse (make-request app :get "/api/user/data/key2" user-a-cookie))
             {:status 200 :body "data2"}))
      (is (= (parse (make-request app :get "/api/user/data/key3" user-a-cookie))
             {:status 404 :body ""}))
      (is (= (parse (make-request app :get "/api/user/data/key3" user-b-cookie))
             {:status 200 :body "data3"}))
      (is (= (parse (make-request app :get "/api/user/data/key4" user-b-cookie))
             {:status 404 :body ""}))

      (is (= (parse (make-request app :get "/api/user/data/key4" user-b-cookie))
             {:status 404 :body ""}))

      (is (= (parse (make-request app :get "/api/admin/users" admin-cookie))
             {:status 200 :body ["user-a" "user-b"]}))

      ;; Delete user a's keys.
      (is (= (parse (make-request app :delete "/api/user/data/key2" user-a-cookie))
             {:status 200 :body {:status "ok"}}))

      (is (= (parse (make-request app :delete "/api/user/data/key1" user-a-cookie))
             {:status 200 :body {:status "ok"}}))
      (is (= (parse (make-request app :get "/api/user/data" user-a-cookie))
             {:status 200 :body []}))

      ;; Expire user a's session, make sure they can't access the data anymore.
      (store/expire-session! "user-a")
      (is (= (parse (make-request app :get "/api/user/data" user-a-cookie))
             {:status 403 :body {:error "Access denied"}}))

      ;; Delete user b's keys as an admin
      (is (= (parse (make-request app :delete "/api/admin/data/user-b" admin-cookie))
             {:status 200 :body {:status "ok"}}))
      (is (= (parse (make-request app :get "/api/user/data" user-b-cookie))
             {:status 200 :body []})))))
