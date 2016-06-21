(defproject zephyr "0.1.0-SNAPSHOT"
  :description "Weirdo blogging platform"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [hiccup "1.0.5"]
                 [ring "1.5.0"]
                 [stasis "2.2.2"]
                 [stencil "0.3.4"]
                 [enlive "1.1.6"]
                 [circleci/clj-yaml "0.5.3"]
                 [clj-time "0.8.0"]
                 [environ "0.5.0"]
                 [me.raynes/fs "1.4.6"]
                 [endophile "0.1.2"]
                 [hieronymus "0.2.1-SNAPSHOT"]
                 [http-kit "2.1.19"]
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [compojure "1.1.8"]
                 [com.cemerick/url "0.1.1"]]
  :main tonal.core
  :source-paths ["src"])
