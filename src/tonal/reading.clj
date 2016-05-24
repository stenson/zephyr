(ns tonal.reading
  (:require [endophile.core :as endo]
            [clojure.string :as string]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.walk :as walk]))

(defn- str->month [str]
  (let [months ["January" "February" "March" "April" "May"
                "June" "July" "August" "September" "October"
                "November" "December"]]
    (+ 1 (.indexOf months str))))

(defn expand-date [date-str]
  "dates must be of type January 1, 2015"
  (let [date-re #"([A-Z][a-z]{1,}) ([0-9]{1,2}), ([0-9]{4})"
        [_ month-name d yyyy] (re-find date-re date-str)
        [year month day] [(Integer/parseInt yyyy)
                          (str->month month-name)
                          (Integer/parseInt d)]
        date (t/date-time year month day)]
    {:string date-str :date date :unix (tc/to-long date)
     :month-name month-name :year year :month month :day day}))

(defn parse-metadata [metadata]
  (->> (string/split (string/join "\n" metadata) #"~")
       (remove string/blank?)
       (map #(string/replace % #"\n" ""))
       (map string/trim)
       (map (fn [s]
              (let [[key-str k] (re-find #"^([^:]+): " s)]
                [(keyword k) (string/replace s key-str "")])))
       (map (fn [[k v]]
              [k (cond
                   (= :date keyword) (expand-date v)
                   (re-find #"^\[(.*)\]$" v)
                   (string/split (string/replace v #"(^\[)|(\]$)" "") #"\s+")
                   :else v)]))
       (into {})))

(defn expand-text-in-element [el]
  el)

(defn expand-text [nodes]
  (->> nodes
       (map (fn [n]
              (if (:attrs n)
                n
                (expand-text-in-element n))))))

(defn parse [md]
  (let [naives (->> (endo/mp md)
                    (endo/to-clj)
                    (remove #(= "\n" %)))
        meta (parse-metadata (:content (first naives)))
        content (expand-text (rest naives))]
    {:meta meta
     :content content}))