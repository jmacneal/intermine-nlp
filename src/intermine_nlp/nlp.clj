(ns intermine-nlp.nlp
  (:require [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.util :refer [prepare-nlquery]]
            [clojure.string :as string])
  (:import  [lemmatizer StanfordLemmatizer])
  (:gen-class))

(defonce core-nlp (delay (StanfordLemmatizer.)))

(defn lemmatize [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp]
    (->> (.lemmatize nlp text)
         (map (partial string/join " "))
         (string/join "\n"))))

(def get-sentences (nlp/make-sentence-detector "resources/nlp_models/en-sent.bin"))
(def tokenize (nlp/make-tokenizer "resources/nlp_models/en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "resources/nlp_models/en-pos-maxent.bin"))
(def chunker (treebank/make-treebank-chunker "resources/nlp_models/en-chunker.bin"))
(def parser (treebank/make-treebank-parser "resources/nlp_models/en-parser-chunking.bin"))

(defn nlquery-to-chunk
  "Use NLP to convert raw text to chunked form"
  [text]
  (-> text prepare-nlquery tokenize pos-tag chunker vec))

(defn nlquery-to-tree
  "Use NLP to convert raw text to treebank tree form"
  [text]
  (-> text prepare-nlquery vector parser first treebank/make-tree))
