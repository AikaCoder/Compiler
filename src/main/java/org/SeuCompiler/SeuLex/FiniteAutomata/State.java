package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
final class State {
    private final UUID uuid; // 每个状态给一个全局唯一ID

    public State() {
        this.uuid = UUID.randomUUID();
    }
}
