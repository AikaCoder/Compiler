package org.SeuCompiler.SeuLex.Regex;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

class MatchedInsideSquareBracket extends MatchedItems {

    private final int ASCII_MIN = 32;
    private final int ASCII_MAX = 126;  //可用ASCII范围, 用于替换[^abc]

    /**
     * 匹配方括号[]内的结果
     *
     * @param input   待匹配字符串
     */
    public MatchedInsideSquareBracket(String input) {
        super(input, Pattern.compile("(?<!\\\\)\\[(\\\\.|[^\\\\\\[\\]])*]"));
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     *  - [0-3] -> (0|1|2|3)
     *  - [abc] -> (a|b|c)
     *  - [^abc] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc
     * @return 范围展开后的正则表达式
     */
    public String expand(){
        String insideStr = StringUtils.strip(rawStr, "[]");
        boolean hasNeg = false;
        boolean hasMinus = false;
        int beginPos = 0;

        if(insideStr.charAt(0) == '^'){
            hasNeg = true;
            beginPos = 1;
        }
        for(int i = beginPos; i < insideStr.length(); i++){
            Character ch = insideStr.charAt(i);
        }
        return "temp";
    }
}
