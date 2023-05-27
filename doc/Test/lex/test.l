%{
#define IF 5
#define ID 12
#define INTEGER 13
#define REAL 14
%}

delim           [\t \n]+
letter          [A-Za-z]
digit           [0-9]
id              {letter}({letter}|{digit})*
integer         {digit}+
real            {digit}+\.({digit}+)?|({digit}+)?\.{digit}+

%%
{delim}         {}
if              {return IF;}
{id}            {yylval = install_id();return ID;}
{real}          {yylval = convert_real();return REAL;}
{integer}       {yylval = convert_int();return INTEGER;}
%%

install_id() {}