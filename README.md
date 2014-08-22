## Description

dlint lints datomic-style datalog queries and rules - checking for unbound variables. Works with datomic (clj) and datascript (cljs).

## Install

Add to your project.clj:

    [me.tagaholic/dlint "0.1.0"]

## Usage

Given a datalog query or rule, dlint gives you back a map of unbound variables keyed
by where they occurred.

Query linting:

```clj
(require '[dlint.core :as dlint])
(dlint/lint '[:find ?e
              :in $ ?name
              :where [?e :tags ?tag] [?tag :name ?named]]
=> {:in #{'?name} :where #{'?named}}
```

Rule linting:

```clj
(dlint/lint '[[(name-or-alias ?e1 ?name)
               [?e :name ?name]]])
=> {:name-or-alias #{?e1 ?e}}
```

Since dlint's goal is to give you back linting _data_, how you wrap dlint is up to you. Some suggested approaches:

Test that your datalog queries lint correctly.
For example, [this LightTable plugin tests](https://github.com/cldwalker/kukui/blob/ea15ddfa1a0c9b6e63af7398fcfe3d35cd535341/test/lt/plugins/kukui/db_test.cljs#L7-L13) that [these datascript queries](https://github.com/cldwalker/kukui/blob/ea15ddfa1a0c9b6e63af7398fcfe3d35cd535341/src/lt/plugins/kukui/db.cljs#L43-L102) are valid.

Alternatively, wrap datomic/datascript's query functions to fail fast if
a query doesn't lint correctly:

```clj
(defn wrap-q [q-fn query & args]
  (let [invalid (dlint/lint query)]
    (when (seq invalid)
      (throw (ex-info (str "Following variables in query are not bound: " invalid)
                      {:query query :variables invalid}))))
  (apply q-fn query args))

;; for datomic
(def lint-and-q (partial wrap-q datomic.api/q))

;; for datascript
(def lint-and-q (partial wrap-q datascript/q))
```

## Bugs/Issues

Please report them [on github](http://github.com/cldwalker/dlint/issues).

## License

See LICENSE.TXT
