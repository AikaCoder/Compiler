package org.SeuCompiler.SeuLex.Regex;

class LexCharacterNode {
    private final Boolean isOperator;
    private LexOperator operator = LexOperator.UNSPECIFIED;
    private Character character = null;

    public LexCharacterNode(char ch){
        isOperator = false;
        this.character = ch;
    }

    public LexCharacterNode(LexOperator operator){
        this.isOperator = true;
        this.operator = operator;
    }
    public boolean isOperator(){
        return isOperator;
    }

    public LexOperator getOperator() {
        return operator;
    }

    public Character getCharacter(){
        if(isOperator) return operator.getCharacter();
        return this.character;
    }
}
