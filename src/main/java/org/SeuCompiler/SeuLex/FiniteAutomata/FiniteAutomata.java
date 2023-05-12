/**
 * 有限状态自动机（FA）的相关内容。裴爱华 2023/3/25
 */

package org.SeuCompiler.SeuLex.FiniteAutomata;

import java.util.*;
import java.util.function.Predicate;
import java.lang.Object;
import java.util.stream.Collectors;

/**
 * 自动机状态
 */
class State{
    private final String uuid; // 每个状态给一个全局唯一ID

    // 构造函数，当给出uuid的具体值时，给它赋值为这个ID，否则随机生成uuid
    public State() {
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public State(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    // 判断两个状态是不是一样的

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(uuid, state.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

/**
 * 特殊字符枚举
 */
enum SpAlpha {
    EPSILON(-1, "[ε]"), // ε
    ANY(-2, "[any]"), // . (any character, except \n, not ε)
    OTHER(-3, "[other]"); // other character not mentioned

    private final int value;
    private final String str;

    SpAlpha(int value, String str) {
        this.value = value;
        this.str = str;
    }

    public int getValue() {
        return value;
    }

    public String getStr() {
        return str;
    }
}

/**
 * 自动机状态转换
 */
class Transform {
    private Integer alpha; // 边上的字母（转换的条件）在this._alphabets中的下标，特殊下标见enum SpAlpha
    private Integer target; // 目标状态在this._states中的下标

    Transform(int alpha, int target){
        this.alpha = alpha;
        this.target = target;
    }

    Transform(){};

    public Integer getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transform transform = (Transform) o;
        return Objects.equals(alpha, transform.alpha) && Objects.equals(target, transform.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alpha, target);
    }
}

/**
 * 有限状态自动机
 */
class FiniteAutomata {
    protected List<String> alphabet; // 字母表
    protected List<State> states; // 全部状态
    protected List<State> startStates; // 初始状态
    protected List<State> acceptStates; // 可接受状态
    protected List<List<Transform>> transformAdjList; // 状态转移邻接链表

    public List<State> getStartStates() {
        return startStates;
    }

    public List<State> getAcceptStates() {
        return acceptStates;
    }

    public List<State> getStates() {
        return states;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public List<List<Transform>> getTransformAdjList() {
        return transformAdjList;
    }

    /**
     * 获得从该状态出发的所有一步转移（不自动扩展）
     * @param state 出发状态
     * @param spAlpha 如果定义，则只考虑该列表中的字母的转移
     */
    protected List<Transform> getTransforms(State state, List<Integer> spAlpha) {
        List<Transform> res = new ArrayList<>();
        List<Transform> T_temp = transformAdjList.get(states.indexOf(state));
        // 深拷贝
        for(Transform t : T_temp){
            Transform temp = new Transform();
            temp.setAlpha(t.getAlpha());
            temp.setTarget(t.getAlpha());
            res.add(temp);
        }
        if (spAlpha != null) {
            List<Transform> filteredRes = new ArrayList<>();
            for (Transform v : res) {
                if (spAlpha.contains(v.getAlpha())) {
                    Transform temp = new Transform();
                    temp.setAlpha(v.getAlpha());
                    temp.setTarget(v.getTarget());
                    filteredRes.add(temp);
                }
            }
            return filteredRes;
        } else {
            return res;
        }
    }

    /**
     * 设置从该状态出发的所有一步转移
     */
    protected void setTransforms(State state, List<Transform> transform) {
        List<Transform> temp = new ArrayList<>();
        for(Transform t : transform){
            Transform T_temp = new Transform();
            T_temp.setAlpha(t.getAlpha());
            T_temp.setTarget(t.getTarget());
            temp.add(T_temp);
        }
        transformAdjList.set(states.indexOf(state), temp);
    }

    /**
     * 深拷贝FA，State的Symbol生成新的，与原FA互不影响）
     */
    public static FiniteAutomata copy(FiniteAutomata fa) {
        FiniteAutomata res = new FiniteAutomata();
        res.states = new ArrayList<>();
        res.startStates = new ArrayList<>();
        res.acceptStates = new ArrayList<>();
        for (int i = 0; i < fa.states.size(); i++) {
            if (fa.startStates.contains(fa.states.get(i))) {
                State newState = new State();
                res.startStates.add(newState);
                res.states.add(newState);
            } else if (fa.acceptStates.contains(fa.states.get(i))) {
                State newState = new State();
                res.acceptStates.add(newState);
                res.states.add(newState);
            } else {
                State newState = new State();
                res.states.add(newState);
            }
        }
        res.alphabet = new ArrayList<>();
        res.alphabet.addAll(fa.alphabet);
        res.transformAdjList = deepCopy(fa.transformAdjList);
        return res;
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
