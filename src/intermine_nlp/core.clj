(ns intermine-nlp.core
  (:require [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]]
            [imcljs.query :as im-query]
            [intermine-nlp.query :as query])
  (:gen-class))

(defn parser-pipeline
  "Generate a parser pipeline for a given InterMine model.
  options:
  "
  [model & {:as options}]
  (let [parser (parse/gen-parser model)]
    #(->> %
          nlp/lemmatize-as-text
          parser
          parse/transform-tree
          (query/gen-query model)
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
