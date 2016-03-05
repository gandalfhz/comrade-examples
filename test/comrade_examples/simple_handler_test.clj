(ns comrade-examples.simple-handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [clojure.string :as s]
            [comrade-examples.handler-test :refer [make-request parse]]
            [comrade-examples.simple-handler :refer :all]))

(deftest test-app
  (testing "simple-example"
    (let
      [admin-login-response
       (make-request app :post "/api/login" nil {:username "admin" :password "admin-password"})
       admin-cookie
       (first (get-in admin-login-response [:headers "Set-Cookie"]))
       user-login-response
       (make-request app :post "/api/login" nil {:username "user" :password "user-password"})
       user-cookie
       (first (get-in user-login-response [:headers "Set-Cookie"]))]

      (is (= (parse (make-request app :get "/api/admin/ping" admin-cookie))
             {:status 200 :body {:ping "admin"}}))
      (is (= (parse (make-request app :get "/api/user/ping" user-cookie))
             {:status 200 :body {:ping "user"}}))

      (is (= (parse (make-request app :get "/admin" admin-cookie))
             {:status 200 :body "Admin site root"}))
      (is (= (parse (make-request app :get "/user" user-cookie))
             {:status 200 :body "User site root"}))
      (is (= (parse (make-request app :get "/" user-cookie))
             {:status 200 :body "Site root"})))))

