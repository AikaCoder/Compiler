package org.SeuCompiler.Yacc.Grammar;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * LR1相关
 * LR1单条产生式
 * lhs->rhs {action}
 */
public record LR1Producer(GrammarSymbol lhs, List<GrammarSymbol> rhs, String action) {
    @Override
    public String toString(){
        String lhsStr = lhs.content();
        StringBuilder rhsStr = new StringBuilder();
        for (GrammarSymbol r : rhs) rhsStr.append(r.getString()).append(" ");
        return lhsStr + "->" + rhsStr;
    }
}
