
(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

; These are the basic axioms of boolean algebra, taken from here:
; http://www-acad.sheridanc.on.ca/~jollymor/info16029/lawsRef.html
(def base-axioms 
  (bidirectional-ruleset 
    '((:a + 0) :a "Identity")
    '((:a * 1) :a "Identity")
    '((:a + 1) 1 "Identity")
    '((:a * 0) 0 "Identity")
    '((:a + (! :a)) 1 "Complement")
    '((:a * (! :a)) 0 "Complement")
    '((! (! :a)) :a "Double Negation")
    '((:a + :a) :a "Idempotent")
    '((:a * :a) :a "Idempotent")
    '((:a + :b) (:b + :a) "Commutative")
    '((:a * :b) (:b * :a) "Commutative")
    '(((:a + :b) + :c) (:a + (:b + :c)) "Associative")
    '(((:a * :b) * :c) (:a * (:b * :c)) "Associative")
    '((:a * (:b + :c)) ((:a * :b) + (:a * :c)) "Distributive")
    '((:a + (:b * :c)) ((:a + :b) * (:a + :c)) "Distributive")
    '((:a + (:a * :b)) :a "Absorption")
    '((:a * (:a + :b)) :a "Absorption")
    '((! (:a + :b)) ((! :a) * (! :b)) "Demorgan")
    '((! (:a * :b)) ((! :a) + (! :b)) "Demorgan")))
                    
(def bool-prove 
  (fn [lhs rhs] (pprint ((build-prover-fn base-axioms 7) lhs rhs))))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/bool-algebra-prover.clj")
;> (bool-prove '(A * ((! A) + B))  '(A * B))
;> (bool-prove '((A * B) + (A * (! B))) 'A)
;> (bool-prove '(((A + B) * A) * (A + B)) 'A)
;> (bool-prove '((C * ((! C) + A)) * B) '((C * A) * B))
;> (bool-prove '((A * ((! A) + B)) * C) '((A * B) * C))

; **NOTE: sample problems above taken from here: 
;         http://www-acad.sheridanc.on.ca/~jollymor/info16029/week2b.html#ex