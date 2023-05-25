import org.SeuCompiler.SeuLex.SeuLex;
import org.junit.Test;

public class LexTest {
    @Test
    public void testLex(){
        SeuLex lex = new SeuLex();
        lex.analyseLex("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\lex\\simple_test.l");
    }
}
