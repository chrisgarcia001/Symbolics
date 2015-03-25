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

### Preliminaries and Key Components

We begin with a quick explanation of the key concepts needed to use
this libaray.

**1) Rules** A rule is simply a list of three elements: a left-hand expression, 
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
;--------------------------------- Expression 1 ----------------------------------------------
'(x + ((y + z) * 1) 
; Matches the simplify rule, where :a = x and :b = (y + z). Here is how it would be rewritten:
'(x + (y + z))

;--------------------------------- Expression 2 ----------------------------------------------
'(x + y) 
; Matches the commutative rule, where :a = x and :b = y. Here is how it would be rewritten:
'(y + x)

;--------------------------------- Expression 3 ----------------------------------------------
'((w + x) + (y + z)) 
; Matches the commutative rule, where :a = (w + x) and :b = (y + z). Here is how it would
  be (directly) rewritten:
'((y + z) + (w + x)
```
In Expression 3 above, we note that the two inner expressions could also be rewritten using
the commutative rule. The term rewriting logic will also recursively descend through expressions
and search through all possible 1-step rewrites for the top-level as well as all sub-expressions. So
the term rewriter will actually produce the following rewrites for Expression 3:

```clojure
'((w + x) + (y + z))
; Produces the following through 1-step transforms:
'((y + z) + (w + x)
'((x + w) + (y + z))
'((w + x) + (z + y))
````

**2) Rulesets:**

**3) Ruleset Builders:**

**4) Terminal Conditon Functions:**

**5) Prover and Reducer Functions and Builders:**

