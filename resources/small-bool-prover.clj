; This is another boolean algebra theorem prover which uses a more
; minimalist set of axioms.

(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

(def small-axioms 
  (bidirectional-ruleset 
    '((:a + 0) :a "Identity")
    '((:a * 1) :a "Identity")
    '((:a + 1) 1 "Identity")
    '((:a * 0) 0 "Identity")
    '((:a + (! :a)) 1 "Complement")
    '((:a * (! :a)) 0 "Complement")
    '((:a + :b) (:b + :a) "Commutative")
    '((:a * :b) (:b * :a) "Commutative")
    '((:a * (:b + :c)) ((:a * :b) + (:a * :c)) "Distributive")
    '((:a + (:b * :c)) ((:a + :b) * (:a + :c)) "Distributive")))
                    
(defn small-bool-prove [lhs rhs] 
  (pprint ((build-prover-fn small-axioms 9) lhs rhs)))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/small-bool-prover.clj")
;> (small-bool-prove '(a * a) 'a)
;> (small-bool-prove '(a + a) 'a)
;> (small-bool-prove '(a + (a * b)) 'a)
;> (small-bool-prove '(a * (a + b)) 'a)

