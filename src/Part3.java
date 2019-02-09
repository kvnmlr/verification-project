import java.util.*;

import mudschecker.*;
import mudspg.Constant;
import mudspg.Evaluation;
import mudspg.Expression;
import mudspg.TFormula;

public class Part3 extends AbstractChecker {
    private Part1 part1Checker;
    private Part2 part2Checker;
    List<State> paths = new ArrayList<>();

    Set<State> computeSatSet(LTS model, TFormula tform) {
        // Check for valid CTL
        if (!part1Checker.isCTL(tform)) {
            System.out.println("Formula is not valid CTL\n");
            return new HashSet<>();
        }

        // Convert property to ENF
        tform = part1Checker.toENF(tform);
        System.out.println("ENF formula: " + tform.toString());

        // Compute satisfaction set
        return part1Checker.satSet(model, tform);
    }

    private void shortestPath(LTS model, TFormula tform) {
        System.out.println("\n### Exercise 2: ###");

        Set<State> satSet = computeSatSet(model, tform);
        System.out.println("Found " + satSet.size() + " satisfying states");
        for (State s : satSet) {
            System.out.println(s);
        }


        // Check if model satisfies property
        boolean satisfies = true;
        for (State initial : model.initialStates) {
            if (!satSet.contains(initial)) {
                System.out.println("An initial state is not in the satisfaction set\n");
                satisfies = false;
            }
        }

        System.out.println("RESULT: " + satisfies );
    }

    private void shortestWinningPaths(LTS model, TFormula tform) {
        System.out.println("\n### Exercise 3: ###");

        Set<State> satSet = computeSatSet(model, tform);
        System.out.println("Found " + satSet.size() + " satisfying states");

        // Check if model satisfies property
        boolean satisfies = true;
        for (State initial : model.initialStates) {
            if (!satSet.contains(initial)) {
                System.out.println("An initial state is not in the satisfaction set\n");
                satisfies = false;
            }
        }

        // remove all states from the model that are not part of any solution
        for(State s : model) {
            if (!satSet.contains(s)) {
                s = null;
            }
        }

        for (State s : model.initialStates) {
            System.out.println("Paths:");
            PrintAllPath(s, satSet);
        }
        int paths = 0;
        System.out.println("RESULT: " + paths);
    }

    private void PrintAllPath(State start, Set<State> satSet) {
        boolean hasStatisfyingSuccessor = false;
        for (Transition succ : start) {
            if (satSet.contains(succ.target)) {
                hasStatisfyingSuccessor = true;
                break;
            }
        }

        if (!hasStatisfyingSuccessor) {
            // leaf node
            paths.add(start);
            System.out.println("Path:");
            PrintPath();
            paths.remove(start);
            return;
        }
        paths.add(start);

        for (Transition succ : start) {
            State target = succ.target;
            if (satSet.contains(target)) {
                PrintAllPath(target, satSet);
            }

        }
        paths.remove(start);
    }

    private void PrintPath() {
        for (State s : paths) {
            System.out.println(s.toString());
        }
        System.out.println();
    }

    /**
     * Exercise 3.4 Reveal situations where it may happen with non-zero probability that no player wins the game
     * @param mode model
     * @param modelBound upper bound on model states
     * @return
     */
    private boolean buggyGame(LTS mode, int modelBound) {
        // TODO implement
        /**
        First Bug: Standing on the fountain on #31 then throwing a five --> engage to loop #31 --> #36 --> #41 --> #36 --> #41 ....

        Second Bug: Standing on #51 throwing an 8 --> engage to loop #51 --> #59 --> (#63 - 4) --> #59 --> (#63 - 4) ....
         */
        return true;
    }

    @Override
    public boolean solve(LTS model, TFormula tform, int bound) {
        part1Checker = new Part1();
        part2Checker = new Part2();
        int n = 0;
        for (State s : model) {
            System.out.println("State " + n + ": " + s.toString());
            n++;
        }
        System.out.println("Model has " + n + " states");

        if (!part1Checker.checkBounded(model, bound)) {
            System.out.println("Model is not bounded\n");
            return false;
        }

        // Exercise 2
        shortestPath(model, tform);

        // Exercise 3
        shortestWinningPaths(model, tform);

        System.out.println("\ndone");
        return true;
    }
}