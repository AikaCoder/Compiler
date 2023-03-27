package org.sj.compiler;
import org.sj.compiler.lex.LexRegex;

/**
 * 测试类, 包含对各种子功能的测试
 */
public class Test {
    public static void TestRegex(){
        LexRegex.addLexRegexSubstitutions("D", "[0-9]");
        LexRegex.addLexRegexSubstitutions("L", "[a-zA-Z_]");
        LexRegex testRegex = new LexRegex("{L}({L}|\"{D}\"[{}])*");
        System.out.println(testRegex.getRawRegex());
        System.out.println(testRegex.getSimpleRegex());
    }
}
