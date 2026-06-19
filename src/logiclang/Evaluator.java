package logiclang;
import static logiclang.AST.*;
import static logiclang.Value.*;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import logiclang.Env.*;

public class Evaluator implements Visitor<Value> {
	
	Printer.Formatter ts = new Printer.Formatter();

	Env initEnv = initialEnv(); 
	
	Value valueOf(Program p) {
			return (Value) p.accept(this, initEnv);
	}
	
	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}
	
	@Override
	public Value visit(UnitExp e, Env env) {
		return new UnitVal();
	}

	@Override
	public Value visit(NumExp e, Env env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(StrExp e, Env env) {
		return new StringVal(e.v());
	}

	@Override
	public Value visit(BoolExp e, Env env) {
		return new BoolVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env) {
		try {
			for(Exp d: p.decls())
				d.accept(this, initEnv);
			return (Value) p.e().accept(this, initEnv);
		} catch (ClassCastException e) {
			return new DynamicError(e.getMessage());
		}
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}	

	@Override
	public Value visit(LetExp e, Env env) {
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());
		
		for(Exp exp : value_exps) 
			values.add((Value)exp.accept(this, env));
		
		Env new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv(new_env, names.get(index), values.get(index));

		return (Value) e.body().accept(this, new_env);		
	}	
	
	@Override
	public Value visit(DefineDecl e, Env env) {
		String name = e.name();
		Exp value_exp = e.value_exp();
		Value value = (Value) value_exp.accept(this, env);
		((GlobalEnv) initEnv).extend(name, value);
		return new Value.UnitVal();		
	}	

	@Override
	public Value visit(LambdaExp e, Env env) {
		return new Value.FunVal(env, e.formals(), e.body());
	}
	
	@Override
	public Value visit(CallExp e, Env env) {
		Object result = e.operator().accept(this, env);
		if(!(result instanceof Value.FunVal))
			return new Value.DynamicError("Operator not a function in call " +  ts.visit(e, env));
		Value.FunVal operator =  (Value.FunVal) result; //Dynamic checking
		List<Exp> operands = e.operands();

		// Call-by-value semantics
		List<Value> actuals = new ArrayList<Value>(operands.size());
		for(Exp exp : operands) 
			actuals.add((Value)exp.accept(this, env));
		
		List<String> formals = operator.formals();
		if (formals.size()!=actuals.size())
			return new Value.DynamicError("Argument mismatch in call " + ts.visit(e, env));

		Env fun_env = operator.env();
		for (int index = 0; index < formals.size(); index++)
			fun_env = new ExtendEnv(fun_env, formals.get(index), actuals.get(index));
		
		return (Value) operator.body().accept(this, fun_env);
	}
		
	@Override
	public Value visit(IfExp e, Env env) {
		Object result = e.conditional().accept(this, env);
		if(!(result instanceof Value.BoolVal))
			return new Value.DynamicError("Condition not a boolean in expression " +  ts.visit(e, env));
		Value.BoolVal condition =  (Value.BoolVal) result; //Dynamic checking
		
		if(condition.v())
			return (Value) e.then_exp().accept(this, env);
		else return (Value) e.else_exp().accept(this, env);
	}

	@Override
	public Value visit(LessExp e, Env env) { 
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() < second.v());
	}
	
	@Override
	public Value visit(EqualExp e, Env env) { 
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() == second.v());
	}

	@Override
	public Value visit(GreaterExp e, Env env) { 
		Value.NumVal first = (Value.NumVal) e.first_exp().accept(this, env);
		Value.NumVal second = (Value.NumVal) e.second_exp().accept(this, env);
		return new Value.BoolVal(first.v() > second.v());
	}
	
	@Override
	public Value visit(CarExp e, Env env) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.fst();
	}
	
	@Override
	public Value visit(CdrExp e, Env env) { 
		Value.PairVal pair = (Value.PairVal) e.arg().accept(this, env);
		return pair.snd();
	}
	
	@Override
	public Value visit(ConsExp e, Env env) { 
		Value first = (Value) e.fst().accept(this, env);
		Value second = (Value) e.snd().accept(this, env);
		return new Value.PairVal(first, second);
	}

	@Override
	public Value visit(ListExp e, Env env) { 
		List<Exp> elemExps = e.elems();
		int length = elemExps.size();
		if(length == 0)
			return new Value.Null();
		
		//Order of evaluation: left to right e.g. (list (+ 3 4) (+ 5 4)) 
		Value[] elems = new Value[length];
		for(int i=0; i<length; i++)
			elems[i] = (Value) elemExps.get(i).accept(this, env);
		
		Value result = new Value.Null();
		for(int i=length-1; i>=0; i--) 
			result = new PairVal(elems[i], result);
		return result;
	}	
	
	@Override
	public Value visit(NullExp e, Env env) {
		Value val = (Value) e.arg().accept(this, env);
		return new BoolVal(val instanceof Value.Null);
	}

	public Value visit(EvalExp e, Env env) {
		StringVal programText = (StringVal) e.code().accept(this, env);
		Program p = _reader.parse(programText.v());
		return (Value) p.accept(this, env);
	}

	public Value visit(ReadExp e, Env env) {
		StringVal fileName = (StringVal) e.file().accept(this, env);
		try {
			String text = Reader.readFile("" + System.getProperty("user.dir") + File.separator + fileName.v());
			return new StringVal(text);
		} catch (IOException ex) {
			return new DynamicError(ex.getMessage());
		}
	}

	// ----- Logic programming: knowledge base and resolution engine -----

	private final java.util.List<RuleDecl> _kb = new java.util.ArrayList<RuleDecl>();
	private int _freshId = 0; // used to rename clause variables apart

	@Override
	public Value visit(FactDecl d, Env env) {
		_kb.add(new RuleDecl(d.fact(), new java.util.ArrayList<Term>()));
		return new UnitVal();
	}

	@Override
	public Value visit(RuleDecl d, Env env) {
		_kb.add(d);
		return new UnitVal();
	}

	@Override
	public Value visit(QueryExp e, Env env) {
		java.util.List<Term> goals = e.goals();
		java.util.LinkedHashSet<String> qvars = new java.util.LinkedHashSet<String>();
		for (Term g : goals) collectVars(g, qvars);
		java.util.List<java.util.Map<String,Term>> sols =
			solve(new java.util.ArrayList<Term>(goals), new java.util.HashMap<String,Term>());
		java.util.List<java.util.Map<String,String>> out =
			new java.util.ArrayList<java.util.Map<String,String>>();
		for (java.util.Map<String,Term> s : sols) {
			java.util.Map<String,String> binding = new java.util.LinkedHashMap<String,String>();
			for (String v : qvars)
				binding.put(v, termToString(resolve(new VarTerm(v), s)));
			out.add(binding);
		}
		return new SolutionsVal(out);
	}

	// SLD resolution: returns every substitution under which all goals hold.
	private java.util.List<java.util.Map<String,Term>> solve(
			java.util.List<Term> goals, java.util.Map<String,Term> s) {
		java.util.List<java.util.Map<String,Term>> results =
			new java.util.ArrayList<java.util.Map<String,Term>>();
		if (goals.isEmpty()) { results.add(s); return results; }
		Term goal = goals.get(0);
		java.util.List<Term> rest = goals.subList(1, goals.size());
		for (RuleDecl clause : _kb) {
			int id = ++_freshId; // rename this clause's variables apart
			Term head = rename(clause.head(), id);
			java.util.Map<String,Term> s2 = new java.util.HashMap<String,Term>(s);
			if (unify(goal, head, s2)) {
				java.util.List<Term> newGoals = new java.util.ArrayList<Term>();
				for (Term b : clause.body()) newGoals.add(rename(b, id));
				newGoals.addAll(rest);
				results.addAll(solve(newGoals, s2));
			}
		}
		return results;
	}

	// Unify a and b, recording bindings in s (modified in place; backtracking
	// callers pass a copy).
	private boolean unify(Term a, Term b, java.util.Map<String,Term> s) {
		a = walk(a, s); b = walk(b, s);
		if (a instanceof VarTerm) { s.put(((VarTerm) a).name(), b); return true; }
		if (b instanceof VarTerm) { s.put(((VarTerm) b).name(), a); return true; }
		if (a instanceof AtomTerm && b instanceof AtomTerm)
			return ((AtomTerm) a).name().equals(((AtomTerm) b).name());
		if (a instanceof StructTerm && b instanceof StructTerm) {
			StructTerm sa = (StructTerm) a, sb = (StructTerm) b;
			if (!sa.functor().equals(sb.functor())) return false;
			if (sa.args().size() != sb.args().size()) return false;
			for (int i = 0; i < sa.args().size(); i++)
				if (!unify(sa.args().get(i), sb.args().get(i), s)) return false;
			return true;
		}
		return false;
	}

	// Follow variable bindings until a non-bound term is reached.
	private Term walk(Term t, java.util.Map<String,Term> s) {
		while (t instanceof VarTerm && s.containsKey(((VarTerm) t).name()))
			t = s.get(((VarTerm) t).name());
		return t;
	}

	// Fully apply the substitution to ground a term for display.
	private Term resolve(Term t, java.util.Map<String,Term> s) {
		t = walk(t, s);
		if (t instanceof StructTerm) {
			StructTerm st = (StructTerm) t;
			java.util.List<Term> args = new java.util.ArrayList<Term>();
			for (Term a : st.args()) args.add(resolve(a, s));
			return new StructTerm(st.functor(), args);
		}
		return t;
	}

	// Rename every variable in t with a unique clause id, keeping a clause's
	// variables apart from the goal's and from other uses of the same clause.
	private Term rename(Term t, int id) {
		if (t instanceof VarTerm) return new VarTerm(((VarTerm) t).name() + "#" + id);
		if (t instanceof StructTerm) {
			StructTerm st = (StructTerm) t;
			java.util.List<Term> args = new java.util.ArrayList<Term>();
			for (Term a : st.args()) args.add(rename(a, id));
			return new StructTerm(st.functor(), args);
		}
		return t;
	}

	private void collectVars(Term t, java.util.Set<String> vars) {
		if (t instanceof VarTerm) vars.add(((VarTerm) t).name());
		else if (t instanceof StructTerm)
			for (Term a : ((StructTerm) t).args()) collectVars(a, vars);
	}

	private String termToString(Term t) {
		if (t instanceof VarTerm) return ((VarTerm) t).name();
		if (t instanceof AtomTerm) return ((AtomTerm) t).name();
		StructTerm st = (StructTerm) t;
		StringBuilder sb = new StringBuilder("(").append(st.functor());
		for (Term a : st.args()) sb.append(" ").append(termToString(a));
		return sb.append(")").toString();
	}

	private Env initialEnv() {
		GlobalEnv initEnv = new GlobalEnv();
		
		/* Procedure: (read <filename>). Following is same as (define read (lambda (file) (read file))) */
		List<String> formals = new ArrayList<>();
		formals.add("file");
		Exp body = new AST.ReadExp(new VarExp("file"));
		Value.FunVal readFun = new Value.FunVal(initEnv, formals, body);
		initEnv.extend("read", readFun);

		/* Procedure: (require <filename>). Following is same as (define require (lambda (file) (eval (read file)))) */
		formals = new ArrayList<>();
		formals.add("file");
		body = new EvalExp(new AST.ReadExp(new VarExp("file")));
		Value.FunVal requireFun = new Value.FunVal(initEnv, formals, body);
		initEnv.extend("require", requireFun);
		
		/* Add new built-in procedures here */ 
		
		return initEnv;
	}
	
	Reader _reader; 
	public Evaluator(Reader reader) {
		_reader = reader;
	}
}
