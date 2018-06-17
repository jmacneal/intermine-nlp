(ns intermine-nlp.randquery
  (:require [imcljs.query :as query]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.util :as util]))



;; (defn rand-query
;;   "Given a data model, randomly walk the model to generate a simple
;;   query, consisting of view(s) and constraint(s)."
;;   [model]
;;   (let [num-views (inc (rand-int 4))
;;         num-constraints (inc (rand-int 4))
;;         cls (rand-class model)]
;;     {:select (rand-views)
;;      :where (rand-constraints)}
;;     ))

(defn rand-class
  "Returns the keyword of a random class in model."
  [model]
  (let [classes (-> model :classes (keys))]
    (rand-nth classes)))

(defn rand-val
  "Get a random value from a map"
  [m]
  (-> m vals rand-nth))

(defn rand-field
  "Given a model and path (seq of class keywords), return a random attribute."
  [model class-kws]
  (let [path (im-path/join-path class-kws)
        [& classes] class-kws]
    (if classes
      (try
        (rand-val (im-path/attributes model path))
        (catch Exception e nil))
      nil)))

(defn rand-path
  "Given a model and path (seq of class keywords), picks a random relation (child)
  and returns the resulting path of that relation."
  [model class-kws]
  (let [[& classes] class-kws]
    (if classes
      (try
        (vec
         (filter identity
                 (conj class-kws (->> class-kws
                                      im-path/join-path
                                      (im-path/relationships model)
                                      keys
                                      rand-nth))))
           (catch Exception e nil))
      nil
      )))


(defn rand-attribute
  "Given a model and class keyword, randomly descend the tree of properties
   belonging to the class. When it has reached the specified depth, returns
  a random attribute. If it bottoms out before reaching depth, returns
  immediately."
  [model class-kws depth]
  (loop [current-kws class-kws height 0]
    (let [path (im-path/join-path current-kws)
          random-path (rand-path model current-kws)]
      (if
          (or
           (empty? random-path)
           (= height depth)) {:path current-kws :field (rand-field model current-kws)}
          (recur random-path (inc height))
          ))))

(defn attr->path
  "Converts a path-attribute map to a string path.
  Example: (attr->path {:path [:Gene :primaryId] :field {:name 'blah'})
         => [:Gene :primaryId :blah]"
  [attribute-map]
  (let [class-kws (:path attribute-map)
        field-key (-> attribute-map :field :name keyword)]
    (im-path/join-path (conj class-kws field-key))))

(defn rand-possible-value
  "Return a random legal/possible value for a given path."
  [service path]
  (let [possible-values (util/possible-values service path)]
    (rand-nth possible-values)))

(def string-ops #{"=" "!=" "CONTAINS" "DOES_NOT_CONTAIN" "LIKE" "LOOKUP" "ONE_OF" "EXACT_MATCH"
                  "MATCH" "DOES_NOT_MATCH" "IS_NULL" })

(defn rand-op
  "Returns a random operation of given type and arity (single vs multiple)."
  [type arity]
  "="

  ;; (case type
  ;;   "java.lang.String"

  ;;   "java.lang.Integer")

  )

(defn rand-constraint
  "Selects a random attribute of given depth at the given path."
  [service class-kws depth]
  (let [attr (rand-attribute (:model service) class-kws depth)
        path (attr->path attr)
        field-type (-> attr :field :type)]
    {:path path
     :op (rand-op field-type nil)
     :value (rand-possible-value service path)}))

(defn rand-depth-constraint
  "Returns a random constraint with depth between 1 and 'depth'.
  Default is flatly distributed, with optional weight vector for
  specifying a probability distribution. class-kws specifies path."
  ([model class-kws max-depth]
   (let [depth (inc (rand-int max-depth))]
     (rand-constraint model class-kws depth)))
  ([model class-kws max-depth probs]
   (let [weighted-vec (flatten (map repeat probs max-depth))
         depth (rand-nth weighted-vec)]
     (rand-constraint model class-kws depth))))
