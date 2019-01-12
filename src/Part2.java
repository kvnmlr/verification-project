import java.util.*;

import helper.NBA;
import helper.NotSupportedFormula;
import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.storage.StoredEdgeWithLabel;
import jhoafparser.storage.StoredState;
import mudschecker.LTS;
import mudschecker.State;
import mudschecker.Transition;
import mudspg.Evaluation;
import mudspg.Expression;
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
        return numberOfStates;
    }

    public int nbStatesCl(TFormula tform) {
        return 0;
    }

    /**
     * Question c
     */
    public LTS product(LTS model, TFormula tform, TFormula.Proposition af) {
        try {
            NBA nba = new NBA(tform);

            List<State> states = new ArrayList<>();

            for (State smodel : model) {
                for (StoredState sform : nba.aut.getStoredStates()) {
                    State s = new State() {
                        @Override
                        public Iterator<Transition> iterator() {
                            return null;
                        }

                        @Override
                        public boolean satisfies(TFormula.Proposition prop) {
                            BooleanExpression<AtomLabel> labelExpr = sform.getLabelExpr();
                            TFormula.Proposition propOfLabel = nba.propOfLabel(labelExpr);

                            return smodel.satisfies(propOfLabel);
                        }
                    };

                    states.add(s);
                }
            }

            for (State initialState : model.initialStates) {
                for (Transition trans : initialState) {

                }


            }

            LTS product = new LTS() {
                @Override
                public Iterator<State> iterator() {
                    return super.iterator();
                }
            };


        } catch (NotSupportedFormula notSupportedFormula) {
            notSupportedFormula.printStackTrace();
        }
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
            for (StoredState state : aphi.aut.getStoredStates()) {
                System.out.println("Description of state " + state.getStateId());
                System.out.println("  Is accepting:" + !state.getAccSignature().isEmpty()); // There only one acceptance set, so the
                //acceptance signature is always [] or [0] (the latter if accepting)
                for (StoredEdgeWithLabel edge : aphi.aut.getEdgesWithLabel(state.getStateId())) {
                    System.out.println("  transition to " + edge.getConjSuccessors().get(0) + " under condition " + aphi.propOfLabel(edge.getLabelExpr()));
                }
            }
            System.out.println("Or you may want the HOA format:");
            aphi.printHOA();

            if (!isLTL(tform)) {
                return false;
            }

            if (!checkBounded(model, bound)) {
                return false;
            }
            TFormula.Proposition propTrue = new TFormula.Proposition(new Expression<>() {
                @Override
                public Boolean compute(Evaluation e) {
                    return true;
                }
            });

            LTS product = product(model, tform, propTrue);
            return (persistenceWit(product, propTrue, bound) != null);

        } catch (NotSupportedFormula e) {
            return false; // Not LTL
        }
    }

    private boolean checkBounded(LTS model, int bound) {
        int n = 0;
        for (State ignored : model) {
            if (++n >= bound) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given formula is a valid LTL formula
     * and goes on recursively for sub formulas
     *
     * @param phi the formula
     * @return true iff the given phi is a valid LTL formula
     */
    private boolean isLTL(TFormula phi) {
        // Start with a state formula and check each sub formula recursively
        String s = phi.getClass().getSimpleName();
        switch (s) {
            case "Proposition":
                return true;
            case "Not":
            case "Next":
            case "Globally":
            case "Eventually":
                return isLTL(((TFormula.UnaryOp) phi).sub);
            case "And":
            case "Or":
            case "Until":
            case "WeakUntil":
                return isLTL(((TFormula.BinaryOp) phi).suba) && isLTL(((TFormula.BinaryOp) phi).subb);
            default:
                return false;
        }
    }
}
