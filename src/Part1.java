import java.util.Collections;
import java.util.Set;

import mudschecker.*;
import mudspg.TFormula;

import java.util.HashSet;

public class Part1 extends AbstractChecker {

    /**
     * Exercise: given a model and a bound, does the model has less than n states ?
     *
     * @param model
     * @param bound
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
     * Checks whether the given formula is either a state or path formula, depending on the flag
     * and goes on recursively for sub formulas
     *
     * @param phi                 the formula
     * @param requireStateFormula whether phi should be a state formula
     * @return
     */
    private boolean checkSubformula(TFormula phi, boolean requireStateFormula) {
        String s = phi.getClass().getSimpleName();
        if (requireStateFormula) {
            // This should be a state formula, check for all state formulas of return false in default case
            switch (s) {
                case "Proposition":
                    return true;
                case "And":
                    return checkSubformula(((TFormula.And) phi).suba, true) && checkSubformula(((TFormula.And) phi).subb, true);
                case "Or":
                    return checkSubformula(((TFormula.Or) phi).suba, true) && checkSubformula(((TFormula.Or) phi).subb, true);
                case "Not":
                    return checkSubformula(((TFormula.Not) phi).sub, true);
                case "Exist":
                    return checkSubformula(((TFormula.Exist) phi).sub, false);
                case "All":
                    return checkSubformula(((TFormula.All) phi).sub, false);
                default:
                    return false;
            }
        } else {
            // This should be a path formula, check for all path formulas or return false in default case
            switch (s) {
                case "Next":
                    return checkSubformula(((TFormula.Next) phi).sub, true);
                case "Globally":
                    return checkSubformula(((TFormula.Globally) phi).sub, true);
                case "Eventually":
                    return checkSubformula(((TFormula.Eventually) phi).sub, true);
                case "Until":
                    return checkSubformula(((TFormula.Until) phi).suba, true) && checkSubformula(((TFormula.Until) phi).subb, true);
                case "WeakUntil":
                    return checkSubformula(((TFormula.WeakUntil) phi).suba, true) && checkSubformula(((TFormula.WeakUntil) phi).subb, true);
                default:
                    return false;
            }
        }
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
        Set<State> res = new HashSet<State>();
        String s = phi.getClass().getSimpleName();
        // This should be a state formula, check for all state formulas of return false in default case
        switch (s) {
            case "Proposition":
                return satSetProp(model, (TFormula.Proposition) phi);
            case "And": {
                Set<State> subASatSet = satSet(model, ((TFormula.And) phi).suba);
                subASatSet.retainAll(satSet(model, ((TFormula.And) phi).subb));
                return subASatSet;
            }
            case "Or": {
                Set<State> subASatSet = satSet(model, ((TFormula.Or) phi).suba);
                subASatSet.addAll(satSet(model, ((TFormula.Or) phi).subb));
                return subASatSet;
            }
            case "Not": {
                Set<State> subSatSet = allStates(model);
                subSatSet.removeAll(satSet(model, ((TFormula.Not) phi).sub));
                return subSatSet;
            }
            case "Exist":
                return Collections.emptySet();
            case "All":
                return Collections.emptySet();
            default:
                return Collections.emptySet();
        }
    }

    /**
     * As a courtesy, and in order to provide an example of the API use, we offer you a solution
     * for the sat. set of a proposition.
     *
     * @param model
     * @param phi
     * @return
     */
    protected Set<State> satSetProp(LTS model, TFormula.Proposition phi) {
        Set<State> res = new HashSet<State>();
        for (State s : model) {
            if (s.satisfies(phi))
                res.add(s);
        }
        return res;
    }

    /**
     * Computes a set of all states
     *
     * @param model
     * @return
     */
    private Set<State> allStates(LTS model) {
        Set<State> res = new HashSet<State>();
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
        System.out.println("Solving");

        if (!checkBounded(model, bound)) {
            System.out.println("Model is not bounded");
            return false;
        }

        if (!isCTL(tform)) {
            System.out.println("Formula is not valid CTL");
            return false;
        }

        Set<State> satSet = satSet(model, tform);
        System.out.println(satSet.size());

        // System.out.println(model);
        return false;
    }


}
