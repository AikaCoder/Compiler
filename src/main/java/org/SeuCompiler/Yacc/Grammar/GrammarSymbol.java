package org.SeuCompiler.Yacc.Grammar;

import java.util.Objects;

/**
 * 语法符号, 包括ascii, token, nonTerminal, SP_END, SP_EPSILON
 * @param type 语法符号类型
 * @param content   语法符号具体内容
 */
public record GrammarSymbol(GrammarSymbolType type, String content) {
    // 用于GrammarSymbol中表示该段类型
    public enum GrammarSymbolType {
        ASCII,
        TOKEN,
        NON_TERMINAL,
        SP_END,
        SP_EPSILON;
    }
    public boolean isType(GrammarSymbolType typeIn){
        return this.type == typeIn;
    }

    public boolean isSpecialToken(){
        return this.type == GrammarSymbolType.SP_END || type == GrammarSymbolType.SP_EPSILON;
    }

    public String getString(){
        if(this.type == GrammarSymbolType.ASCII) return "'"+content+"'";
        else return content;
    }

    public static GrammarSymbol newNonTerminal(String contentIn){
        return new GrammarSymbol(GrammarSymbolType.NON_TERMINAL, contentIn);
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

    public static GrammarSymbol newASCII(Character charStr){
        return new GrammarSymbol(GrammarSymbolType.ASCII, String.valueOf(charStr));
    }

    public static GrammarSymbol newToken(String contentIn){
        return new GrammarSymbol(GrammarSymbolType.TOKEN, contentIn);
    }

    public static GrammarSymbol newSpEnd(){
        return new GrammarSymbol(GrammarSymbolType.SP_END, "SP_END");
    }

    public static GrammarSymbol newSpEpsilon(){
        return new GrammarSymbol(GrammarSymbolType.SP_EPSILON, "SP_EPSILON");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GrammarSymbol that)) return false;
        return type == that.type && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }
}
