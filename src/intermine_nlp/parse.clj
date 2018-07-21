(ns intermine-nlp.parse
  (:require [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.nlp :as nlp])
  (:gen-class))

(defn model-parser
  "Generate a parser for an intermine model.
  Consists of 2 productions: one for classes, one for all fields/attributes.
  options:
         :lematize (default = false)
  "
  [model & {:as options}]
  (let [classes (:classes model)
        class-kws (keys classes)
        class-paths (map #(im-path/join-path [%]) class-kws)
        attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
        attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
        attrs (if (:lemmatize options)
                (map (comp nlp/lemmatize name) attr-keys)
                (map name attr-keys))]
    (merge
     {:CLASS (apply alt (map #(string %) class-paths))}
     {:FIELD (apply alt (map #(string %) attrs))})))

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
