(defproject dlgate "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.5"]
                 [hiccup "1.0.3"]
                 [ring/ring-devel "1.1.8"]
                 [com.taoensso/carmine "2.1.2"]
                 [copy-api "0.1.1-SNAPSHOT"]
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]
                 [clj-http "0.7.6"]
                 [me.raynes/fs "1.4.4"]
                 [compojure "1.1.5"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}}
  :main dlgate.web)
