(ns reverse-groups.core-test
  (:require [clojure.test :refer :all]
            [reverse-groups.core :refer :all]))

(deftest rev-group-test
  (testing "Test of core function"
    (is (= (rev-group '(a b c d e f g) 2) 
           '(b a d c f e g)))
    (is (= (rev-group '(a b c d e f g h) 2) 
           '(b a d c f e h g)))
    ))

(deftest parse-test
  (testing "Test of parse function"
    (is (= (parse-prob "1,2,3,4,5;2") (list ["1" "2" "3" "4" "5"] 2)))))

(deftest solve-test
  (testing "Test of solve function"
    (is (= (solve "1,2,3,4,5;2") "2,1,4,3,5"))))