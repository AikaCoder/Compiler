import java.util.*;

class NFA extends FiniteAutomata {
    private Map<State, Action> acceptActionMap; // 接收态对应的动作

    // 构造一个空NFA
    public NFA() {
        super();
        this.acceptActionMap = new HashMap<>(); // 接收态对应的动作
    }

    public Map<State, Action> getAcceptActionMap() {
        return acceptActionMap;
    }

    // 构造一个形如`->0 --a--> [1]`的原子NFA（两个状态，之间用初始字母连接）
    // `initAlpha`也可以为SpAlpha枚举
    public static NFA atom(Object initAlpha) {
        NFA nfa = new NFA();
        // 开始状态
        nfa.startStates.add(new State());
        // 接收状态
        nfa.acceptStates.add(new State());
        // 全部状态
        nfa.states.addAll(nfa.startStates);
        nfa.states.addAll(nfa.acceptStates);
        // 字母表与状态转移邻接链表
        Transform T_temp = new Transform();
        if (initAlpha instanceof String) {
            nfa.alphabet.add((String) initAlpha);
            T_temp.setAlpha(0);
            T_temp.setTarget(1);
            List<Transform> temp_1 = new ArrayList<>();
            List<Transform> temp_2 = new ArrayList<>(); // 为空，表示接收态没有出边
            temp_1.add(T_temp);
            nfa.transformAdjList.add(temp_1);
            nfa.transformAdjList.add(temp_2);
        } else {
            nfa.alphabet.add(SpAlphaUtil.getSpAlpha((int) initAlpha));
            T_temp.setAlpha((int) initAlpha);
            T_temp.setTarget(1);
            List<Transform> temp_1 = new ArrayList<>();
            List<Transform> temp_2 = new ArrayList<>(); // 为空，表示接收态没有出边
            temp_1.add(T_temp);
            nfa.transformAdjList.add(temp_1);
            nfa.transformAdjList.add(temp_2);
        }

        return nfa;
    }

    /*
     * 返回形状一致的新NFA（深拷贝，State的Symbol生成新的，与原NFA互不关联）
     * 其实和FA.java文件中的copy作用相同
     */
    public static NFA copy(NFA nfa) {
        NFA res = new NFA();
        for (int i = 0; i < nfa.states.size(); i++) {
            if (contains(nfa.startStates, nfa.states.get(i))) {
                State newState = new State();
                res.startStates.add(newState);
                res.states.add(newState);
            } else if (contains(nfa.acceptStates, nfa.acceptStates.get(i))) {
                State newState = new State();
                res.acceptStates.add(newState);
                res.states.add(newState);
            } else {
                State newState = new State();
                res.states.add(newState);
            }
        }
        res.alphabet.addAll(nfa.alphabet);
        res.transformAdjList = deepCopy(nfa.transformAdjList);
        return res;
    }

    /**
     * 获得epsilon闭包，即从某状态集合只通过epsilon边所能到达的所有状态（包括自身）
     * // @param states 状态集合
     */
    public List<State> epsilonClosure(List<State> states) {
        List<State> result = new ArrayList<>(states);
        int[] epsilonspAlpha = {SpAlpha.EPSILON.getValue()};
        // stream可以提供一种类似于数据库查询的方式
        for (int i = 0; i < result.size(); i++) {
            List<State> transitions = getTransforms(result.get(i), epsilonspAlpha).stream()
                    .map(t -> states.get(t.getTarget())).filter(s -> !result.contains(s)).toList();
            result.addAll(transitions);
        }
        return result;
    }

    /**
     * 返回从某状态收到一个字母并消耗它后，能到达的所有其他状态（考虑了利用epsilon边进行预先和预后扩展）
     * // @param state 某状态
     * // @param alpha 字母在字母表的下标
     */
    public List<State> expand(State state, int alpha) {
        List<State> preExpand = epsilonClosure(List.of(state)); // 所有可行的出发状态
        List<State> result = new ArrayList<>();
        for (State s : preExpand) {
            List<Transform> transforms = getTransforms(s, null); // 该状态的所有转移
            for (Transform t : transforms) {
                if (
                        t.getAlpha() == alpha ||
                                (t.getAlpha() == SpAlpha.ANY.getValue() &&
                                        !(this.alphabet.get(alpha).equals("\n")))
                ) {
                    result.add(this.states.get(t.getTarget())); // 能够吃掉当前字符后到达的所有状态
                }
            }
        }
        return new ArrayList<>(epsilonClosure(result)); // 再考虑一次epsilon拓展
    }

