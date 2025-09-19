package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

import java.util.*;

/**
 * Tokenizer
 * ----------
 * This class implements a lexer that recognizes multiple token types based on
 * a set of regular expressions. It applies the "longest match" rule.
 */
public class Tokenizer {
    /**
     * Represents a single lexical rule: a DFA and the token type it recognizes.
     */
    public static class Token {
        public final String type;
        public final String value;

        public Token(String type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Token{" + "type='" + type + '\'' + ", value='" + value + '\'' + '}';
        }
    }

 
    /**
     * Tokenizes the input string using the provided DFAs for each token type.
     *
     * @param input   The string to tokenize.
     * @param tokenDfas A list of DFAs with associated token type names.
     * @return A list of recognized tokens.
     */
    public List<Token> tokenize(String input, List<TokenDfa> tokenDfas) {
        List<Token> tokens = new ArrayList<>();
        int pos = 0;

        while (pos < input.length()) {
            char currentChar = input.charAt(pos);

            // Ignorar espacios en blanco
            if (Character.isWhitespace(currentChar)) {
                pos++;
                continue;
            }

            Token matchedToken = null;
            int maxMatchLength = 0;

            // Recorremos cada token tipo DFA para aplicar maximal munch
            for (TokenDfa tokenDfa : tokenDfas) {
                DFA dfa = tokenDfa.dfa;
                DfaSimulator simulator = new DfaSimulator();
                int length = maximalMatchLength(dfa, simulator, input, pos);

                if (length > maxMatchLength) {
                    maxMatchLength = length;
                    matchedToken = new Token(tokenDfa.tokenType, input.substring(pos, pos + length));
                }
            }

            if (matchedToken != null) {
                tokens.add(matchedToken);
                pos += maxMatchLength;
            } else {
                throw new RuntimeException("Unexpected character at position " + pos + ": '" + currentChar + "'");
            }
        }

        return tokens;
    }

    /**
     * Finds the length of the maximal prefix accepted by the DFA starting at position 'pos'.
     */
    private int maximalMatchLength(DFA dfa, DfaSimulator simulator, String input, int pos) {
        DfaState currentState = dfa.startState;
        int lastAcceptPos = -1;

        for (int i = pos; i < input.length(); i++) {
            char c = input.charAt(i);
            currentState = currentState.getTransition(c);

            if (currentState == null) break;
            if (currentState.isFinal()) lastAcceptPos = i;
        }

        if (lastAcceptPos >= 0) {
            return lastAcceptPos - pos + 1;
        } else {
            return 0;
        }
    }

    /**
     * Helper class to associate a DFA with a token type.
     */
    public static class TokenDfa {
        public final String tokenType;
        public final DFA dfa;

        public TokenDfa(String tokenType, DFA dfa) {
            this.tokenType = tokenType;
            this.dfa = dfa;
        }
    }
}
