import org.SeuCompiler.Yacc.CodeGenerator;
import org.SeuCompiler.Yacc.LR1Analyzer.*;
import org.SeuCompiler.Yacc.SeuYacc;
import org.SeuCompiler.Yacc.YaccParser.*;
import org.junit.Test;

public class YaccTest {
    @Test
    public void yaccParserTest(){
        // YaccParser yacc = new YaccParser("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\Yacc\\c99.y");
        YaccParser yacc = new YaccParser("./doc/Test/Calculator/calculator.y");
        String copyPart =yacc.getCopyPart();

        String userCodePart = yacc.getUserCodePart();
        System.out.print(copyPart);
        System.out.print(userCodePart);
    }

    @Test
    public void yaccAnalyzerTest(){
        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/Simple_test/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/Simple_test/simple_test.y");
    }

    @Test
    public void yaccWholeTest(){
        SeuYacc yacc = new SeuYacc();
        //yacc.setResultDirStr("./doc/Test/Yacc/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/Yacc/YaccTest2.y");
    }

    @Test
    public void yaccC99Test(){
        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/c99/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/c99/c99.y");
    }
}
