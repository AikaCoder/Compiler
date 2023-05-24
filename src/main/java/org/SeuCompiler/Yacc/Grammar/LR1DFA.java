package org.SeuCompiler.Yacc.Grammar;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LR1项目集族（LR1自动机）
 */
@Data
public final class LR1DFA {
    private Integer startStateId;
    private final List<LR1State> states;
    private final List<List<Map<String, Integer>>> adjList; // 分别是to和alpha

    public LR1DFA(int startStateId) {
        this.startStateId = startStateId;
        this.states = new ArrayList<>();
        this.adjList = new ArrayList<>();
    }

    public void addState(LR1State state) {
        this.states.add(state);
        this.adjList.add(new ArrayList<>());
    }

    public void link(int from, int to, int alpha) {
        Map<String, Integer> map = new HashMap<>();
        map.put("to", to);
        map.put("alpha", alpha);
        this.adjList.get(from).add(map);
    }

}
