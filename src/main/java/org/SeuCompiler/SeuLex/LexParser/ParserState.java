package org.SeuCompiler.SeuLex.LexParser;

enum ParserState {
    InRegexAliasPart,   //正则别名部分 在第一个%%前面
    InCopyPart,         //直接复制部分, 在%{ %}内
    InRegexActionPart,  //正则-行为部分 在%% %%内
    InCCodePart,      //用户子程序部分, 在%%后
}
