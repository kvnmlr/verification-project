import java.util.*;

import mudschecker.*;
import mudschecker.Transition;
import mudspg.*;

public class Part1 extends AbstractChecker {

    /**
     * Exercise: given a model and a bound, does the model has less than n states ?
     *
     * @param model the model
     * @param bound a bound
     * @return
     */
    public boolean checkBounded(LTS model, int bound) {
        int n = 0;
        for (State ignored : model) {
            if (++n >= bound) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return boolean is the formula a CTL formula ?
     */
    public boolean isCTL(TFormula phi) {
        // Start with a state formula and check each sub formula recursively
        return checkSubformula(phi, true);
    }

    /**
     * Checks whether the given formula is either a valid state or path formula, depending on the flag
     * and goes on recursively for sub formulas
     * @param phi                 the formula
     * @param requireStateFormula whether phi should be a state formula
     * @return true iff the given phi is a valid CTL formula
     */
    private boolean checkSubformula(TFormula phi, boolean requireStateFormula) {
        String s = phi.getClass().getSimpleName();
        if (requireStateFormula) {
            // This should be a state formula, check for all state formulas of return false in default case
            switch (s) {
                case "Proposition":
                    return true;
                case "And":
                case "Or":
                    return checkSubformula(((TFormula.BinaryOp) phi).suba, true) && checkSubformula(((TFormula.BinaryOp) phi).subb, true);
                case "Not":
                    return checkSubformula(((TFormula.Not) phi).sub, true);
                case "Exist":
                case "All":
                    return checkSubformula(((TFormula.UnaryOp) phi).sub, false);
                default:
                    return false;
            }
        } else {
            // This should be a path formula, check for all path formulas or return false in default case
            switch (s) {
                case "Next":
                case "Globally":
                case "Eventually":
                    return checkSubformula(((TFormula.UnaryOp) phi).sub, true);
                case "Until":
                case "WeakUntil":
                    return checkSubformula(((TFormula.BinaryOp) phi).suba, true) && checkSubformula(((TFormula.BinaryOp) phi).subb, true);
                default:
                    return false;
            }
        }
    }

    /**
     * Exercise: given a temporal formula, construct its ENF, or raise an Exception
     */
    public TFormula toENF(TFormula phi) {
        System.out.println(phi);

        phi = removeForAll(phi, true);

        // After transformation to ENF, this should still be valid CTL or we did something wrong
        if (!isCTL(phi)) {
            System.out.println("Formula is not CTL after ENF transformation");
        }

        return phi; // Obviously wrong
    }

    /**
     * Removes F and W from the formula
     *
     * @param phi    the formula
     * @param exists only for internal recursion
     * @return the transformed formula
     */
    private TFormula removeWeakUntil(TFormula phi, boolean exists) {
        /*String s = phi.getClass().getSimpleName();
        switch (s) {
            case "Exist": {
                // When the parent of a W is an E, we have to transform it into an A
                ((TFormula.Exist) phi).sub = removeWeakUntil(((TFormula.Exist) phi).sub, true);
                break;
            }
            case "All": {
                // When the parent of a W is an A, we have to transform it into an E
                ((TFormula.All) phi).sub = removeWeakUntil(((TFormula.All) phi).sub, false);
                break;
            }

            default: {
                // This subformula is not relevant for the transformation, just descent into the sub formulas
                if (phi instanceof TFormula.UnaryOp) {
                    ((TFormula.UnaryOp) phi).sub = removeWeakUntil(((TFormula.UnaryOp) phi).sub, exists);
                }
                if (phi instanceof TFormula.BinaryOp) {
                    ((TFormula.BinaryOp) phi).suba = removeWeakUntil(((TFormula.BinaryOp) phi).suba, exists);
                    ((TFormula.BinaryOp) phi).subb = removeWeakUntil(((TFormula.BinaryOp) phi).subb, exists);
                }
                break;
            }
        }*/
        return phi;
    }


    private TFormula removeForAll(TFormula phi, boolean exists) {
        String s = phi.getClass().getSimpleName();
        switch (s) {
            case "Exist": {
                // When the parent of a W is an E, we have to transform it into an A
                ((TFormula.Exist) phi).sub = removeForAll(((TFormula.Exist) phi).sub, true);
                break;
            }
            case "All": {
                // When the parent of a W is an A, we have to transform it into an E
                phi = removeForAll(((TFormula.All) phi).sub, false);
                break;
            }
            case "Next": {
                // Only transform Xs that are part of an A
                if (!exists) {
                    TFormula.Not subNeg = new TFormula.Not(removeForAll(((TFormula.Next) phi).sub, false));
                    TFormula.Next next = new TFormula.Next(subNeg);
                    TFormula.Exist exist = new TFormula.Exist(next);
                    return new TFormula.Not(exist);
                }
                break;
            }
            case "WeakUntil": {
                TFormula suba = removeForAll(((TFormula.WeakUntil) phi).suba, false);
                TFormula subb = removeForAll(((TFormula.WeakUntil) phi).subb, false);

                // Create the two negated sub formulas
                TFormula.Not subaNeg = new TFormula.Not(suba);
                TFormula.Not subbNeg = new TFormula.Not(subb);

                // Create the &&
                TFormula.And and = new TFormula.And(subaNeg, subbNeg);

                // Create the U
                TFormula.Until until = new TFormula.Until(subbNeg, and);

                TFormula.UnaryOp pathQuantifier;
                if (exists) {
                    pathQuantifier = new TFormula.All(until);
                } else {
                    pathQuantifier = new TFormula.Exist(until);
                }

                // Since we maybe introduced an A in the subformula, we have to make a recursive call
                return removeWeakUntil(new TFormula.Not(pathQuantifier), exists);
            }
            case "Globally": {
                // Only transform Xs that are part of an A
                if (!exists) {
                    TFormula.Not subNeg = new TFormula.Not(removeForAll(((TFormula.Globally) phi).sub, false));
                    TFormula propTrue = new TFormula.Proposition(Constant.true_value);
                    TFormula.Until until = new TFormula.Until(propTrue, subNeg);
                    TFormula.Exist exist = new TFormula.Exist(until);
                    return new TFormula.Not(exist);
                }
                break;
            }
            case "Eventually": {
                if (!exists) {
                    // We can transform an F x in to true U x
                    TFormula sub = removeForAll(((TFormula.Eventually) phi).sub, false);

                    // Build the true proposition
                    TFormula not = new TFormula.Not(sub);
                    TFormula globally = new TFormula.Globally(not);
                    TFormula existsSub = new TFormula.Exist(globally);
                    return new TFormula.Not(existsSub);
                } else {
                    TFormula sub = removeForAll(((TFormula.Eventually) phi).sub, false);

                    // Build the true proposition
                    TFormula propTrue = new TFormula.Proposition(Constant.true_value);
                    return new TFormula.Until(propTrue, sub);
                }
            }
            case "Until": {
                if (!exists) {
                    TFormula.Not subaNeg = new TFormula.Not(removeForAll(((TFormula.Until) phi).suba, false));
                    TFormula.Not subbNeg = new TFormula.Not(removeForAll(((TFormula.Until) phi).subb, false));

                    // Left part
                    TFormula.And and = new TFormula.And(subaNeg, subbNeg);
                    TFormula.Until until = new TFormula.Until(subbNeg, and);
                    TFormula.Exist existLeft = new TFormula.Exist(until);
                    TFormula.Not existLeftNot = new TFormula.Not(existLeft);

                    // Right part
                    TFormula.Globally globally = new TFormula.Globally(subbNeg);
                    TFormula.Exist existRight = new TFormula.Exist(globally);
                    TFormula.Not existRightNot = new TFormula.Not(existRight);

                    // Conjunction
                    return new TFormula.And(existLeftNot, existRightNot);
                } else {
                    TFormula left = removeForAll(((TFormula.Until) phi).suba, true);
                    TFormula right = removeForAll(((TFormula.Until) phi).subb, true);

                    return new TFormula.Until(left, right);
                }
            }
            default: {
                // This subformula is not relevant for the transformation, just descent into the sub formulas
                if (phi instanceof TFormula.UnaryOp) {
                    ((TFormula.UnaryOp) phi).sub = removeForAll(((TFormula.UnaryOp) phi).sub, exists);
                }
                if (phi instanceof TFormula.BinaryOp) {
                    ((TFormula.BinaryOp) phi).suba = removeForAll(((TFormula.BinaryOp) phi).suba, exists);
                    ((TFormula.BinaryOp) phi).subb = removeForAll(((TFormula.BinaryOp) phi).subb, exists);
                }
                break;
            }
        }
        return phi;
    }

    /**
     * Exercise: given a finite model and a temporal formula in ENF, returns the satisfaction set.
     * Any implementation of Set<State> is allowed (but choose carefully)
     */
    public Set<State> satSet(LTS model, TFormula phi) {
        String symbol = phi.getClass().getSimpleName();
        // This should be a state formula, check for all state formulas of return false in default case
        switch (symbol) {
            case "Proposition": {
                return satSetProp(model, (TFormula.Proposition) phi);
            }
            case "And": {
                Set<State> subASatSet = satSet(model, ((TFormula.And) phi).suba);
                subASatSet.retainAll(satSet(model, ((TFormula.And) phi).subb));
                return subASatSet;
            }
            case "Not": {
                Set<State> subSatSet = allStates(model);
                subSatSet.removeAll(satSet(model, ((TFormula.Not) phi).sub));
                return subSatSet;
            }
            case "Exist": {
                return satSet(model, ((TFormula.Exist) phi).sub);
            }
            case "Globally": {
                Set<State> subSatSet = satSet(model, ((TFormula.Globally) phi).sub);
                Set<State> globallySat = new HashSet<>();

                // Find all states that have an outgoing path that contains only states from subSatSet forever
                for (State s : subSatSet) {
                    globallySat.addAll(DFS(s, subSatSet, new HashSet<>()));
                }
                return globallySat;
            }
            case "Next": {
                Set<State> subSatSet = satSet(model, ((TFormula.Next) phi).sub);
                Set<State> nextSat = new HashSet<>();

                // Find all states that have a state from subSatSet as successor
                for (State s : model) {
                    // A state with no outgoing transitions cannot satisfy an X property
                    for (Transition t : s) {
                        if (subSatSet.contains(t.target)) {
                            nextSat.add(s);
                            break;
                        }
                    }
                }
                return nextSat;
            }
            case "Until": {
                Set<State> subSatSet = satSet(model, ((TFormula.Until) phi).suba);
                Set<State> subUntilSet = satSet(model, ((TFormula.Until) phi).subb);
                Set<State> untilSat = new HashSet<>();

                // If the satisfaction set of the until formula is not empty, i.e. we have a chance to fulfill the property
                if (!subUntilSet.isEmpty()) {
                    // Find all states that have an outgoing path that contains only states from subSatSet forever
                    for (State s : model) {
                        if (subSatSet.contains(s) || subUntilSet.contains(s)) {
                            untilSat.addAll(DFS(s, subSatSet, subUntilSet));
                        }
                    }
                }
                return untilSat;
            }

            default:
                return Collections.emptySet();
        }
    }

    /**
     * DFS that returns the first path from the start node that contains only states from the satSet until it loops
     *
     * @param start  the start node
     * @param satSet set of allowed states
     * @return set of satisfying states
     */
    private Set<State> DFS(State start, Set<State> satSet, Set<State> satSetUntil) {
        Map<State, State> path = new HashMap<>();
        Stack<State> stack = new Stack<>();
        stack.push(start);
        Set<State> visited = new HashSet<>();

        State goal = null;
        while (!stack.isEmpty()) {
            State state = stack.pop();
            if (satSetUntil.contains(state)) {
                // we discovered the until condition on a path where all states so far are form the satSet
                goal = state;
                break;
            }

            if (!satSet.contains(state)) {
                // this successor is not in the satSet, no need to go deeper here
                continue;
            }

            if (visited.contains(state)) {
                if (!satSetUntil.isEmpty()) {
                    // We ran into a loop without seeing the U condition, break
                    break;
                } else {
                    // we discovered a loop or dead-end on a path where all states so far are form the satSet
                    goal = state;
                    break;
                }
            }

            visited.add(state);
            for (Transition t : state) {
                State target = t.target;
                if (state.equals(target)) {
                    path.put(target, state);
                } else {
                    stack.push(target);
                }
            }
        }

        Set<State> globallySatSet = new HashSet<>();
        if (goal != null) {
            State s = goal;
            while (path.containsKey(s)) {
                globallySatSet.add(s);
                s = path.get(s);
            }
            globallySatSet.add(start);
        }
        return globallySatSet;
    }

    /**
     * As a courtesy, and in order to provide an example of the API use, we offer you a solution
     * for the sat. set of a proposition.
     *
     * @param model the model
     * @param phi   the property
     * @return the satisfaction set
     */
    protected Set<State> satSetProp(LTS model, TFormula.Proposition phi) {
        Set<State> res = new HashSet<>();
        for (State s : model) {
            if (s.satisfies(phi))
                res.add(s);
        }
        return res;
    }

    /**
     * Computes a set of all states
     *
     * @param model the model
     * @return all states in the model
     */
    private Set<State> allStates(LTS model) {
        Set<State> res = new HashSet<>();
        for (State s : model) {
            res.add(s);
        }
        return res;
    }

    /**
     * Exercise: wrap it together
     */
    @Override
    public boolean solve(LTS model, TFormula tform, int bound) {
        // Check bounds

        int n = 0;
        for (State s : model) {
            System.out.println("State " + n + ": " + s.toString());
            n++;
        }

        System.out.println("Model has " + n + " states");

        if (!checkBounded(model, bound)) {
            System.out.println("Model is not bounded\n");
            return false;
        }

        // Check for valid CTL
        if (!isCTL(tform)) {
            System.out.println("Formula is not valid CTL\n");
            return false;
        }

        // Convert property to ENF
        tform = toENF(tform);
        System.out.println("ENF formula: " + tform.toString());


        // Compute satisfaction set
        Set<State> satSet = satSet(model, tform);
        System.out.println("Found " + satSet.size() + " satisfying states");

        // Check if model satisfies property
        boolean satisfies = true;
        for (State initial : model.initialStates) {
            if (!satSet.contains(initial)) {
                System.out.println("An initial state is not in the satisfaction set\n");
                satisfies = false;
            }
        }

        return satisfies;
    }
}
