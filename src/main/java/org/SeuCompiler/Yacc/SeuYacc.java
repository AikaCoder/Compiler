package org.SeuCompiler.Yacc;

import lombok.NoArgsConstructor;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1Analyzer;
import org.SeuCompiler.Yacc.YaccParser.YaccParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

@NoArgsConstructor
public class SeuYacc {
    private String resultDirStr = null;
    private boolean debugMode = false;
    private File yaccTabHFile;
    private File yaccCFile;

    public void setResultDirStr(String resultDirStr) {
        this.resultDirStr = resultDirStr;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public File getYaccTabHFile() {
        return yaccTabHFile;
    }

    public File getYaccCFile() {
        return yaccCFile;
    }

    public void analyzeYacc(String filePath){
        try{
            File yaccFile = new File(filePath);
            String yaccFileName = getFileNameNoEx(yaccFile.getName());
            File resultDir = new File(Objects.requireNonNullElseGet(
                    resultDirStr,
                    () -> yaccFile.getParent() + File.separator + yaccFileName + "_result" + File.separator)
            );
            if(!resultDir.exists())
                if(!resultDir.mkdirs()) throw new IOException("无法创建目录: "+resultDir);

            System.out.println("parsing .y file ...");
            YaccParser parser = new YaccParser(filePath);
            System.out.println("building LR1 ...");
            LR1Analyzer analyzer = new LR1Analyzer(parser, false);

            System.out.println("generating yy.tab.h file ...");
            this.yaccTabHFile = new File(resultDir, "yy.tab.h");
            BufferedWriter tabHOut = new BufferedWriter(new FileWriter(yaccTabHFile));
            tabHOut.write(CodeGenerator.generateYTABH(analyzer));
            tabHOut.flush();
            tabHOut.close();
            System.out.println("print yy.tab.h of" + yaccFileName+" in " + yaccTabHFile);

            System.out.println("generating yacc.c file ...");
            String yyacRes = CodeGenerator.generateYTABC(parser, analyzer, this.debugMode);
            this.yaccCFile = new File(resultDir, yaccFileName+".yacc.c");
            BufferedWriter yaccOut = new BufferedWriter(new FileWriter(yaccCFile));
            yaccOut.write(yyacRes);
            yaccOut.flush();
            yaccOut.close();
            System.out.println("print yacc.c of" + yaccFileName+" in " + yaccCFile);

            Visualizer visualizer = new Visualizer();
            visualizer.setResultDir(this.resultDirStr);
            visualizer.visualizeACTIONGOTOTable(analyzer, false);

        }catch (Exception e){
            System.out.println("Other error: "+e);
        }
    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if (dot >-1) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
