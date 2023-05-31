// ===========================================
// |  YTABC generated by seuyacc             |
// |  Visit github.com/z0gSh1u/seu-lex-yacc  |
// ===========================================
#define DEBUG_MODE1
// * ============== copyPart ================
	#define out(...) fprintf(yyout, "%s\n", ##__VA_ARGS__)// * ========== seuyacc generation ============
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "yy.tab.h"
#define STACK_LIMIT 1000
#define SYMBOL_CHART_LIMIT 10000
#define SYMBOL_ATTR_LIMIT 10000
#define STATE_STACK_LIMIT 10000
#define YACC_ERROR -1
#define YACC_NOTHING -2
#define YACC_ACCEPT -42
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
extern FILE *yyin;
extern char yytext[];
extern int yylex();
extern FILE *yyout;
int stateStack[STACK_LIMIT];
int stateStackSize = 0;
int debugMode = 0;
int EOFIndex = 171;
char *symbolAttr[SYMBOL_ATTR_LIMIT];
int symbolAttrSize = 0;
char *curAttr = NULL;
char *curToken = NULL;
FILE *treeout = NULL;
int memoryAddrCnt = 0;
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
    sprintf(addr, "%d",memoryAddrCnt);
    // itoa(memoryAddrCnt, addr, 10);
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
struct TableCell {
    int action;
    int target;
};
struct TableCell table[1][174] = {(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){0, 0},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){1, -1},(struct TableCell){4, 0},(struct TableCell){0, 0},(struct TableCell){1, -1}};
int dealWith(int symbol) {
  if (symbol == WHITESPACE) return YACC_NOTHING;
  if (stateStackSize < 1) throw(ArrayLowerBoundExceeded);
  if (debugMode) printf("Received symbol no.%d\n", symbol);
  int state = stateStack[stateStackSize - 1];
  struct TableCell cell = table[state][symbol];
  switch(cell.action) {
    case 0:
      return YACC_NOTHING;
    case 4:
      return YACC_ACCEPT;
    case 1:
      if (debugMode) printf("Go to state %d\n", cell.target);
      stateStackPush(cell.target);
      return YACC_NOTHING;
    case 2:
      stateStackPush(cell.target);
      if (debugMode) printf("Shift to state %d\n", cell.target);
      curAttr = yytext;
      nodes[nodeNum] = (struct Node *)malloc(sizeof(struct Node));
      nodes[nodeNum]->value = (char *)malloc(sizeof(char) * strlen(curAttr));
      nodes[nodeNum]->yytext = NULL;
      strcpy(nodes[nodeNum]->value, curAttr);
      nodes[nodeNum]->childNum = 0;
      nodeNum++;
      updateSymbolAttr(0);
      return YACC_NOTHING;
    case 3:
      if (debugMode) printf("Reduce by producer %d\n", cell.target);
      switch (cell.target) {
        case 0:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("program"); 
out("Reduce@program->declarations");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(155);
          return symbol;
        case 1:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("declarations"); 
out("Reduce@declarations->declaration declarations");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(156);
          return symbol;
        case 2:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("declarations"); 
out("Reduce@declarations->declaration");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(156);
          return symbol;
        case 3:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("declaration"); 
out("Reduce@declaration->func_declaration");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(157);
          return symbol;
        case 4:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("declaration"); 
out("Reduce@declaration->var_declaration");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(157);
          return symbol;
        case 5:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("var_declaration"); 
out("Reduce@var_declaration->type IDENTIFIER SEMICOLON");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(158);
          return symbol;
        case 6:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("var_declaration"); 
out("Reduce@var_declaration->type assign_expr SEMICOLON");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(158);
          return symbol;
        case 7:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("func_declaration"); 
out("Reduce@func_declaration->type IDENTIFIER LPAREN parameter_list RPAREN block_stmt");          stateStackPop(6);
          reduceNode(6);
          updateSymbolAttr(6);
          dealWith(159);
          return symbol;
        case 8:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("func_declaration"); 
out("Reduce@func_declaration->type IDENTIFIER LPAREN RPAREN block_stmt");          stateStackPop(5);
          reduceNode(5);
          updateSymbolAttr(5);
          dealWith(159);
          return symbol;
        case 9:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("parameter_list"); 
out("Reduce@parameter_list->type IDENTIFIER COMMA parameter_list");          stateStackPop(4);
          reduceNode(4);
          updateSymbolAttr(4);
          dealWith(160);
          return symbol;
        case 10:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("parameter_list"); 
out("Reduce@parameter_list->type IDENTIFIER");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(160);
          return symbol;
        case 11:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->IF LPAREN logic_expr RPAREN stmt");          stateStackPop(5);
          reduceNode(5);
          updateSymbolAttr(5);
          dealWith(161);
          return symbol;
        case 12:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->IF LPAREN logic_expr RPAREN stmt ELSE stmt");          stateStackPop(7);
          reduceNode(7);
          updateSymbolAttr(7);
          dealWith(161);
          return symbol;
        case 13:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->WHILE LPAREN logic_expr RPAREN stmt");          stateStackPop(5);
          reduceNode(5);
          updateSymbolAttr(5);
          dealWith(161);
          return symbol;
        case 14:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->var_declaration");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(161);
          return symbol;
        case 15:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->assign_expr SEMICOLON");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(161);
          return symbol;
        case 16:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->function_call SEMICOLON");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(161);
          return symbol;
        case 17:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->RETURN arithmetic_expr SEMICOLON");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(161);
          return symbol;
        case 18:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmt"); 
out("Reduce@stmt->block_stmt");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(161);
          return symbol;
        case 19:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmts"); 
out("Reduce@stmts->stmt stmts");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(162);
          return symbol;
        case 20:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("stmts"); 
out("Reduce@stmts->stmt");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(162);
          return symbol;
        case 21:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("block_stmt"); 
out("Reduce@block_stmt->LBRACE stmts RBRACE");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(163);
          return symbol;
        case 22:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("block_stmt"); 
out("Reduce@block_stmt->LBRACE RBRACE");          stateStackPop(2);
          reduceNode(2);
          updateSymbolAttr(2);
          dealWith(163);
          return symbol;
        case 23:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("type"); 
out("Reduce@type->INT");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(164);
          return symbol;
        case 24:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("type"); 
out("Reduce@type->FLOAT");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(164);
          return symbol;
        case 25:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("expr"); 
out("Reduce@expr->assign_expr");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(165);
          return symbol;
        case 26:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("expr"); 
out("Reduce@expr->arithmetic_expr");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(165);
          return symbol;
        case 27:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("expr"); 
out("Reduce@expr->logic_expr");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(165);
          return symbol;
        case 28:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("assign_expr"); 
out("Reduce@assign_expr->IDENTIFIER ASSIGN arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(166);
          return symbol;
        case 29:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("assign_expr"); 
out("Reduce@assign_expr->IDENTIFIER ADD_ASSIGN arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(166);
          return symbol;
        case 30:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->arithmetic_expr PLUS arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(167);
          return symbol;
        case 31:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->arithmetic_expr MULTIPLY arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(167);
          return symbol;
        case 32:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->LPAREN arithmetic_expr RPAREN");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(167);
          return symbol;
        case 33:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->IDENTIFIER");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(167);
          return symbol;
        case 34:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->CONSTANT");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(167);
          return symbol;
        case 35:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->STRING_LITERAL");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(167);
          return symbol;
        case 36:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("arithmetic_expr"); 
out("Reduce@arithmetic_expr->function_call");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(167);
          return symbol;
        case 37:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->logic_expr AND_OP logic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(168);
          return symbol;
        case 38:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->logic_expr OR_OP logic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(168);
          return symbol;
        case 39:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->LPAREN logic_expr RPAREN");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(168);
          return symbol;
        case 40:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->arithmetic_expr EQ_OP arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(168);
          return symbol;
        case 41:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->arithmetic_expr NE_OP arithmetic_expr");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(168);
          return symbol;
        case 42:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->TRUE");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(168);
          return symbol;
        case 43:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("logic_expr"); 
out("Reduce@logic_expr->FALSE");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(168);
          return symbol;
        case 44:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("function_call"); 
out("Reduce@function_call->IDENTIFIER LPAREN argument_list RPAREN");          stateStackPop(4);
          reduceNode(4);
          updateSymbolAttr(4);
          dealWith(169);
          return symbol;
        case 45:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("function_call"); 
out("Reduce@function_call->IDENTIFIER LPAREN RPAREN");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(169);
          return symbol;
        case 46:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("argument_list"); 
out("Reduce@argument_list->arithmetic_expr COMMA argument_list");          stateStackPop(3);
          reduceNode(3);
          updateSymbolAttr(3);
          dealWith(170);
          return symbol;
        case 47:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
reduceTo("argument_list"); 
out("Reduce@argument_list->arithmetic_expr");          stateStackPop(1);
          reduceNode(1);
          updateSymbolAttr(1);
          dealWith(170);
          return symbol;
        case 48:
          curAttr = (char *)malloc(1024 * sizeof(char));
          memset(curAttr, '\0', sizeof(curAttr));
curAttr = symbolAttr[0]; reduceTo("program'");          stateStackPop(0);
          reduceNode(0);
          updateSymbolAttr(0);
          dealWith(173);
          return symbol;
      }
    default:
      return symbol;
  }
  return YACC_NOTHING;
}
void printTree(struct Node *curNode, int depth) {
  if (curNode == NULL) return;
  for (int i = 0; i < depth * 2; i++)
    fprintf(treeout, " ");
  fprintf(treeout, "%s", curNode->value);
  if (curNode->yytext != NULL && strlen(curNode->yytext) > 0)
    fprintf(treeout, " (%s)", curNode->yytext);
  if (curNode->childNum < 1) return;
  fprintf(treeout, " {\n");
  for (int i = 0;i < curNode->childNum; i++) {
    printTree(curNode->children[i], depth+1);
    if (i+1 < curNode->childNum)
      fprintf(treeout, ",");
    fprintf(treeout, "\n");
  }
  for (int i = 0; i < depth * 2; i++)
    fprintf(treeout, " ");
  fprintf(treeout, "}");
}int yyparse() {
  if (yyout == NULL) yyout = stdout;
  int token;
  stateStackPush(0);
  while (token != YACC_ACCEPT && (token = yylex()) && token != YACC_ERROR) {
    do {
      token = dealWith(token);
      free(curToken);
      curToken = NULL;
    } while (token >= 0);
  }
  if (token == 0) {
    token = EOFIndex;
    do {
      token = dealWith(token);
    } while (token >= 0);
  }
  strcpy(yytext, curAttr);
  if (token == YACC_ERROR) return 1;
  if (token == YACC_ACCEPT) {
    treeout = fopen("yacc.tree", "w");
    printTree(nodes[0], 0);
    fclose(treeout);
    return 0;
  }
  else return 1;
} 
#include <stdio.h>

int main(int argc, char** argv) {
  // redirect yyin
  yyin = fopen(argv[1], "r");
	// redirect yyout if you want, or stdout by default
	// yyout = stdout;
	int c;
	// keep calling yyparse
  c = yyparse();
	if (c == 0) printf("Result is %s", yytext);
	else printf("Oh no!");
  fclose(yyin);
  return 0;
}

int yywrap() {
  return 1;
}