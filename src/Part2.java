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
     *
     * @param model The model
     * @param prop  The property
     * @param bound The bound on the model state space
     * @return The witness iff one exists or null otherwise or if the model has more than @bound states or a
     * terminal state is found during exploration (TODO)
     */
    public List<State> persistenceWit(LTS model, TFormula.Proposition prop, int bound) {
        Set<State> outerVisitingSet = new HashSet<>();
        Set<State> innerVisitingSet = new HashSet<>();
        List<State> witness = new ArrayList<>();

        if (!checkBounded(model, bound)) {
            return null;
        }

        for (State initial : model.initialStates) {
            if (!witness.isEmpty()) {
                break;
            }
            System.out.println(initial.hashCode());
            List<State> trace = new ArrayList<>();  // Trace of the current DFS run (i.e. list of explored states)
            DFS(initial, outerVisitingSet, innerVisitingSet, prop, trace, witness);
        }

        if (witness.isEmpty()) {
            return null;
        }
        for(State s : witness){
            System.out.println(s.hashCode());
        }
        return witness;
    }

    /**
     * Implements the outer DFS to encounter reachable states
     *
     * @param startState       The start state from where to continue exploring
     * @param outerVisitingSet The global visiting set of the outer DFS
     * @param innerVisitingSet The global visiting set of the inner DFS
     * @param prop             The proposition (a)
     * @param trace            The trace up to the current start state
     * @param witness          The global witness
     * @return false iff a cycle or a terminal state is found from the current start state
     */
    private boolean DFS(State startState, Set<State> outerVisitingSet, Set<State> innerVisitingSet, TFormula.Proposition prop, List<State> trace, List<State> witness) {
        System.out.println("Starting outer DFS");
        if (!witness.isEmpty()) {
            System.out.println("Witness is empty");
            // If the program is correct, this should not happen
            return false;
        }

        if (!startState.iterator().hasNext()) {
            System.out.println("Terminal state has been found");
            // Found a terminal state
            return false;
        }

        trace.add(startState);
        if (!outerVisitingSet.contains(startState)) {
            outerVisitingSet.add(startState);
            System.out.println(startState.hashCode());
            for (Transition trans : startState) {
                State post = trans.target;
                System.out.println("Before recursive DFS call: " + post.hashCode());
                // TODO not sure if this is correct. If the DFS returns false (i.e. cycle discovered) we return all recursive calls with false
                if (!DFS(post, outerVisitingSet, innerVisitingSet, prop, new ArrayList<>(trace), witness)) {
                    System.out.println("DFS is not true for successors");
                    return false;
                }
            }

            if (!startState.satisfies(prop)) {
                System.out.println("About to call cyclecheck for state:" + startState.hashCode());
                Stack<State> cycle = cycleCheck(startState, innerVisitingSet);
                if (cycle != null) {
                    // Construct the witness given the trace and the cycle
                    witness.addAll(trace);
                    witness.remove(witness.size()-1);// start state has already been added to trace
                    witness.addAll(cycle);  // adds states in the order they have been pushed to the stack
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The innter DFS that checks for a cycle from a given start state.
     * This should be an exact implementation of the CYCLE_CHECK(s) algorithm
     *
     * @param startState       the start state of the cycle
     * @param innerVisitingSet the set of explored states
     * @return A non-null stack containing the loop states if a loop was discovered, null otherwise
     */
    private Stack<State> cycleCheck(State startState, Set<State> innerVisitingSet) {
        if (innerVisitingSet == null) {
            innerVisitingSet = new HashSet<>();
        }
        Stack<State> stack = new Stack<>();

        stack.push(startState);
        System.out.println("Entered CC with state and pushed to stack: " + startState.hashCode());
        innerVisitingSet.add(startState);

        while (!stack.isEmpty()) {
            System.out.println("Stack not empty");
            State nextState = stack.peek();

            // If the start state is in the post set of the next state
            for (Transition trans : nextState) {
                State post = trans.target;
                if (startState.equals(post)) {
                    stack.push(startState);
                    return stack;
                }
            }

            // If the post set of the next state is not a true subset of the explored set
            boolean foundUnexplored = false;
            for (Transition trans : nextState) {
                State post = trans.target;
                if (!innerVisitingSet.contains(post)) {
                    // choose the first post state that is not yet explored
                    foundUnexplored = true;
                    innerVisitingSet.add(post);
                    stack.push(post);
                }
            }

            if (!foundUnexplored) {
                stack.pop();
            }
        }
        return null;
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
        /*
         __Closure:__
         cl(phi) = {a, !a, b, !b, z, !z, (aUz), (bUz), !(aUz), !(bUz)}

         __Elementary Sets:__
         {phi, aUz, bUz, z, a, b}
         {phi, aUz, bUz, z, !a, b}
         {phi, aUz, bUz, z, a, !b}
         {phi, aUz, bUz, z, !a, !b}
         {phi, aUz, bUz, !z, a, b}

         {!phi, !(aUz), bUz, !z, a, b}
         {!phi, !(aUz), bUz, !z, !a, b}

         {!phi, !(aUz), !(bUz), !z, a, b}
         {!phi, !(aUz), !(bUz), !z, !a, b}
         {!phi, !(aUz), !(bUz), !z, a, !b}
         {!phi, !(aUz), !(bUz), !z, !a, !b}

         {!phi, aUz, !(bUz), !z, a, b}
         {!phi, aUz, !(bUz), !z, a, !b}
         */
        return 13;
    }

    /**
     * Question c
     */
    public LTS product(LTS model, TFormula tform, TFormula.Proposition af) {
        NBA nba;
        try {
            nba = new NBA(tform);
        } catch (NotSupportedFormula notSupportedFormula) {
            notSupportedFormula.printStackTrace();
            return null;
        }

        LTS product = new LTS() {
            @Override
            public Iterator<State> iterator() {
                return super.iterator();
            }
        };
        HashMap<State,State> ltsStateMap = new HashMap<>();
        HashMap<State,StoredState> nbaStateMap = new HashMap<>();

        for (State initTS : model.initialStates) {
            for (Integer initNBA : nba.aut.getStoredHeader().getStartStates().get(0)) {
                for (StoredEdgeWithLabel edge : nba.aut.getEdgesWithLabel(initNBA)) {
                    if (initTS.satisfies(nba.propOfLabel(edge.getLabelExpr()))) {
                        for(Integer nbaSucc : edge.getConjSuccessors()) {
                            State prodState = new State() {
                                @Override
                                public Iterator<Transition> iterator() {
                                    return null;
                                }

                                @Override
                                public boolean satisfies(TFormula.Proposition prop) {
                                    return nba.propOfLabel(edge.getLabelExpr()).equals(prop);
                                }
                            };
                            product.initialStates.add(prodState);
                            ltsStateMap.put(prodState, initTS);
                            nbaStateMap.put(prodState, nba.aut.getStoredState(nbaSucc));
                        }
                    }

                }
            }
        }
        State terminal = new State() {
            @Override
            public Iterator<Transition> iterator() {
                LinkedList<Transition> list = new LinkedList<>();

                Transition trans = new Transition(this);
                list.add(trans);
                return list.iterator();
            }

            @Override
            public boolean satisfies(TFormula.Proposition prop) {
                return true;
            }
        };
        for(State s : product){
            if(!(s.iterator().hasNext())){
                State copy = s;
                s = new State() {
                    @Override
                    public Iterator<Transition> iterator() {
                        LinkedList<Transition> list = new LinkedList<>();

                        Transition trans = new Transition(terminal);
                        list.add(trans);
                        return list.iterator();
                    }

                    @Override
                    public boolean satisfies(TFormula.Proposition prop) {
                        return copy.satisfies(prop);
                    }
                };
            }

        }
        return model; // obviously wrong
    }

    /**
     * Question d : wrap it together
     * Bonus: don't return "false" for non-LTL formula and do the general model checking
     */
    @Override
    public boolean solve(LTS model, TFormula tform, int bound) {
        System.out.println(tform.toString());

        System.out.println("automaton has number of elementary sets: " + nbStatesCl(tform));

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

            //LTS product = product(model, tform, propTrue);
            return (persistenceWit(model, (TFormula.Proposition )tform , bound) != null);

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
