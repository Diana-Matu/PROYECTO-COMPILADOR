package com.compiler;

import com.compiler.lexer.Tokenizer;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.State;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    /**
     * Crea un DFA que acepta exactamente un carácter.
     */
    private DFA createCharDFA(char accepted) {
        // NFA final dummy
        State finalNfa = new State();
        finalNfa.isFinal = true;

        // DFA estado final
        DfaState finalState = new DfaState(Set.of(finalNfa));

        // DFA estado inicial
        DfaState start = new DfaState(Set.of());
        start.addTransition(accepted, finalState);

        return new DFA(start, Arrays.asList(start, finalState));
    }

    @Test
    public void testIdentifierOnly() {
        Tokenizer tokenizer = new Tokenizer();
        List<Tokenizer.TokenDfa> tokenDfas = List.of(
                new Tokenizer.TokenDfa("ID", createCharDFA('a'))
        );

        List<Tokenizer.Token> tokens = tokenizer.tokenize("a", tokenDfas);
        assertEquals(1, tokens.size());
        assertEquals("ID", tokens.get(0).type);
        assertEquals("a", tokens.get(0).value);
    }

    @Test
    public void testInvalidCharacter() {
        Tokenizer tokenizer = new Tokenizer();
        List<Tokenizer.TokenDfa> tokenDfas = List.of(
                new Tokenizer.TokenDfa("ID", createCharDFA('a'))
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tokenizer.tokenize("b", tokenDfas)
        );

        assertTrue(exception.getMessage().contains("Unexpected character"));
    }

    @Test
    public void testMultipleIdentifiers() {
        Tokenizer tokenizer = new Tokenizer();
        List<Tokenizer.TokenDfa> tokenDfas = List.of(
                new Tokenizer.TokenDfa("A", createCharDFA('a')),
                new Tokenizer.TokenDfa("B", createCharDFA('b'))
        );

        List<Tokenizer.Token> tokens = tokenizer.tokenize("ab", tokenDfas);
        assertEquals(2, tokens.size());
        assertEquals("A", tokens.get(0).type);
        assertEquals("a", tokens.get(0).value);
        assertEquals("B", tokens.get(1).type);
        assertEquals("b", tokens.get(1).value);
    }
}
