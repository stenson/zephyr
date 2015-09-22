(ns tonal.render
  (:require [stencil.core :as stencil]
            [stencil.loader :as loader]
            [clojure.pprint :refer [pprint]]
            [clojure.core.cache :as cache]))

(loader/set-cache (cache/ttl-cache-factory {} :ttl 0))

(defn- current-time-seconds []
  (quot (System/currentTimeMillis) 1000))

(defn- cache-render
  ([template-name metadata key datum]
    (cache-render template-name (assoc metadata key datum)))
  ([template-name data]
   (if-let [template (get-in data [:templates template-name])]
     (stencil/render-string template data)
     (stencil/render
       (loader/load (str "templates/" template-name) :default identity)
       data))))

(defn render [initial-template metadata data]
  (let [config-title (:title metadata)
        local-title (:title data)
        title (if local-title
                (format "%s | %s" local-title config-title)
                config-title)
        meta (merge metadata
                    {:launch-time current-time-seconds
                     :title title
                     :meta-image (:meta-image data)})]
    (->> (cache-render initial-template (merge metadata data))
         (cache-render "shell" (assoc meta :post data) :content)
         (cache-render "layout" (assoc meta :post data) :content))))