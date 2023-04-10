package org.SeuCompiler.SeuLex.Regex;

enum LexOperator {
    UNSPECIFIED,
    ADD('+', 3, true),
    STAR('*', 3, true),
    QUESTION('?', 3, true),
    DOT('·', 2, false),
    OR('|', 1, false),
    LEFT_BRACKET('(',0, false),
    RIGHT_BRACKET(')',0,false),
    ;
    private final Character character;
    private int priority = -1;
    private boolean isUnary = false;  //是否为1元操作符
    LexOperator(Character ch, int priority, boolean isUnary){
        this.character = ch;
        this.priority = priority;
        this.isUnary = isUnary;
    }
    LexOperator(){
        character = null;
    }

    public Character getCharacter(){
        return this.character;
    }

    public boolean isUnary(){
        return this.isUnary;
    }

    public static LexOperator getByChar(char ch){
        for(LexOperator value : LexOperator.values()){
            if(value.getCharacter() == ch)
                return value;
        }
        return UNSPECIFIED;
    }

    public static boolean isOperator(Character ch){
        for(LexOperator value : LexOperator.values()){
            if(value.getCharacter() == ch)
                return true;
        }
        return false;
    }


    public boolean priorTo(LexOperator another){
        return this.priority > another.priority;
    }
}