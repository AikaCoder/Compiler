package org.SeuCompiler.SeuLex;

public class LexCode {
    static String genYYLex(String casePart){
        return yylexUpper+casePart+yylexDown;
    }

    final static String CopyPartBegin = """
            
            ----------      copy part     -----------
            
            """;
    final static String CCodePartBegin = """
            
            ----------     C code part     -----------
            
            """;
    final static String LexGenerationPartBegin = """
            
            ---------- Lex generation part -----------
            
            """;

    static final String preConfigs = """
            #include <stdio.h>
            #include <stdlib.h>
            #include <string.h>
            #define ECHO fprintf(yyout,"%%s\\\\n",yytext);
            int yylineno = 1, yyleng = 0;
            FILE *yyin = NULL, *yyout = NULL;
            char yytext[1024] = {0};
            char _cur_buf[1024] = {0};
            int _cur_char = 0;
            const int _init_state = 0;
            int _cur_state = _init_state, _cur_ptr = 0, _cur_buf_ptr = 0, _lat_acc_state = -1, _lat_acc_ptr = 0;
            int yywrap();
            """;
    final static String yyless = """
            void yyless(int n) {
                int delta = strlen(yytext) - n;
                fseek(yyin, -delta, SEEK_CUR);
                FILE *yyinCopy = yyin;
                while (delta--) fgetc(yyinCopy) == '\\\\n' && yylineno--;
            }
            """;
    final static String yymore = """
            void yymore() {
                char old[1024];
                strcpy(old, yytext);
                yylex();
                strcpy(yytext, strcat(old, yytext));
            }
            """;
    final static String yylexUpper = """
            int yylex() {
                int rollbackLines = 0;
                if (yyout == NULL) yyout = stdout;
                if (_cur_char == EOF) {
                    if (yywrap() == 1) return 0;
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
                    if (DEBUG_MODE) printf("** YYLEX: ** %c | %d\\n", _cur_char, _cur_char);
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
                    memset(_cur_buf, 0, sizeof(_cur_buf));
                    _cur_buf_ptr = 0;
                    int _lat_acc_state_bak = _lat_acc_state;
                    _lat_acc_state = -1;
                    _lat_acc_ptr = 0;
                    switch (_swi_case[_lat_acc_state_bak]) {
                    
                    """;
    final static String yylexDown= """
                    
                    }
                }
                else return -1; // error
                return 0;
            }
            """;
}
