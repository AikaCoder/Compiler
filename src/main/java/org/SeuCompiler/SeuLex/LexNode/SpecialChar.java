package org.SeuCompiler.SeuLex.LexNode;

import lombok.Getter;

@Getter
public
enum SpecialChar {
    EPSILON("[ε]"),
    ANY("[any]"),     //匹配.lex文件里的`.`, 即表示所有输入
    OTHER("[other]"), //匹配出现ANY情况下, 其他所有有用字符的补集
    ;

    private final String str;   //用于打印

    SpecialChar(String str) {
        this.str = str;
    }

    public LexChar toFAChar(){
        return new LexChar(this);
    }
}
