package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.LexErrorCodeEnum;
import org.SeuCompiler.Exception.SeuCompilerException;

import java.util.*;

public class LexRegex {
    private enum State{
        Normal,     //一般状态
        Escape,     //需转义
        Substitute, //需替代
        Expand,     //需扩展
        InQuot,    //在引号内
    }
    private String rawStr;
    private ArrayList<LexCharacterNode> suffixExpression;

    /**
     * 用户自定义的用于简化替换得正则表达式, 需要提前读入
     * 例如 digital [0-9]
     */
    private static HashMap<String, String> lexRegexSubstitutions = new HashMap<>();

    public LexRegex(String str) throws SeuCompilerException {
        this.rawStr = str;

        StringBuffer strBuffer = new StringBuffer();
        State stateNow = State.Normal;
        ArrayList<LexCharacterNode> list = new ArrayList<>();

        for(int i = 0;i<rawStr.length();i++){
            char ch = rawStr.charAt(i);
            switch (stateNow){
                case Normal -> {
                    switch (ch){
                        case '\\' -> stateNow = State.Escape;
                        case '[' -> stateNow = State.Expand;
                        case '{' -> stateNow = State.Substitute;
                        case '"' -> stateNow = State.InQuot;
                        case ']', '}','>' -> throw new SeuCompilerException(LexErrorCodeEnum.NO_CORRESPONDING_FORMER, "Regex: " + rawStr);
                        case '^', '-', '$' -> throw new SeuCompilerException(LexErrorCodeEnum.UNEXPECTED_CHARACTER, "字符: "+ch+ ", Regex: "+rawStr);
                        case '%', '<' -> throw new SeuCompilerException(LexErrorCodeEnum.NO_SUPPORT);
                        default -> {
                            if(LexOperator.isOperator(ch)) list.add(new LexCharacterNode(LexOperator.getByChar(ch)));
                            else list.add(new LexCharacterNode(ch));
                        }
                    }
                }
                case Escape -> {
                    if(ch == 't') list.add(new LexCharacterNode('\t'));
                    else if(ch == 'n') list.add(new LexCharacterNode('\n'));
                    else list.add(new LexCharacterNode(ch));
                    stateNow = State.Normal;
                }
                case Expand -> {
                    if(ch == ']' && str.charAt(i-1)!='\\'){
                        list.addAll(expand(strBuffer));
                        strBuffer.setLength(0);
                        stateNow = State.Normal;
                    }
                    else strBuffer.append(ch);

                }
                case Substitute ->{
                    if(ch == '}'){
                        list.addAll(substitute(strBuffer));
                        strBuffer.setLength(0);
                        stateNow = State.Normal;
                    }
                    else strBuffer.append(ch);
                }
                case InQuot -> {
                    if(ch =='"') stateNow = State.Normal;
                    else list.add(new LexCharacterNode(ch));
                }
            }
        }
        if(stateNow != State.Normal)
            throw new SeuCompilerException(LexErrorCodeEnum.UNKNOWN_LEX_ERROR, "Regex: "+rawStr);

        suffixExpression = turnToSuffix(addDots(list));
    }

