package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.RegexErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexNode;
import org.SeuCompiler.SeuLex.LexNode.LexOperator;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LexRegexBuilder {
    private StringBuilder rawRegexBuilder = new StringBuilder();            //最原始的regex
    private final StringBuilder aliasBuilder = new StringBuilder();
    private final StringBuilder regexWithoutAliasBuilder = new StringBuilder();   //替换掉别名后的regex

    private final Map<String, String>  aliasRegexMap;

    private State stateNow = State.Normal;

    //todo 转义符的优先级是一个比较头疼的问题, 它状态应该比其他State优先级高, 后续可能需要优化
    private boolean afterSlash = false;

    /**
     * 初始化, 需要提供regex别名的映射
     * @param aliasRegexMap 别名 -> regex 映射.
     */
    public LexRegexBuilder(Map<String, String>  aliasRegexMap){
        this.aliasRegexMap = aliasRegexMap;
    }

    /**
     * 无别名, 直接构造
     * @param regexWithoutAlias 无别名Regex字符串
     */
    public LexRegexBuilder(String regexWithoutAlias){
        this.aliasRegexMap = null;
        this.rawRegexBuilder = new StringBuilder(regexWithoutAlias);
    }

    public String getRawRegex() {
        return rawRegexBuilder.toString();
    }

    public String getRegexWithoutAlias() {
        return regexWithoutAliasBuilder.toString();
    }

    public LexRegex build() throws SeuCompilerException {
        System.out.println("building --- \t" +this.getRawRegex());
        System.out.println("no alias --- \t" + this.getRegexWithoutAlias());
        LexRegex newLex = new LexRegex(this);
        System.out.println("postfix: --- \t" + newLex.getPostfixStr() +'\n');
        return newLex;
    }

    /**
     * 读取字符构建原始Regex和别名替换后的Regex.
     * @param ch 字符, 读到不在引号或括号内的空格时终止
     * @return 如果成果进行状态转移, 则返回新状态, 如果在Normal状态读到空格, 则返回null
     */
    public State swiftAndBuildRaw(char ch) throws SeuCompilerException {
        if(afterSlash) {
            afterSlash = false;
            rawRegexBuilder.append(ch);
            regexWithoutAliasBuilder.append(ch);
            return this.stateNow;
        }

        switch (this.stateNow){
            case Normal -> {
                switch (ch){
                    case ' ','\t' -> {
                        return null;
                    }
                    case '{' ->{
                        this.stateNow  = State.InBrace;
                        rawRegexBuilder.append(ch);
                        return this.stateNow;
                    }
                    //在中括号`[]`, 引号`""`内, 和反斜杠`\`后 的花括号`{}`可能不表示别名替换
                    case '[' ->this.stateNow  = State.InSquare;
                    case '\"' ->this.stateNow = State.InQuote;
                    case '\\' ->afterSlash = true;
                }
                regexWithoutAliasBuilder.append(ch);
            }
            case InBrace -> {
                if(ch == '}') {
                    String alias = aliasBuilder.toString();
                    aliasBuilder.setLength(0);
                    if(aliasRegexMap == null){
                        throw new SeuCompilerException(RegexErrorCode.not_find_alias, "别名映射表为空");
                    }
                    String replaceRegex = aliasRegexMap.get(alias);
                    if(replaceRegex == null)
                        throw new SeuCompilerException(RegexErrorCode.not_find_alias, "没有对应正则的别名为 "+alias);
                    if(replaceRegex.contains("{")||replaceRegex.contains("}")){
                        //别名替代的内容里仍然包含`{, }`, 需要迭代替换
                        LexRegexBuilder regexBuilder = new LexRegexBuilder(this.aliasRegexMap);
                        for(int i = 0;i<replaceRegex.length();i++) {
                            if(regexBuilder.swiftAndBuildRaw(replaceRegex.charAt(i)) == null)
                                break;
                        }
                        replaceRegex = regexBuilder.getRegexWithoutAlias();
                    }
                    regexWithoutAliasBuilder.append(replaceRegex);

                    this.stateNow = State.Normal;
                    rawRegexBuilder.append(ch);
                    return this.stateNow;
                }

                this.aliasBuilder.append(ch);
            }
            case InSquare -> {
                if(ch == '\\') afterSlash = true;
                else if (ch == ']') this.stateNow = State.Normal;
                this.regexWithoutAliasBuilder.append(ch);
            }
            case InQuote -> {
                if(ch == '\\') afterSlash = true;
                else if(ch == '\"') this.stateNow = State.Normal;
                this.regexWithoutAliasBuilder.append(ch);
            }
        }

        rawRegexBuilder.append(ch);
        return this.stateNow;
    }

    /**
     * 将字符串转换为Regex节点序列
     * @param str 原始字符
     * @return Regex序列
     */
    ArrayList<LexNode> rawStrToList(@NotNull String str) throws SeuCompilerException {
        StringBuffer strBuffer = new StringBuffer();
        State stateNow = State.Normal;
        ArrayList<LexNode> list = new ArrayList<>();

        for(int i = 0;i<str.length();i++){
            char ch = str.charAt(i);
            switch (stateNow){
                case Normal -> {
                    switch (ch){
                        case '\\' -> stateNow = State.AfterSlash;
                        case '[' -> stateNow = State.InSquare;
                        case '"' -> stateNow = State.InQuote;
                        case ']' -> throw new SeuCompilerException(RegexErrorCode.no_corresponding_former, "Regex: " + str);
                        case '^', '-', '$', '>' -> throw new SeuCompilerException(RegexErrorCode.unexpected_character, "字符: "+ch+ ", Regex: "+str);
                        case '%', '<', '{' -> throw new SeuCompilerException(RegexErrorCode.no_support_function);
                        case '.' -> list.add(new LexNode(SpecialChar.ANY));
                        default -> {
                            if(LexOperator.isOperator(ch)) list.add(new LexNode(LexOperator.getByChar(ch)));
                            else list.add(new LexNode(ch));
                        }
                    }
                }
                case AfterSlash -> {
                    if(ch == 't') list.add(new LexNode('\t'));
                    else if(ch == 'n') list.add(new LexNode('\n'));
                    else if(ch == 'r') list.add(new LexNode('\r'));
                    else list.add(new LexNode(ch));
                    stateNow = State.Normal;
                }
                case InSquare -> {
                    if(ch == ']' && str.charAt(i-1)!='\\'){
                        list.addAll(expand(strBuffer));
                        strBuffer.setLength(0);
                        stateNow = State.Normal;
                    }
                    else strBuffer.append(ch);

                }
                case InQuote -> {
                    if(ch =='"') stateNow = State.Normal;
                    else list.add(new LexNode(ch));
                }}
        }
        if(stateNow != State.Normal)
            throw new SeuCompilerException(RegexErrorCode.unknown_regex_error, "Regex: "+str);
        return list;
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     * - [MN0-2a-cAK] -> (M|N|0|1|2|a|b|c|A|K)
     * - [^abc1-2] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc12
     * - 注意, 在这里 反斜杠`\` 和 减号`-` 需要在前面加上转写符`\`
     * @param buffer 字符串缓存
     * @return Regex字符序列
     */
    ArrayList<LexNode> expand(@NotNull StringBuffer buffer) throws SeuCompilerException {
        String insideStr = buffer.toString();
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
                if(lastChar == null){
                    throw new SeuCompilerException(RegexErrorCode.unknown_regex_error, "展开"+insideStr+"时出错.");
                }
                if((int)lastChar > ch){
                    throw new SeuCompilerException(
                            RegexErrorCode.range_expand_wrong,
                            "范围展开 ["+lastChar+"-"+ch+"] 错误, 因为后者ASCII顺序更小."
                    );
                }
                for(int j = (int)lastChar; j <= ch; j++)
                    expandResSet.add((char)j);  //可以重复添加

                lastChar = null;
                rangeFlag = false;
                continue;
            }
            if(transferFlag) {
                if(ch == 'n') ch = '\n';
                else if(ch == 't') ch = '\t';
                else if(ch == 'r') ch = '\r';
            }

            expandResSet.add(ch);
            lastChar = ch;
        }

        if(hasNeg){
            Set<Character> complementSet = new HashSet<>();
            int asciiMin = 32;  //space
            int asciiMax = 126; //~
            for(int i = asciiMin; i<= asciiMax; i++){
                if(expandResSet.contains((char)i)){
                    continue;
                }
                complementSet.add((char)i);
            }
            if(!expandResSet.contains('\n')) complementSet.add('\n');
            if(!expandResSet.contains('\t')) complementSet.add('\t');
            if(!expandResSet.contains('\r')) complementSet.add('\r');
            expandResSet = complementSet;
        }
        List<Character> expandList = expandResSet.stream().sorted().toList();
        ArrayList<LexNode> list = new ArrayList<>();
        list.add(new LexNode(LexOperator.LEFT_BRACKET));
        Iterator<Character> iterator = expandList.iterator();
        list.add(new LexNode(iterator.next()));
        while(iterator.hasNext()){
            list.add(new LexNode(LexOperator.OR));
            list.add(new LexNode(iterator.next()));
        }
        list.add(new LexNode(LexOperator.RIGHT_BRACKET));
        return list;
    }

    /**
     * 连续字符间加乘点算符, 例如
     * ab*(c|d)e -> a·b*·(c|d)·e
     * 优先级为 * > · > |
     * @param list Lex字符节点序列
     * @return 加点后的字符序列
     */
    ArrayList<LexNode> addDots(ArrayList<LexNode> list){
        ArrayList<LexNode> resList = new ArrayList<>(list);
        ListIterator<LexNode> iterator = resList.listIterator();
        while (iterator.hasNext()){
            LexNode node1 = iterator.next();
            LexNode node2;
            if(iterator.hasNext())
                node2 = iterator.next();
            else
                return resList;
            iterator.previous();
            if(
                    !( node1.getOperator() == LexOperator.LEFT_BRACKET
                            || node1.getOperator() == LexOperator.OR
                            || node2.getOperator() == LexOperator.OR
                            || node2.getOperator() == LexOperator.RIGHT_BRACKET
                            || node2.getOperator() == LexOperator.STAR
                            || node2.getOperator() == LexOperator.QUESTION
                            || node2.getOperator() == LexOperator.ADD
                    )) iterator.add(new LexNode(LexOperator.DOT));
        }
        return resList;
    }

    /**
     * 得到表最简表达式的后缀形式, 栈形式存储, 例如 (a|b)·(b·c)* --> ab|bc·*· 栈顶是a
     * @param list Lex符号结点序列
     * @return 后缀形式Lex符号结点序列
     */
    ArrayList<LexNode> turnToSuffix(@NotNull ArrayList<LexNode> list) throws SeuCompilerException {
        Stack<LexNode> stack = new Stack<>();
        ArrayList<LexNode> resList = new ArrayList<>();

        for (LexNode node : list) {
            if (node.isCharacter()) {
                resList.add(node);
                continue;
            }

            LexOperator nodeOp = node.getOperator();
            if (stack.isEmpty() || nodeOp == LexOperator.LEFT_BRACKET) {
                stack.add(node);
            } else if (nodeOp == LexOperator.RIGHT_BRACKET) {
                while (!stack.empty()) {
                    if (stack.peek().getOperator() == LexOperator.LEFT_BRACKET)
                        break;
                    resList.add(stack.pop());
                }
                if (stack.isEmpty())
                    throw new SeuCompilerException(RegexErrorCode.no_corresponding_former, "正则" + regexWithoutAliasBuilder + "括号不匹配");
                stack.pop();
            } else if (nodeOp.isUnary()) {
                resList.add(node);
            } else if (nodeOp.priorTo(stack.peek().getOperator())) {
                stack.add(node);
            } else {
                while (!stack.empty()) {
                    if (nodeOp.priorTo(stack.peek().getOperator()))
                        break;
                    resList.add(stack.pop());
                }
                stack.add(node);
            }
        }

        resList.addAll(stack);
        return resList;
    }

    enum State {
        Normal,     //一般状态
        AfterSlash,     //需转义, 在\后面
        InSquare,     //需扩展, 在[]内
        InQuote,    //在引号""内
        InBrace,    //在花括号{}内
    }
}
