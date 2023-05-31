import org.SeuCompiler.SeuLex.SeuLex;
import org.SeuCompiler.Yacc.SeuYacc;
import org.junit.Test;

public class SeuCompilerTest {
    @Test
    public void calculator(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/c99/result/");
        lex.analyseLex("./doc/Test/c99/c99.l");

        SeuYacc yacc = new SeuYacc();
        yacc.setResultDirStr("./doc/Test/c99/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/c99/c99.y");
    }
}
