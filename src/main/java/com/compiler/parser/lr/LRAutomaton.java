package com.compiler.parser.lr;

import java.util.*;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Builds the canonical collection of LR(1) items (the DFA automaton).
 * Items contain a lookahead symbol.
 */
public class LRAutomaton {
    private final Grammar grammar;
    private final List<Set<LR0Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
    private String augmentedLeftName = null;

    public LRAutomaton(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public List<Set<LR0Item>> getStates() { return states; }
    public Map<Integer, Map<Symbol, Integer>> getTransitions() { return transitions; }
       /**
     * Calcula FIRST sets para todos los símbolos (term y non-term) del grammar.
     * Representa epsilon con el Symbol("ε", TERMINAL).
     */
    private Map<Symbol, Set<Symbol>> computeFirstSets(Symbol epsilon) {
        Map<Symbol, Set<Symbol>> first = new HashMap<>();

        // Inicializar FIRST para terminales: FIRST(t) = { t }
        for (Symbol t : grammar.getTerminals()) {
            Set<Symbol> s = new HashSet<>();
            s.add(t);
            first.put(t, s);
        }

        // Inicializar para no terminales vacíos
        for (Symbol nt : grammar.getNonTerminals()) {
            first.putIfAbsent(nt, new HashSet<>());
        }

        // Asegurar epsilon presente si se usa
        first.putIfAbsent(epsilon, new HashSet<>(Collections.singleton(epsilon)));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol A = p.getLeft();
                List<Symbol> rhs = p.getRight();

                // calcular FIRST(rhs)
                Set<Symbol> firstAlpha = computeFirstOfSequence(rhs, first, epsilon);

                // añadir elementos de firstAlpha a FIRST(A)
                Set<Symbol> target = first.get(A);
                int before = target.size();
                for (Symbol s : firstAlpha) {
                    target.add(s);
                }
                if (target.size() > before) changed = true;
            }
        }

