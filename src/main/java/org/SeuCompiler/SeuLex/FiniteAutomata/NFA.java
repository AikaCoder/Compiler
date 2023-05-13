package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NFA{
    protected Set<State> startStates = new HashSet<>();
    protected Set<State> acceptStates = new HashSet<>();
    protected Set<State> states = new HashSet<>();   //使用ArrayList主要是为了方便拷贝时查询
    protected Set<FAString> alphabet = new HashSet<>();
    protected Map<State, Transform> transforms = new HashMap<>();
    private final Map<State, Action> acceptActionMap = new HashMap<>();

    public NFA copy() {
        NFA nfa = new NFA();
        nfa.alphabet.addAll(this.alphabet);

        Map<State, State> oldNewMap = new HashMap<>();
        for (State s : this.states) {
            State newState = new State();
            if (this.startStates.contains(s)) nfa.startStates.add(newState);
            if (this.acceptStates.contains(s)) nfa.acceptStates.add(newState);
            nfa.states.add(newState);
            oldNewMap.put(s, newState);
        }

        this.transforms.forEach((begin, transform) -> {
            nfa.transforms.put(oldNewMap.get(begin), transform.copyByMap(oldNewMap));
        });
        this.acceptActionMap.forEach((state, action) -> {
            nfa.acceptActionMap.put(oldNewMap.get(state), action);
        });
        return nfa;
    }

    /**
     * 构造一个形如`->0 --a--> [1]`的原子NFA（两个状态，之间用初始字母连接）
     * @param init 初始字符串
     * @return 原子NFA
     */
    public static NFA atom(FAString init){
        NFA nfa = new NFA();
        State start = new State();
        State end = new State();
        nfa.startStates.add(start);
        nfa.acceptStates.add(end);
        nfa.states.addAll(nfa.startStates);
        nfa.states.addAll(nfa.acceptStates);
        nfa.alphabet.add(init);
        nfa.transforms.put(start, new Transform(init, Set.of(end)));
        return nfa;
    }

    /**
     * 获得epsilon闭包，即从某状态集合只通过epsilon边所能到达的所有状态（包括自身）
     * @param states 初始状态集
     * @return 闭包状态集
     */
    public Set<State> epsilonClosure(Set<State> states){
        Set<State> res =  new HashSet<>(states);
        res.forEach((s) ->{
            Set<State> addStates = getNextStates(s, SpecialChar.EPSILON)
                    .stream()
                    .filter(state -> !res.contains(state))
                    .collect(Collectors.toSet());
            res.addAll(addStates);
        });
        return res;
    }

    /**
     * 返回从某状态收到一个字母并消耗它后，能到达的所有其他状态（考虑了利用epsilon边进行预先和预后扩展）
     * @param start 起始状态
     * @param inStr 输入字符
     * @return 扩展状态集
     */
    public Set<State> expandWithEpsilonClosure(State start, FAString inStr){
        Set<State> preExpand = epsilonClosure(Set.of(start));
        Set<State> res = new HashSet<>();
        preExpand.forEach(s -> {
            res.addAll(getNextStates(s,inStr));
            if(!inStr.equalsToStr("\n"))
                res.addAll(getNextStates(s, SpecialChar.ANY));
        });
        return new HashSet<>(epsilonClosure(res));
    }

    /**
     * 返回从某状态集合通过一个字母能到达的所有状态（没有考虑epsilon边扩展）
     * @param start 起始状态
     * @param inStr 输入字符
     * @return 扩展状态集
     */
    public Set<State> expand(Set<State> start, FAString inStr){
        Set<State> res = new HashSet<>();
        start.forEach(s -> {
            res.addAll(getNextStates(s,inStr));
            if(!inStr.equalsToStr("\n"))
                res.addAll(getNextStates(s, SpecialChar.ANY));
        });
        return new HashSet<>(epsilonClosure(res));
    }

    /**
     * 从每个状态出发, 不同字符的映射关系, 考虑了epsilon后扩展
     * @param start 起始状态
     * @return 字符 -> 经过Epsilon扩展的状态集合
     */
    public Transform expandTransformWithEpsilon(Set<State> start){
        Transform res = new Transform();
        for(State s : start){
            transforms.get(s).getMap().forEach((str, states) -> {
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
    public void link(Set<State> begins, Set<State> ends, FAString inStr){
        begins.forEach(b -> {
            Set<State> newEnds = new HashSet<>(ends);
            this.transforms.put(b, new Transform(inStr, newEnds));
        });
    }

    /**
     * 把`begin`中的每个状态到`end`中的每个状态用Epsilon建立边
     * @param begins 开始状态集
     * @param ends  结束状态集
     */
    public void linkByEpsilon(Set<State> begins, Set<State> ends){
        this.link(begins, ends, SpecialChar.EPSILON.toFAString());
    }

    /**
     * 将当前NFA原地做Kleene闭包（星闭包）
     * ```
     *      ________________ε_______________
     *     |                                ↓
     * 新开始 -ε-> 旧开始 --...--> 旧接收 -ε-> 新接收
     *              ↑______ε______|
     * ```
     */
    public void kleene(){
        Set<State> oldStarts = new HashSet<>(this.startStates);    //保存原有starts 到 oldStarts
        State newStarts = new State();                              //新建一个状态作为start
        this.startStates = new HashSet<>(){{add(newStarts);}};      //将新状态放入对应集合
        this.states.add(newStarts);
        linkByEpsilon(this.startStates, oldStarts);                //将新旧状态相连

        Set<State> oldAccepts = new HashSet<>(this.acceptStates);
        State newAccepts = new State();
        this.startStates = new HashSet<>(){{add(newAccepts);}};
        this.states.add(newAccepts);
        linkByEpsilon(oldAccepts, this.acceptStates);

        linkByEpsilon(this.startStates, this.acceptStates);
        linkByEpsilon(oldAccepts, oldStarts);
    }

    /**
     * 检测当前状态是否是可接受状态(考虑了epsilon扩展)
     * @param current 目标当前状态
     * @return 如果为可接受状态, 返回真
     */
    public boolean isAcceptable(State current){
        if(this.acceptStates.contains(current)) return true;

        Stack<State> stack = new Stack<>();
        stack.push(current);
        while (!stack.empty()){
            for(State s : getNextStates(stack.pop(), SpecialChar.EPSILON)){
                if(this.acceptStates.contains(s)) return true;
                if(!stack.contains(s)) stack.push(s);
            }
        }
        return false;
    }

    /**
     * 将两个NFA的转换关系transforms合并(from -> to), 需要先合并状态和字母表
     * @param others 用来合并的nfa
     */
    public void addTransformsFrom(NFA others){
        others.transforms.forEach(this::addTransform);
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
     * 并联所有NFA（对应于|或运算），不收束尾部
     * ```
     *             ε  NFA1
     * new_start <-   ...
     *             ε  NFAn
     * ```
     */
    public static NFA parallelAll (List<NFA> nfas) {
        NFA res = new NFA();
        res.startStates.add(new State());
        res.states.addAll(res.startStates);
        for (NFA nfa : nfas) {
            res.acceptStates.addAll(nfa.acceptStates);
            res.states.addAll(nfa.states);
            res.alphabet.addAll(nfa.alphabet);
            for (State acc : nfa.acceptStates) {
                res.acceptActionMap.put(acc, nfa.acceptActionMap.get(acc));
            }
        }
        for (NFA nfa : nfas)
            res.addTransformsFrom(nfa);
        for (NFA nfa : nfas)
            res.linkByEpsilon(res.startStates, nfa.startStates);
        return res;
    }
    void addTransform(State start, Transform transform){
        if(this.transforms.containsKey(start))
            this.transforms.get(start).merge(transform);
        else
            this.transforms.put(start, transform);
    }

    void addTransform(State start, FAString inStr, Set<State> states){
        if(this.transforms.containsKey(start))
            this.transforms.get(start).add(inStr, states);
        else
            this.transforms.put(start, new Transform(inStr, states));
    }

    /**
     * 得到从State出发的所有一步转移关系
     * 浅拷贝, 得到的结果与原 Map 无关, 但没有新建State
     * @param start 出发的状态
     * @return FAChar -> target 映射
     */
    Transform getTransformFrom(State start){
        return new Transform(this.transforms.get(start));
    }

    /**
     * 得到从State出发的所有状态
     * 没有新建State
     * @param start 出发的状态
     * @return 目标状态集合
     */
    Set<State> getNextStates(State start){
        return this.transforms.get(start).getAllStates();
    }

    /**
     * 得到从State出发, 且输入字符 ch 内的所有一步转移关系
     * 浅拷贝, 得到的结果与原 Map 无关, 但没有新建State
     * @param start 出发的状态
     * @param ch 输入字符
     * @return FAChar -> target 映射
     */
    Transform getTransformFrom(State start, FAString ch){
        return this.transforms.get(start).getTransform(ch);
    }

    Set<State> getNextStates(State start, FAString str){
        return this.transforms.get(start).getAllStates(str);
    }

    Set<State> getNextStates(State start, String str){
        return this.transforms.get(start).getAllStates(new FAString(str));
    }

    Set<State> getNextStates(State start, SpecialChar spChar){
        return this.transforms.get(start).getAllStates(spChar.toFAString());
    }
}
