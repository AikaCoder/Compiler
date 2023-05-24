package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
public final class State {
    private final UUID uuid; // 每个状态给一个全局唯一ID
    private int index; // 可重复识别编号

    private static int _Index = 0;

    public State() {
        this.uuid = UUID.randomUUID();
        index = _Index;
        State._Index = State._Index+1;
    }

    public State(int index){
        this.uuid = UUID.randomUUID();
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return uuid.equals(state.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
