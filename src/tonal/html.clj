(ns tonal.html
  (:require [hiccup.page :as hiccup]))

(defn now []
  (quot (System/currentTimeMillis) 1000))

(defn style-link [href]
  [:link {:type "text/css"
          :href (format "%s.css?n=%s" href (now))
          :rel  "stylesheet"}])

(defn js-link [href]
  [:script {:type "text/javascript"
            :src  (format "%s.js?n=%s" href (now))}])

(defn basic [title {:keys [styles scripts]} content]
  (hiccup/html5
    {:lang "en"}
    (list
      [:head
       [:meta {:charset "utf-8"}]
       [:title title]
       (if (string? styles)
         (format [:style {:type "text/css"} styles])
         (list (map style-link styles)))]
      [:body
       (list
         content
         (list (map js-link scripts)))])))

(defn standard [content]
  (basic
    "Zephyr"
    {:styles
     ["/style"
      "https://fonts.googleapis.com/css?family=Ubuntu+Mono:400,400italic,700,700italic"]}
    [:div#container
     [:div#content
      content]]))

(defn plain
  ([body]
   (plain 200 body))
  ([status body]
   {:status status
    :headers {"Content-type" "text/plain"}
    :body body}))