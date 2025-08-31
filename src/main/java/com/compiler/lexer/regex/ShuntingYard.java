package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // TODO: Implement constructor if needed
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            output.append(c1);

            if (i + 1 < regex.length()) {
                char c2 = regex.charAt(i + 1);

                // Rule: insert concatenation if c1 can end an operand/group and c2 can start one
                if ((isOperand(c1) || c1 == ')' || c1 == '*' || c1 == '+' || c1 == '?') &&
                    (isOperand(c2) || c2 == '(')) {
                    output.append('·');
                }
            }
        }

        return output.toString();
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        // TODO: Implement isOperand
        return !(c == '|' || c == '*' || c == '?' || c == '+' || c == '(' || c == ')' || c == '·');    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // Define operator precedence
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1);
        precedence.put('·', 2); // explicit concatenation
        precedence.put('*', 3);
        precedence.put('+', 3);
        precedence.put('?', 3);

        String regex = insertConcatenationOperator(infixRegex);

        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : regex.toCharArray()) {
            if (isOperand(c)) {
                output.append(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop(); // discard '('
                } else {
                    throw new IllegalArgumentException("Unbalanced parentheses in regex");
                }
            } else { // operator
                while (!stack.isEmpty() &&
                       stack.peek() != '(' &&
                       precedence.get(stack.peek()) >= precedence.get(c)) {
                    output.append(stack.pop());
                }
                stack.push(c);
            }
        }

        // Pop remaining operators
        while (!stack.isEmpty()) {
            char op = stack.pop();
            if (op == '(' || op == ')') {
                throw new IllegalArgumentException("Unbalanced parentheses in regex");
            }
            output.append(op);
        }

        return output.toString();
    }
}
