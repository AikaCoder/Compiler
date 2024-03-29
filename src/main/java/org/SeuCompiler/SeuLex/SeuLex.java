package org.SeuCompiler.SeuLex;

import lombok.NoArgsConstructor;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.FiniteAutomata.*;
import org.SeuCompiler.SeuLex.LexNode.LexChar;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;
import org.SeuCompiler.SeuLex.LexParser.LexParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
@NoArgsConstructor
public class SeuLex {
    private DFA miniDFA;
    private List<State> stateList;   //将集合转换成列表, 便于后续在转换矩阵中确定位置
    private String resultDirStr = null;
    private boolean visualize = true;
    private boolean debugMode = false;
    private File lexCFile;

    /**
     * 设置结果的输入文件夹.
     * 如果不指定, 或设为null, 则会默认在`.l`文件目录下创建同名+`_result`目录存放结果
     * @param dir 输出路径
     */
    public void setResultDir(String dir){
        this.resultDirStr = dir;
    }

    /**
     * 选择最后的结果是否打开debug模式
     * @param mode debug模式, 默认为false, 关闭.
     */
    public void setDebugMode(boolean mode){
        this.debugMode = mode;
    }

    /**
     * @param visualize 选择是否输出DFA, NFA
     */
    public void setVisualize(boolean visualize) {
        this.visualize = visualize;
    }


    public File getLexCFile() {
        return lexCFile;
    }

    public void analyseLex(String filePath) {
        try {
            File file = new File(filePath);
            String lexFileName = getFileNameNoEx(file.getName());
            File resultDir = new File(Objects.requireNonNullElseGet(
                    resultDirStr,
                    () -> file.getParent() + File.separator + lexFileName + "_result" + File.separator)
            );
            this.lexCFile = new File(resultDir, lexFileName+".lex.c");

            if(!resultDir.exists())
                if(!resultDir.mkdirs()) throw new IOException("无法创建目录: "+resultDir);

            LexParser parser = new LexParser(filePath);
            Visualizer visualizer = null;
            if(visualize)
                visualizer = new Visualizer(resultDir);

            NFA nfa = new NFA(parser);
            DFA dfa = new DFA(nfa);
            this.miniDFA = dfa.minimize();
            if (visualize) {
                assert visualizer != null;
                visualizer.print(nfa, lexFileName+"_NFA");
                visualizer.print(dfa, lexFileName+"_DFA");
                visualizer.print(this.miniDFA, lexFileName+"_miniDFA");
            }


            System.out.println("generating C code...");
            this.stateList = new ArrayList<>(miniDFA.getStates());
            Collections.swap(stateList, 0, stateList.indexOf(this.miniDFA.getStartState()));   //把start State 换到第一个位置

            String res =
                    CopyPartBegin +
                        parser.getCopyPart()+
                    LexGenerationPartBegin +
                        preConfigs() + genTransformMatrix() + genSwitchCase() +
                        genYYLex(genCaseAction()) + yyless + yymore +
                    CCodePartBegin +
                        parser.getCCodePart();

            BufferedWriter out = new BufferedWriter(new FileWriter(lexCFile));
            out.write(res);
            out.flush();
            out.close();
            System.out.println("print lex.c of"+lexFileName+" in "+ lexCFile);

        } catch (SeuCompilerException e) {
            if (e.getLineNum() != null) System.out.println("error at line " + e.getLineNum());
            System.out.println("错误码: "+e.getCode() + ": " + e.getDescription());
            if (e.getOtherInfo() != null) System.out.println(e.getOtherInfo() + '\n');
        } catch (IOException e){
            System.out.println("IO error: "+ e);
        } catch (Exception e){
            System.out.println("Other error: "+e);
        }
    }
    private String genTransformMatrix(){
        StringBuilder res = new StringBuilder(String.format("const int _trans_mat[%d][128] = {\n", this.miniDFA.getStates().size()));
        for(State s : stateList){
            String[] targets = new String[128];
            Arrays.fill(targets, "-1"); //表示无法到达
            if(this.miniDFA.getTransforms().containsKey(s)){
                Map<LexChar, State> transform = this.miniDFA.getTransforms().get(s);
                String otherTarget = "-1";      //记录读入 ANY 或 OTHER 后到达的状态
                for (Map.Entry<LexChar, State> entry : transform.entrySet()){
                    LexChar lexChar = entry.getKey();
                    State end = entry.getValue();
                    if(lexChar.equalsTo(SpecialChar.OTHER) || lexChar.equalsTo(SpecialChar.ANY))
                        otherTarget = String.valueOf(this.stateList.indexOf(end));
                    else targets[lexChar.getCharacter()] = String.valueOf(this.stateList.indexOf(end));
                }

                if(!Objects.equals(otherTarget, "-1")){
                    for(int i = 0; i < targets.length; i++){
                        if(Objects.equals(targets[i], "-1")) targets[i] = otherTarget;
                    }
                }
            }
            res.append(String.join(", ", targets)).append(",\n");
        }
        res.append("};\n");
        return res.toString();
    }

