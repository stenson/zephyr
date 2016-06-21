(ns tonal.rss
  (:require [clojure.data.xml :as xml]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [me.raynes.fs :as fs]))

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

(def rfc822 (partial f/unparse (f/formatters :rfc822)))

(defn podcast-entry [config post]
  (let [audio (format "%s%s" (:url config) (:episode post))]
    [:item
     [:title (:title post)]
     ;[:updated (rfc822 (:date (:date post)))]
     [:author (:author config)]
     [:link (format "%s/%s/" (:url config) (:slug post))]
     [:guid audio]
     [:description (:summary post)]
     [:enclosure {:url audio
                  :length (:episode-samples post)
                  :type "audio/mpeg"}]
     [:category "Podcasts"]
     [:pubDate (rfc822 (:date (:date post)))]               ; needs to be rfc
     [:itunes:author (:author config)]
     [:itunes:explicit "no"]
     [:itunes:subtitle (:summary post)]
     [:itunes:duration (:episode-duration post)]
     (when-let [keywords (:keywords post)]
       [:itunes:keywords keywords])]))

(defn podcast-xml [config posts]
  (-> (xml/sexp-as-element
        [:rss {:xmlns:itunes "http://www.itunes.com/dtds/podcast-1.0.dtd"
               :version 2.0}
         [:channel
          [:title (:title config)]
          [:description (:description config)]
          [:link (:url config)]
          [:language "en-us"]
          [:copyright (str "Copyright " (t/year (t/now)))]
          [:pubDate (rfc822 (-> posts first :date :date))]
          [:docs "http://blogs.law.harvard.edu/tech/rss"]
          [:webMaster (:email config)]
          [:image
           [:url (:podcast-artwork config)]
           [:title (:title config)]
           [:link (:url config)]]
          [:category "Music"]
          [:explicit "no"]
          [:itunes:author (:author config)]
          [:itunes:subtitle (:description config)]
          [:itunes:owner
           [:itunes:name (:author config)]
           [:itunes:email (:email config)]]
          [:itunes:explicit "no"]
          [:itunes:category {:text "Music"}]
          [:itunes:image {:href (:podcast-artwork config)}]
          (map (partial podcast-entry config) (filter :podcast posts))]])
      (xml/emit-str)))