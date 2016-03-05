(ns comrade-examples.handler
  (:require [compojure.core :refer [defroutes routes context GET POST PUT DELETE HEAD]]
            [compojure.route :as route]
            [ring.util.response :as ring-response]
            [ring.middleware.defaults :as ring-defaults]
            [comrade.app :as comrade-app]
            [comrade-examples.store :as store]))

(defn- nil-to-response [response]
  (if-not (nil? response)
    (ring-response/response response)
    (ring-response/not-found "")))

(defroutes admin-api-routes
  (GET "/users" []
       (ring-response/response (store/get-users)))
  (DELETE "/data/:target-username" [target-username :as {{username :username} :identity}]
          (store/delete target-username)
          (ring-response/response {:status "ok"})))

(defroutes user-api-routes
  (GET "/data" {{username :username} :identity}
       (ring-response/response (store/get-keys username)))
  (GET "/data/:k" [k :as {{username :username} :identity}]
       (nil-to-response (store/get-value username k)))
  (POST "/data/:k" [k :as {{username :username} :identity} :as {{value :value} :body}]
        (store/add-value! username k value)
        (ring-response/response {:status "ok"}))
  (DELETE "/data/:k" [k :as {{username :username} :identity}]
          (store/delete username k)
          (ring-response/response {:status "ok"})))

(defroutes api-routes
  (POST "/login" request (comrade-app/login request store/authenticate))
  (GET "/logout" [] comrade-app/logout)
  (context "/admin" [] admin-api-routes)
  (context "/user" [] user-api-routes)
  (route/not-found {:body {:error "Not found"}}))

;; User and admin pages are served as protected resources.
(defroutes site-routes
  (GET "/" [] (ring-response/resource-response "index" {:root "public"}))
  (route/not-found "Not Found"))

(defn allow-access? [{{username :username session-id :session-id} :identity}]
  (store/valid-session? username session-id))

(def app
  (comrade-app/define-app
    :api-routes api-routes
    :site-routes site-routes
    :restrictions {:admin-api #"^/api/admin($|/.*)"
                   :user-api #"^/api/user($|/.*)"
                   :admin-site #"^/admin($|/.*)"
                   :user-site #"^/user($|/.*)"}
    :session {:session-key "KEEP-KEY-SECRET!"
              :cookie-name "comrade-examples-session"
              :max-age (* 60 60 24 30)}
    :allow-access-fn? allow-access?
    ;; Update the defaults so that requests for resources
    ;; with no extension are tagged as Content-Type: text/html.
    :site-defaults (assoc-in
                     ring-defaults/site-defaults
                     [:responses :content-types]
                     {:mime-types {nil "text/html"}})))
