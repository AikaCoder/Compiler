package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexNode;
import org.SeuCompiler.SeuLex.LexNode.LexOperator;

import java.util.*;


public class LexRegex {
    private final String rawStr;
    private final ArrayList<LexNode> postfixExpression;
    private final ArrayList<LexNode> standardExpression;

    /**
     * 通过Regex构造器初始化Regex
     * @param builder regex构造器
     */
    public LexRegex(LexRegexBuilder builder) throws SeuCompilerException {
        this.rawStr  = builder.getRegexWithoutAlias();
        ArrayList<LexNode> list = builder.rawStrToList(this.rawStr);
        standardExpression = new ArrayList<>(list);
        postfixExpression = builder.turnToSuffix(builder.addDots(list));
    }

    public String getRawStr(){
        return rawStr;
    }

    /**
     * @return 得到后缀表达式列表
     */
    public ArrayList<LexNode> getPostfixExpression() {
        return postfixExpression;
    }

    public String getPostfixStr(){
        return TurnToPrintedString(this.postfixExpression);
    }

    public String getStandardExpressionStr(){
        return TurnToPrintedString(this.standardExpression);
    }

    private String TurnToPrintedString(List<LexNode> list){
        StringBuilder builder = new StringBuilder();
        for(LexNode node : list){
            builder.append(node.getPrintedCharacter());
        }
        return builder.toString();
    }
}
