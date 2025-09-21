package com.compiler.parser.syntax;

import java.util.*;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        if (!firstSets.isEmpty()) return firstSets; // cache result

        // 1. Initialize
        for (Symbol s : grammar.getNonTerminals()) {
            firstSets.put(s, new HashSet<>());
        }
        for (Symbol t : grammar.getTerminals()) {
            Set<Symbol> set = new HashSet<>();
            set.add(t);
            firstSets.put(t, set);
        }

        // 2. Fixed-point iteration
        boolean changed;
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol A = p.getLeft();
                Set<Symbol> firstA = firstSets.get(A);

                boolean allNullable = true;
                for (Symbol Xi : p.getRight()) {
                    Set<Symbol> firstXi = firstSets.get(Xi);
                    if (firstXi == null) firstXi = new HashSet<>();

                    // add FIRST(Xi) - {ε} to FIRST(A)
                    int before = firstA.size();
                    for (Symbol sym : firstXi) {
                        if (!sym.name.equals("ε")) {
                            firstA.add(sym);
                        }
                    }
                    if (firstA.size() > before) changed = true;

                    // if ε not in FIRST(Xi), stop
                    if (!firstXi.contains(new Symbol("ε", SymbolType.TERMINAL))) {
                        allNullable = false;
                        break;
                    }
                }
                // if all Xi derive ε, add ε to FIRST(A)
                if (allNullable) {
                    if (firstA.add(new Symbol("ε", SymbolType.TERMINAL))) {
                        changed = true;
                    }
                }
            }
        } while (changed);

        return firstSets;
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        if (!followSets.isEmpty()) return followSets; // cache result

        Map<Symbol, Set<Symbol>> first = getFirstSets();

        // 1. Initialize
        for (Symbol nt : grammar.getNonTerminals()) {
            followSets.put(nt, new HashSet<>());
        }
        // Add $ to FOLLOW(start symbol)
        Symbol dollar = new Symbol("$", SymbolType.TERMINAL);
        followSets.get(grammar.getStartSymbol()).add(dollar);

        // 2. Fixed-point iteration
        boolean changed;
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol B = p.getLeft();
                List<Symbol> rhs = p.getRight();

                for (int i = 0; i < rhs.size(); i++) {
                    Symbol Xi = rhs.get(i);
                    if (Xi.type == SymbolType.NON_TERMINAL) {
                        Set<Symbol> followXi = followSets.get(Xi);

                        boolean allNullable = true;
                        for (int j = i + 1; j < rhs.size(); j++) {
                            Symbol Xj = rhs.get(j);
                            Set<Symbol> firstXj = first.get(Xj);

                            int before = followXi.size();
                            for (Symbol sym : firstXj) {
                                if (!sym.name.equals("ε")) {
                                    followXi.add(sym);
                                }
                            }
                            if (followXi.size() > before) changed = true;

                            if (!firstXj.contains(new Symbol("ε", SymbolType.TERMINAL))) {
                                allNullable = false;
                                break;
                            }
                        }
                        if (allNullable) {
                            int before = followXi.size();
                            followXi.addAll(followSets.get(B));
                            if (followXi.size() > before) changed = true;
                        }
                    }
                }
            }
        } while (changed);

        return followSets;
    }
}
