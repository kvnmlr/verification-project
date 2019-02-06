import java.util.*;

import mudschecker.*;
import mudspg.Evaluation;
import mudspg.Expression;
import mudspg.TFormula;

public class Part3 extends AbstractChecker {

    @Override
    public boolean solve(LTS model, TFormula tform, int bound) {
        AbstractChecker part1Checker = new Part1();
        AbstractChecker part2Checker = new Part2();

        System.out.println("Model: " + model.toString());
        System.out.println("Property: " + tform.toString());
        System.out.println("Bound: " + bound);

        System.out.println("Running Checker 1:");
        boolean res = part1Checker.solve(model, tform, bound);

        //System.out.println("Running Checker 2:");
        //part2Checker.solve(model, tform, bound);

        return res;
    }
}