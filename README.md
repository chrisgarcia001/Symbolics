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
keywords (e.g. :x, :y, :x) and constants are ordinary symbols. Here is two example rules:

```clojure
'((:a + (:b * 1)) (:a + :b) "Simplify")
'((:a + (:b + :c)) ((:a + :b) + :c) "Associative")
```



**2) Rulesets**

**3) Ruleset Builders**

**4) Terminal Conditon Functions**

**5) Prover and Reducer Functions and Builders**

