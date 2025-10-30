package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Symbol;

/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LRAutomaton automaton;

    // merged LALR states and transitions
    private java.util.List<java.util.Set<LR0Item>> lalrStates = new java.util.ArrayList<>();
    private java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> lalrTransitions = new java.util.HashMap<>();
    
    // ACTION table: state -> terminal -> Action
    public static class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final com.compiler.parser.grammar.Production reduceProd; // for REDUCE

        private Action(Type type, Integer state, com.compiler.parser.grammar.Production prod) {
            this.type = type; this.state = state; this.reduceProd = prod;
        }

        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(com.compiler.parser.grammar.Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }
    }

    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> action = new java.util.HashMap<>();
    private final java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> gotoTable = new java.util.HashMap<>();
    private final java.util.List<String> conflicts = new java.util.ArrayList<>();
    private int initialState = 0;

    public LALR1Table(LRAutomaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Builds the LALR(1) parsing table.
     */
    public void build() {
        // TODO: Implement the LALR(1) table construction logic.
        // This is a multi-step process.
        
        // Step 1: Ensure the underlying LR(1) automaton is built.
        // automaton.build();

        // Step 2: Merge LR(1) states to create LALR(1) states.
        //  a. Group LR(1) states that have the same "kernel" (the set of LR(0) items).
        //     - A kernel item is an LR(1) item without its lookahead.
        //     - Create a map from a kernel (Set<KernelEntry>) to a list of state IDs that share that kernel.
        //  b. For each group of states with the same kernel:
        //     - Create a single new LALR(1) state.
        //     - This new state is formed by merging the LR(1) items from all states in the group.
        //     - Merging means for each kernel item, the new lookahead set is the union of all lookaheads for that item across the group.
        //     - Store these new LALR states in `lalrStates`.
        //  c. Create a mapping from old LR(1) state IDs to new LALR(1) state IDs.

        // Step 3: Build the transitions for the new LALR(1) automaton.
        //  - For each transition in the original LR(1) automaton `s -X-> t`:
        //  - Add a new transition for the LALR automaton: `merged(s) -X-> merged(t)`.
        //  - Use the mapping from step 2c to find the merged state IDs.
        //  - Store these new transitions in `lalrTransitions`.

        // Step 4: Fill the ACTION and GOTO tables based on the LALR automaton.
        //  - Call a helper method, e.g., `fillActionGoto()`.
        // Step 1: Asegurarse de que el autómata LR(1) esté construido

    // Step 1: Ensure LR(1) automaton is built
    automaton.build();

    // Step 2a: Group LR(1) states by kernel (production + dotPosition)
    Map<Set<KernelEntry>, List<Integer>> kernelGroups = new HashMap<>();
    for (int stateId = 0; stateId < automaton.getStates().size(); stateId++) {
        Set<LR0Item> items = automaton.getStates().get(stateId);
        Set<KernelEntry> kernel = new HashSet<>();
        for (LR0Item it : items) {
            kernel.add(new KernelEntry(it.production, it.dotPosition));
        }
        kernelGroups.computeIfAbsent(kernel, k -> new ArrayList<>()).add(stateId);
    }

    // Step 2b: Create new merged LALR states combining lookaheads
    Map<Integer, Integer> oldToNewState = new HashMap<>();
    for (List<Integer> group : kernelGroups.values()) {
        Set<LR0Item> mergedState = new HashSet<>();
        // map kernel entry -> set of lookaheads (Symbol)
        Map<KernelEntry, Set<Symbol>> lookaheadMap = new HashMap<>();

        // Union lookaheads for each kernel item
        for (int oldId : group) {
            Set<LR0Item> oldItems = automaton.getStates().get(oldId);
            for (LR0Item it : oldItems) {
                KernelEntry k = new KernelEntry(it.production, it.dotPosition);
                // obtain the Symbol lookahead if present
                if (it.lookahead instanceof com.compiler.parser.grammar.Symbol) {
                    com.compiler.parser.grammar.Symbol la = (com.compiler.parser.grammar.Symbol) it.lookahead;
                    lookaheadMap.computeIfAbsent(k, x -> new HashSet<>()).add(la);
                }
                // if it.lookahead is null or not a Symbol, nothing to add
            }
        }

        // Reconstruct LR0Item objects with combined lookaheads
        for (Map.Entry<KernelEntry, Set<Symbol>> entry : lookaheadMap.entrySet()) {
            KernelEntry k = entry.getKey();
            Set<Symbol> lookaheads = entry.getValue();
            if (lookaheads.isEmpty()) {
                // No lookahead recorded: create a core item with null lookahead
                LR0Item coreOnly = new LR0Item(k.production, k.dotPosition, null);
                mergedState.add(coreOnly);
            } else {
                for (Symbol la : lookaheads) {
                    LR0Item newItem = new LR0Item(k.production, k.dotPosition, la);
                    newItem.lookahead = la;
                    mergedState.add(newItem);
                }
            }
        }

        // Edge case: if lookaheadMap empty (all items had null lookahead), collect cores
        if (lookaheadMap.isEmpty()) {
            // get kernel entries and create items with null lookahead
            for (int oldId : group) {
                for (LR0Item it : automaton.getStates().get(oldId)) {
                    KernelEntry k = new KernelEntry(it.production, it.dotPosition);
                    LR0Item newItem = new LR0Item(k.production, k.dotPosition, null);
                    mergedState.add(newItem);
                }
            }
        }

        int newStateId = lalrStates.size();
        lalrStates.add(mergedState);

        for (int oldId : group) {
            oldToNewState.put(oldId, newStateId);
        }
    }

    // Step 3: Build merged transitions (guardando posibles estados sin mapeo)
    for (Map.Entry<Integer, Map<Symbol, Integer>> transEntry : automaton.getTransitions().entrySet()) {
        int oldFrom = transEntry.getKey();
        Integer newFrom = oldToNewState.get(oldFrom);
        if (newFrom == null) {
            // estado antiguo no mapeado (raro), saltar
            continue;
        }

        for (Map.Entry<Symbol, Integer> t : transEntry.getValue().entrySet()) {
            Symbol sym = t.getKey();
            int oldTo = t.getValue();
            Integer newTo = oldToNewState.get(oldTo);
            if (newTo == null) {
                // destino sin mapping => saltar (seguro)
                continue;
            }
            lalrTransitions.computeIfAbsent(newFrom, k -> new HashMap<>()).put(sym, newTo);
        }
    }

    // Step 4: Fill action/goto tables
    fillActionGoto();
}



    private void fillActionGoto() {
        // TODO: Populate the ACTION and GOTO tables based on the LALR states and transitions.
        // 1. Clear the action, gotoTable, and conflicts lists.
        // 2. Iterate through each LALR state `s` from 0 to lalrStates.size() - 1.
        // 3. For each state `s`, iterate through its LR1Item `it`.
        //    a. Get the symbol after the dot, `X = it.getSymbolAfterDot()`.
        //    b. If `X` is a terminal (SHIFT action):
        //       - Find the destination state `t` from `lalrTransitions.get(s).get(X)`.
        //       - Check for conflicts: if action table already has an entry for `[s, X]`, it's a conflict.
        //       - Otherwise, set `action[s][X] = SHIFT(t)`.
        //    c. If the dot is at the end of the production (`X` is null) (REDUCE or ACCEPT action):
        //       - This is an item like `[A -> α •, a]`.
        //       - If it's the augmented start production (`S' -> S •`) and lookahead is `$`, this is an ACCEPT action.
        //         - Set `action[s][$] = ACCEPT`.
        //       - Otherwise, it's a REDUCE action.
        //         - For the lookahead symbol `a` in the item:
        //         - Check for conflicts: if `action[s][a]` is already filled, report a Shift/Reduce or Reduce/Reduce conflict.
        //         - Otherwise, set `action[s][a] = REDUCE(A -> α)`.
        // 4. Populate the GOTO table.
        //    - For each state `s`, look at its transitions in `lalrTransitions`.
        //    - For each transition on a NON-TERMINAL symbol `B` to state `t`:
        //    - Set `gotoTable[s][B] = t`.
    // 1. Clear tables
    action.clear();
    gotoTable.clear();
    conflicts.clear();

    // 2. Iterate LALR states
    for (int s = 0; s < lalrStates.size(); s++) {
        Set<LR0Item> items = lalrStates.get(s);
        Map<Symbol, Integer> trans = lalrTransitions.getOrDefault(s, new HashMap<>());

        for (LR0Item it : items) {
            Symbol X = it.getSymbolAfterDot();

            if (X != null) {
                // SHIFT: terminal with transition
                if (X.type == com.compiler.parser.grammar.SymbolType.TERMINAL) {
                    Integer t = trans.get(X);
                    if (t != null) {
                        action.computeIfAbsent(s, k -> new HashMap<>());
                        Map<com.compiler.parser.grammar.Symbol, Action> row = action.get(s);

                        Action existing = row.get(X);
                        Action shiftAction = Action.shift(t);

                        if (existing != null && !existing.equals(shiftAction)) {
                            conflicts.add("Shift/Shift conflict in state " + s + " on symbol " + X.name);
                        } else {
                            row.put(X, shiftAction);
                        }
                    }
                }
            } else {
                // REDUCE or ACCEPT: dot at end
                com.compiler.parser.grammar.Production prod = it.production;
                Object laObj = it.lookahead;
                com.compiler.parser.grammar.Symbol lookahead = (laObj instanceof com.compiler.parser.grammar.Symbol) ? (com.compiler.parser.grammar.Symbol) laObj : null;

                action.computeIfAbsent(s, k -> new HashMap<>());
                Map<com.compiler.parser.grammar.Symbol, Action> row = action.get(s);

                // Check for accept: augmented start production and lookahead is $
                if (lookahead != null &&
                    prod.getLeft().name.equals(automaton.getAugmentedLeftName()) &&
                    prod.getRight().isEmpty() &&
                    "$".equals(lookahead.name)) {
                    row.put(lookahead, Action.accept());
                } else {
                    // REDUCE for each lookahead — if lookahead is null, we skip (no terminal to place)
                    if (lookahead != null) {
                        Action existing = row.get(lookahead);
                        Action reduceAction = Action.reduce(prod);
                        if (existing != null && !existing.equals(reduceAction)) {
                            conflicts.add("Shift/Reduce or Reduce/Reduce conflict in state " + s + " on symbol " + lookahead.name);
                        } else {
                            row.put(lookahead, reduceAction);
                        }
                    }
                }
            }
        }

        // Fill GOTO entries for non-terminals
        Map<Symbol, Integer> transMap = lalrTransitions.get(s);
        if (transMap != null) {
            for (Map.Entry<Symbol, Integer> e : transMap.entrySet()) {
                Symbol sym = e.getKey();
                Integer t = e.getValue();
                if (sym.type == com.compiler.parser.grammar.SymbolType.NON_TERMINAL) {
                    gotoTable.computeIfAbsent(s, k -> new HashMap<>()).put(sym, t);
                }
            }
        }
    }
}

    // ... (Getters and KernelEntry class can remain as is)
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Action>> getActionTable() { return action; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getGotoTable() { return gotoTable; }
    public java.util.List<String> getConflicts() { return conflicts; }
    private static class KernelEntry {
        public final com.compiler.parser.grammar.Production production;
        public final int dotPosition;
        KernelEntry(com.compiler.parser.grammar.Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof KernelEntry)) return false;
            KernelEntry o = (KernelEntry) obj;
            return dotPosition == o.dotPosition && production.equals(o.production);
        }
        @Override
        public int hashCode() {
            int r = production.hashCode();
            r = 31 * r + dotPosition;
            return r;
        }
    }
    public java.util.List<java.util.Set<LR0Item>> getLALRStates() { return lalrStates; }
    public java.util.Map<Integer, java.util.Map<com.compiler.parser.grammar.Symbol, Integer>> getLALRTransitions() { return lalrTransitions; }
    public int getInitialState() { return initialState; }
}