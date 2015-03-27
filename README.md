Symbolics
===============

### A library for automated theorem proving and symbolic manipulation

Symbolics is a library to simplify the development of automated theorem provers
and symbolic manipulation systems. It is based on algebraic term rewriting, which
entails the step-by-step transformation of one expression into another through a
set of 2-sided rewriting rules. Each rule has a left and right hand side. When
an expression matches the left side of a rule, it is replaced by the right. The
process of rewriting continues until the current expression reaches a terminal 
condition, specified by a user-defined function. A symbolic manipulation system
or theorem prover simply entails writing a set of rules, together with a termination
condition function.

We will begin with a few small but semi-realistic examples to illustrate how to use
this library, and then explain the the anatomy in more depth. All examples are
found in the ![resources](/resources) directory.

#### Example 1: A Symbolic Equation Solver

Computer algebra systems (CAS) can solve equations by manipulating them algebraically, 
allowing symbolic solution as opposed to merely numeric. In this example we show 
a rudimentary CAS-style equation solver (full source code ![here](/resources/sym-equation-solver.clj)).
This allows you to specify an equation and a variable or expression to solve for,
and the solver will find the solution algebraically. It can be extended fairly
easily to include more rules, but this suffices for demonstration:

```clojure
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
    '((:x * 1) :x "Simplification")
    '((:x * 0) 0 "Simplification")
    '((:x + 0) :x "Simplification")
    '((:x / :x) 1 "Simplification")
    '((:x / 1) :x "Simplification")
    '(((:x / :y) * :y) :x "Multiply by denom.")))

; Combine the three rulesets into one.
(def rules (concat equational-eqvs term-eqvs simplifiers))

; Create a function that solves the given equation for the specified 
; target variable or expression. Produces a sequence of up to 9 steps starting
; with given equation and ending with new equation where target is on 
; left hand side. See examples below.
(defn eq-solve [equation target] 
  (let [target-f #(= (first %1) target)]
      (pprint ((build-reducer-fn rules target-f 9) equation))))
```

Below is a sample usage inside the REPL to solve two equations for specified
variables, showing all steps involved to get from start to finish:

```clojure
user=> (load-file "resources/sym-equation-solver.clj")
#'user/eq-solve
user=> (eq-solve '(A = ((W + X) / (Y + Z))) 'Z) ; solve for Z
(((A = ((W + X) / (Y + Z))) start)
 ((((W + X) / (Y + Z)) = A) "Flip Equation")
 (((W + X) = (A * (Y + Z))) "Balance Sides: Multip./Division")
 (((W + X) = (A * (Z + Y))) "Commutative Add.")
 (((W + X) = ((Z + Y) * A)) "Commutative Mult.")
 ((((W + X) / A) = (Z + Y)) "Balance Sides: Multip./Division")
 (((((W + X) / A) - Y) = Z) "Balance Sides: Addition/Subtraction")
 ((Z = (((W + X) / A) - Y)) "Flip Equation"))
nil
user=> (eq-solve '(E = ((Z * S) / (sqrt N))) 'N) ; solve for N
(((E = ((Z * S) / (sqrt N))) start)
 ((((Z * S) / (sqrt N)) = E) "Flip Equation")
 (((Z * S) = (E * (sqrt N))) "Balance Sides: Multip./Division")
 (((Z * S) = ((sqrt N) * E)) "Commutative Mult.")
 ((((Z * S) / E) = (sqrt N)) "Balance Sides: Multip./Division")
 (((power ((Z * S) / E) 2) = N) "Balance Sides: Square/Square Root")
 ((N = (power ((Z * S) / E) 2)) "Flip Equation"))
nil
user=>
```

In the solutions above it may appear that some steps are unnecessary (e.g. equation
flipping). However, this is because we had rules only to deal with one equational
layout. So to find matches, the equations needed to be flipped first, and we 
did this to keep the rulesets short. By adding rules for mirroring layots we would 
have gotten shorter sequences of steps.

#### Example 2: A Boolean Algebra Theorem Prover

Boolean algebra is often studied in discrete math, abstract algebra, and electrical engineering. 
A proof that two expressions are identities (equal to one another) entails transforming one expression 
into the other through a sequence of algebraic transforms based on known equivalences (axioms). 
In this example, we use a typical set of axioms found in most discrete math texts as our rewrite 
rules to prove identities. The code (found ![here](/resources/bool-algebra-prover.clj)) 
is as follows:

```clojure
; This is boolean algebra theorem prover which uses a fairly
; standard set of axioms found in most discrete math books.

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
specified by the user. Using the *build-prover-fn* function we constructed another function 
*bool-prove* (which we then wrapped in pretty printing) which can produce proofs of 
up to 7 steps. The ruleset is bi-directional, meaning that right-hand sides 
(if encountered) can also be rewritten into left-hand sides. Here is a sample 
usage inside the REPL to prove two theorems:

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
right-hand side (RHS) and produces a sequence of transforms based on the rules. In
each step the rule applied to create that step from the previous one is included.
Additionaly, each proof begin with the LHS and ends with the RHS.


#### Documentation

A more systematic overview and a description of the full
range of capabilities can be found in the documentation ![here](/doc/overview.md).

#### License

This project is licensed under the Eclipse Public License. Contributions welcome!