(defproject symbolics "0.1.0-SNAPSHOT"
  :description "A library for automated theorem proving and symbolic manipulation, based on algebraic term rewriting"
  :url "https://github.com/chrisgarcia001/Symbolics"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :source-paths ["src" "src/symbolics" "resources"]
  :jvm-opts ["-Xmx5g"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
