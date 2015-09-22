(ns tonal.rss
  (:require [clojure.data.xml :as xml]))

(defn- entry [config post]
  [:entry
   [:title (:title post)]
   [:updated (:date post)]
   [:author [:name (:author post)]]
   [:link {:href (format "%s/%s/" (:url config) (:slug post))}]
   [:id (format "urn:%s:feed:post:%s" (:id config) (:slug post))]
   [:content {:type "html"} (:html post)]])

(defn atom-xml [config posts]
  (xml/emit-str
    (xml/sexp-as-element
      [:feed {:xmlns "http://www.w3.org/2005/Atom"}
       [:id (format "urn:%s:feed" (:id config))]
       [:updated (-> posts first :date)]
       [:title {:type "text"} (:title config)]
       [:link {:rel "self" :href (format "%s/atom.xml" (:url config))}]
       (map (partial entry config) posts)])))