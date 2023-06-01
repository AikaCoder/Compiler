import org.SeuCompiler.SeuLex.SeuLex;
import org.SeuCompiler.Yacc.SeuYacc;
import org.junit.Test;

public class SeuCompilerTest {
    @Test
    public void testC99(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/c99/result/");
        lex.analyseLex("./doc/Test/c99/c99.l");

        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/c99/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/c99/c99.y");
    }

    @Test
    public void testCalculator(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/Calculator/result/");
        lex.analyseLex("./doc/Test/Calculator/calculator.l");

        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/Calculator/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/Calculator/calculator.y");
    }

    @Test
    public void simpleTest(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/Simple_test/result/");
        lex.analyseLex("./doc/Test/Simple_test/simple_test.l");

        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/Simple_test/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/Simple_test/simple_test.y");
    }
}
