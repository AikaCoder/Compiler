package org.SeuCompiler.Yacc.LR1Analyzer;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * LR1相关
 * LR1单条产生式
 * lhs->rhs {action}
 */
public record LR1Producer(int lhs, List<Integer> rhs, String action) {}
