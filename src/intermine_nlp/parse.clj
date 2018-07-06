(ns intermine-nlp.parse
  (:require [clojure.pprint]
            [clojure.core.match :refer [match]]
            [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [instaparse.core :as insta]
            ;; [instaparse.cfg :refer [ebnf]]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]])
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
        (or
         (= last-char ".")
         (= last-char "?")
         (= last-char "!")) text
        (str text "."))))

(defn nlquery-to-chunk
  "Use NLP to convert raw text to chunked form"
  [text]
  (-> text prepare-nlquery tokenize pos-tag chunker vec))

(defn nlquery-to-tree
  "Use NLP to convert raw text to treebank tree form"
  [text]
  (-> text prepare-nlquery vector parser first treebank/make-tree))


(defn model-parser
  "Parse an intermine model into an extended Backus-Naur form tree.
  Consists of 2 productions: one for classes, one for all fields/attributes."
  [model]
  (let [classes (:classes model)
        class-kws (keys classes)
        class-paths (map #(im-path/join-path [%]) class-kws)
        attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
        attrs (distinct (flatten (map #(->> % :attrs keys ) attr-map)))]
    (merge
     {:CLASS (apply alt (map #(string %) class-paths))}
     {:FIELD (apply alt (map #(string (im-path/join-path [%])) attrs))})
    ))

(defn gen-parser
  ""
  ([model]
   (let [top-grammar (ebnf (slurp "resources/grammar.bnf"))
         model-grammar (model-parser model)]
     (insta/parser (merge top-grammar model-grammar))))
  ([model top-grammar]
   (let [model-grammar (model-parser model)]
     (insta/parser (merge top-grammar model-grammar)))))

;; (def p (insta/parser (instaparse.cfg/ebnf (slurp "resources/grammar.bnf")) :start :QUERY));

;; (defn parse-phrase
;;   [text]
;;   (let [chunks (nlquery-to-chunk text)]
;;     (match chunks
;;            [{:phrase :tag "NP"} {:tag "VP"} {:tag "NP"} {:tag "NP"}] true
;;            :else false
;;            )))


;; (defn text-to-ir
;;   "Match chunked text to internal representation (ir)"
;;   [text]
;;   (let [parsed-chunks (chunk-sentence text)]
;;     ()))
