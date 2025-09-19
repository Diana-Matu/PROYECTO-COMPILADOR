package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * NfaToDfaConverter
 * -----------------
 * This class provides a static method to convert a Non-deterministic Finite Automaton (NFA)
 * into a Deterministic Finite Automaton (DFA) using the standard subset construction algorithm.
 */
/**
 * Utility class for converting NFAs to DFAs using the subset construction algorithm.
 */
public class NfaToDfaConverter {
	/**
	 * Default constructor for NfaToDfaConverter.
	 */
		public NfaToDfaConverter() {
			// TODO: Implement constructor if needed
		}

	/**
	 * Converts an NFA to a DFA using the subset construction algorithm.
	 * Each DFA state represents a set of NFA states. Final states are marked if any NFA state in the set is final.
	 *
	 * @param nfa The input NFA
	 * @param alphabet The input alphabet (set of characters)
	 * @return The resulting DFA
	 */
	public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        // 1. Crear estado inicial DFA como epsilon-closure del estado inicial NFA
        Set<State> startClosure = epsilonClosure(Set.of(nfa.startState));
        DfaState startDfaState = new DfaState(startClosure);

        // marcar como final si algún NFA lo es
        if (containsFinal(startClosure)) {
            startDfaState.setFinal(true);
        }

        List<DfaState> dfaStates = new ArrayList<>();
        dfaStates.add(startDfaState);

        List<DfaState> unmarkedStates = new ArrayList<>();
        unmarkedStates.add(startDfaState);

        // 2. Procesar estados DFA no marcados
        while (!unmarkedStates.isEmpty()) {
            DfaState current = unmarkedStates.remove(0);

            for (char symbol : alphabet) {
                Set<State> moveResult = move(current.nfaStates, symbol);
                Set<State> closure = epsilonClosure(moveResult);

                if (closure.isEmpty()) continue;

                DfaState existing = findDfaState(dfaStates, closure);
                if (existing == null) {
                    existing = new DfaState(closure);
                    if (containsFinal(closure)) {
                        existing.setFinal(true);
                    }
                    dfaStates.add(existing);
                    unmarkedStates.add(existing);
                }

                current.addTransition(symbol, existing);
            }
        }

        return new DFA(startDfaState, dfaStates);
    }


	/**
	 * Computes the epsilon-closure of a set of NFA states.
	 * The epsilon-closure is the set of states reachable by epsilon (null) transitions.
	 *
	 * @param states The set of NFA states.
	 * @return The epsilon-closure of the input states.
	 */
	private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        List<State> stack = new ArrayList<>(states);

        while (!stack.isEmpty()) {
            State s = stack.remove(stack.size() - 1);
            for (State next : s.getEpsilonTransitions()) {
                if (!closure.contains(next)) {
                    closure.add(next);
                    stack.add(next);
                }
            }
        }
        return closure;
    }

	/**
	 * Returns the set of states reachable from a set of NFA states by a given symbol.
	 *
	 * @param states The set of NFA states.
	 * @param symbol The input symbol.
	 * @return The set of reachable states.
	 */
	private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();
        for (State s : states) {
            result.addAll(s.getTransitions(symbol));
        }
        return result;
    }

	/**
	 * Finds an existing DFA state representing a given set of NFA states.
	 *
	 * @param dfaStates The list of DFA states.
	 * @param targetNfaStates The set of NFA states to search for.
	 * @return The matching DFA state, or null if not found.
	 */
	private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState d : dfaStates) {
            if (d.nfaStates.equals(targetNfaStates)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Checks if any NFA state in the set is final.
     */
    private static boolean containsFinal(Set<State> states) {
        for (State s : states) {
            if (s.isFinal) {
                return true;
            }
        }
        return false;
    }
}
