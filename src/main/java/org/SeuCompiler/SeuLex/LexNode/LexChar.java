package org.SeuCompiler.SeuLex.LexNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public final class LexChar {
    private final Boolean isSpecial;
    private final SpecialChar spChar;
    private final Character character;

    public LexChar(Character character) {
        this.isSpecial = false;
        this.spChar = null;
        this.character = character;
    }

    public LexChar(SpecialChar spChar) {
        this.isSpecial = true;
        this.spChar = spChar;
        this.character = null;
    }

    public boolean equalsTo(Character character) {
        if (this.isSpecial) return false;
        else return Objects.equals(this.character, character);
    }

    public boolean equalsTo(SpecialChar spChar) {
        if (!this.isSpecial) return false;
        else return this.spChar == spChar;
    }

    /**
     * 得到可以打印的字符, 不可见的字符如'\t'将转写成"\\t", 如果spChar和character都是null, 则返回"null"
     * @return 可打印(可见)字符串
     */
    public String getPrintedString() {
        String res;
        if(isSpecial && this.spChar!=null)
            res = this.spChar.getStr();
        else if (this.character != null) {
            switch (this.character){
                case '\t' -> res = "\\t";
                case '\n' -> res = "\\n";
                case '\r' -> res = "\\r";
                case ' ' -> res = "[space]";
                default -> res = this.character.toString();
            }
        }
        else
            res = "null";
        return res;
    }
}
