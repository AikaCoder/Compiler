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
@NoArgsConstructor
public class FA {
    protected State startState = null;
    protected final Set<State> acceptStates = new HashSet<>();
    protected final Set<State> states = new HashSet<>();
    protected final Map<State, Action> acceptActionMap = new HashMap<>();
}
