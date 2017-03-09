import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Terrence Park and Skye Pekerti
 *
 */
public class State {

    ArrayList<Variable> vars; //Variable objects from input
    ArrayList<Constraint> cons; //Constraint objects from input
    HashMap<Character, Integer> selected; //Keeps track of which values have been chosen for a Variable
    ArrayList<Variable> solvedVars; //Keeps track of the order the Variables are solved in
    boolean useCEP; //true if forward checking is to be used

	/**
     * Constructor for making a default {@code State}.
     */
    public State() {
        this(new ArrayList<>(), new ArrayList<>(), false);
    }
	
	/**
     * Constructor for making a starting {@code State}.
     * @param vars List of Variable Objects
	 * @param cons List of Constraint Objects
	 * @param useCEP true if forward checking is to be used
     */
    public State(ArrayList<Variable> vars, ArrayList<Constraint> cons, boolean useCEP) {
        this.vars = vars;
        this.cons = cons;
        this.useCEP = useCEP;
        selected = new HashMap<>();
        solvedVars = new ArrayList<>();
    }
	
	/**
     * Creates a copy of a given state.
	 * @return copy of given state
     */
    public State copyOf() {
        State copy = new State();
        for (Variable v : vars) {
            copy.vars.add(v.copyOf());
        }
        copy.cons = cons;
        copy.selected.putAll(selected);
        for (Variable v : solvedVars) {
            copy.solvedVars.add(v.copyOf());
        }
        copy.useCEP = useCEP;
        return copy;
    }
	
	/**
     * Checks to see if a given state is a solution
	 * @return true if it is a solution
     */
    public boolean isSolved() {
        for (Constraint c : cons) {
            if (!selected.containsKey(c.var1) || !selected.containsKey(c.var2)) {
                return false;
            }
            int val1 = selected.get(c.var1);
            int val2 = selected.get(c.var2);
            if (!c.valid(val1, val2)) {
                return false;
            }
        }
        return true;
    }
	
	/**
     * Sorts the Variables so the first one is the Variable to be chosen
     */
    public void selectNextVar() {
        Collections.sort(vars);
    }

    /**
     * Checks if the current variable selection failed forward check.
     * @return true if failed forward check
     */
    public boolean failedFC() {
        return vars.get(0).values.size() == 0;
    }

	/**
     * Sorts the values of a given Variable so the best value is chosen
	 * @return array of sorted values for a Variable
     */
    public int[] getOrderedVals() {
        Variable nextVar = vars.get(0);
        int[][] valPairs = new int[nextVar.values.size()][2];
        for (int i = 0; i < valPairs.length; i++) {
            valPairs[i][0] = nextVar.values.get(i);
            valPairs[i][1] = getAffectedValues(nextVar, valPairs[i][0]);
        }
        Arrays.sort(valPairs, (one, two) -> one[1] - two[1]);
        int[] orderedVals = new int[nextVar.values.size()];
        for (int i = 0; i < valPairs.length; i++) {
            orderedVals[i] = valPairs[i][0];
        }
        return orderedVals;
    }

	/**
     * Finds the amount of values affected by choosing a value of a Variable
	 * @param nextVar Variable where the value came from
	 * @param nextVal integer of the value chosen
	 * @return integer of total affected values
     */
    private int getAffectedValues(Variable nextVar, int nextVal) {
        int numAffected = 0;
        for (Constraint c : cons) {
            if (c.var1 == nextVar.var) {
                for (Variable v : vars) {
                    if (c.var2 == v.var) {
                        for (int val : v.values) {
                            if (!c.valid(nextVal, val)) {
                                numAffected++;
                            }
                        }
                        break;
                    }
                }
            } else if (c.var2 == nextVar.var) {
                for (Variable v : vars) {
                    if (c.var1 == v.var) {
                        for (int val : v.values) {
                            if (!c.valid(val, nextVal)) {
                                numAffected++;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return numAffected;
    }

	/**
     * Sets the next variable to {@code chosenVal} and updates constraints.
	 * @param chosenVal chosen for a move
     */
    public void setVar(int chosenVal) {
		selected.put(vars.get(0).var,chosenVal);
		solvedVars.add(vars.get(0));
        if (useCEP) {
            for (Constraint c : cons) {
                if (c.var1 == vars.get(0).var) {
                    for (Variable v : vars) {
                        if (c.var2 == v.var) {
                            int i = 0;
                            while (i < v.values.size()) {
                                int val = v.values.get(i);
                                if (!c.valid(chosenVal, val)) {
                                    v.values.remove(i);
                                } else {
                                    i++;
                                }
                            }
                            break;
                        }
                    }
                } else if (c.var2 == vars.get(0).var) {
                    for (Variable v : vars) {
                        if (c.var1 == v.var) {
                            int i = 0;
                            while (i < v.values.size()) {
                                int val = v.values.get(i);
                                if (!c.valid(val, chosenVal)) {
                                    v.values.remove(i);
                                } else {
                                    i++;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (Constraint c : cons) {
            if (c.var1 == vars.get(0).var) {
                for (Variable v : vars) {
                    if (c.var2 == v.var) {
                        v.numConstraints--;
                    }
                }
            } else if (c.var2 == vars.get(0).var) {
                for (Variable v : vars) {
                    if (c.var1 == v.var) {
                        v.numConstraints--;
                    }
                }
            }
        }
        vars.remove(0);
    }

	/**
     * Checks to see if a given state is consistent to all of the constraints
	 * @return true if it is consistent
     */
    public boolean consistent() {
        for (Constraint c : cons) {
            if (!selected.containsKey(c.var1) || !selected.containsKey(c.var2)) {
                continue;
            }
            int val1 = selected.get(c.var1);
            int val2 = selected.get(c.var2);
            if (!c.valid(val1, val2)) {
                return false;
            }
        }
        return true;
    }

	/**
     * Gives the formatted output of the CSP
	 * @return String of given output
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Variable v : solvedVars) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(v.var).append("=").append(selected.get(v.var));
        }
        sb.append("  ");
        if (isSolved()) {
            sb.append("solution");
        } else {
            sb.append("failure");
        }
        return sb.toString();
    }
}
