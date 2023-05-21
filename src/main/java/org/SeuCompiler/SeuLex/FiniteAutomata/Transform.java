package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.Getter;
import org.SeuCompiler.SeuLex.LexNode.LexChar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
class Transform {
    final Map<LexChar, Set<State>> map;
    Transform(){
        map = new HashMap<>();
    }
    Transform(LexChar str, Set<State> states){
        Set<State> ends = new HashSet<>(states);
        this.map = new HashMap<>(Map.of(str, ends));
    }

    void add(LexChar inStr, Set<State> inStates){
        if(this.map.containsKey(inStr))
            this.map.get(inStr).addAll(inStates);
        else
            this.map.put(inStr, new HashSet<>(inStates));
    }

    void merge(Transform other){
        for(Map.Entry<LexChar, Set<State>> entry : other.map.entrySet()){
            this.add(entry.getKey(), entry.getValue());
        }
    }

    Transform copyByMap(Map<State, State> oldNewMap){
        Transform newTransform = new Transform();
        this.map.forEach((ch, ends) -> {
            Set<State> newEnds = new HashSet<>();
            ends.forEach((end) -> newEnds.add(oldNewMap.get(end)));
            newTransform.getMap().put(ch, newEnds);
        });
        return newTransform;
    }
}
