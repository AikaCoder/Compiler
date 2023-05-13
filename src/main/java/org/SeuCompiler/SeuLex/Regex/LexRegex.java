package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.RegexErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import java.util.*;


public class LexRegex {
    private final String rawStr;
    private final String regexWithoutAlias;
    private final ArrayList<LexCharacterNode> postfixExpression;
    private final ArrayList<LexCharacterNode> standardExpression;


    /**
     * 通过Regex构造器初始化Regex
     * @param builder regex构造器
     */
    public LexRegex(LexRegexBuilder builder) throws SeuCompilerException {
        this.rawStr = builder.getRawRegex();
        this.regexWithoutAlias = builder.getRegexWithoutAlias();
        ArrayList<LexCharacterNode> list = builder.rawStrToList(this.regexWithoutAlias);
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
    public ArrayList<LexCharacterNode> getPostfixExpression() {
        return postfixExpression;
    }

    public String getPostfixStr(){
        StringBuilder builder = new StringBuilder();
        for(LexCharacterNode node : postfixExpression){
            Character ch = node.getCharacter();
            if(!node.isOperator() && LexOperator.isOperator(ch)){
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    public String getStandardExpressionStr(){
        StringBuilder builder = new StringBuilder();
        for(LexCharacterNode node : standardExpression){
            Character ch = node.getCharacter();
            if(!node.isOperator() && LexOperator.isOperator(ch)){
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }
}
