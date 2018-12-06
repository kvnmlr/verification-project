import java.util.Collections;
import java.util.Set;

import mudschecker.*;
import mudspg.TFormula;
import java.util.HashSet;

public class Part1 extends AbstractChecker {
	
	/**
	 * Exercise: given a model and a bound, does the model has less than n states ?
	 * @param model
	 * @param bound
	 * @return
	 */
	public boolean checkBounded(LTS model, int bound) {
		return false; // Obviously wrong
	}
	
	/**
	 * @return boolean is the formula a CTL formula ?
	 */
	public boolean isCTL(TFormula phi) {
		return false; // Obviously wrong
	}
	
	/**
	 * Exercise: given a temporal formula, construct its ENF, or raise an Exception
	 */
	public TFormula toENF(TFormula phi) {
		return phi; // Obviously wrong
	}
	
	/**
	 * Exercise: given a finite model and a temporal formula in ENF, returns the satisfaction set.
	 * Any implementation of Set<State> is allowed (but choose carefully)
	 */
	public Set<State> satSet(LTS model, TFormula phi) {
		if(phi instanceof TFormula.Proposition) 
			return satSetProp(model, (TFormula.Proposition) phi);
		// Write the rest !
		return Collections.emptySet();
	}
	
	/** 
	 * As a courtesy, and in order to provide an example of the API use, we offer you a solution
	 * for the sat. set of a proposition.
	 * @param model
	 * @param phi
	 * @return
	 */
	protected Set<State> satSetProp(LTS model, TFormula.Proposition phi) {
		Set<State> res = new HashSet<State>();
		for(State s: model) {
			if(s.satisfies(phi))
				res.add(s);
		}
		return res;
	}
	
	/**
	 * Exercise: wrap it together
	 */
	@Override
	public boolean solve(LTS model, TFormula tform, int bound) {
		System.out.println(model);
		return false;
	}


}
