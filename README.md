Symbolics
===============

#### A library for automated theorem proving and symbolic manipulation

Symbolics is a library to simplify the development of automated theorem provers
and symbolic manipulation systems. It is based on algebraic term rewriting, which
entails the step-by-step transformation of one expression into another through a
set of 2-sided rewriting rules. Each rule has a left and right hand side. When
the expression matches the left side of a rule, it is replaced by the right. The
process of rewriting continues until the current expression reaches a terminal 
condition, specified by a user-defined function. A symbolic manipulation system
or theorem prover simply entails writing a set of rules, together with a termination
condition function.

We will begin with a few small but semi-realistic examples to illustrate how to use
this library, and then explain the the anatomy in more depth. All examples are
found in the ![resources](/resources) directory.

### Example 1: A Boolean Algebra Theorem Prover

Boolean algebra is often studied in discrete math, abstract algebra,  and electronics. 
In this example, we use a typical set of axioms found in most discrete math texts
as our rewrite rules. The code (found ![here](/resources/bool-algebra-prover.clj)) is as follows:

```clojure
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
                    
(defn bool-prove [lhs rhs] 
  (pprint ((build-prover-fn base-axioms 7) lhs rhs)))
```

This code can be loaded into the REPL and can prove boolean algebra identity theorems
interactively. We constructed a function which can produce proofs of up to 7 steps,
and wrapped it in pretty printing. The ruleset is bi-directional, meaning that right-hand
sides (if encountered) can also be rewritten into left-hand sides. Here is a sample usage
inside the REPL to prove two theorems:

```clojure
user=> (load-file "resources/bool-algebra-prover.clj")
#'user/bool-prove
user=> (bool-prove '(A * ((! A) + B))  '(A * B))
(((A * ((! A) + B)) start)
 ((A * (B + (! A))) "Commutative")
 (((A * B) + (A * (! A))) "Distributive")
 (((A * B) + 0) "Complement")
 ((A * B) "Identity"))
nil
user=> (bool-prove '(((A + B) * A) * (A + B)) 'A)
(((((A + B) * A) * (A + B)) start)
 (((A * (A + B)) * (A + B)) "Commutative")
 ((A * (A + B)) "Absorption")
 (A "Absorption"))
nil
user=>
```

In the example above, the bool-prove function takes in a left-hand side (LHS) and
right-hand side (RHS) and produces a sequence of transforms based on the rule. In
each step the rule applied to create that step from the previous one is included.
Additionaly, each proof begin with the LHS and end with the RHS.


### Key Components in More Depth

We begin with a quick explanation of the key concepts needed to use
this libaray.

**1) Rules:** A rule is simply a list of three elements: a left-hand expression, 
a right-hand expression, and a rule name. An expression is enclosed in parentheses
and can contain any mix of variables or constants. Variables are represented by
keywords (e.g. :x, :y, :x) and constants are anything else. Here is two example rules:

```clojure
'((:a + (:b * 1)) (:a + :b) "Simplify")
'((:a + :b) (:b + :a) "Commutative")
```
In the rules above the variables are :a, :b, and :c and the constants are + and 1. 
To illustrate how the matching and rewriting happens, consider the following expressions:

```clojure
;-------------------- Expression 1 --------------------------
'(x + ((y + z) * 1) 
; Matches the simplify rule, where :a = x and :b = (y + z). 
;Here is how it would be rewritten:
'(x + (y + z))

;-------------------- Expression 2 --------------------------
'(x + y) 
; Matches the commutative rule, where :a = x and :b = y. 
; Here is how it would be rewritten:
'(y + x)

;-------------------- Expression 3 --------------------------
'((w + x) + (y + z)) 
; Matches the commutative rule, where :a = (w + x) and :b = (y + z). 
;Here is how it would be (directly) rewritten:
'((y + z) + (w + x)
```
In Expression 3 above, we note that the two inner expressions could also be rewritten using
the commutative rule. The term rewriting logic will also recursively descend through expressions
and search through all possible 1-step rewrites for the top-level as well as all sub-expressions. So
the term rewriter will actually produce the following rewrites for Expression 3:

```clojure
'((w + x) + (y + z)) ; Expression 3
; Produces the following through 1-step transforms:
'((y + z) + (w + x)
'((x + w) + (y + z))
'((w + x) + (z + y))
````

**2) Rulesets:**

**3) Ruleset Builders:**

**4) Terminal Conditon Functions:**

**5) Prover and Reducer Functions and Builders:**

