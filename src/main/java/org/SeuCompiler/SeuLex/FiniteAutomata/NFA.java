package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.*;
import org.SeuCompiler.Exception.CompilerErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexChar;
import org.SeuCompiler.SeuLex.LexParser.LexParser;
import org.SeuCompiler.SeuLex.LexNode.LexNode;
import org.SeuCompiler.SeuLex.LexNode.LexOperator;
import org.SeuCompiler.SeuLex.Regex.LexRegex;
import org.SeuCompiler.SeuLex.Visualizer;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public final class NFA extends FA{
    private TransformTable transforms = new TransformTable();

    public NFA(LexRegex regex, Action action) throws SeuCompilerException {
        this();
        Stack<NFA> stack = new Stack<>();
        for(LexNode lexNode : regex.getPostfixExpression()) {
            if (lexNode.isCharacter()) {
                stack.push(new NFA(lexNode.getLexChar()));
            } else {
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
                        nfa.transforms.linkByEpsilon(nfa.startStates, nfa.acceptStates);
                        stack.push(nfa);
                    }
                    default -> throw new SeuCompilerException(
                            CompilerErrorCode.UNEXPECTED_OPERATOR,
                            "操作符为: " + operator.getCharacter()
                    );
                }
            }
        }

        if(stack.size()!=1)
            throw new SeuCompilerException(CompilerErrorCode.BUILD_NFA_FAILED);
        NFA res = stack.pop();

        this.startStates.addAll(res.startStates);
        this.acceptStates.addAll(res.acceptStates);
        this.states.addAll(res.states);
        this.transforms.putAll(res.transforms);
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
            this.transforms.merge(nfa.transforms);
            this.transforms.linkByEpsilon(this.startStates, nfa.startStates);
            this.acceptActionMap.putAll(nfa.acceptActionMap);
        }
        System.out.print("========== build NFAs complete ==========\n");
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
        this.transforms.put(start, new Transform(init, Set.of(end)));
    }

    /**
     * 拷贝实现函数, 会生成新状态, 但底层字符不会被拷贝
     * @param other 原本NFA
     */
    private void copyFrom(NFA other){
        Map<State, State> oldNewMap = new HashMap<>();
        for (State s : other.states) {
            State newState = new State();
            if (other.startStates.contains(s)) this.startStates.add(newState);
            if (other.acceptStates.contains(s)) this.acceptStates.add(newState);
            this.states.add(newState);
            oldNewMap.put(s, newState);
        }
        this.transforms.putAll(other.transforms.copyByMap(oldNewMap));
        other.acceptActionMap.forEach((state, action) ->
                this.acceptActionMap.put(oldNewMap.get(state), action)
        );
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
        this.startStates.clear();
        this.startStates.add(newStart);      //将新状态放入对应集合
        this.states.add(newStart);
        this.transforms.linkByEpsilon(this.startStates, oldStarts);                //将新旧状态相连

        Set<State> oldAccepts = new HashSet<>(this.acceptStates);
        State newAccept = new State();
        this.acceptStates.clear();
        this.acceptStates.add(newAccept);
        this.states.add(newAccept);
        this.transforms.linkByEpsilon(oldAccepts, this.acceptStates);

        this.transforms.linkByEpsilon(this.startStates, this.acceptStates);
        this.transforms.linkByEpsilon(oldAccepts, oldStarts);
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
        res.transforms.merge(nfa1.transforms);
        res.transforms.merge(nfa2.transforms);
        res.transforms.linkByEpsilon(nfa1.acceptStates, nfa2.startStates);
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

        res.states.addAll(res.startStates);
        res.states.addAll(nfa1.states);
        res.states.addAll(nfa2.states);
        res.states.addAll(res.acceptStates);

        res.transforms.merge(nfa1.transforms);
        res.transforms.merge(nfa2.transforms);
        res.transforms.linkByEpsilon(res.startStates, nfa1.startStates);
        res.transforms.linkByEpsilon(res.startStates, nfa2.startStates);
        res.transforms.linkByEpsilon(nfa1.acceptStates, res.acceptStates);
        res.transforms.linkByEpsilon(nfa2.acceptStates, res.acceptStates);
        return res;
    }
}
