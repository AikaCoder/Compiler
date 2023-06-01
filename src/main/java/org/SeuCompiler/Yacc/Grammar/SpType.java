package org.SeuCompiler.Yacc.Grammar;

/**
 * 特殊Symbol
 */
public enum SpType {
    END("SP_END"),
    EPSILON("SP_EPSILON");

    private final String spTypeStr;

    SpType(String spType) {
        this.spTypeStr = spType;
    }

    public GrammarSymbol getSpSymbol(){
        return new GrammarSymbol(GrammarSymbol.GrammarSymbolType.SPTOKEN, spTypeStr);
    }
}
