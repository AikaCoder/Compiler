package org.sj.compiler.lex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LexRegex {
    private final String rawRegex;    //原始正则
    private final String simpleRegex; //简单正则, 只有`|` `*` `.`运算和`()`
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>(); //最后将表达式转换为后缀表达式, 并以栈的形式存储

    /**
     * `(?<!\\)"(\\.|[^\\"\n])*"` 表示被引号`"`包裹的内容, 且引号前没有转义符: `\`
     * 原理解释:
     *  - `(?<!\\)`表示匹配项前面不是`\`
     *  - `(\\.|[^\\"\n])*`表示引号内部不能有单独的`\`, `"`或是`\n`, 但可以有连在一起的`\"`, `\t`, `\n`等用`\`转义的字符
     */
    private final Pattern insideQuotatinsPattern = Pattern.compile("(?<!\\\\)\"(\\\\.|[^\\\\\"\\n])*\"");

    /**
     * (?<!\\)\[(\\.|[^\\\[\\\]\n])*\] 表示被方括号[]包裹的内容, 且方括号前没有转义符: \
     */
    private final Pattern insideSquareBracketsPattern = Pattern.compile("(?<!\\\\)\\[(\\\\.|[^\\\\\\[\\]])*]");

    /**
     * (?<!\\)\{(\\.|[^\\\{\\\}\n])*\} 表示被花括号{}包裹的内容, 且花括号前没有转义符: \
     */
    private final Pattern insideBracePattern = Pattern.compile("(?<!\\\\)\\{(\\\\.|[^\\\\{}])*}");
    private final int ASCII_MIN = 32;
    private final int ASCII_MAX = 126;  //可用ASCII范围

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
    public Stack<ExpressionNode> getExpressionNodeStack() {
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
        AllMatchedItems insideQuotationItems = new AllMatchedItems(input, insideQuotatinsPattern);
        AllMatchedItems insideSquareBracketsItems = new AllMatchedItems(input, insideSquareBracketsPattern);
        AllMatchedItems insideBraceItems = new AllMatchedItems(input, insideBracePattern);
        //确保替换的别名不是在`""`或`[]`内
        insideBraceItems.removeItemContainedIn(insideQuotationItems);
        insideBraceItems.removeItemContainedIn(insideSquareBracketsItems);
        return  insideBraceItems.SubstituteByMap(input, lexRegexSubstitutions);
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     *  - [0-3] -> (0|1|2|3)
     *  - [abc] -> (a|b|c)
     *  - [^abc] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc
     * @return 范围展开后的正则表达式
     */
    private String expandRange(String input){
        return input; //todo
    }

    /**
     *  - a+ -> aa*
     *  - (a|bc)+ -> (a|bc)(a|bc)*
     *  - b? -> (\e|b)    (\e为自定义空字符)
     * @param input
     * @return
     */
    private String replaceRepeatOperator(String input){
        return input;
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
    private Stack<ExpressionNode> turnToPostFix(String str){
        return new Stack<>(); //todo
    }

    /**
     * 正则匹配结果
     * @param content 匹配到的字符串
     * @param head 匹配结果在原字符串开始的位置, pos >= head
     * @param tail 匹配结果在原字符串结束的位置, pos < tail
     */
    private record MatchedItem(String content, int head, int tail) {
        public boolean insideRangeOf(int pos) {
                return pos >= head && pos < tail;
        }
        public boolean insideRangeOf(MatchedItem another){
            boolean res = another.head() < head && tail < another.tail();
            return res;
        }
    }

    private class AllMatchedItems{
        private List<MatchedItem> allItem = new ArrayList<>();

        public List<MatchedItem> getAllItem() {
            return allItem;
        }

        /**
         * 对给定的字符串和正则规则, 返回所有的匹配结果
         * @param input 待匹配字符串
         * @param pattern 匹配正则
         */
        public AllMatchedItems(String input, Pattern pattern){
            Matcher matcher = pattern.matcher(input);
            List<MatchedItem> res = new ArrayList<>();
            while(matcher.find()){
                MatchedItem t = new MatchedItem(matcher.group(0), matcher.start(), matcher.end());
                res.add(t);
            }
            allItem = res;
        }

        /**
         * 对所有匹配项, 如果其在给定container的某一匹配项的范围内, 则将其移除
         * @param container 给定的另外一个匹配结果
         */
        public void removeItemContainedIn(AllMatchedItems container){
            Iterator<MatchedItem> iterator = allItem.iterator();
            while(iterator.hasNext()){
                MatchedItem former = iterator.next();
                for(MatchedItem latter : container.getAllItem()){
                    if(former.insideRangeOf(latter)){
                        iterator.remove();
                    }
                }
            }
        }

        /**
         * 将给定字符串中匹配项 按说给映射关系, 映射到一般的正则表达式.
         * //todo
         * 耦合度比较高, 可能需要用新的方法尽量解耦
         * //todo
         * 可能出现替代错误, 需要处理错误
         * @param input 待替换字符串
         * @param map 替代映射关系
         */
        public String SubstituteByMap(String input, HashMap<String, String> map){
            int offset = 0;
            String result = input;
            for(MatchedItem item : allItem){
                String former = result.substring(0, item.head+offset);
                String latter = result.substring(item.tail+offset);
                result = former + map.get(item.content) + latter;
                offset += map.get(item.content).length() - item.content.length();
            }
            return result;
        }
    }
}
