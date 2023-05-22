package org.SeuCompiler.Yacc.YaccParser;

import java.util.ArrayList;
import java.util.List;

/**
 * YaccParser相关
 * YaccParser读出的产生式
 */
public record YaccParserProducer(String lhs, List<String> rhs, List<String> actions) {}