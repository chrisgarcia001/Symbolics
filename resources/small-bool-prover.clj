
(use 'symbolics.core)

(def small-axioms 
  (identity-ruleset '((:a + 0) :a "Identity")
                    '((:a * 1) :a "Identity")
                    '((:a + 1) 1 "Identity")
                    '((:a * 0) 0 "Identity")
                    '((:a + (! :a)) 1 "Complement")
                    '((:a * (! :a)) 0 "Complement")
                    '((:a + :b) (:b + :a) "Commutative")
                    '((:a * :b) (:b * :a) "Commutative")
                    '((:a * (:b + :c)) ((:a * :b) + (:a * :c)) "Distributive")
                    '((:a + (:b * :c)) ((:a + :b) * (:a + :c)) "Distributive")
                    ))
                    
(def small-bool-prove (build-prover-fn small-axioms 9))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/small-bool-prover.clj")
;> (small-bool-prove '(a * a) 'a)
;> (small-bool-prove '(a + a) 'a)
;> (small-bool-prove '(a + (a * b)) 'a)
;> (small-bool-prove '(a * (a + b)) 'a)

