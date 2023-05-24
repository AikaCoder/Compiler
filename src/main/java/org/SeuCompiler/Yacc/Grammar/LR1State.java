package org.SeuCompiler.Yacc.Grammar;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LR1项目集（LR1自动机状态）
 */
@EqualsAndHashCode
@Getter
public final class LR1State {
    private final List<LR1Item> items;

    public LR1State(){this.items = new ArrayList<>();}
    public LR1State(List<LR1Item> in){this.items = in;}
    public static LR1State copy(LR1State state) {
        LR1State copy = new LR1State();
        for(LR1Item item : state.items){
            copy.items.add(LR1Item.copy(item, false));
        }
        return copy;
    }

    public void addItem(LR1Item item) {
        this.items.add(item);
    }
}
