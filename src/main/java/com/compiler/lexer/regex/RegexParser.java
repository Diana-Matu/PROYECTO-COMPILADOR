package com.compiler.lexer.regex;

import java.util.Stack;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
        public RegexParser() {
        }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
        String postfix = ShuntingYard.toPostfix(infixRegex);
        return buildNfaFromPostfix(postfix);
    }
    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
        Stack<NFA> stack = new Stack<>();

        for (char c : postfixRegex.toCharArray()) {
            if (isOperand(c)) {
                stack.push(NFA.basic(c));
            } else {
                switch (c) {
                    case '*': stack.push(NFA.kleeneStar(stack.pop())); break;
                    case '+': stack.push(NFA.plus(stack.pop())); break;
                    case '?': stack.push(NFA.optional(stack.pop())); break;
                    case '·':
                        NFA b = stack.pop();
                        NFA a = stack.pop();
                        stack.push(NFA.concatenate(a, b));
                        break;
                    case '|':
                        NFA right = stack.pop();
                        NFA left = stack.pop();
                        stack.push(NFA.union(left, right));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid regex, stack size: " + stack.size());
        }
        return stack.pop();
    }
    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();

        // ε-transitions desde el nuevo start al NFA original y al nuevo end
        start.transitions.add(new Transition(null, nfa.startState));
        start.transitions.add(new Transition(null, end));

        // Conectar el final del NFA original al nuevo end
        nfa.endState.transitions.add(new Transition(null, end));
        nfa.endState.isFinal = false;
        end.isFinal = true;

        stack.push(new NFA(start, end));
    }
    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        State start = nfa.startState; // reutilizamos inicio de X
        State end = new State();

        // loop del final al inicio (para repetir)
        nfa.endState.transitions.add(new Transition(null, start));
        nfa.endState.transitions.add(new Transition(null, end));
        nfa.endState.isFinal = false;
        end.isFinal = true;

        stack.push(new NFA(start, end));
    }

    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
        State start = new State();
        State end = new State();
        start.transitions.add(new Transition(c, end));
        return new NFA(start, end);
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
    NFA nfa2 = stack.pop(); // segundo NFA
    NFA nfa1 = stack.pop(); // primer NFA

    // Conectar final del primero con inicio del segundo
    nfa1.endState.transitions.add(new Transition(null, nfa2.startState));

    // El nuevo NFA tiene el mismo inicio de nfa1 y el mismo final de nfa2
    stack.push(new NFA(nfa1.startState, nfa2.endState));
}
    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        NFA nfa2 = stack.pop();
        NFA nfa1 = stack.pop();
        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, nfa1.startState));
        start.transitions.add(new Transition(null, nfa2.startState));

        nfa1.endState.transitions.add(new Transition(null, end));
        nfa2.endState.transitions.add(new Transition(null, end));

        nfa1.endState.isFinal = false;
        nfa2.endState.isFinal = false;
        end.isFinal = true;

        stack.push(new NFA(start, end));
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, nfa.startState)); // entrar en X
        start.transitions.add(new Transition(null, end));           // salto directo (acepta vacío)

        nfa.endState.transitions.add(new Transition(null, nfa.startState)); // repetir X
        nfa.endState.transitions.add(new Transition(null, end));             // terminar
        nfa.endState.isFinal = false;
        end.isFinal = true;

        stack.push(new NFA(start, end));
    }
    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
        return !(c == '*' || c == '+' || c == '?' || c == '·' || c == '|');
    }
}