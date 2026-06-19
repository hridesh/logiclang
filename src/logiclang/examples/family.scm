/* Family relationships: who are abraham's grandchildren? */
(fact (parent abraham isaac))
(fact (parent isaac jacob))
(fact (parent jacob joseph))
(rule (grandparent ?x ?z) (parent ?x ?y) (parent ?y ?z))
(query (grandparent abraham ?who))
