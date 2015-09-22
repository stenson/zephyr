(ns tonal.parsing
  (:require ))

(defn text->data-structure [file config]
  (hieronymus/parse (slurp file) config))