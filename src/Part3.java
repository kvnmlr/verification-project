import java.util.*;

import mudschecker.*;
import mudspg.TFormula;

public class Part3 extends AbstractChecker {
    private Part1 part1Checker;
    private List<State> currentPath = new ArrayList<>();

    private Set<State> computeSatSet(LTS model, TFormula tform) {
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
    private void generateAllPaths(State start, Set<State> satSet, List<List<State>> paths) {
        boolean hasStatisfyingSuccessor = false;

        // Check if the current state has a successor that is satisfied (if not, this is the goal state)
        for (Transition succ : start) {
            if (satSet.contains(succ.target)) {
                hasStatisfyingSuccessor = true;
                break;
            }
        }

        // if this is the goal state, add the current path to the list of all paths
        if (!hasStatisfyingSuccessor) {
            currentPath.add(start);
            paths.add(new ArrayList<>(currentPath));
            currentPath.remove(start);
            return;
        }

        // continue exploring the current path
        currentPath.add(start);
        for (Transition succ : start) {
            State target = succ.target;
            if (satSet.contains(target)) {
                generateAllPaths(target, satSet, paths);
            }
        }
        currentPath.remove(start);
    }

    /**
     * Exercise 3.2 Determine the minimum number of rounds2 needed by the winning player to complete a game.
     * We do this by specifying an upper bound on the number of actions in the model property (exercise2) and check whether
     * the goal state can be reached within this bound.
     * @param model model
     * @param tform property
     */
    private void shortestPath(LTS model, TFormula tform) {
        System.out.println("\n### Exercise 2: ###");

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

        System.out.println("RESULT: " + satisfies );
    }

    /**
     * Exercise 3.3: Obtain as many distinct winning paths with minimum number of rounds as you can.
     * We do this by generating all paths that lead to the goal state and satisfy the property (exercise3) that says that
     * the player should reach the goal state with the number of actions determined in exercise 2.
     * @param model model
     * @param tform property
     */
    private void shortestWinningPaths(LTS model, TFormula tform) {
        System.out.println("\n### Exercise 3: ###");

        Set<State> satSet = computeSatSet(model, tform);
        System.out.println("Found " + satSet.size() + " satisfying states");

        // Check if model satisfies property
        for (State initial : model.initialStates) {
            if (!satSet.contains(initial)) {
                System.out.println("An initial state is not in the satisfaction set\n");
                return;
            }
        }

        List<List<State>> paths = new ArrayList<>();
        for (State s : model.initialStates) {
            generateAllPaths(s, satSet, paths);
        }

        System.out.println("Found " + paths.size() + " winning paths");
        int i = 1;
        for (List<State> path : paths) {
            System.out.println("Path " + i++ + ": ");
            for (State s : path) {
                System.out.println(s);
            }
            System.out.println();
        }

        System.out.println("RESULT: " + paths.size());
    }

    /**
     * Exercise 3.4 Reveal situations where it may happen with non-zero probability that no player wins the game.
     * We do this by discovering all paths where the property (exercise4) that leads to infinity cycles is satisfied.
     * @param model model
     * @param tform property
     */
    private void buggyGame(LTS model, TFormula tform) {
        /*
        First Bug: Standing on the fountain on #31 then throwing a five --> engage to loop #31 --> #36 --> #41 --> #36 --> #41 ....
        Second Bug: Standing on #51 throwing an 8 --> engage to loop #51 --> #59 --> (#63 - 4) --> #59 --> (#63 - 4) ....
         */
        shortestWinningPaths(model, tform);
    }

    @Override
    public boolean solve(LTS model, TFormula tform, int bound) {
        part1Checker = new Part1();

        int n = 0;
        for (State s : model) {
            //System.out.println("State " + n + ": " + s.toString());
            n++;
        }
        System.out.println("Model has " + n + " states");

        if (!part1Checker.checkBounded(model, bound)) {
            System.out.println("Model is not bounded\n");
            return false;
        }

        short runExercise = 4;
        switch (runExercise) {
            case 2:
                // Exercise 2
                shortestPath(model, tform);
                break;
            case 3:
                // Exercise 3
                shortestWinningPaths(model, tform);
                break;
            case 4:
                // Exercise 4
                buggyGame(model, tform);
                break;
            default:
                System.out.println("Invalid exercise number");
                break;
        }

        System.out.println("\nDone");
        return true;
    }
}