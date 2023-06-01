%{
#include <stdlib.h>
#define out(fmt, ...) fprintf(yyout, fmt, ##__VA_ARGS__)
%}

%token TEST_1 TEST_2
%start expr

%%

expr
  : TEST_1 TEST_2
	;

%%

int main(int argc, char** argv) {
    yyin = fopen(argv[1], "r");
    int c;
    c = yyparse();
    if (c == 0) printf("Result is %d", atoi(yytext));
    else printf("Oh no!");
    fclose(yyin);
    return 0;
}

int yywrap() {
    return 1;
}