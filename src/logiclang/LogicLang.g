grammar LogicLang;

// LogicLang: a logic-programming language (a compact Prolog) layered on the
// functions+lists base.
//
// Standalone grammar in the style of the other *Lang grammars (no imports).
// In addition to the base expressions, LogicLang adds facts, rules, and queries
// over terms (atoms, variables written ?x, and compound terms). Facts and rules
// populate a knowledge base; a query is solved by unification and backtracking.

 program returns [Program ast]
 		locals [ArrayList<Exp> defs, Exp expr]
 		@init { $defs = new ArrayList<Exp>(); $expr = new UnitExp(); } :
		(d=decl { $defs.add($d.ast); } )* (e=exp { $expr = $e.ast; } )?
		{ $ast = new Program($defs, $expr); }
		;

 decl returns [Exp ast] :
 		dd=definedecl { $ast = $dd.ast; }
 		| fd=factdecl { $ast = $fd.ast; }
 		| rd=ruledecl { $ast = $rd.ast; }
 		;

 definedecl returns [DefineDecl ast] :
 		'(' Define
 			id=Identifier
 			e=exp
 		')' { $ast = new DefineDecl($id.text, $e.ast); }
 		;

 // New declarations and query for LogicLang

 factdecl returns [FactDecl ast] :
 		'(' Fact t=term ')' { $ast = new FactDecl($t.ast); }
 		;

 ruledecl returns [RuleDecl ast]
        locals [ArrayList<Term> body]
 		@init { $body = new ArrayList<Term>(); } :
 		'(' Rule h=term ( g=term { $body.add($g.ast); } )+ ')'
 		{ $ast = new RuleDecl($h.ast, $body); }
 		;

 queryexp returns [QueryExp ast]
        locals [ArrayList<Term> goals]
 		@init { $goals = new ArrayList<Term>(); } :
 		'(' Query ( g=term { $goals.add($g.ast); } )+ ')'
 		{ $ast = new QueryExp($goals); }
 		;

 term returns [Term ast]
        locals [ArrayList<Term> args]
 		@init { $args = new ArrayList<Term>(); } :
 		v=Variable { $ast = new VarTerm($v.text); }
 		| id=Identifier { $ast = new AtomTerm($id.text); }
 		| n=Number { $ast = new AtomTerm($n.text); }
 		| '(' f=Identifier ( a=term { $args.add($a.ast); } )* ')'
 			{ $ast = new StructTerm($f.text, $args); }
 		;

 exp returns [Exp ast]:
		va=varexp { $ast = $va.ast; }
		| num=numexp { $ast = $num.ast; }
		| str=strexp { $ast = $str.ast; }
		| bl=boolexp { $ast = $bl.ast; }
        | add=addexp { $ast = $add.ast; }
        | sub=subexp { $ast = $sub.ast; }
        | mul=multexp { $ast = $mul.ast; }
        | div=divexp { $ast = $div.ast; }
        | let=letexp { $ast = $let.ast; }
        | lam=lambdaexp { $ast = $lam.ast; }
        | i=ifexp { $ast = $i.ast; }
        | less=lessexp { $ast = $less.ast; }
        | eq=equalexp { $ast = $eq.ast; }
        | gt=greaterexp { $ast = $gt.ast; }
        | car=carexp { $ast = $car.ast; }
        | cdr=cdrexp { $ast = $cdr.ast; }
        | cons=consexp { $ast = $cons.ast; }
        | list=listexp { $ast = $list.ast; }
        | nl=nullexp { $ast = $nl.ast; }
        | qu=queryexp { $ast = $qu.ast; }
        | call=callexp { $ast = $call.ast; }
        ;

 // Expressions inherited from the functions + lists base

 lambdaexp returns [LambdaExp ast]
        locals [ArrayList<String> formals ]
 		@init { $formals = new ArrayList<String>(); } :
 		'(' Lambda
 			'(' (id=Identifier { $formals.add($id.text); } )* ')'
 			body=exp
 		')' { $ast = new LambdaExp($formals, $body.ast); }
 		;

 callexp returns [CallExp ast]
        locals [ArrayList<Exp> arguments = new ArrayList<Exp>();  ] :
 		'(' f=exp
 			( e=exp { $arguments.add($e.ast); } )*
 		')' { $ast = new CallExp($f.ast,$arguments); }
 		;

 ifexp returns [IfExp ast] :
 		'(' If
 		    e1=exp
 			e2=exp
 			e3=exp
 		')' { $ast = new IfExp($e1.ast,$e2.ast,$e3.ast); }
 		;

 lessexp returns [LessExp ast] :
 		'(' Less
 		    e1=exp
 			e2=exp
 		')' { $ast = new LessExp($e1.ast,$e2.ast); }
 		;

 equalexp returns [EqualExp ast] :
 		'(' Equal
 		    e1=exp
 			e2=exp
 		')' { $ast = new EqualExp($e1.ast,$e2.ast); }
 		;

 greaterexp returns [GreaterExp ast] :
 		'(' Greater
 		    e1=exp
 			e2=exp
 		')' { $ast = new GreaterExp($e1.ast,$e2.ast); }
 		;

 carexp returns [CarExp ast] :
 		'(' Car
 		    e=exp
 		')' { $ast = new CarExp($e.ast); }
 		;

 cdrexp returns [CdrExp ast] :
 		'(' Cdr
 		    e=exp
 		')' { $ast = new CdrExp($e.ast); }
 		;

 consexp returns [ConsExp ast] :
 		'(' Cons
 		    e1=exp
 			e2=exp
 		')' { $ast = new ConsExp($e1.ast,$e2.ast); }
 		;

 listexp returns [ListExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' List
 		    ( e=exp { $list.add($e.ast); } )*
 		')' { $ast = new ListExp($list); }
 		;

 nullexp returns [NullExp ast] :
 		'(' Null
 		    e=exp
 		')' { $ast = new NullExp($e.ast); }
 		;

 strexp returns [StrExp ast] :
 		s=StrLiteral { $ast = new StrExp($s.text); }
 		;

 boolexp returns [BoolExp ast] :
 		TrueLiteral { $ast = new BoolExp(true); }
 		| FalseLiteral { $ast = new BoolExp(false); }
 		;

 numexp returns [NumExp ast]:
 		n0=Number { $ast = new NumExp(Integer.parseInt($n0.text)); }
  		| '-' n0=Number { $ast = new NumExp(-Integer.parseInt($n0.text)); }
  		| n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble($n0.text+"."+$n1.text)); }
  		| '-' n0=Number Dot n1=Number { $ast = new NumExp(Double.parseDouble("-" + $n0.text+"."+$n1.text)); }
  		;

 addexp returns [AddExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '+'
 		    e=exp { $list.add($e.ast); }
 		    ( e=exp { $list.add($e.ast); } )+
 		')' { $ast = new AddExp($list); }
 		;

 subexp returns [SubExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '-'
 		    e=exp { $list.add($e.ast); }
 		    ( e=exp { $list.add($e.ast); } )+
 		')' { $ast = new SubExp($list); }
 		;

 multexp returns [MultExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '*'
 		    e=exp { $list.add($e.ast); }
 		    ( e=exp { $list.add($e.ast); } )+
 		')' { $ast = new MultExp($list); }
 		;

 divexp returns [DivExp ast]
        locals [ArrayList<Exp> list]
 		@init { $list = new ArrayList<Exp>(); } :
 		'(' '/'
 		    e=exp { $list.add($e.ast); }
 		    ( e=exp { $list.add($e.ast); } )+
 		')' { $ast = new DivExp($list); }
 		;

 varexp returns [VarExp ast]:
 		id=Identifier { $ast = new VarExp($id.text); }
 		;

 letexp  returns [LetExp ast]
        locals [ArrayList<String> names, ArrayList<Exp> value_exps]
 		@init { $names = new ArrayList<String>(); $value_exps = new ArrayList<Exp>(); } :
 		'(' Let
 			'(' ( '(' id=Identifier e=exp ')' { $names.add($id.text); $value_exps.add($e.ast); } )+  ')'
 			body=exp
 			')' { $ast = new LetExp($names, $value_exps, $body.ast); }
 		;

 // Lexical Specification of this Programming Language

 Define : 'define' ;
 Let : 'let' ;
 Dot : '.' ;
 Lambda : 'lambda' ;
 If : 'if' ;
 Car : 'car' ;
 Cdr : 'cdr' ;
 Cons : 'cons' ;
 List : 'list' ;
 Null : 'null?' ;
 Fact : 'fact' ;
 Rule : 'rule' ;
 Query : 'query' ;
 Less : '<' ;
 Equal : '=' ;
 Greater : '>' ;
 TrueLiteral : '#t' ;
 FalseLiteral : '#f' ;

 Number : DIGIT+ ;

 Variable : '?' Letter LetterOrDigit* ;

 Identifier :   Letter LetterOrDigit*;

 Letter :   [a-zA-Z$_]
	|   ~[ -ÿ\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|   [\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}? ;

 LetterOrDigit: [a-zA-Z0-9$_]
	|   ~[ -ÿ\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|    [\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?;

 fragment DIGIT: ('0'..'9');

 AT : '@';
 ELLIPSIS : '...';
 WS  :  [ \t\r\n]+ -> skip;
 Comment :   '/*' .*? '*/' -> skip;
 Line_Comment :   '//' ~[\r\n]* -> skip;

 fragment ESCQUOTE : '\\"';
 StrLiteral :   '"' ( ESCQUOTE | ~('\n'|'\r') )*? '"';
