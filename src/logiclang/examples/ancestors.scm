/* Ancestor is the transitive closure of parent. */
(fact (parent abraham isaac))
(fact (parent isaac jacob))
(fact (parent jacob joseph))
(rule (ancestor ?x ?y) (parent ?x ?y))
(rule (ancestor ?x ?z) (parent ?x ?y) (ancestor ?y ?z))
(query (ancestor abraham ?who))
