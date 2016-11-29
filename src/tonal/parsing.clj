(ns tonal.parsing
  (:require [tonal.hieronymus :as hiero]))

(defn text->data-structure [file config]
  (hiero/str->data-structure (slurp file) config))