package org.SeuCompiler.SeuLex.Regex;

import java.util.regex.Pattern;

class MatchedInsideQuotation extends MatchedItems {
    /**
     * 返回所有在""内的匹配项
     *
     * @param input   待匹配字符串
     */
    public MatchedInsideQuotation(String input){
        super(input, Pattern.compile("(?<!\\\\)\"(\\\\.|[^\\\\\"\\n])*\""));
    }
}
