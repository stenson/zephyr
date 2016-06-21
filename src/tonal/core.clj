(ns tonal.core
  (:require [stasis.core :as stasis]
            [tonal.articles :as articles]
            [ring.util.response :as res]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [tonal.html :as html]
            [compojure.core :as c]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :as reload]))

(defn read-config [where]
  (articles/yaml-config-at-path where))

(defn wrap-charset [handler]
  (fn [req]
    (let [resp (handler req)
          ct (get-in resp [:headers "Content-Type"])]
      (if (= "text/html" ct)
        (res/header resp "Content-Type" "text/html;charset=utf-8")
        resp))))

(defn app [where]
  (-> (stasis/serve-pages (fn [] (articles/all-pages (read-config where))))
      (wrap-file (format "%s/assets/" (:root (read-config where))))
      (wrap-content-type)
      (wrap-charset)))

(defn print-site [where]
  (let [{:keys [root] :as config} (read-config where)
        qualify #(str root "/" %)
        printed-dir (qualify "printed")]
    (stasis/empty-directory! printed-dir)
    (stasis/export-pages (articles/all-pages config) printed-dir)))

(def where (atom nil))

(def index
  )

(c/defroutes
  all-routes
  (c/GET "/" [] (fn [_] (html/standard [:h1 "Hello world"])))
  (route/resources "/"))

(def handler
  (-> (handler/site #'all-routes)
      (reload/wrap-reload)
      (ring.middleware.keyword-params/wrap-keyword-params)
      (ring.middleware.params/wrap-params)
      #_(stack/wrap-stacktrace)))

(defn run-localhost []
  (run-server handler {:port 3001}))

(defn -main [& args]
  (do (run-localhost)
      (println "Running on 5002...")))