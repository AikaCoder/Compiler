package org.SeuCompiler.Yacc.LR1Analyzer;

import lombok.Getter;
import org.SeuCompiler.Yacc.Grammar.*;
import org.SeuCompiler.Yacc.YaccParser.*;
import org.SeuCompiler.Yacc.Grammar.GrammarSymbol.GrammarSymbolType;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Getter
public class LR1Analyzer {
    protected List<GrammarSymbol> symbols; //
    protected List<LR1Operator> operators;
    protected List<LR1Producer> producers; //
    protected int startSymbol;
    protected LR1DFA dfa; //
    protected List<List<ACTIONTableCell>> ACTIONTable; //
    protected List<List<Integer>> GOTOTable; //
    protected List<Integer> ACTIONReverseLookup; //
    protected List<Integer> GOTOReverseLookup; //
    protected List<List<Integer>> first;
    protected int epsilon;
    protected Map<GOTOCacheKey, LR1State> GOTOCache;

    public LR1Analyzer(YaccParser yaccParser, boolean useLALR) {
        this.symbols = new ArrayList<>();
        this.producers = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.ACTIONTable = new ArrayList<>();
        this.GOTOTable = new ArrayList<>();
        this.ACTIONReverseLookup = new ArrayList<>();
        this.GOTOReverseLookup = new ArrayList<>();
        this.GOTOCache = new HashMap<>();
        this.first = new ArrayList<>();

        // 下面是this._distributeId(yaccParser)
        this.distributeId(yaccParser);
        this.convertProducer(yaccParser.getProducers());
        this.convertOperator(yaccParser.getOperatorDecl());
        this.epsilon = this.getSymbolId(SpType.EPSILON.getSpSymbol());
        System.out.print("\n[ constructLR1DFA or LALRDFA, this might take a long time... ]");
        this.preCalFirst();
        this.constructLR1DFA();

        // 如果构造LALR
        if (useLALR) {
            this.dfa = LR1DFAtoLALRDFA(this);
        }
        System.out.print("\n[ constructACTIONGOTOTable, this might take a long time... ]");

        this.constructACTIONGOTOTable();
        System.out.print("\n");
    }

