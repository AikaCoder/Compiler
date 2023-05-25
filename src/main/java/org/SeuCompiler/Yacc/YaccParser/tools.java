package org.SeuCompiler.Yacc.YaccParser;

import java.util.HashMap;
import java.util.Map;


public class tools {
    // ========= yacc用到的正则 =========
    //在java中需要对{}进行转义
    //正则表达式怎么写？？？
    static final String PATTERN_BLOCK_PRODUCER = "(\\w+)\\s*\\n\\s+:(\\s+(.+?)(\\{[\\s\\S]*?\\})?\\n)(\\s+\\|\\s+(.+?)(\\{[\\s\\S]*?\\})?\\n)*\\s+;";
    // $1为LHS，$3为首个RHS，$4为动作代码（带大括号）
    static final String PATTERN_INITIAL_PRODUCER = "(\\w+)\\n\\s+:(\\s+(.+?)(\\{[\\s\\S]*?\\})?\\n)";
    // $2为RHS，$3为动作代码（带大括号）
    static final String PATTERN_CONTINUED_PRODUCER = "(\\s+\\|\\s+(.+?)(\\{[\\s\\S]*?\\})?\\n)";
    private static final Map<String, String> table = new HashMap<String, String>();

    static{
        table.put("\\n", "\n");
        table.put("\\t", "\t");
        table.put("\\r", "\r");
        table.put("\\(", "(");
        table.put("\\)", ")");
        table.put("\\[", "[");
        table.put("\\]", "]");
        table.put("\\+", "+");
        table.put("\\-", "-");
        table.put("\\*", "*");
        table.put("\\?", "?");
        table.put("\\\"", "\"");
        table.put("\\.", ".");
        table.put("\\'", "'");
        table.put("\\|", "|");
        table.put("\\\\", "\\");
    }

    public static String cookString( String str){
        String ret = " ";
        boolean bSlash = false;//记录是否遇到\\

        for(String c:str.split("")){
            if (bSlash) {
                String ch = '\\' + c;
                //hasOwnProperty()方法用于检测一个对象是否含有特定的自身属性，返回一个布尔值
                ret += table.getOrDefault(ch, ch);
                bSlash = false;
            }
            else if (c.equals("\\"))
                bSlash = true;
            else
                ret += c;
        };
        return ret;
    }
}
