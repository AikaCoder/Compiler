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
        YaccParser yacc = new YaccParser(
                "./doc/Test/Calculator/calculator.y"
        );
        //YaccParser yacc = new YaccParser("E:\\Desktop\\c99.y");
        //YaccParser yacc = new YaccParser("E:\\Desktop\\c99_test.y");
        //String _infoPart = yacc.getInfoPart();
        String _copyPart =yacc.getCopyPart();
        //String _producerPart = yacc.getProducers();
        String _userCodePart = yacc.getUserCodePart();
        //System.out.print(_copyPart);
        //System.out.print(_userCodePart);
        LR1Analyzer analyzer = new LR1Analyzer(yacc, false);
        String tab = CodeGenerator.generateYTABH(analyzer);
        System.out.print(tab);
    }

    @Test
    public void yaccWholeTest(){
        SeuYacc yacc = new SeuYacc();
        //yacc.setResultDirStr("./doc/Test/Yacc/result/");
        yacc.setDebugMode(true);
        yacc.analyzeYacc("./doc/Test/Yacc/YaccTest2.y");
    }
}
