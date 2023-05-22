package org.SeuCompiler.Yacc.LR1Analyzer;

// 语法符号
public record GrammarSymbol(GrammarSymbolType type, String content) {
    // 用于GrammarSymbol中表示该段类型
    public enum GrammarSymbolType {
        ASCII("ascii"),
        TOKEN("token"),
        NONTERMINAL("nonterminal"),
        SPTOKEN("sptoken");

        private final String type;

        GrammarSymbolType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
