package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.SeuCompiler.Exception.CompilerErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexChar;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;
import org.SeuCompiler.SeuLex.LexParser.LexParser;
import org.SeuCompiler.SeuLex.LexNode.LexNode;
import org.SeuCompiler.SeuLex.LexNode.LexOperator;
import org.SeuCompiler.SeuLex.Regex.LexRegex;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NFA{
    private Set<State> startStates = new HashSet<>();
    private Set<State> acceptStates = new HashSet<>();
    private Set<State> states = new HashSet<>();   //使用ArrayList主要是为了方便拷贝时查询
    private Set<LexChar> alphabet = new HashSet<>();
    private Map<State, Transform> transforms = new HashMap<>();
    private Map<State, Action> acceptActionMap = new HashMap<>();

    public NFA(LexRegex regex, Action action) throws SeuCompilerException {
        this();
        int step = 0;
        Stack<NFA> stack = new Stack<>();
        for(LexNode lexNode : regex.getPostfixExpression()){
            step++;
            if(!lexNode.isOperator()){
                stack.push(new NFA(lexNode.getLexChar()));
            }else {
                LexOperator operator = lexNode.getOperator();
                switch (operator) {
                    case OR -> stack.push(parallel(stack.pop(), stack.pop()));  // |
                    case DOT -> {   //dot
                        NFA nfa2 = stack.pop();
                        NFA nfa1 = stack.pop();
                        stack.push(serial(nfa1, nfa2));
                    }
                    case STAR -> stack.peek().kleene();
                    case ADD -> {   //A+ -> AA*
                        NFA nfa = stack.pop();
                        NFA copy = new NFA(nfa);
                        copy.kleene();
                        stack.push(serial(nfa, copy));
                    }
                    case QUESTION -> { //A? -> A|\ε
                        NFA nfa = stack.pop();
                        nfa.linkByEpsilon(nfa.startStates, nfa.acceptStates);
                        stack.push(nfa);
                    }
                    default -> throw new SeuCompilerException(
                            CompilerErrorCode.UNEXPECTED_OPERATOR,
                            "操作符为: " + operator.getCharacter()
                    );
                }
            }
            //todo
//            if(step < 10) {
//                System.out.printf("\n--------step %d--------\n", step);
//                stack.peek().print();
//            }
//            else System.out.printf("---step %d: success.\n", step);
        }

        if(stack.size()!=1)
            throw new SeuCompilerException(CompilerErrorCode.BUILD_NFA_FAILED);
        NFA res = stack.pop();

        this.startStates = res.startStates;
        this.acceptStates = res.acceptStates;
        this.states = res.states;
        this.transforms = res.transforms;
        this.alphabet = res.alphabet;

        if(action!=null)
            this.acceptStates.forEach(s -> this.acceptActionMap.put(s, action));
    }

    public NFA(LexParser parser) throws SeuCompilerException {
        this();
        List<NFA> nfas = new ArrayList<>();
        int count = 1;
        for(LexRegex regex : parser.getRegexActionMap().keySet()){
            System.out.printf("========== build NFA_%d ==========\n", count++);
            nfas.add(new NFA(regex, parser.getRegexActionMap().get(regex)));
        }
        State start = new State();
        this.startStates.add(start);
        this.states.add(start);
        for (NFA nfa : nfas) {
            this.acceptStates.addAll(nfa.acceptStates);
            this.states.addAll(nfa.states);
            this.alphabet.addAll(nfa.alphabet);
            this.addTransformsFrom(nfa);
            this.linkByEpsilon(this.startStates, nfa.startStates);
            this.acceptActionMap.putAll(nfa.acceptActionMap);
        }
        System.out.print("========== build complete ==========\n");
        //print();
        //todo
    }

    /**
     * 拷贝构造函数, 会生成新状态, 但底层字符不会被拷贝
     * @param other 原本NFA
     */
    public NFA(NFA other) {
        this();
        copyFrom(other);
    }

    /**
     * 由初始字符构造一个形如`0 --a--> 0`的原子NFA
     * @param init 初始字符串
     */
    public NFA(LexChar init){
        this();
        State start = new State();
        State end = new State();
        this.startStates.add(start);
        this.acceptStates.add(end);
        this.states.addAll(Set.of(start,end));
        this.alphabet.add(init);
        this.transforms.put(start, new Transform(init, Set.of(end)));
    }

    /**
     * 拷贝实现函数, 会生成新状态, 但底层字符不会被拷贝
     * @param other 原本NFA
     */
    private void copyFrom(NFA other){
        this.alphabet.addAll(other.alphabet);

        Map<State, State> oldNewMap = new HashMap<>();
        for (State s : other.states) {
            State newState = new State();
            if (other.startStates.contains(s)) this.startStates.add(newState);
            if (other.acceptStates.contains(s)) this.acceptStates.add(newState);
            this.states.add(newState);
            oldNewMap.put(s, newState);
        }

        other.transforms.forEach((begin, transform) ->
                this.transforms.put(oldNewMap.get(begin), transform.copyByMap(oldNewMap))
        );
        other.acceptActionMap.forEach((state, action) ->
                this.acceptActionMap.put(oldNewMap.get(state), action)
        );
    }


    /**
     * 获得epsilon闭包，即从某状态集合只通过epsilon边所能到达的所有状态（包括自身）
     * @param states 初始状态集
     * @return 闭包状态集
     */
    public Set<State> epsilonClosure(Set<State> states){
        Set<State> res =  new HashSet<>(states);
        states.forEach((s) ->{
            Set<State> addStates = getNextStatesByEpsilon(s);
            if(!addStates.isEmpty())
                addStates.addAll(epsilonClosure(addStates));
            res.addAll(addStates);
        });
        return res;
    }


    /**
     * 从每个状态出发, 不同非空字符的映射关系, 考虑了epsilon后扩展
     * @param start 起始状态
     * @return 字符 -> 经过Epsilon扩展的状态集合
     */
    public Transform expandTransformWithEpsilon(Set<State> start){
        Transform res = new Transform();
        for(State s : start){
            if(!this.transforms.containsKey(s)) continue;
            this.transforms.get(s).getMap().forEach((str, states) -> {
                if(str.equalsTo(SpecialChar.EPSILON)) return;
                res.add(str, epsilonClosure(states));
            });
        }
        return res;
    }

    /**
     * 把`begin`中的每个状态到`end`中的每个状态用字母inStr建立边
     * @param begins 开始状态集
     * @param ends 结束状态集
     * @param inStr 输入字符
     */
    public void link(Set<State> begins, Set<State> ends, LexChar inStr){
        begins.forEach(b ->  addTransform(b, new Transform(inStr, new HashSet<>(ends))));
    }

    /**
     * 把`begin`中的每个状态到`end`中的每个状态用Epsilon建立边
     * @param begins 开始状态集
     * @param ends  结束状态集
     */
    public void linkByEpsilon(Set<State> begins, Set<State> ends){
        this.link(begins, ends, SpecialChar.EPSILON.toFAChar());
    }

    /**
     * 将当前NFA **原地** 做Kleene闭包（星闭包）
     * 注意, 会对当前nfa造成影响
     * ```
     *      ________________ε_______________
     *     |                                ↓
     * 新开始 -ε-> 旧开始 --...--> 旧接收 -ε-> 新接收
     *              ↑______ε______|
     * ```
     */
    public void kleene(){
        Set<State> oldStarts = new HashSet<>(this.startStates);    //保存原有starts 到 oldStarts
        State newStart = new State();                              //新建一个状态作为start
        this.startStates = new HashSet<>(Set.of(newStart));      //将新状态放入对应集合
        this.states.add(newStart);
        linkByEpsilon(this.startStates, oldStarts);                //将新旧状态相连

        Set<State> oldAccepts = new HashSet<>(this.acceptStates);
        State newAccept = new State();
        this.acceptStates = new HashSet<>(Set.of(newAccept));
        this.states.add(newAccept);
        linkByEpsilon(oldAccepts, this.acceptStates);

        linkByEpsilon(this.startStates, this.acceptStates);
        linkByEpsilon(oldAccepts, oldStarts);
    }

    /**
     * 串联两个NFA，丢弃所有动作
     * ```
     * NFA1 --epsilon--> NFA2
     * ```
     */
    public static NFA serial(NFA nfa1, NFA nfa2){
        NFA res = new NFA();
        res.startStates.addAll(nfa1.startStates);
        res.acceptStates.addAll(nfa2.acceptStates);
        res.states.addAll(nfa1.states);
        res.states.addAll(nfa2.states);
        res.alphabet.addAll(nfa1.alphabet);
        res.alphabet.addAll(nfa2.alphabet);
        res.addTransformsFrom(nfa1);
        res.addTransformsFrom(nfa2);
        res.linkByEpsilon(nfa1.acceptStates, nfa2.startStates);
        return res;
    }

    /**
     * 并联两个NFA（对应于|或运算），收束尾部，丢弃所有动作
     * ```
     *             ε  NFA1  ε
     * new_start <             > new_accept
     *             ε  NFA2  ε
     * ```
     */
    public static NFA parallel (NFA nfa1, NFA nfa2) {
        NFA res = new NFA();
        res.startStates.add(new State());
        res.acceptStates.add(new State());
        res.alphabet.addAll(nfa1.alphabet);
        res.alphabet.addAll(nfa2.alphabet);

        res.states.addAll(res.startStates);
        res.states.addAll(nfa1.states);
        res.states.addAll(nfa2.states);
        res.states.addAll(res.acceptStates);

        res.addTransformsFrom(nfa1);
        res.addTransformsFrom(nfa2);
        res.linkByEpsilon(res.startStates, nfa1.startStates);
        res.linkByEpsilon(res.startStates, nfa2.startStates);
        res.linkByEpsilon(nfa1.acceptStates, res.acceptStates);
        res.linkByEpsilon(nfa2.acceptStates, res.acceptStates);
        return res;
    }

    /**
     * 将两个NFA的转换关系transforms合并(from -> to), 需要先合并状态和字母表
     * @param others 用来合并的nfa
     */
    public void addTransformsFrom(NFA others){
        others.transforms.forEach(this::addTransform);
    }

    void addTransform(State start, Transform transform){
        if(this.transforms.containsKey(start))
            this.transforms.get(start).merge(transform);
        else
            this.transforms.put(start, transform);
    }

    Set<State> getNextStates(State start, LexChar lexChar){
        Set<State> res = new HashSet<>();
        if(!this.transforms.containsKey(start))
            return res;
        Set<State> temp = this.transforms.get(start).getMap().get(lexChar);
        if(temp != null)
            res.addAll(temp);
        return res;
    }

    Set<State> getNextStatesByEpsilon(State start){
        return getNextStates(start, SpecialChar.EPSILON.toFAChar());
    }

    public void print(){
        System.out.println("start states:");
        for(State s : this.states){
            if(this.startStates.contains(s))
                System.out.println("\t"+s.getIndex());
        }
        System.out.print("accept states:\n");
        StringBuilder builder = new StringBuilder("\t");
        this.acceptStates.forEach(s -> builder.append(s.getIndex()).append(", "));
        System.out.print(builder.append("\n"));
        System.out.print("transform table\n");
        System.out.printf("%-8s%-8s%-20s\n", "start", "char", "end");
        this.transforms.forEach((start, transforms) -> transforms.getMap().forEach(((faChar, ends) -> {
            StringBuilder endsBuilder = new StringBuilder();
            for(State end : ends){
                endsBuilder.append(end.getIndex()).append(", ");
            }
            System.out.printf("%-8d%-8s%-20s\n", start.getIndex(), faChar.getString(), endsBuilder);
        })));
    }
}
