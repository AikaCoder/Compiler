package org.SeuCompiler.Yacc.LR1Analyzer;

import lombok.Getter;
import org.SeuCompiler.Yacc.Grammar.*;
import org.SeuCompiler.Yacc.Grammar.GrammarSymbol.GrammarSymbolType;
import org.SeuCompiler.Yacc.YaccParser.YaccParser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

class LRU extends LinkedHashMap<LR1Analyzer.GOTOCacheKey, LR1State>
{
    private final int capacity;
    public LRU(int capacity) {
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * LinkedHashMap自带的判断是否删除最老的元素方法，默认返回false，即不删除老数据
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<LR1Analyzer.GOTOCacheKey, LR1State> eldest)
    {
        return size() > capacity;
    }
}

@Getter
public class LR1Analyzer {
    protected List<GrammarSymbol> symbols; //
    protected List<LR1Operator> operators;
    protected List<LR1Producer> producers; //
    protected GrammarSymbol startSymbol;
    protected GrammarSymbol epsilonSymbol;
    protected GrammarSymbol endSymbol;
    protected LR1DFA dfa; //
    protected List<List<ActionTableCell>> ACTIONTable; //
    protected List<List<Integer>> GOTOTable; //
    protected Map<GrammarSymbol, List<GrammarSymbol>> first;
    protected Map<GOTOCacheKey, LR1State> GOTOCache;

    public LR1Analyzer(YaccParser yaccParser, boolean useLALR) {
        this.symbols = new ArrayList<>();
        this.producers = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.ACTIONTable = new ArrayList<>();
        this.GOTOTable = new ArrayList<>();
        this.first = new HashMap<>();
        this.GOTOCache = new LRU(8192000);

        // 下面是this._distributeId(yaccParser)
        this.distributeId(yaccParser);
        this.convertProducer(yaccParser.getProducers());
        this.convertOperator(yaccParser.getOperatorDecl());
        System.out.print("\n[ constructLR1DFA or LALRDFA, this might take a long time... ]");
        this.preCalFirst();
        this.constructLR1DFA();

        // 如果构造LALR
        if (useLALR) {
            this.dfa = LR1DFAtoLALRDFA(this);
        }
        System.out.print("\n[ constructACTIONGOTOTable, this might take a long time... ]");

        this.constructActionGotoTable();
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

        for (LR1State state : dfaStates) {
            List<LR1Item> core = new ArrayList<>(state.getItems());
            int checker = IntStream.range(0, coreArr.size())
                    .filter(
                            x -> sameCore(coreArr.get(x).core, core)
                    )
                    .findFirst()
                    .orElse(-1);
            if (checker != -1) {
                // 存在同核心状态，直接加入
                coreArr.get(checker).states.add(state);
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
                temp.states.add(state);
                coreArr.add(temp);
            }
        }
        // LALR构建
        LR1DFA LALRDFA = new LR1DFA();
        Map<LR1State, LR1State> old2new = new HashMap<>(); // 旧状态 - 新状态下标对应，用于重构边
        for (CoreArrCell coreArrCell : coreArr) {
            LR1State newState = new LR1State(coreArrCell.core);
            for (LR1State ls : coreArrCell.states) old2new.put(ls, newState);
            LALRDFA.addState(newState);
        }
        for (int i = 0; i < LALRDFA.getStates().size(); i++) {
            LR1State representativeOldState = coreArr.get(i).states.get(0); // 选取第一个对应状态作为代表
            // 采纳它的边
            Map<GrammarSymbol, LR1State> oldEdges = lr1.dfa.getAdjMap().get(representativeOldState);
            oldEdges.forEach((symbol, state) -> LALRDFA.link(old2new.get(representativeOldState), old2new.get(state), symbol));
        }
        // 修正起始状态号
        LALRDFA.setStartState((old2new.get(lr1.dfa.getStartState())));
        return LALRDFA;
    }

    private static boolean sameCore(List<LR1Item> core1, List<LR1Item> core2) {
        return core1.stream().allMatch(
                i1 ->
                        core2.stream().anyMatch(
                                i2 -> i1.producer().equals(i2.producer()) && i1.dotPosition() == i2.dotPosition()
                        )
        )
                && core2.stream().allMatch(
                i1 -> core1.stream().anyMatch(
                        i2 -> i1.producer().equals(i2.producer()) && i1.dotPosition() == i2.dotPosition()
                )
        );
    }

