import org.SeuCompiler.SeuLex.SeuLex;
import org.junit.Test;

public class LexTest {
    @Test
    public void testLex(){
        SeuLex lex = new SeuLex();
        lex.setDebugMode(true);
        lex.setResultDir("./doc/Test/Calculator/result/");
        lex.analyseLex("./doc/Test/Calculator/calculator.l");
    }
}
