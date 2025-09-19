package com.compiler.lexer.nfa;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 * <p>
 * An NFA is used in lexical analysis to model regular expressions and pattern matching.
 * This class encapsulates the start and end states of the automaton.
 */

public class NFA {
    /**
     * The initial (start) state of the NFA.
     */
    public final State startState;

    /**
     * The final (accepting) state of the NFA.
     */
    public final State endState;

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start The initial state.
     * @param end The final (accepting) state.
     */
    public NFA(State start, State end) {
        this.startState = start;
        this.endState = end;
    }

    /**
     * Returns the initial (start) state of the NFA.
     * @return the start state
     */
    public State getStartState() {
        return this.startState;
    }

     /**
     * Creates a basic NFA for a single symbol
     * @param c transition symbol
     */
    public static NFA basic(char c) {
        State start = new State();
        State end = new State();
        end.isFinal = true;
        start.transitions.add(new Transition(c, end));
        return new NFA(start, end);
    }

 /**
     * Concatenation of two NFAs (A·B).
     */
    public static NFA concatenate(NFA a, NFA b) {
        a.endState.isFinal = false;
        a.endState.transitions.add(new Transition(null, b.startState));
        return new NFA(a.startState, b.endState);
    }
    /**
     * Unión of two NFAs (A|B).
     */
    public static NFA union(NFA a, NFA b) {
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, a.startState));
        start.transitions.add(new Transition(null, b.startState));

        a.endState.isFinal = false;
        b.endState.isFinal = false;

        a.endState.transitions.add(new Transition(null, end));
        b.endState.transitions.add(new Transition(null, end));

        return new NFA(start, end);
    }

    /**
     * CKleene star clousure (A*).
     */
    public static NFA kleeneStar(NFA a) {
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, a.startState));
        start.transitions.add(new Transition(null, end));

        a.endState.isFinal = false;
        a.endState.transitions.add(new Transition(null, a.startState));
        a.endState.transitions.add(new Transition(null, end));

        return new NFA(start, end);
    }


    /**
     * Operator + 
     */
    public static NFA plus(NFA a) {
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, a.startState));

        a.endState.isFinal = false;
        a.endState.transitions.add(new Transition(null, a.startState));
        a.endState.transitions.add(new Transition(null, end));

        return new NFA(start, end);
    }
    /**
     * Operator ? (zero or occurrence).
     */
    public static NFA optional(NFA a) {
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, a.startState));
        start.transitions.add(new Transition(null, end));

        a.endState.isFinal = false;
        a.endState.transitions.add(new Transition(null, end));

        return new NFA(start, end);
    }
}