; This performs symbolic equation solving using algebraic rules,
; similar to the way a human would solve.

(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

;(def equational-eqvs
;  (bidirectional-ruleset 
;    '((= :x :y) (= :y :x) "Flip Equation")
;    '((= :x (+ :y :z)) (= (- :x :z) :y) "Subtract")
;    '((= :x (+ :y :z)) (= (- :x :y) :z) "Subtract")
;    '((= :x (* :y :z)) (= (/ :x :z) :y) "Divide")
;    '((= :x (* :y :z)) (= (/ :x :y) :z) "Divide")
;    '((= (power :x 2) :y) (= :x (sqrt :y)) "Power")))

;(def term-eqvs
;  (bidirectional-ruleset 
;    '((+ :x :y) (+ :y :x) "Commutative Add.")
;    '((* :x :y) (* :y :x) "Commutative Mult.")
;    '((* :x (+ :y :z)) (+ (* :x :y) (* :x :z)) "Distributive")))

;(def simplifiers
 ; (unidirectional-ruleset 
 ;   '((* (/ :x :y) :y) :x "Mult by denom.")
 ;   '((* :x 1) :x "Simplification")
 ;   '((* :x 0) 0 "Simplification")
 ;   '((+ :x 0) :x "Simplification")
 ;   '((/ (:x :x)) 1 "Simplification")
 ;   '((/ (:x 1)) :x "Simplification")
 ;   '((/ (:x 1)) :x "Simplification")
 ;   '((/ (* :x :y) (* :x :z)) (* :y :z) "Simplification")))

;--------------------------- normal notation
(def equational-eqvs
  (bidirectional-ruleset 
    '((:x = :y) (:y = :x) "Flip Equation")
    '((:x = (:y + :z)) ((:x - :z) = :y) "Subtract")
    '((:x = (:y * :z)) ((:x / :z) = :y) "Divide")
    '(((power :x 2) = :y) (:x = (sqrt :y)) "Power")))

(def term-eqvs
  (bidirectional-ruleset 
    '((:x + :y) (:y + :x) "Commutative Add.")
    '((:x * :y) (:y * :x) "Commutative Mult.")
    '((:x * (:y  + :z)) ((:x * :y) + (:x * :z)) "Distributive")))

(def simplifiers
  (unidirectional-ruleset 
    '(((:x / :y) * :y) :x "Mult by denom.")
    '((:x * 1) :x "Simplification")
    '((:x * 0) 0 "Simplification")
    '((:x + 0) :x "Simplification")
    '((:x / :x) 1 "Simplification")
    '((:x / 1) :x "Simplification")
    '(((:x / :y) * :y) :x "Multiply by denom.")))

(def rules (concat equational-eqvs term-eqvs simplifiers))

(defn eq-solve [equation target] 
    ;(let [target-f #(= (second %1) target)]
  (let [target-f #(= (first %1) target)]
      (pprint ((build-reducer-fn rules target-f 9) equation))))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/sym-equation-solver.clj")

;> (eq-solve '(X = (Y / Z)) 'Y)
;> (eq-solve '(A = ((W + X) / (Y + Z))) 'Z)
;> (eq-solve '(E = ((Z * S) / (sqrt n))) 'n)

;> (eq-solve '(= X (/ Y Z)) 'Y)
;> (eq-solve '(= A (/ (+ W X) (+ Y Z))) 'Z)
;> (eq-solve '(= E (/ (* Z S) (sqrt n))) 'n)
    