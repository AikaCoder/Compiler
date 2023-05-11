package org.SeuCompiler.SeuLex.LexParser;

import org.SeuCompiler.Exception.LexParserErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.FiniteAutomata.Action;
import org.SeuCompiler.SeuLex.Regex.LexRegex;
import org.SeuCompiler.SeuLex.Regex.LexRegexBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum ParserState {
    InRegexAliasPart,   //正则别名部分 在第一个%%前面
    InCopyPart,         //直接复制部分, 在%{ %}内
    InRegexActionPart,  //正则-行为部分 在%% %%内
    InCCodePart,      //用户子程序部分, 在%%后
}

public class LexParser {
    private final StringBuilder copyPartBuilder = new StringBuilder();  //直接复制部分, 在%{ %}内
    private final StringBuilder cCodePartBuilder = new StringBuilder(); //末尾c代码, 在第二个%%后
    private final Map<LexRegex, Action> regexActionMap = new HashMap<>();
    private final Map<String, String> aliasRegexMap = new HashMap<>();

    private int actionOrder = 0;

    public String getCopyPart() {
        return copyPartBuilder.toString();
    }
    public String getCCodePart() {
        return cCodePartBuilder.toString();
    }
    public Map<LexRegex, Action> getRegexActionMap() {
        return regexActionMap;
    }

    public LexParser(String filePath) throws SeuCompilerException {
        List<String> lineList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null)
                lineList.add(line.trim());
        }catch (IOException e) {
            throw new SeuCompilerException(LexParserErrorCode.lex_file_IO_exception,e);
        }

        //将.lex文件分割成不同部分
        int lineNum = 0;
        ParserState state = ParserState.InRegexAliasPart;
        for(String line : lineList){
            lineNum ++;
            if (line.equals("")) continue;
            try{
                state = swiftAndBuild(state, line);
            }catch (SeuCompilerException exception){
                exception.setLineNum(lineNum);
                throw exception;
            }
        }

        if(state != ParserState.InCCodePart)
            throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "Lex文件要至少有两个%%");
    }

    /**
     * 读取一行, 转移到新状态, 并进行分割的具体操作
     * @param state 旧状态
     * @param line 读取的行内容
     * @return 新状态
     */
    private ParserState swiftAndBuild(ParserState state, String line) throws SeuCompilerException {
        ParserState newState = state;
        switch (state) {
        case InRegexAliasPart -> {
            switch (line) {
            case "%{" -> newState = ParserState.InCopyPart;
            case "%%" -> newState = ParserState.InRegexActionPart;
            case "}%" -> throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "前面没有对应的%{");
            default -> parserRegexAliasFrom(line);
            }
        }
        case InCopyPart -> {
            switch (line) {
            case "}%" -> newState = ParserState.InRegexAliasPart;
            case "%{" -> throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "重复的%{");
            case "%%" -> throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "%{ }%内不能有%%");
            default -> this.copyPartBuilder.append(line).append("\n");
            }
        }
        case InRegexActionPart -> {
            if(line.equals("%%")) newState = ParserState.InCCodePart;
            else if(line.equals("%{") || line.equals("}%"))
                throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "两个%%中间不能有%{或}%");
            else parserRegexAndActionFrom(line);
        }
        case InCCodePart -> {
            if(line.equals("%{") || line.equals("}%") || line.equals("%%"))
                throw new SeuCompilerException(LexParserErrorCode.bad_lex_file_structure, "第二个%%后面不能有 {%, }% 或 %%");
            else this.cCodePartBuilder.append(line).append("\n");
        }
        }
        return newState;
    }

    /**
     * 读取Regex别名定义, 结果放入regexAliases
     * @param line 读取的行的内容
     */
    private void parserRegexAliasFrom(String line) throws SeuCompilerException {
        Matcher spaceTest = Pattern.compile("\\s+").matcher(line);
        if (!spaceTest.find())
            throw new SeuCompilerException(LexParserErrorCode.invalid_regex_alias, "Regex别名定义中间缺少空格");
        String alias = line.substring(0, spaceTest.start());
        String regex = line.substring(spaceTest.start()).trim();

        Matcher specialCharTest = Pattern.compile("[^\\w_]").matcher(alias);
        if(specialCharTest.find())
            throw new SeuCompilerException(LexParserErrorCode.invalid_regex_alias, "Regex别名只能由字母,数字和下划线组成");
        if (aliasRegexMap.containsKey(alias))
            throw new SeuCompilerException(LexParserErrorCode.invalid_regex_alias, "Regex别名重复定义");

        aliasRegexMap.put(alias, regex);
    }

    /**
     * 读取正则表达式和相应的动作
     * @param line 当前读取行的内容
     */
    private void parserRegexAndActionFrom(String line) throws SeuCompilerException {
        line=line.trim();
        LexRegexBuilder regexBuilder = new LexRegexBuilder(this.aliasRegexMap);
        StringBuilder actionBuilder = new StringBuilder();
        boolean isBuildingRegex = true;

        for(int i = 0;i<line.length();i++) {
            char ch = line.charAt(i);
            if(isBuildingRegex){
                if(regexBuilder.swiftAndBuildRaw(ch) == null)
                    isBuildingRegex = false;
            }
            else {
                actionBuilder.append(ch);
            }
        }

        LexRegex regex = regexBuilder.build();
        String action = actionBuilder.toString().trim();
        if(action.equals(";")) action = "";
        else if(action.charAt(0) == '{'){
            if(action.charAt(action.length()-1) == '}')
                action = action.substring(1, action.length()-1);    //去除最外面括号
            else{
                throw new SeuCompilerException(LexParserErrorCode.action_error, "大括号不匹配");
            }
        }

        this.regexActionMap.put(regex, new Action(this.actionOrder, action));
        this.actionOrder ++;
    }

}
