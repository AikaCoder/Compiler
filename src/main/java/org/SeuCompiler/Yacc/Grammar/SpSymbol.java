package org.SeuCompiler.Yacc.Grammar;

/**
 * 特殊Symbol
 */
public enum SpSymbol {
    END("SP_END"),
    EPSILON("SP_EPSILON");

    private final String spType;

    SpSymbol(String spType) {
        this.spType = spType;
    }

    public String getSpType() {
        return spType;
    }
}
