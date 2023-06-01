package org.SeuCompiler.Yacc.Grammar;

import java.util.Objects;

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
    }
    public boolean isType(GrammarSymbolType typeIn){
        return this.type == typeIn;
    }
    public static GrammarSymbol newNonTerminal(String contentIn){
        return new GrammarSymbol(GrammarSymbolType.NONTERMINAL, contentIn);
    }

    /**
     * 返回ASCII Symbol, 如果是可打印的, 则content为可打印字符, 如果为不可打印的, 则content为"[UNPRINTABLE]"
     * @param ascii ASCII序号.
     * @return 类型为ASCII的GrammarSymbol.
     */
    public static GrammarSymbol newASCII(int ascii){
        assert ascii >=0 && ascii <= 127;
        if(ascii<32 || ascii == 127)
            return new GrammarSymbol(GrammarSymbolType.ASCII, "[UNPRINTABLE]");
        return new GrammarSymbol(GrammarSymbolType.ASCII, String.valueOf((char)ascii));
    }

    public static GrammarSymbol newASCII(String charStr){
        return new GrammarSymbol(GrammarSymbolType.ASCII, charStr);
    }

    public static GrammarSymbol newToken(String contentIn){
        return new GrammarSymbol(GrammarSymbolType.TOKEN, contentIn);
    }
}
