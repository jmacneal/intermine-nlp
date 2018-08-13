(ns intermine-nlp.query
  (:require [intermine-nlp.parse :as parse]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]]
            [intermine-nlp.util :as util]
            [clojure.string :as string])
  (:gen-class))

(def op-map {
             :EQ "="
             :NEQ "!="
             :GT ">"
             :LT "<"
             :GEQ ">="
             :LEQ "<="
             :IN "IN"
             :NIN "NOT IN"
             :M_ONE_OF "ONE OF"
             :M_NONE_OF "NONE OF"
             :M_OVERLAPS "OVERLAPS"
             :M_NOVERLAPS "DOES NOT OVERLAP"
             :M_OUTSIDE "OUTSIDE"
             :M_WITHIN "WITHIN"
             :M_CONTAINS "CONTAINS"
             :M_NCONTAINS "DOES NOT CONTAIN"
             :NULL "IS NULL"
             :NNULL "IS NOT NULL"})

(defn gen-summary
  "Generate a summary view for a given class path (returns nil if unrecognized class).
  Do not use lemmatized class name."
  [service path]
  (util/class-summary service path))

(defn gen-root
  [view lemma-class-map]
  (let [classes (distinct (flatten (map #(get-in % [:PATH :CLASS]) view)))
        class-paths (distinct (map (partial get lemma-class-map) classes))]
    (first class-paths)))

(defn gen-path
  "Merge the values in a map of view elements (:CLASS, :FIELD) into an
  imcljs path map."
  [path root lemma-class-map lemma-field-map]
  (let [class-name (get lemma-class-map (:CLASS path))
        class (if (= class-name root) class-name (string/lower-case class-name))
        field (get lemma-field-map (:FIELD path))]
    (cond
      (and (not-empty class) (not-empty field)) (str class "." field)
      (not-empty class) class
      (not-empty field) field)))

(defn gen-view
  "Merge the values in a seq of :PATH elements into an imcljs view map."
  [service paths root lemma-class-map lemma-field-map]
  (let [view (vec
              (map #(gen-path (:PATH %) root lemma-class-map lemma-field-map)
                   paths))]
    (cond
      (= view [root]) (gen-summary service root)
      :else           view)))

(defn gen-constraint
  "Merge the values in a map of constraint elements(:CLASS, :FIELD, :VALUE,
  :COMPARE, :MULTI_COMPARE, :UNARY_OP) into an imcljs constraint map."
  [constraint root lemma-class-map lemma-field-map]
  (let [path (gen-path (:PATH constraint) root lemma-class-map lemma-field-map)
        compare (get op-map (:COMPARE constraint))
        multi-compare (get op-map (:MULTI_COMPARE constraint))
        unary-op (get op-map (:UNARY_OP constraint))
        value (:VALUE constraint)
        values (:VALUES constraint)]
    (cond
      compare {:path path
               :op compare
               :value value}
      multi-compare {:path path
                     :op compare
                     :value values}
      unary-op {:path path
                :op unary-op}
      value    {:path path
                :op "="
                :value value}
      values   {:path path
                :op "ONE OF"
                :value values}
      :else    {:value "ERROR"}
    )))

(defn gen-constraints
  "Merge the values in a seq of :CONSTR elements into an imcljs constraints map."
  [constraints root lemma-class-map lemma-field-map]
  (vec
   (map #(gen-constraint (:CONSTR %) root lemma-class-map lemma-field-map) constraints)))

(defn gen-query
  "Generate a query from a map containing :VIEW :CONSTRS and :ORGANISM keys."
  [service parse-map]
  (let [model (:model service)
        lemma-class-map (map-invert (parse/class-lemma-mapping model))
        lemma-field-map (map-invert (parse/field-lemma-mapping model))
        paths (:VIEW parse-map)
        constraints (:CONSTRS parse-map)
        root (gen-root paths lemma-class-map)]
    {:from root
     :select (gen-view service paths root lemma-class-map lemma-field-map)
     :where (gen-constraints constraints root lemma-class-map lemma-field-map)
     }))