    private String genSwitchCase(){
        StringBuilder res = new StringBuilder(String.format("const int _swi_case[%d] = { ", this.stateList.size()));
        for(int i = 0; i<this.stateList.size(); i++){
            if(miniDFA.getAcceptStates().contains(stateList.get(i)))
                res.append(i).append(", ");
            else res.append("-1, ");
        }
        res.append(" };\n");
        return res.toString();
    }

    private String genCaseAction(){
        StringBuilder res = new StringBuilder();
        for(State state : this.miniDFA.getAcceptStates()){
            int index = this.stateList.indexOf(state);
            res.append(String.format(
                """
                        case %d:
                            %s
                            break;
                """
                , index, this.miniDFA.getAcceptActionMap().get(state).code())
            );
        }
        res.append(
            """
                    default:
                        break;
            """
        );
        return res.toString();
    }
    private static String genYYLex(String casePart){
        return yylexUpper+casePart+yylexDown;
    }

    private final static String CopyPartBegin = """
            
            //----------      copy part     -----------
            
            """;
    private final static String CCodePartBegin = """
            
            //----------     C code part     -----------
            
            """;
    private final static String LexGenerationPartBegin = """
            
            //---------- Lex generation part -----------
            
            """;

    private String preConfigs() {
        return String.format(
                """
                        #include <stdio.h>
                        #include <stdlib.h>
                        #include <string.h>
                        #define ECHO fprintf(yyout,"%%s\\n",yytext);
                        
                        #define LEX_DEBUG_MODE %d
                        int yylineno = 1, yyleng = 0;
                        FILE *yyin = NULL, *yyout = NULL;
                        char yytext[1024] = {0};
                        char _cur_buf[1024] = {0};
                        int _cur_char = 0;
                        const int _init_state = 0;
                        int _cur_state = 0, _cur_ptr = 0, _cur_buf_ptr = 0, _lat_acc_state = -1, _lat_acc_ptr = 0;
                        int yywrap();
                        """, this.debugMode?1:0);
    }
    private final static String yyless = """
            void yyless(int n) {
                int delta = strlen(yytext) - n;
                fseek(yyin, -delta, SEEK_CUR);
                FILE *yyinCopy = yyin;
                while (delta--) fgetc(yyinCopy) == '\\n' && yylineno--;
            }
            """;
    private final static String yymore = """
            void yymore() {
                char old[1024];
                strcpy(old, yytext);
                yylex();
                strcpy(yytext, strcat(old, yytext));
            }
            """;
    private final static String yylexUpper = """
            int yylex() {
                int rollbackLines = 0;
                if (yyout == NULL) yyout = stdout;
                if (_cur_char == EOF) {
                    if (yywrap() == 1) return 0;    //YACC_END
                    else {
                        yylineno = 1;
                        yyleng = 0;
                        memset(yytext, 0, sizeof(_cur_buf));
                        memset(_cur_buf, 0, sizeof(_cur_buf));
                        _cur_char = 0;
                        _cur_state = _init_state, _cur_ptr = 0, _cur_buf_ptr = 0;
                        _lat_acc_state = -1, _lat_acc_ptr = 0;
                    }
                }
                while (_cur_state != -1) {
                    _cur_char = fgetc(yyin);
                    if (LEX_DEBUG_MODE) printf("** YYLEX: ** %c | %d\\n", _cur_char, _cur_char);
                    _cur_ptr++;
                    if (_cur_char == '\\n') yylineno++, rollbackLines++;
                    _cur_buf[_cur_buf_ptr++] = _cur_char;
                    _cur_state = _trans_mat[_cur_state][_cur_char];
                    if (_swi_case[_cur_state] != -1) {
                        _lat_acc_state = _cur_state;
                        _lat_acc_ptr = _cur_ptr - 1;
                        rollbackLines = 0;
                    }
                }
                if (_lat_acc_state != -1) {
                    fseek(yyin, _lat_acc_ptr - _cur_ptr + 1, SEEK_CUR);
                    yylineno -= rollbackLines;
                    _cur_ptr = _lat_acc_ptr;
                    _cur_state = 0;
                    _cur_buf[_cur_buf_ptr - 1] = '\\0';
                    memset(yytext, 0, sizeof(yytext));
                    yyleng = strlen(_cur_buf);
                    strcpy(yytext, _cur_buf);
                    if (LEX_DEBUG_MODE) printf("** YYTEXT: ** %s\\n", yytext);
                    memset(_cur_buf, 0, sizeof(_cur_buf));
                    _cur_buf_ptr = 0;
                    int _lat_acc_state_bak = _lat_acc_state;
                    _lat_acc_state = -1;
                    _lat_acc_ptr = 0;
                    switch (_swi_case[_lat_acc_state_bak]) {
                    
                    """;
    private final static String yylexDown= """
                    
                    }
                }
                else return -1; // YACC_ERROR
                return -2;      // YACC_NOTHING
            }
            """;

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
