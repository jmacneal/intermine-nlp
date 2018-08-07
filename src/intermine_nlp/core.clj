(ns intermine-nlp.core
  (:require [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]])
  (:gen-class))

(defn gen-view
  "Merge the values in a map of view elements (:CLASS, :FIELD) into an
  imcljs view map."
  [view lemma-class-map lemma-field-map]
  (let [class (get lemma-class-map (:CLASS view))
        field (get lemma-field-map (:FIELD view))]
    (if class
      (str class "." field)
      field)))

(defn gen-constraint
  "Merge the values in a map of constraint elements(:CLASS, :FIELD, :VALUE,
  :COMPARE, :MULTI_COMPARE, :UNARY_OP) into an imcljs constraint map."
  [constraints lemma-class-map lemma-field-map])

(defn gen-query
  "Generate a query from a map containing :VIEW :CONSTRS and :ORGANISM keys."
  [model query-map]
  (let [lemma-class-map (map-invert (parse/class-lemma-mapping model))
        lemma-field-map (map-invert (parse/field-lemma-mapping model))
        views (flatten (map vals (:VIEW query-map)))
        constraints (flatten (map vals (:CONSTRS query-map)))]
    {:select (map #(gen-view % lemma-class-map lemma-field-map) views)
     :where (map #(gen-constraint % lemma-class-map lemma-field-map) constraints)}))


(defn parser-pipeline
  "Generate a parser pipeline for a given InterMine model.
  options:
  "
  [model & {:as options}]
  (let [parser (parse/gen-parser model)]
    #(->> %
          nlp/lemmatize-as-text
          parser
          ;; parse/transform-tree
        )))

(defn -main
  "In the future I'll return a query, right now I'll just give you the parse tree."
  [& args]
  (let [fly-model (model/fetch-model "fly")
        pipeline (parser-pipeline fly-model :lemmatize true)]
    (pprint "Enter a simple query and I'll attempt to parse it!")
    (pprint "Example: Show me genes with primaryIdentifier like ovo.")
    (loop []
      (print "\n")
      (pprint "To quit, hit <ENTER>")
      (let [text (read-line)
            result (pipeline text)]
        (if (not (empty? text))
          (do (cond
                (insta/failure? result) (pprint "Sorry, I couldn't parse that.")
                :else                   (pprint result))
              (recur))
          (pprint "Bye! Happy hacking."))))))
