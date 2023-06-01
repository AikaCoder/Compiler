package org.SeuCompiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.SeuCompiler.SeuLex.SeuLex;
import org.SeuCompiler.Yacc.SeuYacc;
import java.io.* ;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class SeuCompiler {

    @Parameter(names = {"--lexPath", "-L"}, description = "The path of lex file (absolute or relative)\n", required = true)
    String lexPath;

    @Parameter(names = {"--yaccPath", "-Y"}, description = "The path of yacc file (absolute or relative)\n", required = true, help = true)
    String yaccPath;

    @Parameter(names = "--resultDir", description = "Specifying the output directory (absolute or relative), default: .\\result\\\n", help = true)
    String resultDirStr  = ".\\result\\";

    @Parameter(names = "--resultName", description = "Specifying the result exe name\n", help = true)
    String resultName = null;

    @Parameter(names = "--debug", description = "Determine whether the output .exe file print debug information [1/0]\n", help = true)
    int debug = 0;

    @Parameter(names = "--visualize", description = "Whether print DFAs into result file[1/0]\n", help = true)
    int visualize = 0;

    @Parameter(names = "--help", help = true, description = "Get help\n")
    private boolean help;

    public static void main(String[] argv){
        SeuCompiler compiler = new SeuCompiler();
        JCommander jCmdr = JCommander.newBuilder()
                .addObject(compiler)
                .build();
        jCmdr.setProgramName("Seu Compiler");
        try{
            jCmdr.parse(argv);
            if(compiler.help){
                jCmdr.usage();
                return;
            }
        }catch (ParameterException parameterException ) {
            // 为了方便使用，同时输出exception的message
            System.out.printf(parameterException.toString() + "\r\n");
            jCmdr.usage();
        }
        compiler.run();
    }

    public void run(){
        assert (this.debug == 1 || this.debug == 0);
        assert (this.visualize == 1 || this.visualize == 0);
        File resultDir = new File(this.resultDirStr);
        assert resultDir.isDirectory();
        if(this.resultName == null)
            this.resultName = getYaccName(yaccPath)+".exe";

        SeuLex lex = new SeuLex();
        lex.setDebugMode(this.debug == 1);
        lex.setResultDir(this.resultDirStr);
        lex.setVisualize(this.visualize == 1);
        lex.analyseLex(this.lexPath);

        SeuYacc yacc = new SeuYacc();
        yacc.setDebugMode(this.debug == 1);
        yacc.setResultDirStr(this.resultDirStr);
        yacc.analyzeYacc(this.yaccPath);


        try {
            String compileResult = execCmd("gcc " + lex.getLexCFile().getName() + " " + yacc.getYaccCFile().getName() + " -o " + resultName, resultDir);
            if(compileResult.equals(""))
                System.out.println("compile success");
            else{
                int errorIndex = compileResult.indexOf("error");
                System.out.println(compileResult.substring(errorIndex));
            }
        } catch (Exception e){
            System.out.println("Compile error: \n"+e);
        }
    }

    /**
     * 执行系统命令, 返回执行结果
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    private String execCmd(String cmd, File dir) throws Exception {
        StringBuilder result = new StringBuilder();

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(cmd, null, dir);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 读取输出
            String line;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }

        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        return result.toString();
    }
    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
    public static String getYaccName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            int slash = filename.lastIndexOf(File.separator);
            if (dot >-1) {
                return filename.substring(slash+1, dot);
            }
        }
        return filename;
    }
}
