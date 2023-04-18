package org.SeuCompiler.SeuLex.LexParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexParser {
    //private String _filePath;//只在构造函数中使用一次的变量，不需要定义全局变量
    private String rawContent;
    private String copyPart;
    private String cCodePart;
    private String regexActionPart;
    private String regexAliasPart;
    private Map<String, String> _actions;
    private final Map<String, String> regexAliases;//枚举类加final避免产生set方法
    private final Map<String, String> regexActionMap;

    //构造函数
    public LexParser(String filePath) {
        //捕获和处理异常
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            //逐行读取文件，sb可变字符串;遇到\r,\n,\r\rn才会返回
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            rawContent = sb.toString().replaceAll("\r\n", "\n");
        } catch (IOException e) {
            //在命令行打印异常信息在程序中出错的位置及原因
            e.printStackTrace();
        }
        regexAliases = new HashMap<>();
        regexActionMap = new HashMap<>();
        splitContent();//将文件分为四部分
        regexAction();
    }

    public String getCopyPart() {
        return copyPart;
    }

    public String getCCodePart() {
        return cCodePart;
    }

    public String getRegexActionPart() {
        return regexActionPart;
    }

    public Map<String, String> getRegexAliases() {
        return regexAliases;
    }

    public Map<String, String> getRegexActionMap() {
        return regexActionMap;
    }

    /**
     * 解析出四部分的文本
     */
    private void splitContent() {
        //以\n为分割，进行分裂，返回分裂后的数组,不包含\n
        List<String> splitContent= List.of(rawContent.split("\n"));
        //String[] splitContent = rawContent.split("\n");
        int copyPartStart = -1, copyPartEnd = -1;
        int[] twoPercentPos = new int[2];//记录两个%%的位置
        int twoPercentCount = 0;
        int i;
        for (i = 0; i < splitContent.size(); i++) {
            String line = splitContent.get(i).trim();
            // 寻找分界符位置
            if (line.equals("%{")) {
                assert copyPartStart == -1 : "Bad .l structure. Duplicate %{.";
                copyPartStart = i;
            } else if (line.equals("%}")) {
                assert copyPartEnd == -1 : "Bad .l structure. Duplicate %}.";
                copyPartEnd = i;
            } else if (line.equals("%%")) {
                assert twoPercentCount < 2 : "Bad .l structure. Duplicate %%.";
                twoPercentPos[twoPercentCount++] = i;
            }
        }
        assert copyPartStart != -1 : "Bad .l structure. {% not found.";
        assert copyPartEnd != -1 : "Bad .l structure. %} not found.";
        assert twoPercentCount == 2 : "Bad .l structure. No enough %%.";
        //新的写法；用字符串链表实现；代码更简洁
        // 最末尾的C代码部分
        this.cCodePart = String.join("\n", splitContent.subList(twoPercentPos[1] + 1, splitContent.size()));
        // 开头的直接复制部分
        this.copyPart = String.join("\n", splitContent.subList(copyPartStart + 1, copyPartEnd));
        // 中间的正则-动作部分
        this.regexActionPart= String.join("\n", splitContent.subList(twoPercentPos[0] + 1, twoPercentPos[1]));
        // 剩余的是正则别名部分
        this.regexAliasPart = String.join("\n", splitContent.subList(0, copyPartStart))
                + String.join("\n", splitContent.subList(copyPartEnd + 1, twoPercentPos[0]));

    }

    /**
     * 填充解析结果
     */
    private void regexAction() {
        //处理正则别名部分
        for (String v : this.regexAliasPart.split("\n")) {
            if (v.trim().length() > 0) {
                v = v.trim();
                //\s：表示一个空白字符（空格，tab，换页符等）
                //规范的正则别名中间应该有空格
                Matcher spaceTest = Pattern.compile("\\s+").matcher(v);
                assert spaceTest.find() : "Invalid regex alias line: " + v;
                String alias = v.substring(0, spaceTest.start());
                assert spaceTest.start() < v.length() - 1 : "Invalid regex alias line: " + v;
                String regex = v.substring(spaceTest.start()).trim();
                assert !this.regexAliases.containsKey(alias) : "Regex alias re-definition found: " + v;
                this.regexAliases.put(alias, regex);
            }
        }

        //处理正则表达式部分

        String regexPart = "";//读取的正则部分
        String actionPart = "";//读取的动作部分
        //别名展开后的正则列表
        List<String> regexes = new ArrayList<>();
        boolean isReadingRegex = true, isWaitingOr = false;//是否正在读取正则;是否正在等待正则间的“或”运算符
        boolean isInQuote = false, isInSquare = false;//是否在引号内;是否在方括号内
        boolean isSlash = false;
        int braceLevel = 0, codeOrder = 0;
        for (String c : this.regexActionPart.split(" ")) {
            if (isReadingRegex) {//是否正在读取正则
                if (isWaitingOr) {
                    if (!c.trim().equals(" ")) {
                        isWaitingOr = false;
                        if (!c.equals("|"))
                            isReadingRegex = false;
                    }
                } else {//减少了isInBrackets
                    if (!isInQuote && !isInSquare && !c.trim().equals(" ") && regexPart.length() > 0) {
                        int ptr1 = 0;
                        isSlash = false;
                        for (; ptr1 < regexPart.length(); ptr1++) {
                            char ch = regexPart.charAt(ptr1);
                            if (!isInQuote && !isSlash && !isInSquare && ch == '{') {
                                int ptr2 = ptr1 + 1;
                                StringBuilder aliasBuilder = new StringBuilder();
                                for (; ptr2 < regexPart.length(); ptr2++) {
                                    ch = regexPart.charAt(ptr2);
                                    if (ch == '}')
                                        break;
                                    aliasBuilder.append(ch);
                                }
                                String alias = aliasBuilder.toString();
                                assert ptr2 < regexPart.length() : "Missing right brace at the end of alias: " + alias;
                                if (this.regexAliases.containsKey(alias)) {
                                    regexPart = regexPart.substring(0, ptr1) + "(" + this.regexAliases.get(alias) + ")" + regexPart.substring(ptr2 + 1);
                                    ptr1 -= 1;
                                } else
                                    ptr1 = ptr2;
                            } else if (ch == '\\')
                                isSlash = !isSlash;
                            else if (!isSlash && !isInSquare && ch == '"')
                                isInQuote = !isInQuote;
                            else if (!isSlash && !isInQuote && ch == '[')
                                isInSquare = true;
                            else if (!isSlash && isInSquare && ch == ']')
                                isInSquare = false;
                            else
                                isSlash = false;
                        }
                        assert !this._actions.containsKey(regexPart) : "Regex re-definition found: " + regexPart;
                        regexes.add(regexPart);
                        regexPart = "";
                        isSlash = false;
                        isInQuote = false;
                        isInSquare = false;
                        isWaitingOr = true;
                    } else {
                        regexPart += !c.trim().equals(" ") ? c : regexPart.isEmpty() ? "" : " ";
                        if (c.equals("\\"))
                            isSlash = !isSlash;
                        else if (c.equals("\"") && !isSlash && !isInSquare)
                            isInQuote = !isInQuote;
                    }
                }
            }
            //正在读取动作
            if (!isReadingRegex) {
                actionPart += !c.trim().equals(" ") ? c : ' ';
                if ((!isInQuote && braceLevel == 0 && c.equals(";")) ||
                        (!isInQuote && c.equals("}") && braceLevel == 1)) {
                    for (String regex : regexes) {
                        actionPart = actionPart.trim();
                        if (actionPart.equals(";")) {
                            actionPart = "";
                        } else if (actionPart.charAt(0) == '{') {
                            actionPart = actionPart.substring(1, actionPart.length() - 1);
                        }
                        this._actions.put(regex, actionPart.trim());
                        //需要用到Regex函数
                        /*this.regexActionMap.put(new Regex(regex), new RegexAction(
                                actionPart.trim(),
                                codeOrder++
                        ));*/
                    }
                    regexes.clear();
                    isSlash = false;
                    isInQuote = false;
                    isInSquare = false;
                    braceLevel = 0;
                    actionPart = "";
                    isReadingRegex = true;
                } else {
                    if (c.equals("\\")) {
                        isSlash = !isSlash;
                    } else if (!isSlash && (c.equals("'") || c.equals("\""))) {
                        isInQuote = !isInQuote;
                    } else if (!isInQuote && c.equals("{")) {
                        braceLevel += 1;
                    } else if (!isInQuote && c.equals("}")) {
                        braceLevel = Math.max(0, braceLevel - 1);
                    } else {
                        isSlash = false;
                    }
                }
            }
        }
    }
    public void set_actions (Map < String, String > _actions){
        this._actions = _actions;
    }
}