    /**
     * 获取编号后的符号的编号
     */
    public int getSymbolId(GrammarSymbol grammarSymbol) {
        int i = 0;
        for (GrammarSymbol gs : this.symbols) {
            if(gs.equals(grammarSymbol))
                return i;
            i++;
        }
        return -1;
    }

    private void convertOperator(List<YaccParserOperator> operatorDeclare) {
        for (YaccParserOperator declare : operatorDeclare) {
            GrammarSymbol symbol;
            if (declare.literal() != null)
                symbol = GrammarSymbol.newASCII(declare.literal());
            else if (declare.tokenName() != null)
                symbol = GrammarSymbol.newToken(declare.tokenName());
            else symbol = null;
            assertCondition(symbol != null,
                    "Operator declaration not found. This should never occur.");

            this.operators.add(new LR1Operator(symbol, declare.assoc(), declare.procedure()));
        }
    }

    /**
     * 在state下接收到symbol能到达的目标状态
     * 这里没有实现原代码中有的非空断言，姑且认为没有关系
     */
    private LR1State getNext(LR1State state, GrammarSymbol symbol) {
        return this.dfa.getNext(state, symbol);
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
        int i = 0;  //id和下标一一对应
        for (; i < 128; i++)
            this.symbols.add(GrammarSymbol.newASCII(i));

        for (String token : yaccParser.getTokenDecl()) {
            this.symbols.add(GrammarSymbol.newToken(token));
            i++;
        }

        for (String nonTerminal : yaccParser.getNonTerminals()) {
            this.symbols.add(GrammarSymbol.newNonTerminal(nonTerminal));
            i++;
        }

        this.endSymbol = GrammarSymbol.newSpEnd();
        this.symbols.add(this.endSymbol);
        this.epsilonSymbol = GrammarSymbol.newSpEpsilon();
        this.symbols.add(this.epsilonSymbol);

        this.startSymbol = GrammarSymbol.newNonTerminal(yaccParser.getStartSymbol());
    }

