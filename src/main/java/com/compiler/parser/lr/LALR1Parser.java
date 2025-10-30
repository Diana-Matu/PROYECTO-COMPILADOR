package com.compiler.parser.lr;

import java.util.List;

import com.compiler.lexer.Token;
import com.compiler.parser.grammar.Symbol;

/**
 * Implements the LALR(1) parsing engine.
 * Uses a stack and the LALR(1) table to process a sequence of tokens.
 * Complementary task for Practice 9.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

   // package-private accessor for tests
   LALR1Table getTable() {
       return table;
   }

   /**
    * Parses a sequence of tokens using the LALR(1) parsing algorithm.
    * @param tokens The list of tokens from the lexer.
    * @return true if the sequence is accepted, false if a syntax error is found.
    */
   public boolean parse(List<Token> tokens) {
        // TODO: Implement the LALR(1) parsing algorithm.
        // 1. Initialize a stack for states and push the initial state (from table.getInitialState()).
        // 2. Create a mutable list of input tokens from the parameter and add the end-of-input token ("$").
        // 3. Initialize an instruction pointer `ip` to 0, pointing to the first token.
        // 4. Start a loop that runs until an ACCEPT or ERROR condition is met.
        //    a. Get the current state from the top of the stack.
        //    b. Get the current token `a` from the input list at index `ip`.
        //    c. Look up the action in the ACTION table: action = table.getActionTable()[state][a.type].
        //    d. If no action is found (it's null), it's a syntax error. Return false.
        //    e. If the action is SHIFT(s'):
        //       i. Push the new state s' onto the stack.
        //       ii. Advance the input pointer: ip++.
        //    f. If the action is REDUCE(A -> β):
        //       i. Pop |β| symbols (and states) from the stack. Handle epsilon productions (where |β|=0).
        //       ii. Get the new state `s` from the top of the stack.
        //       iii. Look up the GOTO state: goto_state = table.getGotoTable()[s][A].
        //       iv. If no GOTO state is found, it's an error. Return false.
        //       v. Push the goto_state onto the stack.
        //    g. If the action is ACCEPT:
        //       i. The input has been parsed successfully. Return true.
        //    h. If the action is none of the above, it's an unhandled case or error. Return false.
    // 1. Inicializar la pila de estados y poner el estado inicial
    java.util.Deque<Integer> stack = new java.util.ArrayDeque<>();
    stack.push(table.getInitialState());

    // 2. Crear lista mutable de tokens y agregar marcador de fin "$"
    java.util.List<Token> input = new java.util.ArrayList<>(tokens);
    input.add(new Token("$", "$"));

    // 3. Puntero a la posición actual del input
    int ip = 0;

    while (true) {
        int state = stack.peek();
        Token a = input.get(ip);

        // 4c. Obtener acción de la tabla ACTION
        LALR1Table.Action action = null;
        java.util.Map<Symbol, LALR1Table.Action> row = table.getActionTable().get(state);
        if (row != null) {
            action = row.get(new com.compiler.parser.grammar.Symbol(a.type, com.compiler.parser.grammar.SymbolType.TERMINAL));
        }

        // 4d. Si no hay acción, error sintáctico
        if (action == null) {
            return false;
        }

        switch (action.type) {
            case SHIFT:
                // 4e. SHIFT: push nuevo estado y avanzar input
                stack.push(action.state);
                ip++;
                break;

            case REDUCE:
                // 4f. REDUCE: pop |β| estados de la pila
                com.compiler.parser.grammar.Production prod = action.reduceProd;
                int betaSize = prod.getRight().size();
                for (int i = 0; i < betaSize; i++) {
                    if (!stack.isEmpty()) stack.pop();
                }

                // 4f ii. Obtener estado actual del top
                int s = stack.peek();

                // 4f iii. Buscar GOTO[s][A]
                Integer gotoState = null;
                java.util.Map<Symbol, Integer> gotoRow = table.getGotoTable().get(s);
                if (gotoRow != null) {
                    gotoState = gotoRow.get(prod.getLeft());
                }

                // 4f iv. Si no hay goto, error
                if (gotoState == null) {
                    return false;
                }

                // 4f v. Push goto_state
                stack.push(gotoState);
                break;

            case ACCEPT:
                // 4g. ACCEPT: entrada parseada correctamente
                return true;

            default:
                // 4h. Cualquier otro caso es error
                return false;
        }
        }
    }
   }