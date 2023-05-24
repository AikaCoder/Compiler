package org.SeuCompiler.SeuLex.LexNode;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LexNode {
    private final Boolean isOperator;
    private final LexOperator operator;
    private final LexChar lexChar;

    public LexNode(char ch){
        isOperator = false;
        this.operator = null;
        this.lexChar = new LexChar(ch);
    }

    public LexNode(LexOperator operator){
        this.isOperator = true;
        this.operator = operator;
        this.lexChar = null;
    }

    public LexNode(SpecialChar specialChar){
        this.isOperator = false;
        this.operator = null;
        this.lexChar = new LexChar(specialChar);
    }

    public boolean isCharacter(){
        return !isOperator;
    }

    public LexOperator getOperator() {
        return operator;
    }

    /**
     * 得到可以打印的字符, 不可见的字符如'\t'将转写成"\\t", 如果spChar和character都是null, 则返回"null"
     * 如果是和运算符的重名的字符, 则会在其两边加上飘号以作区分, 比如"*" -> "`*`"
     * @return 可打印(可见)字符串
     */
    public String getPrintedCharacter(){
        if(isOperator) {
            assert operator != null;
            return operator.getCharacter().toString();
        }
        assert this.lexChar != null;
        String ch = this.lexChar.getPrintedString();
        if(this.isCharacter() && LexOperator.isOperator(ch))
            return "`"+ch+"`";
        else return ch;
    }
    public LexChar getLexChar(){
        return lexChar;
    }

    public boolean equalsTo(LexOperator operator){
        if(!isOperator) return false;
        return this.operator == operator;
    }

    public boolean equalsTo(Character character){
        if(isOperator) return false;
        assert this.lexChar != null;
        return this.lexChar.equalsTo(character);
    }
}
