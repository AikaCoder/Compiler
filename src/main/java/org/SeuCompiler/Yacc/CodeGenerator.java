package org.SeuCompiler.Yacc;

import org.SeuCompiler.Yacc.Grammar.LR1Producer;
import org.SeuCompiler.Yacc.LR1Analyzer.ACTIONTableCell;
import org.SeuCompiler.Yacc.LR1Analyzer.GrammarSymbol;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1Analyzer;
import org.SeuCompiler.Yacc.YaccParser.YaccParser;

public class CodeGenerator {
    //生成Token编号供Lex使用
    public static String generateYTABH(LR1Analyzer analyzer) {
        StringBuilder res = new StringBuilder();
        res.append("#ifndef Y_TAB_H_\n");
        res.append("#define Y_TAB_H_\n");
        res.append("#define WHITESPACE -10\n");
        for (int i = 0; i < analyzer.getSymbols().size(); i++) {
            if (analyzer.getSymbols().get(i).type().getType().equals("sptoken") || analyzer.getSymbols().get(i).type().getType().equals("token")) {
                //res.append("#define " + analyzer.symbols.get(i).getContent() + " " + i + "\n");
                res.append("#define ").append(analyzer.getSymbols().get(i).content()).append(" ").append(i).append("\n");
            }
        }
        res.append("#endif\n");
        return res.toString();
    }

    //声明了四个外部变量和函数
    public static String genExtern() {
        //yyin：一个指向 FILE 类型的指针；
        //yytext：一个字符数组；
        //yylex()：一个返回整数类型的函数；
        //yyout：一个指向 FILE 类型的指针。
        return """
                extern FILE *yyin;
                extern char yytext[];
                extern int yylex();
                extern FILE *yyout;
                """;
    }

    public static String genSymbolChartClass() {
        //定义了一个名为 SymbolChart 的结构体，以及两个函数
        //value(char *name, char *type)：根据给定的名称和类型查找符号表中是否存在对应的变量，并返回其值。
        //createSymbol(char *name, char *type, int size)：在符号表中创建一个新的变量，并分配内存地址。
        return """
                struct SymbolChart {
                    int symbolNum;
                    char *name[SYMBOL_CHART_LIMIT];
                    char *type[SYMBOL_CHART_LIMIT];
                    char *value[SYMBOL_CHART_LIMIT];
                } symbolChart = {.symbolNum = 0};
                char *value(char *name, char *type) {
                    for (int i = 0; i < symbolChart.symbolNum; i++) {
                        if (strcmp(name, symbolChart.name[i]) == 0 && strcmp(type, symbolChart.type[i]))
                            return symbolChart.value[i];
                    }
                    return NULL;
                }
                void createSymbol(char *name, char *type, int size) {
                    if (symbolChart.symbolNum >= SYMBOL_CHART_LIMIT) throw(ArrayUpperBoundExceeded);
                    if (value(name, type) != NULL) throw(SomethingRedefined);
                    char *addr = (char *)malloc(32 * sizeof(char));
                    itoa(memoryAddrCnt, addr, 10);
                    memoryAddrCnt += size;
                    symbolChart.name[symbolChart.symbolNum] = (char *)malloc(strlen(name) + 1);
                    symbolChart.type[symbolChart.symbolNum] = (char *)malloc(strlen(type) + 1);
                    symbolChart.value[symbolChart.symbolNum] = (char *)malloc(strlen(addr) + 1);
                    strcpy(symbolChart.name[symbolChart.symbolNum], name);
                    strcpy(symbolChart.type[symbolChart.symbolNum], type);
                    strcpy(symbolChart.value[symbolChart.symbolNum], addr);
                    symbolChart.symbolNum++;
                    free(addr);
                }
                """;
    }

