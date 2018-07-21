(ns intermine-nlp.parse
  (:require [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            ;; [instaparse.cfg :refer [ebnf]]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn model-parser
  "Generate a parser for an intermine model.
  Consists of 2 productions: one for classes, one for all fields/attributes."
  [model & [options]]
  (let [classes (:classes model)
        class-kws (keys classes)
        class-paths (map #(im-path/join-path [%]) class-kws)
        attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
        attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
        attrs (map name attr-keys)]
    (merge
     {:CLASS (apply alt (map #(string %) class-paths))}
     {:FIELD (apply alt (map #(string (im-path/join-path [%])) attrs))})
    ))

(defn gen-parser
  "Generate an instaparse parser object by merging a top-level parser (default: grammar.bnf)
  with a model-specific parser."
  ([model]
   (let [top-grammar (ebnf (slurp "resources/grammar.bnf"))
         model-grammar (model-parser model)]
     (insta/parser (merge top-grammar model-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true)))
  ([model top-grammar]
   (let [model-grammar (model-parser model)]
     (insta/parser (merge top-grammar model-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true))))
