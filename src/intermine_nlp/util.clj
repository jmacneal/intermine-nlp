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

;; (defn possible-values
;;   "imcljs.fetch/possible-values seems to be broken, so here's an implementation
;;   using only imcljs.fetch/unique-values."
;;   [service model path]
  ;; (let [summary-path (path/adjust-path-to-last-class model path)
  ;;       split-summary-path (split summary-path #"\.")
  ;;       query {:from (first split-summary-path)
  ;;              :select [(last split-summary-path)]}]
;;     (pprint [query summary-path])
;;     (fetch/unique-values service query summary-path)))

(defn kws->path
  "Merge class keywords into an intermine path."
  [service class-kws]
  ;; (->> class-kws path/join-path (path/relationships (:model service)) keys)
  )

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

(def possible-values (memoize possible-values-))
