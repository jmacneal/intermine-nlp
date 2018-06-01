(ns intermine-nlp.model
  (:require [imcljs.fetch :as fetch]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))


(defn parse-model
  "Parse an intermine db model (as a nested map).
  Returns a map of :class {:attributes {& attrs} :collections {& colls}}."
  [model]
  (let [classes (-> model :classes)]
    (apply merge
           (for [[k v] classes]
             (hash-map k (select-keys v [:attributes :collections]))))))

(defn fetch-model
  "Fetch a model for the given dataset. Defaults to flymine.
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
                    :model "genomic"}
        model (try
                (fetch/model db-request)
                (catch java.lang.Exception e (load-local-model db-name)))]
    (parse-model model)))

(defn load-local-model
  "Load a local copy of a model EDN file (backup for fetch-model).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (try (with-open [file (io/reader path)]
           (-> file java.io.PushbackReader. edn/read))
         (catch Exception e (printf "Couldn't open local model '%s'." path)))))

(defn store-model
  "Store a model in the appropriate directory (resources/db_models)."
  [model db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (try (->> model prn-str (spit path))
         (catch Exception e (printf "Couldn't store model at '%s'." path)))))
