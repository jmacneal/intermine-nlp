(ns intermine-nlp.core
  (:require [imcljs.fetch :as fetch]
            [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [clojure.string :as string])
  (:gen-class))


(defn read-sentence
  "Read a sentence from a file (for testing)"
  [path]
  (-> path slurp read-string :english))

(defn read-pathquery
  "Read a PathQuery from a file (for testing)"
  [path]
  (-> path slurp read-string :pathquery))

(defn parse-tree-to-query
  "TODO: translate internal representation (ir) to PathQuery (JSON/XML)"
  [tree])


(def transform-view
  {:ORGANISM (fn [text] {:from text})
   :CLASS (fn [text] {:select text})
   :FIELD (fn [text] {:select text})})

(def transform-constraint
  {:CLASS (fn [text] [:from text])
   :FIELD (fn [text] [:select text])})

(def transform-map
  {:QUERY (fn [& children] (remove string? children))
   :VIEW (fn [& children] (insta/transform transform-view children))
   :CONSTR (fn [& children] [:CONSTR (remove string? children)])
   :VALUE (fn [text] [:VALUE text])})


(defn parser-pipeline
  "Generate a parser pipeline for a given InterMine model.
  options:
         :lemmatize (default = false)"
  [model & {:as options}]
  (let [class-lemma-map (->> model
                             :classes
                             keys
                             (map name)
                             (clojure.string/join " ")
                             nlp/lemma-map)
        parser (parse/gen-parser model)
        parser-lemmatized (parse/gen-parser model class-lemma-map)]
    #(cond->> %
        (:lemmatize options) nlp/lemmatize-as-text
        (:lemmatize options) parser-lemmatized
        (not (:lemmatize options)) parser
        (:lemmatize options) (insta/transform transform-map))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
