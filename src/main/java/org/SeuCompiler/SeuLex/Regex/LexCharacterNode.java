package org.SeuCompiler.SeuLex.Regex;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;

@EqualsAndHashCode
public class LexCharacterNode {
    private final Boolean isOperator;
    private final LexOperator operator;
    private final Character character;

    public LexCharacterNode(char ch){
        isOperator = false;
        this.operator = null;
        this.character = ch;
    }

    public LexCharacterNode(LexOperator operator){
        this.isOperator = true;
        this.operator = operator;
        this.character = null;
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

    public boolean equals(LexOperator operator){
        if(!isOperator) return false;
        return this.operator == operator;
    }

    public boolean equals(Character character){
        if(isOperator) return false;
        return this.character.equals(character);
    }
}
