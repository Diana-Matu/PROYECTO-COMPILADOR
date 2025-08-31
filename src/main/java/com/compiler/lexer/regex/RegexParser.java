package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;

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
            // TODO: Implement constructor if needed
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
            switch (c) {
                case '·': // concatenation
                    handleConcatenation(stack);
                    break;
                case '|': // union
                    handleUnion(stack);
                    break;
                case '*': // Kleene star
                    handleKleeneStar(stack);
                    break;
                case '?': // optional
                    handleOptional(stack);
                    break;
                case '+': // one or more
                    handlePlus(stack);
                    break;
                default: // operand (character)
                    stack.push(createNfaForCharacter(c));
                    break;
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid postfix regex: " + postfixRegex);
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
        NFA optionalNfa = NFA.optional(nfa);
        stack.push(optionalNfa);
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        NFA plusNfa = NFA.plus(nfa);
        stack.push(plusNfa);
    }
    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
    // TODO: Implement createNfaForCharacter
    // Pseudocode: Create start/end state, add transition for character
        return NFA.basic(c);
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
        NFA second = stack.pop();
        NFA first = stack.pop();
        NFA concatNfa = NFA.concatenate(first, second);
        stack.push(concatNfa);
    }
    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        NFA second = stack.pop();
        NFA first = stack.pop();
        NFA unionNfa = NFA.union(first, second);
        stack.push(unionNfa);
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        NFA starNfa = NFA.kleeneStar(nfa);
        stack.push(starNfa);
    }

    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
    // TODO: Implement isOperand
    // Pseudocode: Return true if c is not an operator
        return !(c == '·' || c == '|' || c == '*' || c == '?' || c == '+' || c == '(' || c == ')');    }
}