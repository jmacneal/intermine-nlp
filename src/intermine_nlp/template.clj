(ns intermine-nlp.template
  (:require [imcljs.fetch :as fetch]
            [imcljs.path :as path]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.util :as util]))

(defn load-local-templates
  "Load a local copy of templates EDN file (backup for fetch-templates).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "test/queries/" db-name "_templates.edn")]
    (util/read-edn path)))


(defn fetch-templates-
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

(def fetch-templates
  (memoize fetch-templates-))

(defn store-templates
  "Store templates map in the appropriate directory (test/queries/)."
  [templates path]
  (try (->> templates prn-str (spit path))
       (catch Exception e (printf "Couldn't store templates at '%s'." path))))
