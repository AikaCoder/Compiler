package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.Getter;

@Getter
public enum SpecialChar {
    EPSILON("[ε]"),
    ANY("[any]"),     //匹配.lex文件里的`.`, 即表示所有输入
    OTHER("[other]"), //匹配出现ANY情况下, 其他所有有用字符的补集
    ;

    private final String str;

    SpecialChar(String str) {
        this.str = str;
    }

    FAString toFAString(){
        return new FAString(this);
    }
}
