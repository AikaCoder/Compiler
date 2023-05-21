package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexNode;
import org.SeuCompiler.SeuLex.LexNode.LexOperator;

import java.util.*;


public class LexRegex {
    private final String rawStr;
    private final String regexWithoutAlias;
    private final ArrayList<LexNode> postfixExpression;
    private final ArrayList<LexNode> standardExpression;


    /**
     * 通过Regex构造器初始化Regex
     * @param builder regex构造器
     */
    public LexRegex(LexRegexBuilder builder) throws SeuCompilerException {
        this.rawStr = builder.getRawRegex();
        this.regexWithoutAlias = builder.getRegexWithoutAlias();
        ArrayList<LexNode> list = builder.rawStrToList(this.regexWithoutAlias);
        standardExpression = new ArrayList<>(list);
        postfixExpression = builder.turnToSuffix(builder.addDots(list));
    }

    public String getRawStr(){
        return rawStr;
    }

    public String getRegexWithoutAlias() {
        return regexWithoutAlias;
    }

    /**
     * @return 得到后缀表达式列表
     */
    public ArrayList<LexNode> getPostfixExpression() {
        return postfixExpression;
    }

    public String getPostfixStr(){
        StringBuilder builder = new StringBuilder();
        for(LexNode node : postfixExpression){
            String ch = node.getPrintedCharacter();
            if(!node.isOperator() && LexOperator.isOperator(ch)){
                builder.append('\\');
            }
            else if(Objects.equals(ch, "\t")) builder.append("\\t");
            else if(Objects.equals(ch, "\n")) builder.append("\\n");
            else if(Objects.equals(ch, "\r")) builder.append("\\r");
            else builder.append(ch);
        }
        return builder.toString();
    }

    public String getStandardExpressionStr(){
        StringBuilder builder = new StringBuilder();
        for(LexNode node : standardExpression){
            String ch = node.getPrintedCharacter();
            if(!node.isOperator() && LexOperator.isOperator(ch)){
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }
}
