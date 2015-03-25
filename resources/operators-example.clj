; This illustrates a small system which uses operators and user-defined
; functions with symbolic rewriting.

(use 'symbolics.core)
(use '[clojure.pprint :only [pprint]])

; Raise x to the power of n.
(defn my-pow [x n]
  (if (= n 0) 1 (* x (my-pow x (- n 1)))))

; Define operators
(def ops (operator-ruleset 
           '(+ "Func. Eval.") 
           '(* "Func. Eval.") 
           '(my-pow "Func. Eval.")))

; Define rewrite rules
(def sym-rules (bidirectional-ruleset 
                 '((+ :a :b) (+ :b :a) "Commutative-Add")
                 '((* :a :b) (* :b :a) "Commutative-Mult")
                 '((+ (* :a :c) (* :b :c)) (* (+ :a :b) :c) "Dist")))

; Combine evaluative operators/functions with symbolic rules into one set
(def rules (concat ops sym-rules))

; Termination function: does the expression have at most one non-numeric value?
(defn termination-f [exp]
  (if (number? exp) exp
    (<= (count (filter #(not (number? %1)) exp)) 2))) 

; Make a function to reduce expression to one with at most one number
(defn reduce-down [expression] 
  (pprint ((build-reducer-fn rules termination-f 9) expression)))

;-------- SAMPLE REPL USAGE---------------------------
;> (load-file "resources/operators-example.clj")
;> (reduce-down '(+ (* 2 4) (* 4 3)))
;> (reduce-down '(+ (* 2 x) (* x 3)))
;> (reduce-down '(+ (* 2 x) (* x (my-pow 3 2))))