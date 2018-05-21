(ns intermine-nlp.json_parse
  (:require [clojure.data.json :as json]))

(defn read-json-file
  "Read a json file into a nested map."
  [path]
  (let [json-string (-> path java.io.FileInputStream. slurp)]
    (json/read-str json-string :key-fn keyword)))

(defn parse-class-fields
  "Parse an intermine db model (as a nested map).
  Returns a map of :class {:attributes {& attrs} :collections {& colls}}."
  [model]
  (let [classes (-> model :model :classes)
        class-names (keys classes)]
    (apply merge
           (for [[k v] classes]
             (hash-map k (select-keys v [:attributes :collections]))))))
