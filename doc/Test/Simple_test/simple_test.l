%{
#include "yy.tab.h"
%}

%%
a|b         return(TEST_1);
c+e         return(TEST_2);
[ \n\t]     ;
%%