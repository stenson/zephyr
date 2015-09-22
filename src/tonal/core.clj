(ns tonal.core
  (:require [stasis.core :as stasis]
            [tonal.articles :as articles]
            [me.raynes.fs :as fs]
            [environ.core :as environ]
            [ring.util.response :as res]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn- read-config [config-mod]
  (if (fn? config-mod)
    (config-mod (articles/yaml-config-at-path (environ/env :config)))
    config-mod))

(defn- wrap-charset [handler]
  (fn [req]
    (let [resp (handler req)
          ct (res/get-header resp "Content-Type")]
      (if (= "text/html" ct)
        (res/header resp "Content-Type" "text/html;charset=utf-8")
        resp))))

(defn app-with-mod
  ([]
    (app-with-mod (fn [handler]
                    (fn [req]
                      (handler req)))
                  identity))
  ([func config-mod]
   (-> (stasis/serve-pages (fn [] (articles/all-pages (read-config config-mod))))
       (wrap-file (format "%s/assets/" (:root (read-config config-mod))))
       (wrap-content-type)
       (wrap-charset)
       (func))))

(def app
  (if (environ/env :config)
    (app-with-mod)))

(defn print-site
  ([]
    (print-site true identity))
  ([include-assets config-mod]
   (let [{:keys [root] :as config} (read-config config-mod)
         qualify #(str root "/" %)
         printed-dir (qualify "printed")]
     (stasis/empty-directory! printed-dir)
     (stasis/export-pages (articles/all-pages config) printed-dir)
     (when (and false include-assets)
       (fs/copy-dir-into (qualify "assets") printed-dir)))))

(defn print-articles []
  (print-site false identity))