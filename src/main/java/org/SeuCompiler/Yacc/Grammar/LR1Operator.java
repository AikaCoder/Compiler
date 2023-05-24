package org.SeuCompiler.Yacc.Grammar;

/**
 * LR1运算符
 *
 * @param precedence the bigger, the higher
 */
public record LR1Operator(int symbolId, OperatorAssoc assoc, int precedence) {
}
