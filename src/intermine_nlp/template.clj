(ns intermine-nlp.template
  (:require [imcljs.fetch :as fetch]
            [imcljs.path :as path]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.util :as util])
  (:gen-class))

(defn prepare-templates
  "Move each query map in templates into another map.
  Add a vector of additional (blank) natural language queries as well.
  Pathquery queries are indexed by :pathquery, natural queries by :nlqueries"
  [templates]
  (loop [ks (keys templates) tmp templates]
    (cond
      (empty? ks) tmp
      :else (recur (rest ks)
                   (update-in tmp
                              [(first ks)]
                              #(hash-map :nlqueries [] :pathquery %))))))

(defn load-local-templates
  "Load a local copy of templates EDN file (backup for fetch-templates).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "test/queries/" db-name "_templates.edn")]
    (util/read-edn path)))


(defn fetch-templates
  "Fetch templates for the given dataset. Defaults to flymine.
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [url (case db-name
              "medic"    "medicmine.jcvi.org/medicmine"
              "fly"      "www.flymine.org/query"
              "human"    "www.humanmine.org/humanmine"
              "yeast"    "yeastmine.yeastgenome.org/yeastmine"
              "rat"      "ratmine.mcw.edu/ratmine"
              "mouse"    "www.mousemine.org/mousemine"
              "fly-beta" "beta.flymine.org/beta"
              "www.flymine.org/query")
        db-request {:root url
                    :token nil
                    :model "genomic"}]
    (try
      (fetch/templates db-request)
      (catch java.lang.Exception e (load-local-templates db-name)))))


(defn store-templates
  "Store templates map in the appropriate directory (test/queries/)."
  [templates path]
  (try (->> templates prn-str (spit path))
       (catch Exception e (printf "Couldn't store templates at '%s'." path))))


;; (defn -main
;;   "Take user input to insert English natural language query translations for
;;   PathQuery queries stored in a templates file. Overwrites file on exit."
;;   [& args]
;;   (if-let [file-path (first args)]
;;     (let [templates (util/read-edn file-path)]
;;       (loop [ks (keys templates) tmp templates]
;;         (print "Type an English query for the following (<ENTER> to skip, CTRL-c to exit).")
;;         (cond
;;           (empty? ks) tmp
;;           :else
;;           (do
;;             (print (first ks))
;;             (print (:description ((first ks) tmp)))
;;             (print (select-keys tmp [:select :where :joins :orderBy :model]))
;;             (let [input (read-line)]
;;               (if (empty? input)
;;                 (recur (rest ks)
;;                        (update-in tmp
;;                                   [(first ks) :nlqueries]
;;                                   #(conj % input)))
;;                 (recur (rest ks) tmp)))))))))
