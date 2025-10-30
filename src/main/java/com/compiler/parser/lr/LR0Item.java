package com.compiler.parser.lr;

import java.util.Collection;

import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;

/**
 * Represents an LR(0) item, which is a production with a dot (•)
 * at some position in the right-hand side.
 * Task for Practice 8.
 */
public class LR0Item {
    public static final Collection<? extends Symbol> lookaheadSet = null;
    public final Production production;
    public final int dotPosition;
    public Symbol lookahead;


    /**
     * Constructs an LR(0) item with the given production and dot position.
     * @param production The production rule.
     * @param dotPosition The position of the dot in the right-hand side.
     * Detailed pseudocode:
     * 1. Assign the production to the field.
     * 2. Assign the dotPosition to the field.
     * 3. Optionally, validate that dotPosition is within valid bounds (0 <= dotPosition <= production.right.size()).
     * @param b 
     */
    public LR0Item(Production production, int dotPosition, Symbol b) {
        if (production == null) {
            throw new IllegalArgumentException("Production cannot be null.");
        }
        if (dotPosition < 0 || dotPosition > production.getRight().size()) {
            throw new IllegalArgumentException("Invalid dot position: " + dotPosition);
        }
        this.production = production;
        this.dotPosition = dotPosition;
    }

    // TODO: Implement equals, hashCode, and a method to get the symbol after the dot.
    /*
     * Detailed pseudocode:
     *
     * equals(Object obj):
     *   1. If obj is not an LR0Item, return false.
     *   2. Compare production and dotPosition for equality.
     *   3. Return true if both match, false otherwise.
     *
     * hashCode():
     *   1. Compute hash using production and dotPosition.
     *   2. Return the combined hash value.
     *
     * getSymbolAfterDot():
     *   1. If dotPosition < production.right.size():
     *        - Return production.right.get(dotPosition).
     *      Else:
     *        - Return null (dot is at the end).
     */
    /**
     * Returns the symbol that appears immediately after the dot (•).
     * If the dot is at the end of the production, returns null.
     *
     * @return the Symbol after the dot, or null if at the end.
     */
    public Symbol getSymbolAfterDot() {
        if (dotPosition < production.getRight().size()) {
            return production.getRight().get(dotPosition);
        }
        return null; // dot at end
    }

    /**
     * Checks if this LR(0) item is equal to another object.
     * Two items are equal if they have the same production and dot position.
     *
     * @param obj the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LR0Item)) return false;
        LR0Item other = (LR0Item) obj;
        return this.dotPosition == other.dotPosition &&
               this.production.equals(other.production);
    }

    /**
     * Returns a hash code based on the production and dot position.
     *
     * @return hash code value for this item
     */
    @Override
    public int hashCode() {
        int result = production.hashCode();
        result = 31 * result + dotPosition;
        return result;
    }

    /**
     * Returns a string representation of this LR(0) item,
     * showing the dot position inside the production.
     *
     * Example: E -> E • + T
     *
     * @return String representation of the LR(0) item
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(production.getLeft().name).append(" -> ");

        for (int i = 0; i <= production.getRight().size(); i++) {
            if (i == dotPosition) sb.append("• ");
            if (i < production.getRight().size()) {
                sb.append(production.getRight().get(i).name).append(" ");
            }
        }
        return sb.toString().trim();
    }
}