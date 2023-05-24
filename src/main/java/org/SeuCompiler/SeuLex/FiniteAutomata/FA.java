package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.SeuCompiler.SeuLex.LexNode.LexChar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class FA {
    protected final Set<State> startStates = new HashSet<>();
    protected final Set<State> acceptStates = new HashSet<>();
    protected final Set<State> states = new HashSet<>();
    protected final Map<State, Action> acceptActionMap = new HashMap<>();

    protected void addAllSet(FA other){
        this.startStates.addAll(other.startStates);
        this.acceptStates.addAll(other.acceptStates);
        this.states.addAll(other.states);
    }

}