    /**
     * 将LexRegex中范围表示进行展开, 例如
     * - [MN0-2a-cAK] -> (M|N|0|1|2|a|b|c|A|K)
     * - [^abc1-2] -> (\s|!|"|...|z) 从ASCII_MIN到ASCII_MAX, 除去abc12
     * - 注意, 在这里 反斜杠`\` 和 减号`-` 需要在前面加上转写符`\`
     * @param buffer 字符串缓存
     * @return Regex字符序列
     */
    private ArrayList<LexCharacterNode> expand(StringBuffer buffer) throws SeuCompilerException {
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
                    throw new SeuCompilerException(LexErrorCodeEnum.UNKNOWN_REGEX_ERROR, "展开"+insideStr+"时出错.");
                }
                if((int)lastChar > ch){
                    SeuCompilerException e = new SeuCompilerException(LexErrorCodeEnum.WRONG_LEX_RANGE_GRAMMAR);
                    e.setOtherInfo("范围展开 ["+lastChar+"-"+ch+"] 错误, 因为后者ASCII顺序更小.");
                    throw e;
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
            expandResSet = complementSet;
        }
        ArrayList<LexCharacterNode> list = new ArrayList<>();
        list.add(new LexCharacterNode(LexOperator.LEFT_BRACKET));
        Iterator<Character> iterator = expandResSet.iterator();
        list.add(new LexCharacterNode(iterator.next()));
        while(iterator.hasNext()){
            list.add(new LexCharacterNode(LexOperator.OR));
            list.add(new LexCharacterNode(iterator.next()));
        }
        list.add(new LexCharacterNode(LexOperator.RIGHT_BRACKET));
        return list;
    }

    /**
     * 将{}内的内容按提前记录的对应关系展开
     * @param buffer 字符串缓存
     * @return Regex字符序列
     */
    private ArrayList<LexCharacterNode> substitute(StringBuffer buffer) throws SeuCompilerException {
        String str = buffer.toString();
        String substituteStr = lexRegexSubstitutions.get(str);
        ArrayList<LexCharacterNode> list = new ArrayList<>();
        if(substituteStr == null)
            throw new SeuCompilerException(LexErrorCodeEnum.NO_CORRESPONDING_USER_DEFINE, "简写"+str+"未找到对应用户定义");
        for(int i = 0; i<substituteStr.length();i++){
            list.add(new LexCharacterNode(substituteStr.charAt(i)));
        }
        return list;
    }


    /**
     * 连续字符间加乘点算符, 例如
     * ab*(c|d)e -> a·b*·(c|d)·e
     * 优先级为 * > · > |
     * @param list Lex字符节点序列
     * @return 加点后的字符序列
     */
    private ArrayList<LexCharacterNode> addDots(ArrayList<LexCharacterNode> list){
        ListIterator<LexCharacterNode> iterator = list.listIterator();
        while (iterator.hasNext()){
            LexCharacterNode node1 = iterator.next();
            LexCharacterNode node2;
            if(iterator.hasNext())
                node2 = iterator.next();
            else
                return list;
            iterator.previous();
            if(
                !( node1.getOperator() == LexOperator.LEFT_BRACKET
                || node1.getOperator() == LexOperator.OR
                || node2.getOperator() == LexOperator.OR
                || node2.getOperator() == LexOperator.RIGHT_BRACKET
                || node2.getOperator() == LexOperator.STAR
                || node2.getOperator() == LexOperator.QUESTION
                || node2.getOperator() == LexOperator.ADD
            )) iterator.add(new LexCharacterNode(LexOperator.DOT));
        }
        return list;
    }

    /**
     * 得到表最简表达式的后缀形式, 栈形式存储, 例如 (a|b)·(b·c)* --> ab|bc·*· 栈顶是a
     * @param list Lex符号结点序列
     * @return 后缀形式Lex符号结点序列
     */
    private ArrayList<LexCharacterNode> turnToSuffix(ArrayList<LexCharacterNode> list) throws SeuCompilerException {
        Stack<LexCharacterNode> stack = new Stack<>();
        ArrayList<LexCharacterNode> resList = new ArrayList<>();

        for (LexCharacterNode node : list) {
            if (!node.isOperator()) {
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
                    throw new SeuCompilerException(LexErrorCodeEnum.NO_CORRESPONDING_USER_DEFINE, "正则" + rawStr + "没有对应的括号");
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

    /**
     * 将在第一部分定义的, 用于简化替代的正则表达式添加到LexRegex类, 应该在调用构造函数之前使用
     * 例如:
     *  digital   [0-9]
     *  H         [a-fA-F0-9]
     * @param name 用于替换的别名, 例如 digital
     * @param regexStr 对应的正则表达式, 例如[0-9]
     */
    public static void addLexRegexSubstitutions(String name, String regexStr){
        lexRegexSubstitutions.put(name, regexStr);
    }

    /**
     * @return 得到后缀表达式列表
     */
    public ArrayList<LexCharacterNode> getSuffixExpression() {
        return suffixExpression;
    }

    public String getRawStr(){
        return rawStr;
    }
    public String getSuffixStr(){
        StringBuilder builder = new StringBuilder();
        for(LexCharacterNode node : suffixExpression){
            Character ch = node.getCharacter();
            if(!node.isOperator() && LexOperator.isOperator(ch)){
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }
}
