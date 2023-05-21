package org.SeuCompiler;

import org.SeuCompiler.SeuLex.SeuLex;

public class main {
    public static void main(String args[]){
        SeuLex lex = new SeuLex();
        String res = lex.analyseLex("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\lex\\c99\\c99.l");
        System.out.print(res);
    }
}
