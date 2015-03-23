(ns symbolics.core)


; This constant is used to separate a list head from tail in pattern matching.
(def list-splitter '|)

; This is an option for rules that tells to evaluate the rewrite form if possible.
(def rule-eval-designator :eval)

;--------------------------------------------------------------------------------------
;--- UTILS
(defn third [sq] (second (rest sq))) 
(defn fourth [sq] (second (rest (rest sq))))
              
              
; Breaks a list into 3 parts: beginning (before position n),
; middle - element at position n, and end - elements after position n.
; Example: (break-list-at '(a b c d e) 2)
;          => ((a b) c (d e))
(defn break-list-at [elements n]
  (let [begin (take n elements)
        mid (nth elements n)
        end (drop (+ n 1) elements)]
    (list begin mid end)))

; Given a list of elements, a set of replacement items, and a position n, 
; form a new list replacing the nth position in elements with each replacement
; item.
; Example: (replace-at '(a b c d e) '((p q) (r s) (t u)) 3)
;          => ((a b c (p q) e) (a b c (r s) e) (a b c (t u) e))
(defn replace-at [elements replacements n]
  (let [parts (break-list-at elements n)
        begin (first parts)
        end (third parts)
        f (fn [rep] (concat begin (list rep) end))]
    (map f replacements)))

; Gets indices of elements which meet the specified criteria f.
; Example: (get-positions #(> % 4) [1 1 1 3 1 6 8 1 2]) => (5 6)
(defn get-positions [f elts]
  (let [indexed (map-indexed #(list %1 (f %2)) elts)
        selector (fn [x] (not (false? (second x))))
        selected (filter selector indexed)
        inds (map #(first %1) selected)]
    inds))
        
;--------------------------------------------------------------------------------------
;--- DOCUMENTATION OF STRUCTURES

; Rule: (pattern rewrite-form rulename)
; Rule Set: (rule1 rule2 ....)
; Step: (expression rulename-used)
; Derivation: (step1 step2 ....)  ; most recent first - next steps are cons'ed on
; Terminal-f: (f exp) -> true | false

;--------------------------------------------------------------------------------------
; RULE CONSTRUCTORS & ACCESSORS
(defn make-rule [pat rewrite-fm rname & options] 
  "Makes a rewriting rule based on the specified matching pattern, pattern (form) to be 
   rewritten, and rule name. This will specify a purely symbolic rewrite rule - no function
   evaluation will be applied to the resulting new expression."
  (concat (list pat rewrite-fm rname) (if (= nil options) '(()) (list options))))
(defn make-evaluative-rule [pat rewrite-fm rname]
  "Makes a rewriting rule based on the specified matching pattern, pattern (form) to be 
   rewritten, and rule name. The rewritten form will be attempted to be evaluated - if
   unsuccessful, the symbolic rewritten form will be returned."
  (make-rule pat rewrite-fm rname rule-eval-designator))
(defn is-evaluative-rule? [rule] (not (empty? (filter #(= rule-eval-designator %1) (last rule)))))
(defn get-pattern [rule] (first rule))
(defn get-rewrite-form [rule] (second rule)) 
(defn get-rulename [rule] (third rule))
(defn get-options [rule] (fourth rule))

; STEP CONSTRUCTORS & ACCESSORS
(defn make-step [exp rname] (list exp rname))
(defn make-steps [expressions rulename] (map #(list %1 rulename) expressions))
(defn get-exp [step] (first step))
(defn get-rule-used [step] (second step))

; DERIVATION CONSTRUCTORS & ACCESSORS
(defn make-start-derivation-list [exp] (list (list (make-step exp  'start))))
(defn add-step [step derivation] (cons step derivation))
(defn add-step-parts [exp rname derivation] (add-step (make-step exp rname) derivation))
(defn get-top-exp [deriv] (get-exp (first deriv)))

;--------------------------------------------------------------------------------------
; CORE LOGIC FUNCTIONS:

; match (pattern exp {:var -> val}): {:var -> val} | false
; match-rule (Rule exp): {:var -> val} | false
; direct-rewrite (Rule exp): exp | false
; all-rewrites (Rule exp): (exp*)
; gen-next-steps (Ruleset exp): (Step*)
; rewrite-reduce (Ruleset Terminal-f current-derivs seen depth): Derivation | false

;--------------------------------------------------------------------------------------

; Matches expression e1 to e2, Returns a map of variable bindings if there is a match, or
; false if no match. Variables are represented as keywords in e1 (ex. :a, :b, :c, etc.)
; Examples:  
;   (match 1 1 {}) => {} ; 1 and 1 are constants, don't put in map.
;   (match '(:a 1) '(1 1) {}) => {:a 1} ; Variable :a binds to 1
;   (match '(:a 1) '(3 5) {}) => false ; 1 does not match 5
;   (match '(:a (:b :c)) '(3 (5 (7 8))) {}) => {:b 5, :a 3, :c (7 8)}
(defn match [e1 e2 vars]
  (cond (= e1 '_) vars
        (and (= e1 e2) (= e1 '())) vars
        (and (keyword? e1) (contains? vars e1) (not (= e2 (get vars e1)))) false
        (and (keyword? e1) (contains? vars e1) (= e2 (get vars e1))) vars
        (and (keyword? e1) (not (contains? vars e1))) (merge {e1 e2} vars)
        ;---- Match the (head | tail) form:
        (and (seq? e1) (seq? e2) (= (count e1) 2) (= list-splitter (first e1))) 
        (match (second e1) e2 vars) 
        ;----- Perform full recursive matching:
        (and (seq? e1) (seq? e2)) 
        (let [m1 (match (first e1) (first e2) vars)]
          (if m1 (match (rest e1) (rest e2) m1) false))
        ;----
        (= e1 e2) vars 
        true false))

; Binds a set of variable values into an expression.
; Example: (bind '(+ :a (* :a :b)) {:a 4, :b 3}) => (+ 4 (* 4 3))
(defn bind [exp vars]
  (cond (= exp '()) '() 
        (keyword? exp) (get vars exp)
        ;---
        (and (seq? exp) (= (count exp) 2) (= (first exp) list-splitter)) (bind (second exp) vars)
        ;---
        (seq? exp) (cons (bind (first exp) vars) (bind (rest exp) vars))
        true exp))

; Rewrites a matching rule DIRECTLY (no sub-expression replacement)
; Example: (def r1 (make-rule '(:a + :b) '(:b + :a) "Commutative"))
;          (direct-rewrite r1 '(2 + 3))
;           => (3 + 2)
(defn direct-rewrite [rule exp]
  (let [vars (match (get-pattern rule) exp {})]
    (if vars 
      (let [newexp (bind (get-rewrite-form rule) vars)]
        (if (is-evaluative-rule? rule) 
          (try (eval newexp) (catch Exception e newexp)) newexp))
      false)))

; For an expression, find all possible rewrites by applying 
; the given rule. This includes the rule applied to the main expression
; and recursively to each sub-expression.
; Example: (def r1 (make-rule '(:a + :b) '(:b + :a) "Commutative"))
;          (all-rewrites r1 '((x + y) + (y + z)))
;          => (((y + z) + (x + y)) ((y + x) + (y + z)) ((x + y) + (z + y)))
(defn all-rewrites [rule exp]
  (let [top (direct-rewrite rule exp)
        first-rewrites (if top (list top) '())]
    (if (not (coll? exp)) first-rewrites
      (let [all-sub-rewrites (map #(all-rewrites rule %1) exp)
            selector (fn [pair] (not (empty? (second pair))))
            sub-rewrites (filter selector (map-indexed list all-sub-rewrites))
            replacer (fn [pair] (replace-at exp (second pair) (first pair)))
            rest-rewrites (apply concat (map replacer sub-rewrites))
            all-rewrites (concat first-rewrites rest-rewrites)]
        all-rewrites))))


; For a given ruleset and expression, find all possible rewrites and generate 
; all next corresponding steps.
; Example: (def r1 (make-rule '(:a + :b) '(:b + :a) "Commutative"))
;          (def r2 (make-rule '(:a + (:b + :c)) '((:a + :b) + :c) "Associative"))
;          (def rs (list r1 r2))
;          (gen-next-steps rs '((p + q) + (q + r)))     
;          => ((((q + r) + (p + q)) "Commutative")
;              (((q + p) + (q + r)) "Commutative")
;              (((p + q) + (r + q)) "Commutative")
;              ((((p + q) + q) + r) "Associative"))
(defn gen-next-steps [ruleset exp]
  (let [build-steps (fn [rule] (make-steps (all-rewrites rule exp)  (get-rulename rule)))
        next-steps (map build-steps ruleset)]
    (apply concat next-steps)))
          
        
; rewrite-reduce-rec (Ruleset Terminal-f current-derivs seen depth): Derivation | false
; Recursively reduces the derivations until a terminating condition is reached or
; max depth is reached. This is the core rewriting function. Note in example below
; that reductions are in reverse order (last step first).
; Example: (def r1 (make-rule '(:a + :b) '(:b + :a) "Commutative"))
;          (def r2 (make-rule '(:a + (:b + :c)) '((:a + :b) + :c) "Associative"))
;          (def rs (list r1 r2))
;          (def termf (fn [exp] (= exp '(c + (a + b)))))
;          (rewrite-reduce-rec rs termf (make-start-derivation-list '(a + (b + c))) (set '()) 5)
;          => (((c + (a + b)) "Commutative")
;              (((a + b) + c) "Associative")
;              ((a + (b + c)) "Start"))   
(defn rewrite-reduce-rec [ruleset termf derivs seen depth]
  (if (= depth 0) false
    (let [unseen? (fn [step] (not (contains? seen (get-exp step)))) 
          make-next-steps 
          (fn [deriv] (filter unseen? (gen-next-steps ruleset (get-top-exp deriv))))
          expand-deriv (fn [deriv] (map #(add-step %1 deriv) (make-next-steps deriv)))
          next-derivs (apply concat (map expand-deriv derivs))
          solutions (filter #(termf (get-top-exp %1)) next-derivs)]
      (if (not (empty? solutions)) (first solutions)   ;; Easy to get ALL solutions - just don't use first
        (let [next-exps (map get-top-exp next-derivs)
              next-seen (set (concat seen next-exps))]
          (rewrite-reduce-rec ruleset termf next-derivs next-seen (- depth 1)))))))

;--------------------------------------------------------------------------------------
; MAIN FUNCTIONS.   

; rewrite-reduce (Ruleset Terminal-f exp depth): Derivation | false 
; Using the ruleset, build a chain of rewrites until a termination condition
; is reached or max depth is reached. Return the resulting reduction sequence.
; Example: (def r1 (make-rule '(:a + :b) '(:b + :a) "Commutative"))
;          (def r2 (make-rule '(:a + (:b + :c)) '((:a + :b) + :c) "Associative"))
;          (def rs (list r1 r2))
;          (def termf (fn [exp] (= exp '((c + a) + b))))
;          (rewrite-reduce rs termf (make-start-derivation-list '(a + (b + c))) (set '()) 5)
;          => (((a + (b + c)) "Start")
;              (((b + c) + a) "Commutative")
;              (((c + b) + a) "Commutative"))  
(defn rewrite-reduce [ruleset termf start-exp maxsteps]
  "Given a ruleset, termination-testing function, starting expression, and maxsteps,
   construct a sequence of rewrites beginning with the start expression until  
   either an expression meeting the termination criteria is found or the maxsteps
   is reached.
   Example: (def r1 (make-rule '(:a + :b) '(:b + :a) 'commutative))
            (def r2 (make-rule '(:a + (:b + :c)) '((:a + :b) + :c) 'associative))
            (def rs (list r1 r2))
            (def termf (fn [exp] (= exp '((c + a) + b))))
            (rewrite-reduce rs termf (make-start-derivation-list '(a + (b + c))) 5)
             => (((a + (b + c)) Start)
                (((b + c) + a) 'commutative)
                (((c + b) + a) 'commutative))"
  (let [sol (rewrite-reduce-rec
              ruleset termf (make-start-derivation-list start-exp) (set '()) maxsteps)]
    (if sol (reverse sol) false)))
             

; Documentation below.         
(defn rewrite-prove [ruleset start-exp end-exp maxsteps]
  "Constructs an identity proof, transforming start-exp into end-exp through a sequence
   of rewrites. If this fails, it attempts to reverse the start and end expression
   and work backwards. If this fails, returns false.
   Example: (def r1 (make-rule '(:a + :b) '(:b + :a) 'commutative))
            (def r2 (make-rule '(:a + (:b + :c)) '((:a + :b) + :c) 'associative))
            (def rs (list r1 r2))
            (def start-exp '(x + (y + z)))
            (def end-exp '(z + (y + x)))
            (rewrite-prove rs start-exp end-exp 7)
             => (((x + (y + z)) start)
                 (((x + y) + z) 'associative)
                 ((z + (x + y)) 'commutative)
                 ((z + (y + x)) 'commutative))"
  (let [proof (rewrite-reduce ruleset #(= %1 end-exp) start-exp maxsteps)]
    (if proof proof
      (let [proof2 (rewrite-reduce ruleset #(= %1 start-exp) end-exp maxsteps)]
        (if proof2 proof2 false)))))

; Documentation below. 
(defn build-prover-fn [ruleset maxsteps]
  "Constructs an identiy theorem prover function based on the ruleset and maxsteps.
   The resulting function takes starting and ending expressions, and attempts to
   transform start into end. If this cannot be done in maxsteps steps, returns false."
  (fn [start-exp end-exp] (rewrite-prove ruleset start-exp end-exp maxsteps)))        

; Documentation below.        
(defn build-reducer-fn [ruleset termf maxsteps]
  "Constructs a rewrite reducer function based on the ruleset, termination-testing function,
   and maxsteps specified. The resulting function takes in an expression and constructs a 
   sequence of rewrites leading to a terminating expression, or maxsteps is reached.
   If a solution is found the sequence of rewrites is returned, otherwise false."
  (fn [start-exp] (rewrite-reduce ruleset termf start-exp maxsteps)))

;--------------------------------------------------------------------------------------
; VARIOUS RULESET BUILDERS

(defn symbolic-ruleset [& rule-forms]
  "Constructs a unidirectional set of rewrite rules (purely symbolic). Each
   rule input is a list of (<pattern 1> <pattern 2> <rule name>). In the resulting
   set of rewrite rules, if pattern 1 is detected, it will be rewritten as 
   pattern 2."
  (map #(apply make-rule %1) rule-forms))

(defn bidirectional-symbolic-ruleset [& rule-forms]
  "Constructs a bi-directional set of rewrite rules (purely symbolic). Each
   rule input is a list of (<pattern 1> <pattern 2> <rule name>). In the resulting
   set of rewrite rules, if pattern 1 is detected, it will be rewritten as 
   pattern 2."
  (let [flip #(concat (list (second %1) (first %1)) (rest (rest %1)))
        opps (map flip rule-forms)]
    (apply symbolic-ruleset (concat rule-forms opps))))

; This is an alias for the function above.                      
(def identity-ruleset bidirectional-symbolic-ruleset)

(defn evaluative-ruleset [& rule-forms]
  "Constructs a unidirectional set of rewrite rules (evaluative). Each
   rule input is a list of (<pattern 1> <pattern 2> <rule name>). In the resulting
   set of rewrite rules, if pattern 1 is detected, it will be rewritten as 
   pattern 2."
  (map #(apply make-evaluative-rule %1) rule-forms))

(defn operator-ruleset [& op-forms]
  "Creates a set of rules to evalute the specified operator forms. Each
   operator-form hs form (<operator or function> <name of operator or function>).
   Whenever a specified operator is encountered it will always attempt to
   be evaluated. "
  (let [ls list-splitter
        rb (fn [form] (list (list (first form) ls :rest)
                            (list (first form) ls :rest)
                            (second form)))]
    (apply evaluative-ruleset (map rb op-forms))))



