; This performs symbolic equation solving using algebraic rules,
; similar to the way a human would solve.

(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

(def equational-eqvs
  (bidirectional-ruleset 
    '((+ :x :y) (+ :y :x) "Commutative Add.")
    '((* :x :y) (* :y :x) "Commutative Mult.")
    '((* :x 1) :x "Simplification")
    '((* :x 0) 0 "Simplification")
    '((+ :x 0) :x "Simplification")
    '((/ (:x :x)) 1 "Simplification")
    '((/ (:x 1)) :x "Simplification")
    '((/ (:x 1)) :x "Simplification")
    '((/ (* :x :y) (* :x :z)) (* :y :z) "Simplification")
    '((= :x :y) (= :y :x) "Commutative Eq.")
    '((= :x (+ :y :z)) (= (- :x :z) :y) "Subtract")
    '((= :x (+ :y :z)) (= (- :x :y) :z) "Subtract")
    '((= :x (* :y :z)) (= (/ :x :z) :y) "Divide")
    '((= :x (* :y :z)) (= (/ :x :y) :z) "Divide")
    '((= (power :x 2) :y) (= :x (sqrt :y)) "Power")))

(def eq-solve
  (fn [equation target] 
    (let [target-f #(= (second %1) target)]
      (pprint ((build-reducer-fn equational-eqvs target-f 9) equation)))))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/sym-equation-solver.clj")
;> (eq-solve '(= X (/ Y Z)) 'Y)
;> (eq-solve '(= E (/ (* Z S) (sqrt n))) 'S)
    