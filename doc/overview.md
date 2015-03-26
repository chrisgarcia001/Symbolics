Overview of Symbolics
===============

It is fairly easy to create systems using symbolics by looking at examples. If you haven't already,
read through the main page to get an idea how this library works ![here](../README.md). In this 
document we explain the core ideas and illustrate the fuller range of capabilities for this library. 
A a few tips for usage are also given toward the end. 

#### Key Components in More Depth

In this section we delve a little deeper into the key components and concepts needed to build symbolic
manipulation systems with this library. The key components are described below:

**1) Rules and Pattern Matching:** A rule is simply a list of three elements: a left-hand expression, 
a right-hand expression, and a rule name. An expression is enclosed in parentheses
and can contain any mix of variables or constants. Variables are represented by
keywords (e.g. *:x*, *:y*, and *:x*) and constants are anything else. Here is two example rules:

```clojure
'((:a + (:b * 1)) (:a + :b) "Simplify")
'((:a + :b) (:b + :a) "Commutative")
```
In the rules above the variables are *:a*, *:b*, and *:c* and the constants are *+* and *1*. 
To illustrate how the matching and rewriting happens, consider the following expressions:

```clojure
'(x + ((y + z) * 1) ; Expression 1
; Matches the simplify rule, where :a = x and :b = (y + z). 
; Here is how it would be rewritten:
'(x + (y + z))

'(x + y) ; Expression 2
; Matches the commutative rule, where :a = x and :b = y. 
; Here is how it would be rewritten:
'(y + x)


'((w + x) + (y + z)) ; Expression 3 
; Matches the commutative rule, where :a = (w + x) and :b = (y + z). 
; Here is how it would be (directly) rewritten:
'((y + z) + (w + x)
```
In Expression 3 above, we note that the two inner expressions could also be rewritten using
the commutative rule. The term rewriting logic will also recursively descend through expressions
and search through all possible 1-step rewrites for the top-level as well as all sub-expressions. So
the term rewriter will actually produce the following rewrites for Expression 3:

```clojure
'((w + x) + (y + z)) ; Expression 3
; Any of the following can be produced in 1 step:
'((y + z) + (w + x)
'((x + w) + (y + z))
'((w + x) + (z + y))
````

**2) Rulesets:** A ruleset is simply a list of rules. Accordingly, two or more sets of rules can be merged 
into a single set with the *concat* function.

**3) Ruleset Builders:** There are three basic functions for building rulesets: *unidirectional-ruleset*, 
*bidirectional-ruleset*, and *operator-ruleset*. The *unidirectional-ruleset* matches only the left hand expression
and rewrites as the right. The *bidirectional-ruleset* function will match either side and rewrite as the opposite 
expression. Usage of the *unidirectional-ruleset* and *bidirectional-ruleset* functions is illustrated in the examples 
above. 

Sometimes we want to have a function or operator (which can compute something) to also be incorporated as something
to be manipulated symbolically, and applied where it can be applied. For example, we would **not** want *(2 + 3)* to appear
in a (partially) symbolic expression, we would rather it be *5*. However, we **would** want *(a + 3)*, since *a* is a symbol
and the expression *(a + 3)* cannot be evaluated. 

Any operator or user-defined function can be incorporated like this. To do so, just use the *operator-ruleset' function 
to build a ruleset specifying which symbols should be evaluated as a function where posible, and then merge with a set of
rewrite rules. Here is an example:

```clojure
(def ops (operator-ruleset '(+ "Add.") '(* "Mult."))) ; Evaluate + and * where possible
```

Any function or operator must be evaluated in standard Clojure prefix (e.g. *(+ 2 3)*), not infix (e.g. *(2 + 3)*). 
If infix is desired then rewrite rules which go from infix to prefix can simply be incorporated.

Here is a small sample which uses rewrite rules together with operators (source ![here](../resources/operators-example.clj)):

```clojure
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
```

The function above reduces an expression down to one with at most 2 non-numbers.
Below is a sample usage inside the REPL to solve two equations for specified
variables:

```clojure
user=> (load-file "resources/operators-example.clj")
#'user/reduce-down
user=> (reduce-down '(+ (* 2 x) (* x 3)))
(((+ (* 2 x) (* x 3)) start)
 ((+ (* 2 x) (* 3 x)) "Commutative-Mult")
 ((* (+ 2 3) x) "Dist")
 ((* 5 x) "Func. Eval."))
nil
user=> (reduce-down '(+ (* 2 x) (* x (my-pow 3 2))))
(((+ (* 2 x) (* x (my-pow 3 2))) start)
 ((+ (* 2 x) (* (my-pow 3 2) x)) "Commutative-Mult")
 ((* (+ 2 (my-pow 3 2)) x) "Dist")
 ((* 11 x) "Func. Eval."))
nil
user=>
```

**4) Prover and Reducer Functions and Builders:** There are two main ways this
library can be used: 1) either for automated theorem proving, or 2) as a general
term rewriting system. Algebraic theorem proving involves transforming one 
expression into another, while term rewriting involves successively transforming
an expression until it meets some termination criteria. Accordingly, there are two
functions used to build prover or rewriting systems

A theorem prover can be built with the following function:

*(build-prover-fn [ruleset] [max steps in proof])*

The function *build-prover-fn* will construct a new function which takes a 
left and right expressions, and builds a proof that transforms the left into the 
right. Here is a sample usage:

```clojure
(let [prove (build-prover-fn ruleset 10)
      proof (prove lhs rhs)]
   proof)  ; will return a proof transforming lhs into rhs of at most 10 steps
```

In some cases we want to terminate not when LHS is turned into RHS, but rather 
when the current expression meets a more general termination criteria. A general 
rewriting system of this form can be constructed with the following function:

*(build-reducer-fn [ruleset] [termination criteria function] [max steps])*

```clojure
; In this example the termination criteria is if the expression has 1 element.
(let [termination-criteria (fn [exp] (= (count exp) 1)) 
      reducer-fn (build-reducer-fn ruleset termination-criteria 10)
      steps (reducer-fn starting-expression)]
  steps) ; Return the results - a set of steps to reach stopping criteria
```
In both of the above cases, if a proof or terminating reduction cannot be produced
within the maximum number of steps, the solver function (i.e *prove* in the first
example and "reducer-fn* in the second) will simply return *false*.

#### Tips for Usage

The general philosophy for this library is ease of use and generality. Some 
performance is sacrificed for generality, and this does not incorporate optimizations
based on characteristics specific to certain axiom or rule systems. 

The underlying algorithm uses dynamic programming to avoid resolving 
sub-problems. It uses memory to speed up the processing, and it consumes a lot of memory.
It is recommended to up your JVM memory allocation when using this library. If you are using
Leiningen, you can do this easily by adding the following (or similar) to your project.clj:

```clojure
:jvm-opts ["-Xmx3g"] ; allocate 3 GB to JVM
```

Theorem proving is in general very computationally intensive. It may take 15-30 
seconds (or more) in some cases for proofs to be constructed. Increasing memory will
speed the process. 

As a final note, when constructing these types of systems it can be difficult to know at the start
which rules should be used. In many systems there are known axioms, but these are often 
insufficent for achieving proofs or other symbolic manipulations. Often other
rules are needed (such as a rule that x * 1 = x). So try to take an incremental 
approach. Add a few rules at a time and experimentally build up your system. 

