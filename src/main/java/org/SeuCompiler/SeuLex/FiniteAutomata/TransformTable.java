package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.SeuCompiler.SeuLex.LexNode.LexChar;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class TransformTable {
    private Map<State, Transform> transformMap = new HashMap<>();
    public void putAll(TransformTable other){
        this.transformMap.putAll(other.transformMap);
    }

    public void put(State start, Transform transform){
        this.transformMap.put(start, transform);
    }

    public TransformTable copyByMap(Map<State, State> oldNewMap){
        TransformTable res = new TransformTable();
        this.transformMap.forEach((begin, transform) ->
                res.transformMap.put(oldNewMap.get(begin), transform.copyByMap(oldNewMap))
        );
        return res;
    }

    /**
     * 得到从start出发, 接收lexChar后到达的状态集
     * @param start 起始状态
     * @param lexChar 接收字符, 如果为空, 则返回所有字符
     * @return 到达状态集
     */
    public Set<State> getNextStates(State start, LexChar lexChar){
        Set<State> res = new HashSet<>();
        if(!this.transformMap.containsKey(start))
            return res;
        if(lexChar == null){
            Set<State> temp = this.transformMap.get(start).getMap().get(lexChar);
            if(temp != null)
                res.addAll(temp);
        }else{
            this.transformMap.get(start).getMap().forEach((ch, states) ->{
                if(ch.equals(lexChar))
                    res.addAll(states);
            });
        }
        return res;
    }

    /**
     * 获得输入状态集合的epsilon闭包，即从某状态集合只通过epsilon边所能到达的所有状态（包括自身）
     * @param states 初始状态集
     * @return 闭包状态集
     */
    public Set<State> epsilonClosure(Set<State> states){
        Set<State> res =  new HashSet<>(states);
        states.forEach((start) ->{
            Set<State> addStates = getNextStates(start, SpecialChar.EPSILON.toFAChar());
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
            if(!this.transformMap.containsKey(s)) continue;
            this.transformMap.get(s).getMap().forEach((str, states) -> {
                if(str.equalsTo(SpecialChar.EPSILON)) return;
                res.add(str, epsilonClosure(states));
            });
        }
        return res;
    }

    /**
     * 向跳转表格中添加新跳转
     * @param start 起始状态
     * @param transform 字符->下一状态跳转
     */
    public void addTransform(State start, Transform transform){
        if(this.transformMap.containsKey(start))
            this.transformMap.get(start).merge(transform);
        else
            this.transformMap.put(start, transform);
    }

    /**
     * 合并跳转表格
     * @param other 另一跳转表格
     */
    public void merge(TransformTable other){
        other.transformMap.forEach(this::addTransform);
    }


    /**
     * 把`begin`中的每个状态到`end`中的每个状态用字母inStr建立边
     * @param begins 开始状态集
     * @param ends 结束状态集
     * @param inStr 输入字符
     */
    public void link(Set<State> begins, Set<State> ends, LexChar inStr){
        begins.forEach(b ->  this.addTransform(b, new Transform(inStr, new HashSet<>(ends))));
    }

    /**
     * 把`begin`中的每个状态到`end`中的每个状态用Epsilon建立边
     * @param begins 开始状态集
     * @param ends  结束状态集
     */
    public void linkByEpsilon(Set<State> begins, Set<State> ends){
        this.link(begins, ends, SpecialChar.EPSILON.toFAChar());
    }
}