    /**
     * 返回从某状态集合通过一个字母能到达的所有状态（没有考虑epsilon边扩展）
     * // @param states 状态集合
     * // @param alpha 字母在字母表的下标
     */
    public List<State> move(List<State> states, int alpha) {
        List<State> result = new ArrayList<>();
        for (State s : states) {
            List<Transform> transforms = getTransforms(s, null);
            for (Transform t : transforms) {
                if (
                        t.getAlpha() == alpha ||
                                (t.getAlpha() == SpAlpha.ANY.getValue() &&
                                        !(this.alphabet.get(alpha).equals("\n")))
                ) {
                    // 这里注意分辨一下states到底是函数参数还是类参数
                    State targetState = this.states.get(t.getTarget());
                    if (!(result.contains(targetState))) {
                        result.add(targetState);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 把`from`中的每个状态到`to`中的每个状态用字母alpha建立边
     * // @param alpha 字母在字母表的下标
     */
    public void link(List<State> from, List<State> to, int alpha) {
        for (State f : from) {
            List<Transform> transforms = getTransforms(f, null);
            for (State t : to) {
                Transform newTransform = new Transform();
                newTransform.setAlpha(alpha);
                newTransform.setTarget(states.indexOf(t));
                transforms.add(newTransform);
            }
            this.setTransforms(f, transforms);
        }
    }

    /**
     * 把`from`中的每个状态到`to`中的每个状态建立epsilon边
     */
    public void linkEpsilon(List<State> from, List<State> to) {
        this.link(from, to, SpAlpha.EPSILON.getValue());
    }

    /**
     * 将当前NFA原地做Kleene闭包（星闭包），见龙书3.7.1节图3-34
     * ```
     *      ________________ε_______________
     *     |                                ↓
     * 新开始 -ε-> 旧开始 --...--> 旧接收 -ε-> 新接收
     *              ↑______ε______|
     * ```
     */
    public void kleene() {
        // new_start --epsilon--> old_start
        List<State> oldStartstates = new ArrayList<>();
        for (State s : this.startStates) {
            State temp = new State(s.getUuid());
            oldStartstates.add(temp);
        }
        State newStartState = new State();
        this.startStates = new ArrayList<>();
        this.startStates.add(newStartState);
        this.states.add(newStartState);
        this.transformAdjList.add(new ArrayList<>());
        linkEpsilon(this.startStates, oldStartstates);
        // old_accept --epsilon--> new_accept
        List<State> oldAcceptstates = new ArrayList<>();
        for (State s : this.acceptStates) {
            State temp = new State(s.getUuid());
            oldAcceptstates.add(temp);
        }
        State newAcceptState = new State();
        this.acceptStates = new ArrayList<>();
        this.acceptStates.add(newAcceptState);
        this.states.add(newAcceptState);
        this.transformAdjList.add(new ArrayList<>());
        linkEpsilon(oldAcceptstates, this.acceptStates);
        // new_start --epsilon--> new_accept
        this.linkEpsilon(this.startStates, this.acceptStates);
        // old_accept --epsilon--> old_start
        this.linkEpsilon(oldAcceptstates, oldStartstates);
    }

    /**
     * 检测该状态是否到达接收状态（考虑了借助epsilon边预后扩展）
     */
    public boolean hasReachedAccept (State currentState) {
        // 不考虑epsilon边
        if (this.acceptStates.contains(currentState)) {
            return true;
        }
        // 考虑epsilon边
        Stack<State> stack = new Stack<>();
        stack.push(currentState);
        int[] epsilonspAlpha = {SpAlpha.EPSILON.getValue()};
        while (!stack.isEmpty()) {
            List<Transform> transforms = getTransforms(stack.pop(), epsilonspAlpha);
            for (Transform transform : transforms) {
                // 遍历所有epsilon转移
                State targetState = this.states.get(transform.getTarget());
                // 如果到达接收状态就返回真
                if (this.acceptStates.contains(targetState)) {
                    return true;
                }
                // 否则放入栈等待进一步拓展
                else if (!(stack.contains(targetState))) {
                    stack.push(targetState);
                }
            }
        }
        return false;
    }

    /**
     * 就地合并`from`的状态转移表到`to`的。请保证先合并状态和字母表
     */
    public static void mergeTransformAdjList(NFA from, NFA to) {
        // 这里需要注意，原代码中的let关键字，其实是一种浅复制
        List<List<Transform>> mergedAdjList = to.transformAdjList;
        for (List<Transform> transforms : from.transformAdjList) {
            List<Transform> mergedTransforms = new ArrayList<>();
            // 重构from中的所有转移
            for(Transform transform : transforms) {
                int indexOfAlphaInTo = transform.getAlpha() < 0 ?
                        transform.getAlpha() : to.alphabet.indexOf(from.alphabet.get(transform.getAlpha()));
                int indexOfTargetInTo = to.states.indexOf(from.states.get(transform.getTarget()));
                Transform newTransform = new Transform();
                newTransform.setAlpha(indexOfAlphaInTo);
                newTransform.setTarget(indexOfTargetInTo);
                mergedTransforms.add(newTransform);
            }
            mergedAdjList.add(mergedTransforms); // 这里是一种浅拷贝，修改mergedAdjList其实就是修改to.transformAdjList
        }
    }

    /**
     * 串联两个NFA，丢弃所有动作
     * ```
     * NFA1 --epsilon--> NFA2
     * ```
     */
    public static NFA serial (NFA nfa1, NFA nfa2) {
        NFA res = new NFA();
        // 处理开始状态、接收状态、状态、字母表
        // 这里姑且采用浅拷贝，如有需要，后面加以修改
        res.startStates.addAll(nfa1.startStates);
        res.acceptStates.addAll(nfa2.acceptStates);
        res.states.addAll(nfa1.states);
        res.states.addAll(nfa2.states);
        // 请注意，由于使用Set去重后展开，无法保证字母的下标与原先一致！
        List<String> tempAlphabet = new ArrayList<>();
        tempAlphabet.addAll(nfa1.alphabet);
        tempAlphabet.addAll(nfa2.alphabet);
        res.alphabet.addAll(tempAlphabet);
        mergeTransformAdjList(nfa1, res);
        mergeTransformAdjList(nfa2, res);
        res.linkEpsilon(nfa1.acceptStates, nfa2.startStates);
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
        // 合并alphabet
        List<String> tempAlphabet = new ArrayList<>();
        tempAlphabet.addAll(nfa1.alphabet);
        tempAlphabet.addAll(nfa2.alphabet);
        res.alphabet.addAll(tempAlphabet);
        // 构造状态表
        res.states.addAll(res.startStates);
        res.states.addAll(nfa1.states);
        res.states.addAll(nfa2.states);
        res.states.addAll(res.acceptStates);
        res.transformAdjList.add(new ArrayList<>()); // new_start
        mergeTransformAdjList(nfa1, res);
        mergeTransformAdjList(nfa2, res);
        res.transformAdjList.add(new ArrayList<>()); // new_accept
        res.linkEpsilon(res.startStates, nfa1.startStates);
        res.linkEpsilon(res.startStates, nfa2.startStates);
        res.linkEpsilon(nfa1.acceptStates, res.acceptStates);
        res.linkEpsilon(nfa2.acceptStates, res.acceptStates);
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
        List<String> tempAlphabet = new ArrayList<>();
        res.startStates.add(new State());
        res.states.addAll(res.startStates);
        for (NFA nfa : nfas) {
            res.acceptStates.addAll(nfa.acceptStates);
            res.states.addAll(nfa.states);
            tempAlphabet.addAll(nfa.alphabet);
            for (State acc : nfa.acceptStates) {
                res.acceptActionMap.put(acc, nfa.acceptActionMap.get(acc));
            }
        }
        res.alphabet.addAll(tempAlphabet);
        res.transformAdjList.add(new ArrayList<>()); // new_start
        for (NFA nfa : nfas) {
            mergeTransformAdjList(nfa, res);
        }
        for (NFA nfa : nfas) {
            res.linkEpsilon(res.startStates, nfa.startStates);
        }
        return res;
    }

    private static <T> int findIndex(List<T> array, T value) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(value)) return i;
        }
        return -1;
    }

    private static boolean contains(List<State> array, State value) {
        for (State s : array) if (s.same(value)) return true;
        return false;
    }

    private static boolean contains(List<Transform> array, Transform value){
        for (Transform t : array) if (t.same(value)) return true;
        return false;
    }

    private static boolean contains(int[] array, int value) {
        for (int i : array) if (i == value) return true;
        return false;
    }

    private static boolean contains(Object[] array, Object value) {
        for (Object o : array) if (o.equals(value)) return true;
        return false;
    }

    private static List<List<Transform>> deepCopy(List<List<Transform>> original) {
        if (original == null) {
            return null;
        }
        final List<List<Transform>> result = new ArrayList<>();
        for (List<Transform> lt : original) {
            List<Transform> temp = new ArrayList<>();
            for(Transform t : lt){
                Transform t_temp = new Transform();
                t_temp.setAlpha(t.getAlpha());
                t_temp.setTarget(t.getTarget());
                temp.add(t_temp);
            }
            result.add(temp);
        }
        return result;
    }
}
