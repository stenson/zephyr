(ns tonal.articles
  (:require [clojure.java.io :as jio]
            [hieronymus.core :as hieronymus]
            [tonal.render :as render]
            [tonal.rss :as rss]
            [hiccup.core :as h]
            [clj-yaml.core :as yaml]
            [clojure.string :as string]))

(defn- slurp-templates [path]
  (->> (jio/file (str path "/templates/"))
       (file-seq)
       (filter #(re-matches #".*\.mustache$" (.getName %)))
       (map (fn [t]
              [(-> (.getName t)
                   (string/replace #"\.mustache$" ""))
               (slurp t)]))
       (into {})))

(defn yaml-config-at-path [path]
  (if (nil? path)
    (do
      (println "~~ no site ~~")
      nil)
    (let [yaml (yaml/parse-string (slurp (str path "/gloss.yml")))]
      (-> yaml
          (assoc :root path)
          (assoc :templates (slurp-templates path))))))

(defn- drive-download-link [id]
  (format "https://docs.google.com/feeds/download/documents/export/Export?id=%s&exportFormat=txt", id))

(def ^:private memo-article-files (atom nil))

(defn- local-article-files [config]
  (->> (jio/file (str (:root config) "/articles/"))
       (file-seq)
       (filter #(re-matches #".*\.md$" (.getName %)))))

(defn- remote-article-files [config]
  (map drive-download-link (:drive-articles config)))

(defn- article-files [config]
  (if (nil? @memo-article-files)
    (let [locals (local-article-files config)
          remotes (remote-article-files config)
          a-mod (or (:article-mod config) identity)]
      (->> (concat locals remotes)
           (map #(assoc (a-mod (hieronymus/parse (slurp %) config)) :file %))
           (filter #(not (:draft %)))
           (map #(vec [(:slug %) %]))
           (into {})
           (reset! memo-article-files)))
    @memo-article-files))

(defn- sorted-articles [config]
  (->> config
       (article-files)
       (vals)
       (sort-by #(get-in % [:date :unix]))
       (reverse)))

(defn- group-articles-by-month [config]
  (->> (sorted-articles config)
       (partition-by #(get-in % [:date :month]))
       (map #(hash-map :articles %
                       :date (:date (first %))))))

(defn- get-articles-for-tag [tag config]
  (->> (sorted-articles config)
       (filter #(some #{tag} (:tags %)))))

(defn- render-index [config _]
  (do (reset! memo-article-files nil))
  (let [articles (group-articles-by-month config)
        index-mod (or (:index-mod config) identity)]
    (render/render "index" config (index-mod {:grouped-articles articles}))))

(defn- render-atom [config _]
  (let [articles (sorted-articles config)]
    (rss/atom-xml config articles)))

(defn- render-error [config _]
  (render/render "post" config {:title "Four-hundred & four"
                                :tags nil
                                :date {:string "Nothing at this address."}}))

(defn- render-article [file config _]
  (let [a-mod (or (:article-mod config) identity)
        rendered (a-mod (hieronymus/parse (slurp file) config))]
    (render/render "post" config rendered)))

(defn- render-tagged-index [tag config _]
  (let [articles (get-articles-for-tag tag config)]
    (render/render
      "tagged" config
      {:tag (get-in config [:tags (keyword tag)])
       :grouped-articles {:articles articles}})))

(defn- map-of-all [config]
  (->> (article-files config)
       (vals)
       (map #(vec [(str "/" (:slug %) "/")
                   (partial render-article (:file %) config)]))
       (into {})))

(defn- tagged-indices [config]
  (->> (sorted-articles config)
       (map :tags)
       (flatten)
       (into #{})
       (map
         (fn [tag]
           {(format "/tagged/%s/index.html" tag)
            (partial render-tagged-index tag config)}))
       (apply merge)))

(defn all-pages [config]
  (let [articles (map-of-all config)]
    (-> articles
        (merge (tagged-indices config))
        (assoc "/index.html" (partial render-index config))
        (assoc "/feed.xml" (partial render-atom config))
        (assoc "/error.html" (partial (partial render-error config))))))