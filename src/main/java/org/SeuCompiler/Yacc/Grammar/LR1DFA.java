package org.SeuCompiler.Yacc.Grammar;

import lombok.Data;

import java.util.*;

/**
 * LR1项目集族（LR1自动机）
 */
@Data
public final class LR1DFA {
    private LR1State startState;
    private final List<LR1State> states;
    private final Map<LR1State, Map<GrammarSymbol, LR1State>> adjMap;

    public LR1DFA(LR1State startStateId) {
        this.startState = startStateId;
        this.states = new ArrayList<>();
        this.adjMap = new HashMap<>();
    }

    public LR1DFA() {
        this.startState = null;
        this.states = new ArrayList<>();
        this.adjMap = new HashMap<>();
    }

    public void addState(LR1State state) {
        this.states.add(state);

    }

    public void link(LR1State from, LR1State to, GrammarSymbol alpha){
        if(adjMap.containsKey(from)){
            adjMap.get(from).put(alpha, to);
        }else{
            Map<GrammarSymbol, LR1State> temp = new HashMap<>(Map.of(alpha, to));
            adjMap.put(from, temp);
        }
    }

    public LR1State getNext(LR1State start, GrammarSymbol alpha){
        return adjMap.get(start).get(alpha);
    }
}