        return first;
    }

    /**
     * CLOSURE for LR(1): standard algorithm using FIRST sets to compute lookaheads for new items.
     */
    private Set<LR0Item> closure(Set<LR0Item> items) {
        // TODO: Implement the CLOSURE algorithm for a set of LR(1) items.
        // 1. Initialize a new set `closure` with the given `items`.
        // 2. Create a worklist (like a Queue or List) and add all items from `items` to it.
        // 3. Pre-calculate the FIRST sets for all symbols in the grammar.
        // 4. While the worklist is not empty:
        //    a. Dequeue an item `[A -> α • B β, a]`.
        //    b. If `B` is a non-terminal:
        //       i. For each production of `B` (e.g., `B -> γ`):
        //          - Calculate the FIRST set of the sequence `βa`. This will be the lookahead for the new item.
        //          - For each terminal `b` in FIRST(βa):
        //             - Create a new item `[B -> • γ, b]`.
        //             - If this new item is not already in the `closure` set:
        //               - Add it to `closure`.
        //               - Enqueue it to the worklist.
        // 5. Return the `closure` set.
        //return new HashSet<>();  Placeholder

        Set<LR0Item> closure = new HashSet<>(items);
        Deque<LR0Item> worklist = new ArrayDeque<>(items);

        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
        Map<Symbol, Set<Symbol>> firstSets = computeFirstSets(epsilon);

        while (!worklist.isEmpty()) {
            LR0Item item = worklist.poll();
            Symbol B = item.getSymbolAfterDot();
            if (B == null) continue;
            if (B.type != SymbolType.NON_TERMINAL) continue;

            // beta = symbols after B in item's rhs
            List<Symbol> rhs = item.production.getRight();
            List<Symbol> beta = new ArrayList<>();
            for (int i = item.dotPosition + 1; i < rhs.size(); i++) beta.add(rhs.get(i));

            // lookahead 'a' es item.lookahead si está definido, si no se ignora
            Symbol lookaheadSymbol = null;
            if (item.lookahead instanceof Symbol) {
                lookaheadSymbol = (Symbol) item.lookahead;
            }

            // construir secuencia beta + a (si a existe), para FIRST(beta a)
            List<Symbol> betaAndA = new ArrayList<>(beta);
            if (lookaheadSymbol != null) betaAndA.add(lookaheadSymbol);

            Set<Symbol> lookaheadSet = computeFirstOfSequence(betaAndA, firstSets, epsilon);

            // por cada producción B -> gamma
            for (Production prod : grammar.getProductions()) {
                if (!prod.getLeft().equals(B)) continue;
                for (Symbol b : lookaheadSet) {
                    if (b.equals(epsilon)) continue; // no usamos ε como lookahead terminal
                    // Crear nuevo ítem [B -> • γ, b]
                    LR0Item newItem = new LR0Item(prod, 0, null);
                    newItem.lookahead = b;
                    // Si el conjunto no contiene el core, agregarlo; si contiene el core pero queremos
                    // reflejar distinta lookahead, añadimos igualmente (aunque equals/hashCode ignoran lookahead).
                    if (!closure.contains(newItem)) {
                        closure.add(newItem);
                        worklist.add(newItem);
                    } else {
                        // el core ya existe: debemos comprobar si hay un item existente con mismo core
                        // pero distinto lookahead almacenado; como equals/hashcode ignoran lookahead,
                        // buscamos el objeto existente para actualizar su campo lookahead si es necesario.
                        for (LR0Item existing : closure) {
                            if (existing.equals(newItem)) {
                                // si existing.lookahead es null o distinto, y queremos mantener uno solo,
                                // podemos combinar: si existing.lookahead no contiene b, no hay estructura
                                // para guardar múltiples lookaheads por core (tu LR0Item tiene Object lookahead).
                                // Para mantener compatibilidad simple, si existing.lookahead es null,
                                // asignamos b; si ya existe y distinto, lo dejamos (no hay set de lookaheads).
                                if (existing.lookahead == null) {
                                    existing.lookahead = b;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        return closure;
    }

    /**
     * Compute FIRST of a sequence of symbols.
     */
    private Set<Symbol> computeFirstOfSequence(List<Symbol> seq, Map<Symbol, Set<Symbol>> firstSets, Symbol epsilon) {
        // TODO: Implement the logic to compute the FIRST set for a sequence of symbols.
        // 1. Initialize an empty result set.
        // 2. If the sequence is empty, add epsilon to the result and return.
        // 3. Iterate through the symbols `X` in the sequence:
        //    a. Get `FIRST(X)` from the pre-calculated `firstSets`.
        //    b. Add all symbols from `FIRST(X)` to the result, except for epsilon.
        //    c. If `FIRST(X)` does not contain epsilon, stop and break the loop.
        //    d. If it does contain epsilon and this is the last symbol in the sequence, add epsilon to the result set.
        // 4. Return the result set.
       Set<Symbol> result = new HashSet<>();
        if (seq == null || seq.isEmpty()) {
            result.add(epsilon);
            return result;
        }

        for (int i = 0; i < seq.size(); i++) {
            Symbol X = seq.get(i);
            Set<Symbol> firstX = firstSets.get(X);
            if (firstX == null) {
                // si X no tiene FIRST precomputado (posible si X es algo extraño), tratar como terminal
                result.add(X);
                break;
            }

            // añadir FIRST(X) \ {ε}
            for (Symbol s : firstX) {
                if (!s.equals(epsilon)) result.add(s);
            }

            // si FIRST(X) contiene ε, continuar; si no, terminar
            if (!firstX.contains(epsilon)) {
                return result;
            } else {
                // si es el último y contiene ε, agregamos ε al resultado
                if (i == seq.size() - 1) result.add(epsilon);
                // else continuar al siguiente símbolo
            }
        }

        return result;
    }


    /**
     * GOTO for LR(1): moves dot over symbol and takes closure.
     */
    private Set<LR0Item> goTo(Set<LR0Item> state, Symbol symbol) {
        // TODO: Implement the GOTO function.
        // 1. Initialize an empty set `movedItems`.
        // 2. For each item `[A -> α • X β, a]` in the input `state`:
        //    a. If `X` is equal to the input `symbol`:
        //       - Add the new item `[A -> α X • β, a]` to `movedItems`.
        // 3. Return the `closure` of `movedItems`.
        Set<LR0Item> movedItems = new HashSet<>();
        for (LR0Item item : state) {
            Symbol after = item.getSymbolAfterDot();
            if (after != null && after.equals(symbol)) {
                LR0Item moved = new LR0Item(item.production, item.dotPosition + 1, null);
                // propagar lookahead si existe (LR0Item.lookahead es Object)
                moved.lookahead = item.lookahead;
                movedItems.add(moved);
            }
        }
        return closure(movedItems);
    }

    /**
     * Build the LR(1) canonical collection: states and transitions.
     */
    public void build() {
        // TODO: Implement the construction of the canonical collection of LR(1) item sets (the DFA).
        // 1. Clear any existing states and transitions.
        // 2. Create the augmented grammar: Add a new start symbol S' and production S' -> S.
        // 3. Create the initial item: `[S' -> • S, $]`.
        // 4. The first state, `I0`, is the `closure` of this initial item set. Add `I0` to the list of states.
        // 5. Create a worklist (queue) and add `I0` to it.
        // 6. While the worklist is not empty:
        //    a. Dequeue a state `I`.
        //    b. For each grammar symbol `X`:
        //       i. Calculate `J = goTo(I, X)`.
        //       ii. If `J` is not empty and not already in the list of states:
        //          - Add `J` to the list of states.
        //          - Enqueue `J` to the worklist.
        //       iii. Create a transition from the index of state `I` to the index of state `J` on symbol `X`.
        states.clear();
        transitions.clear();

        // símbolos especiales
        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
        Symbol dollar = new Symbol("$", SymbolType.TERMINAL);

        // Augment grammar: S' -> S
        Symbol originalStart = grammar.getStartSymbol();
        if (originalStart == null) {
            throw new IllegalStateException("Grammar has no start symbol.");
        }
        this.augmentedLeftName = originalStart.name + "'";
        Symbol augmentedLeft = new Symbol(augmentedLeftName, SymbolType.NON_TERMINAL);
        Production augmentedProd = new Production(augmentedLeft, Collections.singletonList(originalStart));

        // Inicial: [S' -> • S, $]
        LR0Item startItem = new LR0Item(augmentedProd, 0, null);
        startItem.lookahead = dollar;
        Set<LR0Item> I0seed = new HashSet<>();
        I0seed.add(startItem);

        Set<LR0Item> I0 = closure(I0seed);
        states.add(I0);

        // BFS sobre conjuntos de ítems (estados)
        Deque<Set<LR0Item>> queue = new ArrayDeque<>();
        queue.add(I0);

        while (!queue.isEmpty()) {
            Set<LR0Item> I = queue.poll();
            int iIndex = states.indexOf(I);

            // recopilar todos los símbolos que aparecen inmediatamente después del punto en I
            Set<Symbol> symbolsAfterDot = new HashSet<>();
            for (LR0Item itm : I) {
                Symbol s = itm.getSymbolAfterDot();
                if (s != null) symbolsAfterDot.add(s);
            }

            // para cada símbolo X calcular goto(I, X)
            for (Symbol X : symbolsAfterDot) {
                Set<LR0Item> J = goTo(I, X);
                if (J.isEmpty()) continue;

                int jIndex = states.indexOf(J);
                if (jIndex == -1) {
                    states.add(J);
                    jIndex = states.size() - 1;
                    queue.add(J);
                }

                transitions.computeIfAbsent(iIndex, k -> new HashMap<>()).put(X, jIndex);
            }
        }
    }

    public String getAugmentedLeftName() { return augmentedLeftName; }
}