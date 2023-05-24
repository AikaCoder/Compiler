package org.SeuCompiler;

import org.SeuCompiler.SeuLex.SeuLex;

import java.io.File;

public class main {
    public static void main(String args[]){
        SeuLex lex = new SeuLex();
        //lex.analyseLex("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\lex\\c99\\c99.l");
        lex.analyseLex("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\lex\\simple_test.l");
        //System.out.print(res);
    }
}
