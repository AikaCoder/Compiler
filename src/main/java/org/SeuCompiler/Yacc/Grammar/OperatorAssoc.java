package org.SeuCompiler.Yacc.Grammar;

public enum OperatorAssoc {
    LEFT("left"),
    RIGHT("right"),
    NON("non");

    private final String assoc;

    OperatorAssoc(String assoc) {
        this.assoc = assoc;
    }

    //通过静态函数来完成枚举类的实例化
    //需要import这个静态函数
    public static OperatorAssoc sTy(String assoc) {
        return switch (assoc) {
            case "left" -> OperatorAssoc.LEFT;
            case "right" -> OperatorAssoc.RIGHT;
            //break;
            default -> OperatorAssoc.NON;
            //break;
        };
    }

    public String getAssoc() {
        return assoc;
    }
}