    // 去除转义斜杠，相当于String.raw的逆方法
    private static String cookString(String str) {
        StringBuilder res = new StringBuilder();
        boolean bslash = false;
        for (char c : str.toCharArray()) {
            if (bslash) {
                String charStr = "\\" + c;
                res.append(
                        get_escape_reverse(charStr).equals("")
                                ?
                                charStr
                                :
                                get_escape_reverse(charStr)
                );
                bslash = false;
            } else if (c == '\\') {
                bslash = true;
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }
    public static String get_escape_reverse(String str) {
        Map<String, String> table = new HashMap<>();
        table.put("\\n", "\n");
        table.put("\\t", "\t");
        table.put("\\r", "\r");
        table.put("\\(", "(");
        table.put("\\)", ")");
        table.put("\\[", "[");
        table.put("\\]", "]");
        table.put("\\+", "+");
        table.put("\\-", "-");
        table.put("\\*", "*");
        table.put("\\?", "?");
        table.put("\\\\\"", "\"");
        table.put("\\.", ".");
        table.put("\\'", "'");
        table.put("\\|", "|");
        table.put("\\\\", "\\");
        return table.getOrDefault(str, "");
    }
    /**
     * Ensure `condition`. Else throw Error `hint`.
     */
    public static void assertCondition(Object condition, String hint) {
        if (!((boolean) condition)) {
            throw new Error(hint);
        }
    }

    // 这里将LALR.ts里面的函数也一并写入这里
    public static LR1DFA LR1DFAtoLALRDFA(LR1Analyzer lr1) {
        // 计算同心集
        List<CoreArrCell> coreArr = new ArrayList<>();
        List<LR1State> dfaStates = lr1.dfa.getStates();

        for (int i = 0; i < dfaStates.size(); i++) {
            List<LR1Item> core = new ArrayList<>(dfaStates.get(i).getItems());
            int checker = IntStream.range(0, coreArr.size())
                    .filter(
                            x -> sameCore(coreArr.get(x).core, core)
                    )
                    .findFirst()
                    .orElse(-1);
            if (checker != -1) {
                // 存在同核心状态，直接加入
                coreArr.get(checker).states.add(i);
                // 注意合并lookahead
                for (LR1Item item : core) {
                    if (
                            coreArr.get(checker).core.stream().noneMatch(
                                    c -> Objects.equals(c, item)
                            )
                    ) coreArr.get(checker).core.add(item);
                }
            } else {
                CoreArrCell temp = new CoreArrCell();
                temp.core = core;
                temp.states.add(i);
                coreArr.add(temp);
            }
        }
        // LALR构建
        LR1DFA LALRDFA = new LR1DFA(-1);
        Map<Integer, Integer> old2new = new HashMap<>(); // 旧状态 - 新状态下标对应，用于重构边
        for (int i = 0; i < coreArr.size(); i++) {
            LR1State newState = new LR1State(coreArr.get(i).core);
            for (Integer ls : coreArr.get(i).states) old2new.put(ls, i);
            LALRDFA.addState(newState);
        }
        for (int i = 0; i < LALRDFA.getStates().size(); i++) {
            Integer representativeOldState = coreArr.get(i).states.get(0); // 选取第一个对应状态作为代表
            // 采纳它的边
            List<Map<String, Integer>> oldEdges = lr1.dfa.getAdjList().get(representativeOldState);
            for (Map<String, Integer> edge : oldEdges) {
                LALRDFA.link(
                        old2new.get(representativeOldState),
                        old2new.get(edge.get("to")),
                        edge.get("alpha")
                );
            }
        }
        // 修正起始状态号
        LALRDFA.setStartStateId(old2new.get(lr1.dfa.getStartStateId()));
        return LALRDFA;
    }

    private static boolean sameCore(List<LR1Item> core1, List<LR1Item> core2) {
        return core1.stream().allMatch(
                i1 ->
                        core2.stream().anyMatch(
                                i2 -> i1.producer() == i2.producer() && i1.dotPosition() == i2.dotPosition()
                        )
        )
                && core2.stream().allMatch(
                i1 -> core1.stream().anyMatch(
                        i2 -> i1.producer() == i2.producer() && i1.dotPosition() == i2.dotPosition()
                )
        );
    }

    /**
     * 获取编号后的符号的编号
     */
    public int getSymbolId(GrammarSymbol grammarSymbol) {
        for (GrammarSymbol gs : this.symbols) {
            if ((grammarSymbol.type() == null || gs.type().equals(grammarSymbol.type()))
                    && gs.content().equals(grammarSymbol.content())) {
                return symbols.indexOf(gs);
            }
        }
        return -1;
    }

    private void convertOperator(List<YaccParserOperator> operatorDecl) {
        for (YaccParserOperator decl : operatorDecl) {
            int id;
            if(decl.literal() != null)
                id = getSymbolId(GrammarSymbol.newASCII(decl.literal()));
            else if(decl.tokenName() != null)
                id = getSymbolId(GrammarSymbol.newToken(decl.tokenName()));
            else id = -1;
            assertCondition(id != -1,
                    "Operator declaration not found. This should never occur.");

            this.operators.add(new LR1Operator(id, decl.assoc(), decl.procedure()));
        }
    }

    /**
     * 在state下接收到symbol能到达的目标状态
     * 这里没有实现原代码中有的非空断言，姑且认为没有关系
     */
    private int getNext(LR1State state, GrammarSymbol symbol) {
        int alpha = this.getSymbolId(symbol);
        int index = 0;
        int target = 0;

        for (LR1State s : this.dfa.getStates()) {
            if (Objects.equals(s, state)) {
                index = this.dfa.getStates().indexOf(s);
                break;
            }
        }

        for (Map<String, Integer> v : this.dfa.getAdjList().get(index)) {
            if (v.get("alpha") == alpha) {
                target = v.get("to");
                break;
            }
        }

        return target;
    }

    /**
     * 为文法符号（终结符、非终结符、特殊符号）分配编号
     * // @test pass
     */
    private void distributeId(YaccParser yaccParser) {
        // 处理方式参考《Flex与Bison》P165
        // 0~127 ASCII文字符号编号
        // 128~X Token编号
        // X+1~Y 非终结符编号
        // Y+1~Y+3 特殊符号
        for (int i = 0; i < 128; i++)
            this.symbols.add(GrammarSymbol.newASCII(i));

        for (String token : yaccParser.getTokenDecl())
            this.symbols.add(GrammarSymbol.newToken(token));

        for (String nonTerminal : yaccParser.getNonTerminals())
            this.symbols.add(GrammarSymbol.newNonTerminal(nonTerminal));

        for (SpType spType : SpType.values())
            this.symbols.add(spType.getSpSymbol());

        this.startSymbol = this.getSymbolId(GrammarSymbol.newNonTerminal(yaccParser.getStartSymbol()));
        assert this.startSymbol != -1 : "LR1 startSymbol unset.";
    }

    /**
     * 判断符号是否是某个类型
     * type取值限定在'ascii' | 'token' | 'nonterminal' | 'sptoken'
     */
    private boolean symbolTypeIs(int id, GrammarSymbolType type) {
        return this.symbols.get(id).isType(type);
    }

    public String getSymbolString(int id) {
        return this.symbolTypeIs(id, GrammarSymbolType.ASCII) ?
                "'" + this.symbols.get(id).content() + "'" :
                this.symbols.get(id).content();
    }

    public String formatPrintProducer(LR1Producer producer) {
        String lhs = this.symbols.get(producer.lhs()).content();
        StringBuilder rhs = new StringBuilder();
        for (int r : producer.rhs()) rhs.append(this.getSymbolString(r)).append(" ");
        return lhs + "->" + rhs;
    }

    /**
     * 预先计算各符号的FIRST集
     */
    public void preCalFirst() {

        for (int index = 0; index < this.symbols.size(); index++) {
            if (this.symbols.get(index).type().equals(GrammarSymbolType.NONTERMINAL)) {
                this.first.add(new ArrayList<>());
            } else {
                List<Integer> temp = new ArrayList<>();
                temp.add(index);
                this.first.add(temp);
            }
        }
        boolean changed;
        do {
            changed = false;
            for (int index = 0; index < this.symbols.size(); index++) {
                if( ! symbolTypeIs(index, GrammarSymbolType.NONTERMINAL)) continue;
                List<Integer> nowFirstList = this.first.get(index);
                for (LR1Producer producer : this.producersOf(index)) {
                    int i = 0;
                    boolean hasEpsilon = false;
                    do {
                        if (i >= producer.rhs().size() && ! nowFirstList.contains(this.epsilon)) {
                            nowFirstList.add(this.epsilon);
                            changed = true;
                            break;
                        }
                        for (Integer symbol : this.first.get(producer.rhs().get(i))) {
                            if (!nowFirstList.contains(symbol)) {
                                nowFirstList.add(symbol);
                                changed = true;
                            }
                            if (symbol == this.epsilon) {
                                hasEpsilon = true;
                            }
                        }
                        i++;
                    } while (hasEpsilon);
                }
            }
        } while (changed);
    }

    /**
     * 求取FIRST集
     */
    public List<Integer> FIRST(List<Integer> symbols) {
        List<Integer> res = new ArrayList<>();
        int i = 0;
        AtomicBoolean hasEpsilon = new AtomicBoolean(false);
        do {
            hasEpsilon.set(false);
            if (i >= symbols.size()) {
                res.add(this.epsilon);
                break;
            }
            this.first.get(symbols.get(i)).forEach(
                    symbol -> {
                        if (symbol == this.epsilon) hasEpsilon.set(true);
                        else if (!res.contains(symbol)) res.add(symbol);
                    }
            );
            i++;
        } while (hasEpsilon.get());
        return res;
    }

    /**
     * 获取指定非终结符为左侧的所有产生式
     */
    private List<LR1Producer> producersOf(int nonterminal) {
        List<LR1Producer> res = new ArrayList<>();
        for (LR1Producer producer : this.producers) {
            if (producer.lhs() == nonterminal) res.add(producer);
        }
        return res;
    }

    /**
     * 将产生式转换为单条存储的、数字->数字[]形式
     * // @test pass
     */
    private void convertProducer(List<YaccParserProducer> stringProducers) {
        for (YaccParserProducer stringProducer : stringProducers) {
            int lhs = this.getSymbolId(GrammarSymbol.newNonTerminal(stringProducer.lhs()));
            assert lhs != -1 : "lhs not found in symbols. This error should never occur.";
            for (String right : stringProducer.rhs()) {
                int index = stringProducer.rhs().indexOf(right);
                List<Integer> rhs = new ArrayList<>();
                Pattern PATTERN = Pattern.compile("(' '|[^ ]+)");   //匹配空格外的东西, 或者单引号包裹的空格' '. 每个匹配单独分为一组
                Matcher matcher = PATTERN.matcher(right);
                while (matcher.find()) {
                    String tmp = matcher.group().trim();
                    int id;
                    if (Pattern.matches("'.+'", matcher.group())) {//   如果匹配到内容被单引号包裹, 即为普通字符
                        tmp = matcher.group().substring(1, matcher.group().length() - 1);
                        if (tmp.charAt(0) == '\\') tmp = cookString(tmp);
                        id = this.getSymbolId(GrammarSymbol.newASCII(tmp));
                    } else {
                        int a = this.getSymbolId(GrammarSymbol.newNonTerminal(tmp));
                        int b = this.getSymbolId(GrammarSymbol.newToken(tmp));
                        if(a != -1) id = a;
                        else id = b;
                    }
                    assert id != -1 : "symbol not found in symbols. This error should never occur. symbol=" + tmp;
                    rhs.add(id);
                }
                this.producers.add(new LR1Producer(lhs, rhs,
                        "reduceTo(\"" + stringProducer.lhs() + "\"); \n"
                                +
                                stringProducer.actions().get(index)));
            }
        }
    }

    private void constructLR1DFA() {
        // 将C初始化为 {CLOSURE}({|S'->S, $|})
        StringBuilder newStartSymbolContent = new StringBuilder(
                this.symbols.get(this.startSymbol).content() + "'"
        );
        while (this.symbols.stream().anyMatch(symbol ->
                symbol.content().equals(newStartSymbolContent.toString()))
        ) {
            newStartSymbolContent.append("'");
        }

        this.symbols.add(GrammarSymbol.newNonTerminal(newStartSymbolContent.toString()));
        this.producers.add(
                new LR1Producer(
                        this.symbols.size() - 1,
                        new ArrayList<>(List.of(this.startSymbol)),
                        "$$ = $1; reduceTo(\"" + newStartSymbolContent + "\");"
                )
        );
        this.startSymbol = this.symbols.size() - 1;
        LR1Producer initProducer = this.producersOf(this.startSymbol).get(0);
        LR1State I0 = this.CLOSURE(
                new LR1State(
                        new ArrayList<>(
                                List.of(
                                        new LR1Item(
                                                this.producers.indexOf(initProducer),
                                                initProducer,
                                                0,
                                                this.getSymbolId(SpType.END.getSpSymbol())
                                                )
                                        )
                                )
                        )
                );
        // 初始化自动机
        LR1DFA dfa = new LR1DFA(0);
        dfa.addState(I0);
        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        JProgressBar pb = new JProgressBar(0, dfa.getStates().size() * this.symbols.size());
        pb.setValue(0);
        pb.setStringPainted(true);
        while (stack.size() > 0) {
            LR1State I = dfa.getStates().get(stack.pop()); // for C中的每个项集I
            for (int X = 0; X < this.symbols.size(); X++) {
                // for 每个文法符号X
                LR1State gotoIX = this.GOTO(I, X);
                pb.setValue(this.GOTOCache.size());
                if (gotoIX.getItems().size() == 0) continue; // gotoIX要非空
                int sameStateCheck = IntStream.range(0, dfa.getStates().size())
                        .filter(i -> Objects.equals(dfa.getStates().get(i), gotoIX))
                        .findFirst()
                        .orElse(-1); // 存在一致状态要处理
                if (sameStateCheck != -1)
                    dfa.link(dfa.getStates().indexOf(I), sameStateCheck, X);
                else {
                    // 新建状态并连接
                    dfa.addState(gotoIX);
                    dfa.link(dfa.getStates().indexOf(I), dfa.getStates().size() - 1, X);
                    stack.push(dfa.getStates().size() - 1);
                }
            }
        }
        this.dfa = dfa;
    }

    /**
     * 求取GOTO(I, X)
     * 见龙书算法4.53
     */
    private LR1State _GOTO(LR1State I, int X) {
        LR1State J = new LR1State(new ArrayList<>());
        for (LR1Item item : I.getItems()) {
            // for I中的每一个项
            if (item.dotAtLast()) continue;
            if (this.producers.get(item.producer()).rhs().get(item.dotPosition()) == X) {
                J.addItem(LR1Item.copy(item, true));
            }
        }
        return this.CLOSURE(J);
    }

    /**
     * 缓存包装版本的GOTO
     * // @param i 状态
     * // @param a 符号下标
     */
    private LR1State GOTO(LR1State i, int a) {
        GOTOCacheKey gotoCacheKey = new GOTOCacheKey();
        gotoCacheKey.i = i;
        gotoCacheKey.a = a;
        LR1State cached = this.GOTOCache.get(gotoCacheKey);
        LR1State _goto;
        if (cached == null) {
            _goto = this._GOTO(i, a);
            this.GOTOCache.put(gotoCacheKey, _goto);
        } else {
            _goto = cached;
        }
        return _goto;
    }

    /**
     * 求取CLOSURE(I)（I为某状态）
     * 见龙书算法4.53
     */
    private LR1State CLOSURE(LR1State I) {
        LR1State res = LR1State.copy(I);
        List<LR1Item> allItemsOfI = new ArrayList<>(I.getItems());
        while (allItemsOfI.size() > 0) {
            LR1Item oneItemOfI = allItemsOfI.remove(allItemsOfI.size() - 1);
            if (oneItemOfI.dotAtLast()) continue;
            Integer currentSymbol =
                    this.producers.get(oneItemOfI.producer()).rhs().get(oneItemOfI.dotPosition());
            if (!this.symbolTypeIs(currentSymbol, GrammarSymbolType.NONTERMINAL)) continue; // 非终结符打头才有闭包
            List<LR1Producer> extendProducers = new ArrayList<>();
            for (LR1Producer producerInG : this.producers) { // for G'中的每个产生式
                // 左手边是当前符号的，就可以作为扩展用
                if (producerInG.lhs() == currentSymbol) extendProducers.add(producerInG);
            }
            int lookahead = oneItemOfI.lookahead();
            for (LR1Producer extendProducer : extendProducers) {
                // 求取新的展望符号
                List<Integer> newLookaheads = this.FIRST(
                        this.producers.get(oneItemOfI.producer()).rhs().
                                subList(
                                        oneItemOfI.dotPosition() + 1,
                                        this.producers.get(oneItemOfI.producer()).rhs().size()
                                )
                );
                // 存在epsilon作为FIRST符，可以用它“闪过”
                if (newLookaheads.contains(this.getSymbolId(SpType.EPSILON.getSpSymbol()))) {
                    newLookaheads.removeIf(v -> v == this.getSymbolId(SpType.EPSILON.getSpSymbol()));
                    // 闪过，用旧展望符号
                    if (!newLookaheads.contains(lookahead)) {
                        newLookaheads.add(lookahead);
                    }
                }
                // for FIRST(βa)中的每个终结符号b
                for (int newlookahead : newLookaheads) {
                    LR1Item newItem = new LR1Item(
                            this.producers.indexOf(extendProducer),
                            extendProducer,
                            0,
                            newlookahead
                    );
                    // 重复的情况不再添加，避免出现一样的Item
                    if (res.getItems().stream().anyMatch(item -> item.equals(newItem))) continue;
                    if (allItemsOfI.stream().noneMatch(item -> item.equals(newItem))) {
                        allItemsOfI.add(newItem);
                    } // 继续拓展
                    res.addItem(newItem);
                }
            }
        }
        return res;
    }

    /**
     * 生成语法分析表
     * 见龙书算法4.56
     */
    public void constructACTIONGOTOTable() {
        List<LR1State> dfaStates = this.dfa.getStates();
        // 初始化ACTIONTable
        for (int i = 0; i < dfaStates.size(); i++) {
            List<ACTIONTableCell> row = new ArrayList<>();
            for (int j = 0; j < this.symbols.size(); j++) {
                if (this.symbolTypeIs(j, GrammarSymbolType.NONTERMINAL)) continue;
                row.add(new ACTIONTableCell(ACTIONTableCell.ACTIONTableCellType.NONE, -1));
            }
            this.ACTIONTable.add(row);
        }
        // 初始化GOTOTable
        for (int i = 0; i < dfaStates.size(); i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < this.symbols.size(); j++) {
                if (this.symbolTypeIs(j, GrammarSymbolType.NONTERMINAL)) row.add(-1); // GOTO nowhere
            }
            this.GOTOTable.add(row);
        }
        // 初始化倒查表（由于前两个函数通过continue的方式排除不合适的符号，造成编号的错乱，故需要两张倒查表)
        //倒查指: 下标为GOTOtable中序号的项, 对应结果是symbol编号
        for (int j = 0; j < this.symbols.size(); j++) {
            if (this.symbolTypeIs(j, GrammarSymbolType.NONTERMINAL)) this.GOTOReverseLookup.add(j);
            else this.ACTIONReverseLookup.add(j);
        }
        // ===========================
        // ===== 填充ACTIONTable =====
        // ===========================
        Function<Integer, Integer> lookup =
                x -> this.ACTIONReverseLookup.indexOf(x);
        JProgressBar pb = new JProgressBar(0, dfaStates.size());
        pb.setValue(0);
        pb.setStringPainted(true);
        // 在该过程中，我们强制处理了所有冲突，保证文法是LR(1)的
        for (int i = 0; i < dfaStates.size(); i++) {
            if (i == dfaStates.size() - 1) {
                for (LR1Item pro : dfaStates.get(i).getItems()) {
                    System.out.println(this.formatPrintProducer(pro.rawProducer()));
                }
            }
            pb.setValue(i);
            // 处理移进的情况
            // ① [A->α`aβ, b], GOTO(Ii, a) = Ij, ACTION[i, a] = shift(j)
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (item.dotAtLast()) continue; // 没有aβ
                int a = this.producers.get(item.producer()).rhs().get(item.dotPosition());
                if (this.symbolTypeIs(a, GrammarSymbolType.NONTERMINAL)) continue;
                LR1State _goto = this.dfa.getStates().get(
                        this.getNext(dfaStates.get(i), this.symbols.get(a))
                );
                for (int j = 0; j < dfaStates.size(); j++)
                    if (Objects.equals(_goto, dfaStates.get(j)))
                        this.ACTIONTable.get(i).set(
                                lookup.apply(a),
                                new ACTIONTableCell(ACTIONTableCell.ACTIONTableCellType.SHIFT, j)
                        );
            }
            // 处理规约的情况
            // ② [A->α`, a], A!=S', ACTION[i, a] = reduce(A->α)
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (!item.dotAtLast()) continue;
                if (item.producer() == this.producers.size() - 1) continue; // 增广产生式也不处理
                if (this.symbolTypeIs(item.lookahead(), GrammarSymbolType.NONTERMINAL))
                    continue; // 展望非终结符的归GOTO表管
                boolean shouldReplace = false;
                if (
                        Objects.equals(
                                this.ACTIONTable.get(i).get(lookup.apply(item.lookahead()))
                                        .type().getType(), "shift"
                        )
                ) {
                    // 处理移进-规约冲突
                    // 展望符的优先级就是移进的优先级
                    LR1Operator shiftOperator = this.operators.stream()
                            .filter(x -> x.symbolId() == item.lookahead())
                            .findFirst().orElse(null);
                    int shiftPrecedence = (shiftOperator != null) ?
                            shiftOperator.precedence() : -1;
                    // 最后一个终结符的优先级就是规约的优先级
                    LR1Operator reduceOperator = null;
                    for (int _i = item.dotPosition() - 1; _i >= 0; _i--) {
                        int symbol = this.producers.get(item.producer()).rhs().get(_i);
                        if (!this.symbolTypeIs(symbol, GrammarSymbolType.NONTERMINAL)) {
                            reduceOperator = this.operators.stream()
                                    .filter(x -> x.symbolId() == symbol)
                                    .findFirst().orElse(null);
                            break;
                        }
                    }
                    int reducePrecedence = (reduceOperator != null) ?
                            reduceOperator.precedence() : -1;
                    if (
                            shiftOperator == null || reduceOperator == null
                                    || shiftPrecedence == -1 || reducePrecedence == -1
                    ) {
                        // 没有完整地定义优先级，就保持原有的移进
                    } else {
                        if (reducePrecedence == shiftPrecedence) {
                            if (reduceOperator.assoc().getAssoc().equals("left")) {
                                // 同级的运算符必然具备相同的结合性（因为在.y同一行声明）
                                shouldReplace = true; // 左结合就规约
                            }
                        } else if (reducePrecedence > shiftPrecedence) {
                            shouldReplace = true; // 规约优先级更高，替换为规约
                        }
                    }
                } else if (this.ACTIONTable.get(i).get(lookup.apply(item.lookahead()))
                        .type().getType().equals("reduce")) {
                    // 处理规约-规约冲突，越早定义的产生式优先级越高
                    // 不可能出现同级产生式
                    if (
                            this.ACTIONTable.get(i).get(lookup.apply(item.lookahead())).data()
                                    < item.producer()
                    ) {
                        shouldReplace = true;
                    }
                } else {
                    // 没有冲突
                    this.ACTIONTable.get(i).set(
                            lookup.apply(item.lookahead()),
                            new ACTIONTableCell(ACTIONTableCell.ACTIONTableCellType.REDUCE, item.producer())
                    ); // 使用item.producer号产生式规约
                }
                if (shouldReplace) {
                    this.ACTIONTable.get(i).set(
                            lookup.apply(item.lookahead()),
                            new ACTIONTableCell(ACTIONTableCell.ACTIONTableCellType.REDUCE, item.producer())
                    ); // 使用item.producer号产生式规约
                }
            }
            // 处理接受的情况
            // ③ [S'->S`, $], ACTION[i, $] = acc
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (
                        item.producer() == this.producers.size() - 1 &&
                                item.dotAtLast() &&
                                item.lookahead() == this.getSymbolId(SpType.END.getSpSymbol())
                ) {
                    this.ACTIONTable.get(i).set(
                            lookup.apply(this.getSymbolId(SpType.END.getSpSymbol())),
                            new ACTIONTableCell(
                                    ACTIONTableCell.ACTIONTableCellType.ACC,
                                    0
                            )
                    );
                }
            }
        }
        // ===========================
        // ====== 填充GOTOTable ======
        // ===========================
        Function<Integer, Integer> lookup_01 = x -> this.GOTOReverseLookup.indexOf(x);
        for (int i = 0; i < dfaStates.size(); i++)
            for (int A = 0; A < this.symbols.size(); A++) {
                if (!this.symbols.get(A).isType(GrammarSymbolType.NONTERMINAL)) continue;
                for (int j = 0; j < dfaStates.size(); j++)
                    if (Objects.equals(this.GOTO(dfaStates.get(i), A), dfaStates.get(j)))
                        this.GOTOTable.get(i).set(
                                lookup_01.apply(A),
                                j
                        );
            }
    }

    // 源于LALR.ts的函数中需要用到的一个类
    // 核+该核所在所有状态号的索引
    // {核的内容，包含这个核的状态号的数组}
    static class CoreArrCell {
        public List<LR1Item> core;

        public final List<Integer> states;

        public CoreArrCell() {
            this.core = new ArrayList<>();
            this.states = new ArrayList<>();
        }
    }

    static class GOTOCacheKey {
        public LR1State i;
        public int a;
    }
}

