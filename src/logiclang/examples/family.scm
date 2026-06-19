// Family relationships in LogicLang.
//
// Facts and a rule populate a knowledge base; the query is answered by
// unification and backtracking. Run with:  run family.scm
//
// Expected answer:  ?who = jacob   (abraham -> isaac -> jacob)
(fact (parent abraham isaac))
(fact (parent isaac jacob))
(fact (parent jacob joseph))
(rule (grandparent ?x ?z) (parent ?x ?y) (parent ?y ?z))
(query (grandparent abraham ?who))
