import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import helper.NBA;
import helper.NotSupportedFormula;
import jhoafparser.storage.StoredEdgeWithLabel;
import jhoafparser.storage.StoredState;
import mudschecker.LTS;
import mudschecker.State;
import mudspg.TFormula;

public class Part2 extends AbstractChecker {
	
	/** 
	 * Question a
	 */
	public List<State> persistenceWit(LTS model, TFormula.Proposition prop, int bound) {
		return Collections.emptyList(); // obviously wrong
	}
	
	/**
	 * Question b
	 * You are up to hard code the answer for the particular formula of the subject, or compute it for each tform.
	 * For ease of use, the answers to both questions correspond to the number of generated states (even if not reachable/coreachable).
	 */
	public int nbStatesBb(TFormula tform) {
		NBA aphi = null;
		try {
			aphi = new NBA(tform);
		} catch (NotSupportedFormula notSupportedFormula) {
			notSupportedFormula.printStackTrace();
		}
		int numberOfStates = aphi.aut.getNumberOfStates();
		return numberOfStates
				;
	}
	
	public int nbStatesCl(TFormula tform) {
		return 0;
	}
	
	/**
	 * Question c
	 */
	public LTS product(LTS model, TFormula tform, TFormula.Proposition af) {
		return model; // obviously wrong
	}
	
	/**
	 * Question d : wrap it together
	 * Bonus: don't return "false" for non-LTL formula and do the general model checking
	 */
	@Override
	public boolean solve(LTS model, TFormula tform, int bound) {
		try {
			// Demonstration of the helper tools here, you are not forced to use them:
			NBA aphi = new NBA(tform);
			System.out.println("automaton has number of states: " + nbStatesBb(tform));

			System.out.println("automaton starts in" + aphi.aut.getStoredHeader().getStartStates());
			for( StoredState state: aphi.aut.getStoredStates()) {
				System.out.println("Description of state " + state.getStateId());
				System.out.println("  Is accepting:" + !state.getAccSignature().isEmpty()); // There only one acceptance set, so the
					//acceptance signature is always [] or [0] (the latter if accepting)
				for( StoredEdgeWithLabel edge: aphi.aut.getEdgesWithLabel(state.getStateId())) {
					System.out.println("  transition to " + edge.getConjSuccessors().get(0) + " under condition " + aphi.propOfLabel(edge.getLabelExpr()));	
				}
			}
			System.out.println("Or you may want the HOA format:");
			aphi.printHOA();
		} catch (NotSupportedFormula e) {
			return false; // Not LTL
		}
		return false;
	}

}
