(ns intermine-nlp.parse
  (:require [clojure.pprint]
            [clojure.core.match :refer [match]]
            [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank])
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


(def get-sentences (nlp/make-sentence-detector "resources/nlp_models/en-sent.bin"))
(def tokenize (nlp/make-tokenizer "resources/nlp_models/en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "resources/nlp_models/en-pos-maxent.bin"))
(def chunker (treebank/make-treebank-chunker "resources/nlp_models/en-chunker.bin"))
(def parser (treebank/make-treebank-parser "resources/nlp_models/en-parser-chunking.bin"))

(defn prepare-nlquery
  "Raw natural language queries must end in a period"
  [text]
  (let [last-char (subs text (dec (count text)))]
    (if
        (= last-char ".") text
        (str text "."))))

(defn nlquery-to-chunk
  "Use NLP to convert raw text to chunked form"
  [text]
  (-> text prepare-nlquery tokenize pos-tag chunker))

(defn nlquery-to-tree
  "Use NLP to convert raw text to treebank tree form"
  [text]
  (-> text prepare-nlquery vector parser first treebank/make-tree))

;; (defn parse-phrase
;;   [text]
;;   (let [tree]))

;; (defn text-to-ir
;;   "Match chunked text to internal representation (ir)"
;;   [text]
;;   (let [parsed-chunks (chunk-sentence text)]
;;     ()))