    /**
     * 定义了一个名为 Node 的结构体，以及两个变量和一个函数：
     * nodes[SYMBOL_CHART_LIMIT]：一个指针数组，用于存储所有的节点。
     * int nodeNum = 0：记录当前已经创建的节点数。
     * reduceNode(int num)：根据给定的子节点数量，生成一个新的父节点，并将子节点添加到父节点中。
     */
    public static String genNode() {
        return """
                struct Node {
                    char *value;
                    char *yytext;
                    struct Node *children[SYMBOL_CHART_LIMIT];
                    int childNum;
                } *nodes[SYMBOL_CHART_LIMIT];
                int nodeNum = 0;
                void reduceNode(int num) {
                    struct Node *newNode = (struct Node *)malloc(sizeof(struct Node));
                    char *nonterminal = curToken;
                    if (nonterminal == NULL) nonterminal = curAttr;
                    newNode->childNum = num;
                    newNode->value = (char *)malloc(strlen(nonterminal) + 1);
                    newNode->yytext = (char *)malloc(strlen(curAttr) + 1);
                    strcpy(newNode->value, nonterminal);
                    strcpy(newNode->yytext, curAttr);
                    for (int i = 1; i <= num; i++) {
                        newNode->children[num-i] = nodes[nodeNum-i];
                        nodes[nodeNum-i] = NULL;
                    }
                    nodeNum = nodeNum - num;
                    nodes[nodeNum++] = newNode;
                }
                """;
    }

    /**
     * updateSymbolAttr(int popNum)：弹出栈顶的 popNum 个元素，并将它们的属性保存到 symbolAttr 数组中。
     * stateStackPop(int popNum)：弹出栈顶的 popNum 个状态，并返回栈顶状态。
     * stateStackPush(int state)：将指定的状态压入状态栈中。
     * reduceTo(char *nonterminal)：生成一个非终结符节点，并将该节点的值赋为 nonterminal。
     */
    public static String genFunctions() {
        return """
                void updateSymbolAttr(int popNum) {
                    char *temp = (char *)malloc(strlen(curAttr) + 1);
                    strcpy(temp, curAttr);
                    while (popNum--) {
                        if (symbolAttrSize == 0) throw(ArrayLowerBoundExceeded);
                        free(symbolAttr[--symbolAttrSize]);
                    }
                    if (symbolAttrSize >= SYMBOL_ATTR_LIMIT) throw(ArrayUpperBoundExceeded);
                    symbolAttr[symbolAttrSize] = (char *)malloc(strlen(temp) + 1);
                    strcpy(symbolAttr[symbolAttrSize++], temp);
                }
                int stateStackPop(int popNum) {
                    while (popNum--) {
                        if (stateStackSize == 0) throw(ArrayLowerBoundExceeded);
                        stateStackSize--;
                    }
                    if (stateStackSize == 0) return YACC_NOTHING;
                    else return stateStack[stateStackSize - 1];
                }
                void stateStackPush(int state) {
                    if (stateStackSize >= STATE_STACK_LIMIT) throw(ArrayUpperBoundExceeded);
                    stateStack[stateStackSize++] = state;
                }
                void reduceTo(char *nonterminal) {
                    if (curToken != NULL) {
                        free(curToken);
                        curToken = NULL;
                    }
                    curToken = (char *)malloc(strlen(nonterminal) + 1);
                    strcpy(curToken, nonterminal);
                }
                """;
    }

    //C代码内容
    public String genPresetContent(LR1Analyzer analyzer) {
        //StringBuilder sb = new StringBuilder();
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <string.h>\n" +
                "#include \"yy.tab.h\"\n" +
                "#define STACK_LIMIT 1000\n" +
                "#define SYMBOL_CHART_LIMIT 10000\n" +
                "#define SYMBOL_ATTR_LIMIT 10000\n" +
                "#define STATE_STACK_LIMIT 10000\n" +
                "#define YACC_ERROR -1\n" +
                "#define YACC_NOTHING -2\n" +
                "#define YACC_ACCEPT -42\n" +
                genExceptions() +
                genExtern() +
                "int stateStack[STACK_LIMIT];\n" +
                "int stateStackSize = 0;\n" +
                "int debugMode = 0;\n" +
                //int EOFIndex = ${analyzer._getSymbolId(SpSymbol.END)};
                //END: { type: 'sptoken', content: 'SP_END' } as GrammarSymbol,
                String.format("int EOFIndex = %d;\n", analyzer.getSymbolId(new GrammarSymbol(GrammarSymbol.GrammarSymbolType.SPTOKEN, "SP_END"))) +
                "char *symbolAttr[SYMBOL_ATTR_LIMIT];\n" +
                "int symbolAttrSize = 0;\n" +
                "char *curAttr = NULL;\n" +
                "char *curToken = NULL;\n" +
                "FILE *treeout = NULL;\n" +
                "int memoryAddrCnt = 0;\n" +
                genSymbolChartClass() +
                genNode() +
                genFunctions();
    }

