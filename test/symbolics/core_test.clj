(ns symbolics.core-test
  (:require [clojure.test :refer :all]
            [symbolics.core :refer :all]))

;------------------ Test 1 -------------------------------------
; Purely symbolic rewriting, small ruleset
(def rs1 (unidirectional-ruleset 
           '((:a + :b) (:b + :a) "Commutative")
           '((:a + (:b + :c)) ((:a + :b) + :c) "Associative")))


(deftest test-1
  (testing "Basic rewriting 1"
           (let [start-exp '(x + (y + z))
                 end-exp '(z + (y + x))]
             (is (= (first (last (rewrite-prove rs1 start-exp end-exp 15))) 
                    end-exp)))))

;------------------ Test 2 -------------------------------------
; Purely symbolic rewriting, few more rules
(def rs2 (unidirectional-ruleset
           '((:a + :b) (:b + :a) "Commutative-Add")
           '((:a * :b) (:b * :a) "Commutative-Mult")
           '(((:a * :c) + (:b * :c)) ((:a + :b) * :c) "simp")
           '(((:a + :b) * :c) ((:a * :c) + (:b * :c)) "simp")))

(deftest test-2
  (testing "Basic rewriting 2"
           (let [start-exp '((2 * x) + (x * 3))
                 end-exp '(x * (3 + 2))]
             (is (= (first (last (rewrite-prove rs2 start-exp end-exp 15))) 
                    end-exp)))))

;------------------ Test 3 -------------------------------------
; Combine evaluative operators/functions with symbolic rules
(def ops3 (operator-ruleset '(+ "Add.") '(* "Mult.")))
(def sym3 (unidirectional-ruleset 
           '((+ :a :b) (+ :b :a) "Commutative-Add")
           '((* :a :b) (* :b :a) "Commutative-Mult")
           '((+ (* :a :c) (* :b :c)) (* (+ :a :b) :c) "simp")
           '((* (+ :a :b) :c) (+ (* :a :c) (* :b :c)) "simp")))
(def rs3 (concat ops3 sym3))

(deftest test-3
  (testing "Rewriting with operators/functions"
           (let [start-exp '(+ (* 2 x) (* x 3))
                 end-exp '(* x 5)]
             (is (= (first (last (rewrite-prove rs3 start-exp end-exp 15))) 
                    end-exp)))))

;------------------ Test 4 -------------------------------------
; Bidirectional rewrite rules
(def rs4 (bidirectional-ruleset 
           '((:a + :b) (:b + :a) "Commutative-Add")
           '((:a * :b) (:b * :a) "Commutative-Mult")
           '(((:a * :c) + (:b * :c)) ((:a + :b) * :c) "simp")))

(deftest test-4
  (testing "Bidirectional rewrite rules"
           (let [start-exp '(x * (3 + 2))
                 end-exp '((2 * x) + (x * 3))]
             (is (= (first (last (rewrite-prove rs4 start-exp end-exp 15))) 
                    end-exp)))))

;------------------ Test 5 -------------------------------------
; Test failure using RS2
(deftest test-5
  (testing "Basic failure test"
           (let [start-exp '((2 * x) + (x * 3))
                 end-exp '(x * (3 + 3))]
             (is (not (rewrite-prove rs2 start-exp end-exp 15)))))) 

;------------------ Test 6 -------------------------------------
; Test failure for provable but with too small max-depth, using RS4
(deftest test-6
  (testing "Test failure due to too small max-depth"
           (let [start-exp '(x * (3 + 2))
                 end-exp '((2 * x) + (x * 3))]
             (is (not (rewrite-prove rs4 start-exp end-exp 2)))))) 

;------------------ Test 7 -------------------------------------
; Test failure for non-matching outer exps., using RS4
(deftest test-7
  (testing "Test failure due to too small max-depth"
           (let [start-exp '((x + y) + (y + z) + (z + a))
                 end-exp '((y + x) + (z + y) + (a + z))]
             (is (not (rewrite-prove rs4 start-exp end-exp 2)))))) 

