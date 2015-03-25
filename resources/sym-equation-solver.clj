; This performs symbolic equation solving using algebraic rules,
; similar to the way a human would solve.

(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

; We use three basic rule sets combined into one overall ruleset:
; 1) equational equivalences, 2) term equivalences, and 3) simplifying rules.

(def equational-eqvs
  (bidirectional-ruleset 
    '((:x = :y) (:y = :x) "Flip Equation")
    '((:x = (:y + :z)) ((:x - :z) = :y) "Balance Sides: Addition/Subtraction")
    '((:x = (:y * :z)) ((:x / :z) = :y) "Balance Sides: Multip./Division")
    '(((power :x 2) = :y) (:x = (sqrt :y)) "Balance Sides: Square/Square Root")))

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

; Combine the three rulesets into one.
(def rules (concat equational-eqvs term-eqvs simplifiers))

; Create a function that solves the given equation for the specified 
; target variable or expression. Produces a sequence of steps starting
; with given equation and ending with new equation where target is on 
; left hand side. See examples below.
(defn eq-solve [equation target] 
  (let [target-f #(= (first %1) target)]
      (pprint ((build-reducer-fn rules target-f 9) equation))))


;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/sym-equation-solver.clj")
;> (eq-solve '(X = (Y / Z)) 'Y)
;> (eq-solve '(A = ((W + X) / (Y + Z))) 'Z)
;> (eq-solve '(E = ((Z * S) / (sqrt N))) 'N)

    