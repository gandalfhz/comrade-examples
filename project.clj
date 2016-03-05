(defproject comrade-examples/app "0.1.0"
  :description "Combined Site and API examples for comrade"
  :url "https://github.com/gandalfhz/comrade-examples"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [cheshire "5.5.0"]
                 [comrade "0.0.1-SNAPSHOT"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler comrade-examples.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
