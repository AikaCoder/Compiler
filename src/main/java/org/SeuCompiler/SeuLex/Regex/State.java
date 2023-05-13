package org.SeuCompiler.SeuLex.Regex;

enum State {
    Normal,     //一般状态
    AfterSlash,     //需转义, 在\后面
    InSquare,     //需扩展, 在[]内
    InQuote,    //在引号""内
    InBrace,    //在花括号{}内
}