    /**
     * C语言中一些异常处理相关的函数的实现
     * 当数组下标超出上限时调用
     * 当数组下标超出下限时调用
     * 当变量或函数被重复定义时调用
     * 当语法错误发生时调用
     * 抛出异常并退出程序
     */
    public String genExceptions() {
        return """
                void ArrayUpperBoundExceeded(void) {
                        printf("Array upper bound exceeded!");
                }
                void ArrayLowerBoundExceeded(void) {
                        printf("Array lower bound exceeded!");
                }
                void SomethingRedefined(void) {
                        printf("Something redefined!");
                }
                void SyntaxError(void) {
                        printf("Syntax error!");
                }
                void throw(void (*func)(void)) {
                    atexit(func);
                    exit(EXIT_FAILURE);
                }
                """;
    }

    /**
     * 这段代码的作用是生成一个LR分析表。
     * 它使用LR1Analyzer对象作为输入，然后遍历状态和符号，根据分析器中的ACTION和GOTO表中的信息来填充一个二维数组。
     * 每个单元格包含两个整数值：action和target。
     */
    public String genTable(LR1Analyzer analyzer) {
        StringBuilder code = new StringBuilder();
        int stateCount = analyzer.getDfa().getStates().size();
        int symbolCount = analyzer.getSymbols().size();
        code.append("struct TableCell {\n")
                .append("    int action;\n")
                .append("    int target;\n")
                .append("};\n")
                .append("struct TableCell table[").append(stateCount)
                .append("][").append(symbolCount).append("] = {");
        for (int state = 0; state < stateCount; state++) {
            int nonCnt = 0;
            int nonnonCnt = 0;
            for (int symbol = 0; symbol < symbolCount; symbol++) {
                int action = -1;
                int target = 0;
                GrammarSymbol sym = analyzer.getSymbols().get(symbol);
                if (sym.type().getType().equals("nonterminal")) {
                    action = 1;
                    target = analyzer.getGOTOTable().get(state).get(nonCnt++);
                } else {
                    ACTIONTableCell lr1Action = analyzer.getACTIONTable().get(state).get(nonnonCnt);
                    switch (lr1Action.type().getType()) {
                        case "shift":
                            action = 2;
                            target = lr1Action.data();
                            break;
                        case "reduce":
                            action = 3;
                            target = lr1Action.data();
                            break;
                        case "acc":
                            action = 4;
                            break;
                        default:
                            action = 0;
                    }
                    nonnonCnt++;
                }
                code.append("(struct TableCell){").append(action)
                        .append(", ").append(target).append("},");
            }
        }
        code.setLength(code.length() - 1);
        code.append("};\n");
        return code.toString();
    }

