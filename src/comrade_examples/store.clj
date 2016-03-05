(ns comrade-examples.store
  (:require [buddy.core.nonce :as nonce]
            [buddy.core.codecs :as codecs]))

(def data (atom {}))
(def sessions (atom {}))

(defn- random-hex []
  (codecs/bytes->hex (nonce/random-bytes 16)))

(defn authenticate
  "Validate the username and password, setting if the user
  is an admin or a regular user. We in this example also
  create a session token that is both sent back to the user,
  as well as kept on the server side, so that we can
  expire user sessions in the future, if we want."
  [username password]
  (let
    [session
     (cond
       (and (= username "admin") (= password "admin-password"))
       {:username username :session-id (random-hex) :admin true}
       (and (= username "user-a") (= password "user-password-a"))
       {:username username :session-id (random-hex) :user true}
       (and (= username "user-b") (= password "user-password-b"))
       {:username username :session-id (random-hex) :user true})]
    (if-not (nil? session)
      (swap! sessions assoc (:username session) (:session-id session)))
    session))

(defn valid-session?
  "Ensure that the client sent back a session id and that it matches
  what we previously set."
  [username session-id]
  (and (some? session-id) (= ((deref sessions) username) session-id)))

(defn expire-session!
  "Expire the session, rendering the session cookie invalid,
  if the user sends it in the future."
  [username]
  (swap! sessions dissoc username))

(defn add-value!
  "Store a value for the given user and key"
  [username k v]
  (swap! data assoc-in [username k] v))

(defn- nil-to-empty-list [s]
  (if-not (nil? s) s '()))

(defn get-keys
  "Return all the keys for the given user"
  [username]
  (-> (deref data)
      (get username)
      (keys)
      (nil-to-empty-list)))

(defn get-value
  "Fetches the value for the given user and key"
  [username k]
  (-> (deref data)
      (get-in [username k])))

(defn delete
  "Delete either a key for the given user, or all keys for the user."
  ([username]
   (swap! data dissoc username))
  ([username k]
   ;; Until dissoc-in makes it into Clojure, this will work
   ;; as a near substitute (leaving empty dictionaries for
   ;; user with no more keys).
   (swap! data (fn [d] (assoc d username (dissoc (get d username) k))))))

(defn get-users
  "Get all users storing data in the system"
  []
  (-> (deref data)
      (keys)
      (nil-to-empty-list)))
