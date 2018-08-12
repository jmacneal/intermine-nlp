(ns intermine-nlp.query
  (:require [intermine-nlp.parse :as parse]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]])
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

(defn gen-path
  "Merge the values in a map of view elements (:CLASS, :FIELD) into an
  imcljs view map."
  [path lemma-class-map lemma-field-map]
  (let [class (get lemma-class-map (:CLASS path))
        field (get lemma-field-map (:FIELD path))]
    (cond
      (and (not-empty class) (not-empty field)) (str class "." field)
      (not-empty class) class
      (not-empty field) field)))

(defn gen-constraint
  "Merge the values in a map of constraint elements(:CLASS, :FIELD, :VALUE,
  :COMPARE, :MULTI_COMPARE, :UNARY_OP) into an imcljs constraint map."
  [constraint lemma-class-map lemma-field-map]
  (let [path (gen-path (:PATH constraint) lemma-class-map lemma-field-map)
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
      :else    {:path path
                :op "="
                :value value}
    )))

(defn gen-query
  "Generate a query from a map containing :VIEW :CONSTRS and :ORGANISM keys."
  [model parse-map]
  (let [lemma-class-map (map-invert (parse/class-lemma-mapping model))
        lemma-field-map (map-invert (parse/field-lemma-mapping model))
        view (:VIEW parse-map)
        constraints (:CONSTRS parse-map)]
    {:select (vec (map #(gen-path (:PATH %) lemma-class-map lemma-field-map) view))
     :where (vec (map #(gen-constraint (:CONSTR %) lemma-class-map lemma-field-map) constraints))}))