    /**
     * 生成dealWith的函数
     * 该函数根据传入的符号执行不同的动作，包括移动状态机、将当前符号加入节点等。
     */
    public String genDealWithFunction(LR1Analyzer analyzer) {
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("int dealWith(int symbol) {\n");
        codeBuilder.append("  if (symbol == WHITESPACE) return YACC_NOTHING;\n");
        codeBuilder.append("  if (stateStackSize < 1) throw(ArrayLowerBoundExceeded);\n");
        codeBuilder.append("  if (debugMode) printf(\"Received symbol no.%d\\n\", symbol);\n");
        codeBuilder.append("  int state = stateStack[stateStackSize - 1];\n");
        codeBuilder.append("  struct TableCell cell = table[state][symbol];\n");
        codeBuilder.append("  switch(cell.action) {\n");
        codeBuilder.append("    case 0:\n");
        codeBuilder.append("      return YACC_NOTHING;\n");
        codeBuilder.append("    case 4:\n");
        codeBuilder.append("      return YACC_ACCEPT;\n");
        codeBuilder.append("    case 1:\n");
        codeBuilder.append("      if (debugMode) printf(\"Go to state %d\\n\", cell.target);\n");
        codeBuilder.append("      stateStackPush(cell.target);\n");
        codeBuilder.append("      return YACC_NOTHING;\n");
        codeBuilder.append("    case 2:\n");
        codeBuilder.append("      stateStackPush(cell.target);\n");
        codeBuilder.append("      if (debugMode) printf(\"Shift to state %d\\n\", cell.target);\n");
        codeBuilder.append("      curAttr = yytext;\n");
        codeBuilder.append("      nodes[nodeNum] = (struct Node *)malloc(sizeof(struct Node));\n");
        codeBuilder.append("      nodes[nodeNum]->value = (char *)malloc(sizeof(char) * strlen(curAttr));\n");
        codeBuilder.append("      nodes[nodeNum]->yytext = NULL;\n");
        codeBuilder.append("      strcpy(nodes[nodeNum]->value, curAttr);\n");
        codeBuilder.append("      nodes[nodeNum]->childNum = 0;\n");
        codeBuilder.append("      nodeNum++;\n");
        codeBuilder.append("      updateSymbolAttr(0);\n");
        codeBuilder.append("      return YACC_NOTHING;\n");
        codeBuilder.append("    case 3:\n");
        codeBuilder.append("      if (debugMode) printf(\"Reduce by producer %d\\n\", cell.target);\n");
        codeBuilder.append("      switch (cell.target) {\n");

        for (int i = 0; i < analyzer.getProducers().size(); i++) {
            LR1Producer producer = analyzer.getProducers().get(i);
            codeBuilder.append(String.format("        case %d:\n", i));
            codeBuilder.append("          curAttr = (char *)malloc(1024 * sizeof(char));\n");
            codeBuilder.append("          memset(curAttr, '\\0', sizeof(curAttr));\n");
            codeBuilder.append(actionCodeModified(producer.action(), producer.rhs().size()));
            codeBuilder.append(String.format("          stateStackPop(%d);\n", producer.rhs().size()));
            //codeBuilder.append("          reduceNode(" + producer.getRhs().size() + ");\n");
            //replace with chained 'append()' calls
            codeBuilder.append("          reduceNode(").append(producer.rhs().size()).append(");\n");
            codeBuilder.append(String.format("          updateSymbolAttr(%d);\n", producer.rhs().size()));
            codeBuilder.append("          dealWith(").append(producer.lhs()).append(");\n");
            codeBuilder.append("          return symbol;\n");
        }

        codeBuilder.append("      }\n");
        codeBuilder.append("    default:\n");
        codeBuilder.append("      return symbol;\n");
        codeBuilder.append("  }\n");
        codeBuilder.append("  return YACC_NOTHING;\n");
        codeBuilder.append("}\n");

        return codeBuilder.toString();
    }

    /**
     * 函数接收两个参数：一个字符串类型的action和一个数字类型的producerLen。
     * 函数的作用是解析传入的动作文本，并将其中的变量（以$n形式表示）替换为对应的C语言代码。
     */
    public String actionCodeModified(String action, int producerLen) {
        boolean bslash = false, inSQuot = false, inDQuot = false, dollar = false;
        StringBuilder buffer = new StringBuilder(), ret = new StringBuilder();
        for (int i = 0; i < action.length(); i++) {
            char c = action.charAt(i);
            if (dollar) {
                if (c == '$') {
                    ret.append("curAttr");
                    dollar = false;
                } else if (c >= '0' && c <= '9') {
                    buffer.append(c);
                } else {
                    int num = Integer.parseInt(buffer.toString());
                    if (num < 1 || num > producerLen) {
                        ret.append('$').append(buffer);
                    } else {
                        ret.append(String.format("symbolAttr[symbolAttrSize-%d]", producerLen - num + 1));
                    }
                    ret.append(c);
                    dollar = false;
                    buffer.setLength(0);
                }
            } else {
                if (!inSQuot && !inDQuot && !dollar && c == '$') {
                    dollar = true;
                } else if (!inDQuot && !bslash && c == '\'') {
                    inSQuot = !inSQuot;
                } else if (!inSQuot && !bslash && c == '\"') {
                    inDQuot = !inDQuot;
                } else if (c == '\\') {
                    bslash = !bslash;
                } else {
                    bslash = false;
                }
                if (c != '$') {
                    ret.append(c);
                }
            }
        }
        return ret.toString();
    }

