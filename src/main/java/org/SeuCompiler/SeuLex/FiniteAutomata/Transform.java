package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Transform {
    Map<FAChar, Set<State>> map = new HashMap<>();

    Transform(Transform other){
        this();
        this.map.putAll(other.map);
    }

    Transform(FAChar str, Set<State> states){
        this(Map.of(str, states));
    }

    void add(FAChar inStr, Set<State> inStates){
        if(this.map.containsKey(inStr))
            this.map.get(inStr).addAll(inStates);
        else
            this.map.put(inStr, inStates);
    }

    Set<State> getAllStates(){
        Set<State> res = new HashSet<>();
        this.map.forEach((str, states) -> {
            res.addAll(states);
        });
        return res;
    }

    Set<State> getAllStates(FAChar inStr){
        Set<State> res = new HashSet<>();
        this.map.forEach((str, states) -> {
            if(str.equals(inStr))
                res.addAll(states);
        });
        return res;
    }

    Transform getTransform(FAChar inStr){
        Transform transform = new Transform();
        this.map.forEach((str, states) -> {
            if(str.equals(inStr))
                transform.map.put(str, states);
        });
        return transform;
    }

    void merge(Transform other){
        other.map.forEach((str, ends) -> {
            if(this.map.containsKey(str)){
                this.map.get(str).addAll(ends);
            }
            else this.map.put(str, ends);
        });
    }

    Transform copyByMap(Map<State, State> oldNewMap){
        Transform newTransform = new Transform();
        this.map.forEach((ch, ends) -> {
            Set<State> newEnds = new HashSet<>();
            ends.forEach((end) -> {
                newEnds.add(oldNewMap.get(end));
            });
            newTransform.getMap().put(ch, newEnds);
        });
        return newTransform;
    }
}
