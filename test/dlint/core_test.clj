(ns dlint.core-test
    (:require [clojure.test :refer :all]
              [dlint.core :refer [lint]]))

(deftest lint-basic-queries
  (testing "query map"
    (is (= {:find #{'?a} :where #{'?e}}
           (lint {:find '[?a] :in '[$ ?tag] :where '[[?e :name ?tag]]}))))
  (testing "no typos"
    (is (= {}
           (lint '[:find ?e :in $ ?tag :where [?e :name ?tag]]))))
  (testing "typo b/n :in and :where"
    (is (= {:in #{'?name} :where #{'?named}}
           (lint '[:find ?e :in $ ?name
                   :where [?e :tags ?tag] [?tag :name ?named]]))))
  (testing "typo b/n :find and :in"
    (is (= {:find #{'?heads} :in #{'?heards}}
           (lint '[:find (sum ?heads) :with ?monster :in [[?monster ?heards]]]))))
  (testing "typo b/n :in and :with"
    (is (= {:in #{'?monsterd} :with #{'?monster}}
           (lint '[:find (sum ?heads) :with ?monster :in [[?monsterd ?heads]]])))))

(deftest lint-intermediate-queries
  (testing "typo in :find aggregator"
    (is (= {:find #{'?a} :where #{'?e}}
           (lint '[:find (count ?a) :in $ ?tag :where [?e :name ?tag]]))))
  (testing "typo in :where function"
    (is (= {:where #{'?t '?t2}}
           (lint '[:find ?e :in $ ?tag
                   :where [?e :name ?t] [(name ?tag) ?t2]]))))
  (testing "typo in :where rule"
    (is (= {:where #{'?t '?t2}}
           (lint '[:find ?e :in $ ?tag
                   :where [?e :name ?t] [(some-rule ?t2 ?tag)]])))))

(def rules
  (concat '[[(tagged-with ?e ?name)
             [?e :tags ?tag]
             [?tag :name ?name]]]
          '[[(name-or-alias ?e1 ?name)
             [?e :name ?name]]
            [(name-or-alias ?e ?name)
             [?e2 :alias ?name]]]))

(deftest lint-rules
  (is (= {:name-or-alias #{'?e1 '?e2}}
         (lint rules))))
