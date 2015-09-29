(ns tonal.parsing
  (:require [hieronymus.core :as hieronymus]))

(defn text->data-structure [file config]
  (hieronymus/parse (slurp file) config))