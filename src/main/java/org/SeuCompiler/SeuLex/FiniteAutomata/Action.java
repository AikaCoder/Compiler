package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record Action(int order, String code) {
    public boolean lessThan(Action other){
        if(other == null) return true;
        return this.order < other.order;
    }
}
