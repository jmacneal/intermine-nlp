(ns intermine-nlp.core
  (:require [imcljs.fetch :as fetch]
            [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util])
  (:gen-class))


(defn read-sentence
  "Read a sentence from a file (for testing)"
  [path]
  (-> path slurp read-string :english))

(defn read-pathquery
  "Read a PathQuery from a file (for testing)"
  [path]
  (-> path slurp read-string :pathquery))

(defn ir-to-query
  "TODO: translate internal representation (ir) to PathQuery (JSON/XML)"
  [ir]
  nil)

(defn parse-pipeline
  [model]
  (let [parser (parse/gen-parser model)]
    (comp parser (partial clojure.string/join " ") first nlp/lemmatize)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
