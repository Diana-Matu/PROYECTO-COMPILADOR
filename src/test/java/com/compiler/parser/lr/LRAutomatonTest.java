package com.compiler.parser.lr;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;

public class LRAutomatonTest {
    @Test
    public void testClosureAndBuild() {
        String grammarDef = "S -> a S | b";
        Grammar grammar = new Grammar(grammarDef);
        LRAutomaton automaton = new LRAutomaton(grammar);
        automaton.build();

        // initial state should exist and contain 3 items: S'->.S, S->.aS, S->.b
        java.util.List<java.util.Set<LR0Item>> states = automaton.getStates();
        int initIdx = automaton.getInitialStateIndex();
        Set<LR0Item> init = states.get(initIdx);
        assertEquals(3, init.size());

        // there should be a transition on terminal 'a' from initial state
        boolean hasA = false;
        for (Symbol t : grammar.getTerminals()) {
            if (t.name.equals("a")) { hasA = true; break; }
        }
        assertTrue(hasA);
        assertTrue(automaton.getTransitions().get(initIdx).keySet().stream().anyMatch(s -> s.name.equals("a")));
    }
}
