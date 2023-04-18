import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.Regex.LexRegex;
import org.junit.Test;

public class RegexTest {

    @Test
    public void testPositive(){
        try {
            LexRegex regex = new LexRegex("[ab0-2MCA-C\\[\\]]\"TEST\"\\\"");
            System.out.println(regex.getRawStr());
            System.out.println(regex.getStandardExpressionStr());
            System.out.println(regex.getPostfixStr());
        } catch (SeuCompilerException e){
            System.out.println("错误码: "+e.getCode());
            System.out.println("错误描述: "+e.getDescription());
            System.out.println("其他信息: "+e.getOtherInfo());
        }
    }

    @Test
    public void testNegativeExpand(){
        try {
            LexRegex regex = new LexRegex("[^abc]");
            System.out.println(regex.getRawStr());
            System.out.println(regex.getStandardExpressionStr());
            System.out.println(regex.getPostfixStr());
        } catch (SeuCompilerException e){
            System.out.println("错误码: "+e.getCode());
            System.out.println("错误描述: "+e.getDescription());
            System.out.println("其他信息: "+e.getOtherInfo());
        }
    }


}
