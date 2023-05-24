package org.SeuCompiler.Yacc.YaccParser;

import org.SeuCompiler.Yacc.Grammar.OperatorAssoc;
import org.SeuCompiler.Yacc.Grammar.YaccParserOperator;
import org.SeuCompiler.Yacc.Grammar.YaccParserProducer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class YaccParser {
    private final List<String> _tokenDecl;//定义的lex可以送来的Token
    private final List<String> _nonTerminals;// 定义的非终结符，在读产生式的过程中顺便填充
    private final List<YaccParserOperator> _operatorDecl;// 定义的运算符
    private final List<YaccParserProducer> _producers;// 定义的产生式
    private String _rawContent;
    private List<String> _splitContent;
    private String _startSymbol;//开始非终结符
    //四个部分
    private String _infoPart;
    private String _copyPart;
    private String _producerPart;
    private String _userCodePart;

    public YaccParser(String filePath) {
        _tokenDecl = new ArrayList<>();
        _operatorDecl = new ArrayList<>();
        _nonTerminals = new ArrayList<>();
        _producers = new ArrayList<>();
        _startSymbol = "";
        //捕获和处理异常
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            //逐行读取文件，sb可变字符串;遇到\r,\n,\r\rn才会返回
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            _rawContent = sb.toString().replaceAll("\r\n", "\n");
            _splitContent = new ArrayList<>(List.of(_rawContent.split("\n")));
        } catch (IOException e) {
            //在命令行打印异常信息在程序中出错的位置及原因
            e.printStackTrace();
        }
        _fillText();
        _parseProducerPart();
        _parseInfoPart();
    }

    public String getCopyPart() {
        return _copyPart;
    }

    public String getUserCodePart() {
        return _userCodePart;
    }

    public List<YaccParserProducer> getProducers() {
        return _producers;
    }

    public List<YaccParserOperator> getOperatorDecl() {
        return _operatorDecl;
    }

    public String getStartSymbol() {
        return _startSymbol;
    }

    public List<String> getNonTerminals() {
        return _nonTerminals;
    }

    public List<String> getTokenDecl() {
        return _tokenDecl;
    }

    private void _parseInfoPart() {
        int currentPrecedence = 0;
        for (String line : _infoPart.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] words = line.split("\\s+");
            switch (words[0]) {
                case "%token" -> {
                    for (int i = 1; i < words.length; i++) {
                        if (!_tokenDecl.contains(words[i])) {
                            _tokenDecl.add(words[i]);
                        }
                    }
                }
                case "%left", "%right" -> {
                    currentPrecedence += 1;
                    String assoc = words[0].substring(1);
                    for (int i = 1; i < words.length; i++) {
                        String temp = words[i];
                        //需要区分优先级使用字面量‘+’‘-’表示，还是token表示：MINUS,PLUS
                        boolean literalOnly = false;
                        if (temp.charAt(0) == '\'') {
                            assert temp.charAt(temp.length() - 1) == '\'' : "Quote not closed: " + temp;
                            temp = tools.cookString(temp.substring(1, temp.length() - 1));
                            literalOnly = true;
                        }
                        boolean operatorDefined = false;
                        for (YaccParserOperator op : _operatorDecl) {
                            if (op.tokenName().equals(temp) || op.literal().equals(temp)) {
                                operatorDefined = true;
                                break;
                            }
                        }
                        assert !operatorDefined : "Operator redefined: " + temp;
                        if (literalOnly) {
                            _operatorDecl.add(new YaccParserOperator(null, temp, OperatorAssoc.sTy(assoc), currentPrecedence));
                        } else {
                            _operatorDecl.add(new YaccParserOperator(temp, null, OperatorAssoc.sTy(assoc), currentPrecedence));
                        }
                    }
                }
                case "%start" -> {
                    for (int i = 1; i < words.length; i++) {
                        //assert _startSymbol.trim().), `Start symbol redefined: ${words[i]}`);
                        assert this._startSymbol.trim().length() == 0 : "Start symbol redefined:" + words[i];
                        assert this._nonTerminals.contains(words[i]) : "Unknown start symbol:" + words[i];
                        this._startSymbol = words[i];
                    }
                }
                default -> {
                    assert false : "Unknown declaration:" + words[0];
                }
            }
        }
    }

    private void _parseProducerPart() {
        Matcher m1, m2, m3;
        Pattern patternBlockProducer = Pattern.compile(tools.PATTERN_BLOCK_PRODUCER);
        Pattern patternInitialProducer = Pattern.compile(tools.PATTERN_INITIAL_PRODUCER);
        Pattern patternContinuedProducer = Pattern.compile(tools.PATTERN_CONTINUED_PRODUCER);

        while ((m1 = patternBlockProducer.matcher(this._producerPart)).find()) {
            String block = m1.group(0);
            String lhs = null;
            ArrayList<String> rhs = new ArrayList<>();
            //ArrayList<String> rhsList = new ArrayList<>();
            ArrayList<String> actionsList = new ArrayList<>();

            m2 = patternInitialProducer.matcher(block);
            if (m2.find()) {
                lhs = m2.group(1);
                this._nonTerminals.add(lhs);
                rhs.add(m2.group(3));
                actionsList.add(m2.group(4) != null ? m2.group(4).substring(1, m2.group(4).length() - 1).trim() : "");
            }

            m3 = patternContinuedProducer.matcher(block);
            while (m3.find()) {
                rhs.add(m3.group(2));
                actionsList.add(m3.group(3) != null ? m3.group(3).substring(1, m3.group(3).length() - 1).trim() : "");
            }

            assert lhs != null;
            //lhs = lhs.trim();
            //rhs = rhs.map(v => v.trim());
            rhs.replaceAll(String::trim);
            this._producers.add(new YaccParserProducer(lhs, rhs, actionsList));
        }
    }

    private void _fillText() {
        int copyPartStart = -1, copyPartEnd = -1;
        ArrayList<Integer> twoPercent = new ArrayList<>();
        for (int i = 0; i < this._splitContent.size(); i++) {
            String v = this._splitContent.get(i).trim();

            switch (v) {
                case "%{" -> {
                    assert copyPartStart == -1 : "Bad .y structure. Duplicate %{.";
                    copyPartStart = i;
                }
                case "%}" -> {
                    assert copyPartEnd == -1 : "Bad .y structure. Duplicate %}.";
                    copyPartEnd = i;
                }
                case "%%" -> {
                    assert twoPercent.size() < 2 : "Bad .y structure. Duplicate %%.";
                    twoPercent.add(i);
                }
            }
        }
        assert copyPartStart != -1 : "Bad .y structure. {% not found.";
        assert copyPartEnd != -1 : "Bad .y structure. %} not found.";
        assert twoPercent.size() == 2 : "Bad .y structure. No enough %%.";

        this._userCodePart = String.join("\n", this._splitContent.subList(twoPercent.get(1) + 1, this._splitContent.size()));
        this._copyPart = String.join("\n", this._splitContent.subList(copyPartStart + 1, copyPartEnd));
        this._producerPart = String.join("\n", this._splitContent.subList(twoPercent.get(0) + 1, twoPercent.get(1)));
        this._infoPart = String.join("\n", this._splitContent.subList(0, copyPartStart))
                + String.join("\n", this._splitContent.subList(copyPartEnd + 1, twoPercent.get(0)));
    }

}

