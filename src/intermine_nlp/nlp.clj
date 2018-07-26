(ns intermine-nlp.nlp
  (:require [opennlp.nlp :as nlp]
            [opennlp.treebank :as treebank]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.util :refer [prepare-nlquery]]
            [clojure.string :as string])
  (:import  [lemmatizer StanfordLemmatizer])
  (:gen-class))

;;; Apache OpenNLP
(def get-sentences (nlp/make-sentence-detector "resources/nlp_models/en-sent.bin"))
(def tokenize
  "Split a string into words and punctuation marks.
  Example:
         'This is a sentence.' -> '('this' 'is' 'a' 'sentence')"
  (comp (partial remove #(= % "'s"))
          (nlp/make-tokenizer "resources/nlp_models/en-token.bin")))
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

(defn prepare-nlquery
  "Raw natural language queries must end in a period for some OpenNLP functions"
  [text]
  (let [last-char (subs text (dec (count text)))]
    (if
        (or
         (= last-char ".")
         (= last-char "?")
         (= last-char "!")) text
        (str text "."))))

;;; Stanford CoreNLP
(defonce core-nlp (delay (StanfordLemmatizer.)))


(defn lemmatize
  "Lemmatize a string, converting words to their canonical/infinitive forms.
  Example: 'These are many words' -> 'these be many word. Returns a vector of strings
  (one string per word). Removes possessive 's'."
  [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp]
    (->> (.lemmatize nlp text)
         vec
         (map (partial remove #(= % "'s")))
         (map vec))))

(defn lemmatize-as-text
    "Lemmatize a string, converting words to their canonical/infinitive forms.
  Example: 'These are many words' -> 'these be many word. Returns a flattened string"
  [^String text]
  (->> (lemmatize text)
       (map (partial string/join " "))
       (string/join " ")))

(defn lemma-map
  "Lemmatize a string, returning a word map between the input words and their
  lemmatized form.
  Example: 'It's nice meeting you' -> {'It' 'it', 'nice' 'be', 'meeting' 'nice', 'you' 'meeting'}"
  [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp
        dumb-tokens (tokenize text)]
    (->> text
         lemmatize
         flatten
         (zipmap tokens)
         )))
