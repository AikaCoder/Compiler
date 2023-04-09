package org.SeuCompiler.SeuLex.Regex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LexRegex {
    private final String rawRegex;    //原始正则
    private final String simpleRegex; //简单正则, 只有`|` `*` `.`运算和`()`
    private String expressionNodeStack; //最后将表达式转换为后缀表达式, 并以栈的形式存储

    /**
     * 用户自定义的用于简化替换得正则表达式, 需要提前读入
     * 例如 digital [0-9]
     */
    private static HashMap<String, String> lexRegexSubstitutions = new HashMap<>();


    /**
     * 构造函数, 放入的是原始正则
     * @param rawRegex 原始正则表达式
     */
    public LexRegex(String rawRegex){
        this.rawRegex = rawRegex;
        this.simpleRegex = addDots(expandRange(substituteRegex(rawRegex)));
        this.expressionNodeStack = turnToPostFix(simpleRegex);
    }

    /**
     * @return 原正则表达式
     */
    public String getRawRegex(){
        return rawRegex;
    }

    /**
     * 只有优先级为 `*` > `·` > `|` 的三个运算符, 以及括号()
     * 例如 a·b*·(c|d)·e
     * @return 最简正则表达式
     */
    public String getSimpleRegex(){
        return simpleRegex;
    }

    /**
     * 得到表最简表达式的后缀形式, 栈形式存储, 例如 (a|b)·(b·c)* --> ab|bc·*· 栈顶是a
     * @return 后缀表达式
     */
    public String getExpressionNodeStack() {
        return expressionNodeStack;
    }

    /**
     * 将在第一部分定义的, 用于简化替代的正则表达式添加到LexRegex类, 应该在调用构造函数之前使用
     * 例如:
     *  digital   [0-9]
     *  H         [a-fA-F0-9]
     * @param name 用于替换的别名, 例如 digital
     * @param regexStr 对应的正则表达式, 例如[0-9]
     */
    public static void addLexRegexSubstitutions(String name, String regexStr){
        String nameWithBrace = "{"+name+"}";
        lexRegexSubstitutions.put(nameWithBrace, regexStr);
    }

    /**
     * 将使用到的, 前面所定义的lexRegex别名进行替代, 按规则, 别名需要放在{}内
     * 例如 {digital} -> [0-9]
     * @return 返回替代后正则表达式
     */
    private String substituteRegex(String input){
        MatchedInsideQuotation insideQuotation = new MatchedInsideQuotation(input);
        MatchedInsideBrace insideBrace = new MatchedInsideBrace(input);
        MatchedInsideSquareBracket insideSquareBracket = new MatchedInsideSquareBracket(input);
        //确保替换的别名不是在`""`或`[]`内
        insideBrace.removeItemContainedIn(insideQuotation);
        insideBrace.removeItemContainedIn(insideSquareBracket);
        return  insideBrace.SubstituteByMap(lexRegexSubstitutions);
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     *  - [0-3] -> (0|1|2|3)
     *  - [abc] -> (a|b|c)
     *  - [^abc] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc
     * @return 范围展开后的正则表达式
     */
    private String expandRange(String input){
        MatchedInsideQuotation insideQuotation = new MatchedInsideQuotation(input);
        MatchedInsideSquareBracket insideSquareBracket = new MatchedInsideSquareBracket(input);
        //确保[]不是在{}内
        insideSquareBracket.removeItemContainedIn(insideQuotation);
        return insideSquareBracket.expand();
    }

    /**
     * 连续字符间加乘点算符, 例如
     * ab*(c|d)e -> a·b*·(c|d)·e
     * 优先级为 * > · > |
     * @return 加入乘点算符后的正则表达式
     */
    private String addDots(String str){
        return str; //todo
    }

    /**
     * 将中缀表达式转换为后缀表达式
     * @return 后缀表达得正则表达式
     */
    private String turnToPostFix(String str){
        return str;
    }

}
