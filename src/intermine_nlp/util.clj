(ns intermine-nlp.util
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [imcljs.fetch :as fetch]
            [imcljs.path :as path]
            [clojure.core.async :refer [<! go]]
            [clojure.string :refer [join split]]))

(defn read-edn
  "Read an edn file into a map"
  [path]
  (try (with-open [file (io/reader path)]
           (-> file java.io.PushbackReader. edn/read))
       (catch Exception e (printf "Couldn't open file '%s'." path))))

(defn possible-values-
  "imcljs.fetch/possible-values seems to be broken, so here's an implementation
  using only imcljs.fetch/unique-values. 'path' must point to a field, not a class."
  [service path]
  (let [split-path (split (path/adjust-path-to-last-class (:model service) path) #"\.")
        query {:from (first split-path)
               :select [(last split-path)]}
        rows (try (fetch/rows service query)
                  (catch java.lang.Exception e nil))]
       (->> rows :body :results (map first) distinct)))

(def possible-values
  "imcljs.fetch/possible-values seems to be broken, so here's an implementation
  using only imcljs.fetch/unique-values. 'path' must point to a field, not a class."
  (memoize possible-values-))


(def summaries
  (memoize (partial fetch/summary-fields)))
