package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.LexCodeEnum;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MatchedInsideSquareBracket extends MatchedItems {

    private final int ASCII_MIN = 32;
    private final int ASCII_MAX = 126;  //可用ASCII范围, 用于替换[^abc]

    /**
     * 匹配方括号[]内的结果
     *
     * @param input 待匹配字符串
     */
    public MatchedInsideSquareBracket(String input) {
        super(input, Pattern.compile("(?<!\\\\)\\[(\\\\.|[^\\\\\\[\\]])*]"));
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     * - [MN0-2a-cAK] -> (M|N|0|1|2|a|b|c|A|K)
     * - [^abc1-2] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc12
     * - 注意, 在这里 反斜杠`\` 和 减号`-` 需要在前面加上转写符`\`
     * @return 范围展开后的正则表达式
     */
    public String expand() {
        String insideStr = StringUtils.strip(rawStr, "[]");
        Set<Character> expandResSet = new HashSet<>();
        boolean hasNeg = false;
        int startPos = 0;
        if (insideStr.charAt(0) == '^') {
            hasNeg = true;
            startPos = 1;
        }
        boolean transferFlag = false;
        boolean rangeFlag = false;
        Character lastChar = null;
        for(int i = startPos; i < insideStr.length(); i++){
            char ch = insideStr.charAt(i);
            if(ch == '\\'){
                transferFlag = true;
                continue;
            }
            if(ch == '-' && !transferFlag && lastChar!=null){
                rangeFlag = true;
                continue;
            }
            if(rangeFlag){
                if((int)lastChar > ch){
                    SeuCompilerException e = new SeuCompilerException(LexCodeEnum.WRONG_LEX_RANGE_GRAMMAR);
                    e.setOtherInfo("范围展开 ["+lastChar+"-"+ch+"] 错误, 因为后者ASCII顺序更小.");
                    throw e;
                }
                for(int j = (int)lastChar; j <= ch; j++)
                    expandResSet.add((char)j);  //可以重复添加

                lastChar = null;
                rangeFlag = false;
                continue;
            }
            if(transferFlag)
                transferFlag = false;//相当于延迟一个字符

            expandResSet.add(ch);
            lastChar = ch;
        }
        StringBuilder expandResBuilder = new StringBuilder("(");
        Iterator<Character> iterator = expandResSet.iterator();
        expandResBuilder.append(iterator.next());
        while (iterator.hasNext()){
            expandResBuilder.append('|');
            expandResBuilder.append(iterator.next());
        }
        expandResBuilder.append(')');
        return expandResBuilder.toString();
    }
}
