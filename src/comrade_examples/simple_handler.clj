(ns comrade-examples.simple-handler
  (:require [compojure.core :refer [defroutes routes context GET POST]]
            [compojure.route :as route]
            [ring.util.response :as ring-response]
            [comrade.app :as comrade-app]))

;; Define your authentication function. A non-nil response
;; indicates the login was successful.
(defn authenticate [username password]
  (cond
    (and (= username "admin") (= password "admin-password")) {:username username :admin true}
    (and (= username "user") (= password "user-password")) {:username username :user true}))

;; REST routes
(defroutes admin-api-routes (GET "/ping" [] (ring-response/response {:ping "admin"})))
(defroutes user-api-routes (GET "/ping" [] (ring-response/response {:ping "user"})))

(defroutes api-routes
  (POST "/login" request (comrade-app/login request authenticate))
  (GET "/logout" [] comrade-app/logout)
  (context "/admin" [] admin-api-routes)
  (context "/user" [] user-api-routes)
  (route/not-found {:body {:error "Not found"}}))

;; site routes
(defroutes admin-site-routes (GET "/" [] "Admin site root"))
(defroutes user-site-routes (GET "/" [] "User site root"))

(defroutes site-routes
  (GET "/" [] "Site root")
  (context "/admin" request admin-site-routes)
  (context "/user" request user-site-routes)
  (route/not-found "Not Found"))

(def app
  (comrade-app/define-app
    :api-routes api-routes
    :site-routes site-routes
    :restrictions {:admin-api #"^/api/admin($|/.*)"
                   :user-api #"^/api/user($|/.*)"
                   :admin-site #"^/admin($|/.*)"
                   :user-site #"^/user($|/.*)"}
    :session {:session-key "KEEP-KEY-SECRET!"
              :cookie-name "comrade-simple-example"
              :max-age (* 60 60 24 30)}))
