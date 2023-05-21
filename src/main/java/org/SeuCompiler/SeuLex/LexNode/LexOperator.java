package org.SeuCompiler.SeuLex.LexNode;

import org.jetbrains.annotations.Nullable;

public enum LexOperator {
    ADD('+', 3, true),
    STAR('*', 3, true),
    QUESTION('?', 3, true),
    DOT('·', 2, false), //不是ASCII字符
    OR('|', 1, false),
    LEFT_BRACKET('(',0, false),
    RIGHT_BRACKET(')',0,false),
    ;
    private final Character character;
    private final Integer priority;
    private final Boolean isUnary;  //是否为1元操作符
    LexOperator(Character ch, Integer priority, Boolean isUnary){
        this.character = ch;
        this.priority = priority;
        this.isUnary = isUnary;
    }

    public Character getCharacter(){
        return this.character;
    }

    public boolean isUnary(){
        return this.isUnary;
    }

    public static @Nullable LexOperator getByChar(char ch){
        for(LexOperator value : LexOperator.values()){
            if(value.getCharacter() == ch)
                return value;
        }
        return null;
    }

    public static boolean isOperator(String ch){
        for(LexOperator value : LexOperator.values()){
            if(value.getCharacter().toString().equals(ch))
                return true;
        }
        return false;
    }

    public static boolean isOperator(Character ch){
        for(LexOperator value : LexOperator.values()){
            if(value.getCharacter() == null) continue;
            if(value.getCharacter().equals(ch))
                return true;
        }
        return false;
    }


    public boolean priorTo(LexOperator another){
        return this.priority > another.priority;
    }
}
