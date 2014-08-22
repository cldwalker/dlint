(defproject dlint "0.1.0-SNAPSHOT"
  :description "TODO"
  :url "http://github.com/cldwalker/dlint"
  :license {:name "The MIT License"
            :url "https://en.wikipedia.org/wiki/MIT_License"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]]
  :test-paths ["target/cljx/test/clj" "test"]
  :source-paths ["target/cljx/test/clj" "src"]

  :profiles {:1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :dev {:plugins [[com.keminglabs/cljx "0.4.0"]
                             [lein-cljsbuild "1.0.3"]
                             [com.cemerick/clojurescript.test "0.3.1"]]
                   :hooks [cljx.hooks leiningen.cljsbuild]}}

  :cljx {:builds [{:source-paths ["test/cljx"]
                   :output-path "target/cljx/test/clj"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "target/cljx/test/cljs"
                   :rules :cljs}]}

  :cljsbuild {:test-commands {"unit" ["phantomjs" :runner
                                      "target/unit-test.js"]}
              :builds
              {:test {:source-paths ["src" "test"
                                     "target/cljx/test/cljs"]
                      :compiler {:output-to "target/unit-test.js"
                                 :optimizations :whitespace
                                 :pretty-print true}}}}

  :aliases {"all" ["with-profile" "dev:dev,1.5"]}
  :test-selectors {:focus :focus :default (constantly true)})
