import org.SeuCompiler.Yacc.YaccParser.YaccParser;
import org.junit.Test;

public class YaccTest {
    @Test
    public void yaccTest(){
        //YaccParser yacc = new YaccParser("E:\\Desktop\\YaccTest2.y");
        YaccParser yacc = new YaccParser("D:\\Stone\\JAVA\\IDEA\\SEUCompiler\\doc\\Yacc\\c99.y");
        //YaccParser yacc = new YaccParser("E:\\Desktop\\c99_test.y");

        //String _infoPart = yacc.getInfoPart();
        String copyPart =yacc.getCopyPart();
        //String producerPart = yacc.getProducers();

        String userCodePart = yacc.getUserCodePart();
        System.out.print(copyPart);
        System.out.print(userCodePart);
    }
}
