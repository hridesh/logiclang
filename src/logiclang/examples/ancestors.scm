// A recursive rule in LogicLang: ancestor is the transitive closure of parent.
//
// Backtracking enumerates every solution. Run with:  run ancestors.scm
//
// Expected answers:  ?who = isaac, ?who = jacob, ?who = joseph
(fact (parent abraham isaac))
(fact (parent isaac jacob))
(fact (parent jacob joseph))
(rule (ancestor ?x ?y) (parent ?x ?y))
(rule (ancestor ?x ?z) (parent ?x ?y) (ancestor ?y ?z))
(query (ancestor abraham ?who))
