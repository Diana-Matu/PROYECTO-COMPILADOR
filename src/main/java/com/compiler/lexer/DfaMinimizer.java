
/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.*;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;


/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
        public DfaMinimizer() {
            // TODO: Implement constructor if needed
        }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        List<DfaState> allStates = originalDfa.allStates;

        // Edge cases: 0 or 1 state -> already minimal
        if (allStates == null || allStates.size() <= 1) {
            return originalDfa;
        }

        // Table: map pair -> marked? (true = distinguishable)
        Map<Pair, Boolean> table = new HashMap<>();

        // Initialize all pairs (i < j)
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair p = new Pair(s1, s2);
                // mark if one is final and the other is not
                boolean mark = s1.isFinal() != s2.isFinal();
                table.put(p, mark);
            }
        }

        // Iteratively mark pairs
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < allStates.size(); i++) {
                for (int j = i + 1; j < allStates.size(); j++) {
                    DfaState s1 = allStates.get(i);
                    DfaState s2 = allStates.get(j);
                    Pair p = new Pair(s1, s2);

                    // skip already marked pairs
                    if (table.getOrDefault(p, false)) continue;

                    // Check for each symbol if transitions lead to a marked pair or asymmetric transition
                    boolean shouldMark = false;
                    for (char symbol : alphabet) {
                        DfaState t1 = s1.getTransition(symbol);
                        DfaState t2 = s2.getTransition(symbol);

                        if (t1 == null && t2 == null) {
                            // both have no transition on this symbol => fine
                            continue;
                        } else if (t1 == null ^ t2 == null) {
                            // only one has transition -> distinguishable
                            shouldMark = true;
                            break;
                        } else {
                            // both non-null: check if pair (t1,t2) is marked
                            Pair tp = new Pair(t1, t2);
                            if (table.getOrDefault(tp, false)) {
                                shouldMark = true;
                                break;
                            }
                        }
                    }

                    if (shouldMark) {
                        table.put(p, true);
                        changed = true;
                    }
                }
            }
        } while (changed);

        // Create partitions (equivalence classes) from unmarked pairs
        List<Set<DfaState>> partitions = createPartitions(allStates, table);

        // Build map from old state -> representative/new state
        Map<DfaState, DfaState> oldToNew = new HashMap<>();
        List<DfaState> newStates = new ArrayList<>();

        for (Set<DfaState> block : partitions) {
            // Merge nfaStates sets from members to form the new state's name
            Set<com.compiler.lexer.nfa.State> unionNfaStates = new HashSet<>();
            boolean newIsFinal = false;
            for (DfaState member : block) {
                unionNfaStates.addAll(member.getName());
                if (member.isFinal()) newIsFinal = true;
            }
            DfaState newState = new DfaState(unionNfaStates);
            newState.setFinal(newIsFinal);

            // map every old member to this newState
            for (DfaState member : block) {
                oldToNew.put(member, newState);
            }
            newStates.add(newState);
        }

        // Reconstruct transitions for new states
        for (DfaState oldState : allStates) {
            DfaState mappedFrom = oldToNew.get(oldState);
            // iterate through old transitions
            for (Map.Entry<Character, DfaState> entry : oldState.getTransitions().entrySet()) {
                Character symbol = entry.getKey();
                DfaState oldTarget = entry.getValue();
                DfaState mappedTo = oldToNew.get(oldTarget);
                // Avoid duplicate/overwrite — addTransition will replace if exists
                mappedFrom.addTransition(symbol, mappedTo);
            }
        }

        // Determine new start state
        DfaState newStart = oldToNew.get(originalDfa.startState);

        // Build minimized DFA states list (unique)
        List<DfaState> minimizedAllStates = new ArrayList<>(newStates);

        return new DFA(newStart, minimizedAllStates);
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        Map<DfaState, DfaState> parent = new HashMap<>();
        // initialize parent
        for (DfaState s : allStates) parent.put(s, s);

        // union states for each unmarked pair
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair p = new Pair(s1, s2);
                if (!table.getOrDefault(p, false)) {
                    union(parent, s1, s2);
                }
            }
        }

        // group by root parent
        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState s : allStates) {
            DfaState root = find(parent, s);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(s);
        }

        return new ArrayList<>(groups.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        DfaState p = parent.get(state);
        if (p == null) {
            parent.put(state, state);
            return state;
        }
        if (p == state) return state;
        DfaState root = find(parent, p);
        parent.put(state, root); // path compression
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        DfaState r1 = find(parent, s1);
        DfaState r2 = find(parent, s2);
        if (r1 == r2) return;
        // attach r2 under r1 (arbitrary)
        parent.put(r2, r1);
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            if (s1 == null || s2 == null) {
                // allow nulls (but shouldn't occur in our usage)
                this.s1 = s1;
                this.s2 = s2;
            } else if (s1.id <= s2.id) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair other = (Pair) o;
            return Objects.equals(s1, other.s1) && Objects.equals(s2, other.s2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s1, s2);
        }
    }
}
