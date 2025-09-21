package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

import java.util.*;

/**
 * Token
 * -----
 * Esta clase combina:
 *  - Representación de un token (tipo, valor).
 *  - Lógica de tokenización basada en DFAs.
 *  - Reglas asociadas (TokenDfa).
 */
public class Token {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token other = (Token) o;
        return Objects.equals(type, other.type) &&
               Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    /**
     * Tokeniza la entrada según las reglas (DFAs asociados).
     */
    public static List<Token> tokenize(String input, List<TokenDfa> tokenDfas) {
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

            // Aplicar maximal munch (longest match)
            for (TokenDfa tokenDfa : tokenDfas) {
                int length = maximalMatchLength(tokenDfa.dfa, input, pos);

                if (length > maxMatchLength) {
                    maxMatchLength = length;
                    matchedToken = new Token(tokenDfa.tokenType, input.substring(pos, pos + length));
                }
            }

            if (matchedToken != null && maxMatchLength > 0) {
                tokens.add(matchedToken);
                pos += maxMatchLength;
            } else {
                throw new RuntimeException("Unexpected character at position " + pos + ": '" + currentChar + "'");
            }
        }

        return tokens;
    }

    /**
     * Encuentra la longitud del prefijo máximo aceptado por el DFA desde 'pos'.
     */
    private static int maximalMatchLength(DFA dfa, String input, int pos) {
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
     * Clase auxiliar: regla léxica (token type + DFA).
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
