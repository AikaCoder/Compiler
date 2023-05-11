/**
 * 有限状态自动机（FA）的相关内容。裴爱华 2023/3/25
 */

package org.SeuCompiler.SeuLex.FiniteAutomata;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.lang.Object;

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
    public boolean same(State another) {
        return this.uuid.equals(another.uuid);
    }

    public static boolean same(State one, State another) {
        return one.uuid.equals(another.uuid);
    }
}

/**
 * 特殊字符枚举
 */
enum SpAlpha {
    EPSILON(-1), // ε
    ANY(-2), // . (any character, except \n, not ε)
    OTHER(-3); // other character not mentioned

    private final int value;

    SpAlpha(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

/**
 * 将特殊字符下标转为字符描述
 */
class SpAlphaUtil {
    public static String getSpAlpha(int alpha) {
        Map<Integer, String> table = new HashMap<>();
        table.put(-1, "[ε]");
        table.put(-2, "[any]");
        table.put(-3, "[other]");
        return table.getOrDefault(alpha, "");
    }
}

/**
 * 有限状态自动机
 */
class FiniteAutomata {
    protected List<String> alphabet; // 字母表
    protected List<State> states; // 全部状态
    protected List<State> startStates; // 初始状态
    protected List<State> acceptStates; // 接收状态
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
    protected List<Transform> getTransforms(State state, int[] spAlpha) {
        List<Transform> res = new ArrayList<>();
        List<Transform> T_temp = transformAdjList.get(findIndex(states, state));
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
                if (contains(spAlpha, v.getAlpha())) {
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
        transformAdjList.set(findIndex(states, state), temp);
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
            if (contains(fa.startStates, fa.states.get(i))) {
                State newState = new State();
                res.startStates.add(newState);
                res.states.add(newState);
            } else if (contains(fa.acceptStates, fa.states.get(i))) {
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

    private static <T> int findIndex(List<T> array, T value) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(value)) return i;
        }
        return -1;
    }

    private static <T> T[] filter(T[] array, Predicate<T> predicate) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : array) {
            if (predicate.test(t)) {
                list.add(t);
            }
        }
        return list.toArray(Arrays.copyOf(array, 0));
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