    //返回一个字符串，表示一种用于打印语法树的C语言函数
    public String genPrintTree() {
        return """
                void printTree(struct Node *curNode, int depth) {
                  if (curNode == NULL) return;
                  for (int i = 0; i < depth * 2; i++)
                    fprintf(treeout, " ");
                  fprintf(treeout, "%s", curNode->value);
                  if (curNode->yytext != NULL && strlen(curNode->yytext) > 0)
                    fprintf(treeout, " (%s)", curNode->yytext);
                  if (curNode->childNum < 1) return;
                  fprintf(treeout, " {\\n");
                  for (int i = 0;i < curNode->childNum; i++) {
                    printTree(curNode->children[i], depth+1);
                    if (i+1 < curNode->childNum)
                      fprintf(treeout, ",");
                    fprintf(treeout, "\\n");
                  }
                  for (int i = 0; i < depth * 2; i++)
                    fprintf(treeout, " ");
                  fprintf(treeout, "}");
                }""";
    }

    //返回一个字符串，表示用于解析语法树的yacc解析器函数
    public String genYaccParse(LR1Analyzer analyzer) {
        return "int yyparse() {\n" +
                "  if (yyout == NULL) yyout = stdout;\n" +
                "  int token;\n" +
                "  stateStackPush(" + analyzer.getDfa().getStartStateId() + ");\n" +
                "  while (token != YACC_ACCEPT && (token = yylex()) && token != YACC_ERROR) {\n" +
                "    do {\n" +
                "      token = dealWith(token);\n" +
                "      free(curToken);\n" +
                "      curToken = NULL;\n" +
                "    } while (token >= 0);\n" +
                "  }\n" +
                "  if (token == 0) {\n" +
                "    token = EOFIndex;\n" +
                "    do {\n" +
                "      token = dealWith(token);\n" +
                "    } while (token >= 0);\n" +
                "  }\n" +
                "  strcpy(yytext, curAttr);\n" +
                "  if (token == YACC_ERROR) return 1;\n" +
                "  if (token == YACC_ACCEPT) {\n" +
                "    treeout = fopen(\"yacc.tree\", \"w\");\n" +
                "    printTree(nodes[0], 0);\n" +
                "    fclose(treeout);\n" +
                "    return 0;\n" +
                "  }\n" +
                "  else return 1;\n" +
                "}";
    }

    //返回一个字符串，表示生成的YTABC代码
    public String generateYTABC(YaccParser yaccParser, LR1Analyzer analyzer) {
        String finalCode = "// ===========================================\n" +
                "// |  YTABC generated by seuyacc             |\n" +
                "// |  Visit github.com/z0gSh1u/seu-lex-yacc  |\n" +
                "// ===========================================\n" +
                "#define DEBUG_MODE 0\n" +
                "// * ============== copyPart ================\n";
        finalCode += yaccParser.getCopyPart();
        finalCode += "// * ========== seuyacc generation ============\n";
        finalCode += genPresetContent(analyzer);
        finalCode += genTable(analyzer);
        finalCode += genDealWithFunction(analyzer);
        finalCode += genPrintTree();
        finalCode += genYaccParse(analyzer);
        finalCode += yaccParser.getUserCodePart();
        return finalCode;
    }

}

