package org.SeuCompiler.Yacc.YaccParser;

/**
 * YaccParser相关
 * YaccParser在头部读出的运算符，tokenName或literal填一项
 * // @example %left '+' ADD_OP
 */
public record YaccParserOperator (String tokenName, String literal, OperatorAssoc assoc,int procedure) {}