    /**
     * 预先计算各符号的FIRST集
     */
    public void preCalFirst() {

        for (GrammarSymbol symbol : this.symbols) {
            if (symbol.isType(GrammarSymbolType.NON_TERMINAL)) {
                this.first.put(symbol,new ArrayList<>());
            } else {
                List<GrammarSymbol> temp = new ArrayList<>();
                temp.add(symbol);
                this.first.put(symbol, temp);
            }
        }
        boolean changed;
        do {
            changed = false;
            for (GrammarSymbol symbol : this.symbols) {
                if (symbol.isType(GrammarSymbolType.NON_TERMINAL)) continue;
                List<GrammarSymbol> nowFirstList = this.first.get(symbol);
                for (LR1Producer producer : this.producersOf(symbol)) {
                    int i = 0;
                    boolean hasEpsilon = false;
                    do {
                        if (i >= producer.rhs().size() && !nowFirstList.contains(this.epsilonSymbol)) {
                            nowFirstList.add(this.epsilonSymbol);
                            changed = true;
                            break;
                        }
                        GrammarSymbol rhs = producer.rhs().get(i);
                        for (GrammarSymbol rhSymbol : this.first.get(rhs)){
                            if (!nowFirstList.contains(rhSymbol)) {
                                nowFirstList.add(rhSymbol);
                                changed = true;
                            }
                            if (rhSymbol.equals(this.epsilonSymbol)) {
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
    public List<GrammarSymbol> FIRST(List<GrammarSymbol> symbols) {
        List<GrammarSymbol> res = new ArrayList<>();
        int i = 0;
        AtomicBoolean hasEpsilon = new AtomicBoolean(false);
        do {
            hasEpsilon.set(false);
            if (i >= symbols.size()) {
                res.add(this.epsilonSymbol);
                break;
            }
            this.first.get(symbols.get(i)).forEach(
                    symbol -> {
                        if (symbol.equals(this.epsilonSymbol)) hasEpsilon.set(true);
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
    private List<LR1Producer> producersOf(GrammarSymbol nonTerminal) {
        List<LR1Producer> res = new ArrayList<>();
        for (LR1Producer producer : this.producers) {
            if (producer.lhs().equals(nonTerminal)) res.add(producer);
        }
        return res;
    }

    /**
     * 将产生式转换为单条存储的、数字->数字[]形式
     * // @test pass
     */
    private void convertProducer(List<YaccParserProducer> stringProducers) {
        for (YaccParserProducer stringProducer : stringProducers) {
            GrammarSymbol lhs = GrammarSymbol.newNonTerminal(stringProducer.lhs());
            for (String right : stringProducer.rhs()) {
                int index = stringProducer.rhs().indexOf(right);
                List<GrammarSymbol> rhs = new ArrayList<>();
                Pattern PATTERN = Pattern.compile("(' '|[^ ]+)");   //匹配空格外的东西, 或者单引号包裹的空格' '. 每个匹配单独分为一组
                Matcher matcher = PATTERN.matcher(right);
                while (matcher.find()) {
                    String tmp = matcher.group().trim();
                    GrammarSymbol rhItem;
                    if (Pattern.matches("'.+'", matcher.group())) {//   如果匹配到内容被单引号包裹, 即为普通字符
                        tmp = matcher.group().substring(1, matcher.group().length() - 1);
                        if (tmp.charAt(0) == '\\') tmp = cookString(tmp);
                        assert tmp.length() == 1;
                        rhItem = GrammarSymbol.newASCII(tmp.charAt(0));
                    } else {
                        GrammarSymbol a = GrammarSymbol.newNonTerminal(tmp);
                        GrammarSymbol b = GrammarSymbol.newToken(tmp);
                        if(this.symbols.contains(a))
                            rhItem = a;
                        else if(this.symbols.contains(b))
                            rhItem = b;
                        else rhItem = null;
                    }
                    assert rhItem != null : "symbol not found in symbols. This error should never occur. symbol=" + tmp;
                    rhs.add(rhItem);
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
        StringBuilder newStartSymbolContent = new StringBuilder(this.startSymbol.content() + "'");
        while (this.symbols.stream().anyMatch(symbol ->
                symbol.content().equals(newStartSymbolContent.toString()))
        ) {
            newStartSymbolContent.append("'");
        }
        GrammarSymbol newStart =GrammarSymbol.newNonTerminal(newStartSymbolContent.toString());
        this.symbols.add(newStart);
        this.producers.add(
                new LR1Producer(
                        newStart,
                        new ArrayList<>(List.of(this.startSymbol)),
                        "$$ = $1; reduceTo(\"" + newStartSymbolContent + "\");"
                )
        );
        this.startSymbol = newStart;
        LR1Producer initProducer = this.producersOf(this.startSymbol).get(0);
        LR1State I0 = this.CLOSURE(
                new LR1State(
                        new ArrayList<>(
                                List.of(
                                        new LR1Item(
                                                initProducer,
                                                0,
                                                this.endSymbol
                                        )
                                )
                        )
                )
        );
        // 初始化自动机
        LR1DFA dfa = new LR1DFA(I0);
        dfa.addState(I0);
        Stack<LR1State> stack = new Stack<>();
        stack.push(I0);
        while (stack.size() > 0) {
            System.out.println(stack.size());
            LR1State I = stack.pop(); // for C中的每个项集I
            for (GrammarSymbol X : this.symbols) {
                // for 每个文法符号X
                LR1State gotoIX = this.GOTO(I, X);
                if (gotoIX.getItems().size() == 0) continue; // gotoIX要非空
                if (dfa.getStates().contains(gotoIX))
                    dfa.link(I, gotoIX, X);
                else {
                    // 新建状态并连接
                    dfa.addState(gotoIX);
                    dfa.link(I, gotoIX, X);
                    stack.push(gotoIX);
                }
            }
        }
        this.dfa = dfa;
    }

    /**
     * 求取GOTO(I, X)
     * 见龙书算法4.53
     */
    private LR1State _GOTO(LR1State I, GrammarSymbol X) {
        LR1State J = new LR1State(new ArrayList<>());
        for (LR1Item item : I.getItems()) {
            // for I中的每一个项
            if (item.dotAtLast()) continue;
            if (item.producer().rhs().get(item.dotPosition()).equals(X)) {
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
    private LR1State GOTO(LR1State i, GrammarSymbol a) {
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
            GrammarSymbol currentSymbol =
                    oneItemOfI.producer().rhs().get(oneItemOfI.dotPosition());
            if ( ! currentSymbol.isType(GrammarSymbolType.NON_TERMINAL)) continue; // 非终结符打头才有闭包
            List<LR1Producer> extendProducers = new ArrayList<>();
            for (LR1Producer producerInG : this.producers) { // for G'中的每个产生式
                // 左手边是当前符号的，就可以作为扩展用
                if (producerInG.lhs().equals(currentSymbol))
                    extendProducers.add(producerInG);
            }
            GrammarSymbol lookahead = oneItemOfI.lookahead();
            for (LR1Producer extendProducer : extendProducers) {
                // 求取新展望符号
                List<GrammarSymbol> newLookaheads = this.FIRST(
                        oneItemOfI.producer().rhs().
                                subList(
                                        oneItemOfI.dotPosition() + 1,
                                        oneItemOfI.producer().rhs().size()
                                )
                );
                // 存在epsilon作为FIRST符，可以用它“闪过”
                if (newLookaheads.contains(this.epsilonSymbol)){
                    newLookaheads.removeIf(v -> v.equals(this.epsilonSymbol));
                    // 闪过，用旧展望符号
                    if (!newLookaheads.contains(lookahead)) {
                        newLookaheads.add(lookahead);
                    }
                }
                // for FIRST(βa)中的每个终结符号b
                for (GrammarSymbol newLookahead : newLookaheads) {
                    LR1Item newItem = new LR1Item(
                            extendProducer,
                            0,
                            newLookahead
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
    public void constructActionGotoTable() {
        List<LR1State> dfaStates = this.dfa.getStates();
        // 初始化 ACTIONTable 和 GOTOTable
        for (int i = 0; i < dfaStates.size(); i++) {
            List<ActionTableCell> actionRow = new ArrayList<>();
            List<Integer> gotoRow = new ArrayList<>();
            for (GrammarSymbol symbol : this.symbols) {
                if (symbol.isType(GrammarSymbolType.NON_TERMINAL)) gotoRow.add(-1);
                else actionRow.add(new ActionTableCell(ActionTableCell.ActionTableCellType.NONE, -1));
            }
            this.ACTIONTable.add(actionRow);
            this.GOTOTable.add(gotoRow);
        }

        Map<GrammarSymbol, Integer> actionLookup = new HashMap<>();
        Map<GrammarSymbol, Integer> gotoLookup = new HashMap<>();
        int actionCount = 0;
        int gotoCount = 0;
        for (GrammarSymbol symbol : this.symbols) {
            if (symbol.isType(GrammarSymbolType.NON_TERMINAL)) gotoLookup.put(symbol, gotoCount++);
            else actionLookup.put(symbol, actionCount++);
        }
        // ===========================
        // ===== 填充ACTIONTable =====
        // ===========================

        // 在该过程中，我们强制处理了所有冲突，保证文法是LR(1)的
        for (int i = 0; i < dfaStates.size(); i++) {
            if (i == dfaStates.size() - 1) {
                for (LR1Item pro : dfaStates.get(i).getItems()) {
                    System.out.println(pro.producer());
                }
            }
            // 处理移进的情况
            // ① [A->α`aβ, b], GOTO(Ii, a) = Ij, ACTION[i, a] = shift(j)
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (item.dotAtLast()) continue; // 没有aβ
                GrammarSymbol a = item.producer().rhs().get(item.dotPosition());
                if (a.isType(GrammarSymbolType.NON_TERMINAL)) continue;
                LR1State _goto = this.getNext(dfaStates.get(i), a);
                for (int j = 0; j < dfaStates.size(); j++)
                    if (Objects.equals(_goto, dfaStates.get(j)))
                        this.ACTIONTable.get(i).set(
                                actionLookup.get(a),
                                new ActionTableCell(ActionTableCell.ActionTableCellType.SHIFT, j)
                        );
            }
            // 处理规约的情况
            // ② [A->α`, a], A!=S', ACTION[i, a] = reduce(A->α)
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (!item.dotAtLast()) continue;
                if (item.producer().equals(this.producers.get(this.producers.size() - 1))) continue; // 增广产生式也不处理
                if (item.lookahead().isType(GrammarSymbolType.NON_TERMINAL))
                    continue; // 展望非终结符的归GOTO表管
                boolean shouldReplace = false;
                if (
                        Objects.equals(
                                this.ACTIONTable.get(i).get(actionLookup.get(item.lookahead()))
                                        .type().getType(), "shift"
                        )
                ) {
                    // 处理移进-规约冲突
                    // 展望符的优先级就是移进的优先级
                    LR1Operator shiftOperator = this.operators.stream()
                            .filter(x -> x.symbol().equals(item.lookahead()))
                            .findFirst().orElse(null);
                    int shiftPrecedence = (shiftOperator != null) ?
                            shiftOperator.precedence() : -1;
                    // 最后一个终结符的优先级就是规约的优先级
                    LR1Operator reduceOperator = null;
                    for (int _i = item.dotPosition() - 1; _i >= 0; _i--) {
                        GrammarSymbol symbol = item.producer().rhs().get(_i);
                        if ( ! symbol.isType(GrammarSymbolType.NON_TERMINAL)) {
                            reduceOperator = this.operators.stream()
                                    .filter(x -> x.symbol().equals(symbol))
                                    .findFirst().orElse(null);
                            break;
                        }
                    }
                    int reducePrecedence = (reduceOperator != null) ?
                            reduceOperator.precedence() : -1;
                    if ( !(shiftOperator == null || reduceOperator == null
                                    || shiftPrecedence == -1 || reducePrecedence == -1)
                    ) {
                        if (reducePrecedence == shiftPrecedence) {
                            if (reduceOperator.assoc().getAssoc().equals("left")) {
                                // 同级的运算符必然具备相同的结合性（因为在.y同一行声明）
                                shouldReplace = true; // 左结合就规约
                            }
                        } else if (reducePrecedence > shiftPrecedence) {
                            shouldReplace = true; // 规约优先级更高，替换为规约
                        }
                    }
                } else if (this.ACTIONTable.get(i).get(actionLookup.get(item.lookahead()))
                        .type().getType().equals("reduce")) {
                    // 处理规约-规约冲突，越早定义的产生式优先级越高
                    // 不可能出现同级产生式
                    if (
                            this.ACTIONTable.get(i).get(actionLookup.get(item.lookahead())).data()
                                    < this.producers.indexOf(item.producer())
                    ) {
                        shouldReplace = true;
                    }
                } else {
                    // 没有冲突
                    this.ACTIONTable.get(i).set(
                            actionLookup.get(item.lookahead()),
                            new ActionTableCell(ActionTableCell.ActionTableCellType.REDUCE, this.producers.indexOf(item.producer()))
                    ); // 使用item.producer号产生式规约
                }
                if (shouldReplace) {
                    this.ACTIONTable.get(i).set(
                            actionLookup.get(item.lookahead()),
                            new ActionTableCell(ActionTableCell.ActionTableCellType.REDUCE, this.producers.indexOf(item.producer()))
                    ); // 使用item.producer号产生式规约
                }
            }
            // 处理接受的情况
            // ③ [S'->S`, $], ACTION[i, $] = acc
            for (LR1Item item : dfaStates.get(i).getItems()) {
                if (
                        item.producer().equals(this.producers.get(this.producers.size() - 1)) &&
                                item.dotAtLast() &&
                                item.lookahead().equals(this.endSymbol)
                ) {
                    this.ACTIONTable.get(i).set(
                            actionLookup.get(this.endSymbol),
                            new ActionTableCell(
                                    ActionTableCell.ActionTableCellType.ACC,
                                    0
                            )
                    );
                }
            }
        }
        // ===========================
        // ====== 填充GOTOTable ======
        // ===========================
        for (int i = 0; i < dfaStates.size(); i++)
            for (GrammarSymbol A : this.symbols) {
                if ( ! A.isType(GrammarSymbolType.NON_TERMINAL)) continue;
                for (int j = 0; j < dfaStates.size(); j++)
                    if (Objects.equals(this.GOTO(dfaStates.get(i), A), dfaStates.get(j)))
                        this.GOTOTable.get(i).set(gotoLookup.get(A), j);
            }
    }

    // 源于LALR.ts的函数中需要用到的一个类
    // 核+该核所在所有状态号的索引
    // {核的内容，包含这个核的状态号的数组}
    static class CoreArrCell {
        public final List<LR1State> states;
        public List<LR1Item> core;

        public CoreArrCell() {
            this.core = new ArrayList<>();
            this.states = new ArrayList<>();
        }
    }

    static class GOTOCacheKey {
        public LR1State i;
        public GrammarSymbol a;
    }
}

