(defproject symbolics "1.0"
  :description "A library for automated theorem proving and symbolic manipulation, based on algebraic term rewriting"
  :url "https://github.com/chrisgarcia001/Symbolics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :jvm-opts ["-Xmx3g"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
