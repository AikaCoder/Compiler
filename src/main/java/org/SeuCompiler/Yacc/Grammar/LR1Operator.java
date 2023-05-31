package org.SeuCompiler.Yacc.Grammar;

/**
 * LR1运算符
 *
 * @param precedence the bigger, the higher
 */
public record LR1Operator(int symbolId, OperatorAssoc assoc, int precedence) {
    public String getAssocStr(){
        if(assoc == null) return null;
        return assoc.getAssoc();
    }
}
