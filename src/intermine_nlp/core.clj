(ns intermine-nlp.core
  (:require [imcljs.fetch :as fetch]
            [intermine-nlp.parse :as parse])
  (:gen-class))

;;; Internal Representation
;;;
;;; All natural language queries will be parsed into an internal representation.
;;; This takes the form of nested maps, with partial SQL-like syntax.
;;; Example:
;; Genes in drosophila of length 20 ->
;;; {:organism "drosophila, :select "Genes", :with "length 20"}

;;; As can be seen, parts of this representation ("Genes", "length 20") will need
;;; to be translated into PathQuery-friendly terms. That is done using matching,
;;; in a secondary phase.

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


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
