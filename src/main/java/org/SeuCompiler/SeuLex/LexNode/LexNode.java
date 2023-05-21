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

    public boolean isOperator(){
        return isOperator;
    }

    public LexOperator getOperator() {
        return operator;
    }

    public String getPrintedCharacter(){
        if(isOperator) return operator.getCharacter().toString();
        return this.lexChar.getString();
    }
    public LexChar getLexChar(){
        return lexChar;
    }

    public boolean equals(LexOperator operator){
        if(!isOperator) return false;
        return this.operator == operator;
    }

    public boolean equals(Character character){
        if(isOperator) return false;
        return this.lexChar.equals(character);
    }
}
