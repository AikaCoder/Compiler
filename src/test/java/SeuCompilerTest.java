import org.SeuCompiler.SeuLex.SeuLex;
import org.junit.Test;

public class SeuCompilerTest {
    @Test
    public void calculator(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/Calculator/result/");
        lex.analyseLex("./doc/Test/Calculator/calculator.l");


    }
}
