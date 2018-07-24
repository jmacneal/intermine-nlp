(ns intermine-nlp.parse
  (:require [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.nlp :as nlp]
            [clojure.string :as string])
  (:gen-class))

(defn model-parser
  "Generate a parser for an intermine model.
  Consists of 2 productions: one for classes, one for all fields/attributes.
  "
  ([model]
   (let [classes (:classes model)
         class-kws (keys classes)
         class-paths (map name class-kws)
         class-lemma-map (nlp/lemma-map (string/join " " class-paths))
         attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
         attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
         attrs (map name attr-keys)]
     (merge
      {:FIELD (apply alt (map #(string %) attrs))}
      {:CLASS (apply alt (map #(string %) class-paths))})))

  ([model class-lemma-map]
   (let [class-paths (keys class-lemma-map)
         attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
         attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
         attrs (map (comp nlp/lemmatize-as-text name) attr-keys)]
     (merge
      {:FIELD (apply alt (map #(string %) attrs))}
      {:CLASS (apply alt (map #(string %) (vals class-lemma-map)))}))))

(defn gen-parser
  "Generate an instaparse parser object by merging a top-level parser (default: grammar.bnf)
  with a model-specific parser.
  "
  ([model]
   (let [top-grammar (ebnf (slurp "resources/grammar.bnf"))
         model-grammar (model-parser model)]
     (insta/parser (merge top-grammar model-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true)))
  ([model class-lemma-map]
   (let [top-grammar (ebnf (slurp "resources/grammar.bnf"))
         model-grammar (model-parser model class-lemma-map)]
     (insta/parser (merge top-grammar model-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true)))
  ([model top-grammar class-lemma-map]
   (let [model-grammar (model-parser model class-lemma-map)]
     (insta/parser (merge top-grammar model-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true))))
