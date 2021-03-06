(ns dlint.core
  (:require [clojure.set :as cset]))
;; Just symlinked this ns instead of .cljx b/c cljx + LT clj(s) repl is a yak

;; thanks to datascript
(defn- parse-query [query]
  (loop [parsed {} key nil qs query]
    (if-let [q (first qs)]
      (if (keyword? q)
        (recur parsed q (next qs))
        (recur (update-in parsed [key] (fnil conj []) q) key (next qs)))
      parsed)))

(defn- variable? [sym]
  (and (symbol? sym) (re-find #"^\?" (str sym))))

(defn- ->symbols [values]
  (set (filter variable? (flatten values))))

(defn- symbol-diff [v1 & others]
  (apply cset/difference
         (->symbols v1)
         (map ->symbols others)))

(defn- lint-map-key
  "Lints map at given key and returns set of unbound variables"
  [map-key lint-map]
  (let [tuples (get lint-map map-key)
        ;; Tuples can be one e.g. :find or a list of them e.g. :where
        tuples (if (sequential? (first tuples)) tuples (list tuples))
        other-key-vals (map #(get lint-map %)
                            (disj (set (keys lint-map)) map-key))]
    (apply cset/union
           (map #(apply symbol-diff %
                        (into other-key-vals
                              (disj (set (get lint-map map-key)) %)))
                tuples))))

(defn- unbound-query-variables [lint-map]
  (->> (keys lint-map)
       (map #(vector % (lint-map-key % lint-map)))
       (remove (comp empty? second))
       (into {})))

(defn- unbound-rule-variables [rules]
  (->> (map-indexed #(vector %1 %2) rules)
       (reduce
        (fn [accum [index rule-tuples]]
          (let [unbound (lint-map-key index {index rule-tuples})
                rule-name (keyword (ffirst rule-tuples))]
            (update-in accum [rule-name]
                       (fnil into #{}) unbound)))
        {})
       (remove (comp empty? second))
       (into {})))

(defn lint
  "Lints a datomic-style datalog query or rule. For queries, returns a map of
  query keys to sets of unbound variables e.g. {:find #{'?entity}}.
  For rules, returns a map of rule names to sets of unbound variables e.g.
  {:some-rule #{'?aarg1}}"
  [query-or-rule]
  (if (and (sequential? (first query-or-rule)) (sequential? (ffirst query-or-rule)))
    (unbound-rule-variables query-or-rule)
    (unbound-query-variables (if (map? query-or-rule)
                               query-or-rule (parse-query query-or-rule)))))
