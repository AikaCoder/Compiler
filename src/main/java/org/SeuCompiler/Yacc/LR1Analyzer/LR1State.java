package org.SeuCompiler.Yacc.LR1Analyzer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LR1项目集（LR1自动机状态）
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class LR1State {
    private List<LR1Item> items = new ArrayList<>();

    public static LR1State copy(LR1State state) {
        return new LR1State(state.getItems().stream().
                map(i -> LR1Item.copy(i, false)).collect(Collectors.toList()));
    }

    public void addItem(LR1Item item) {
        this.items.add(item);
    }

    public void forceSetItems(List<LR1Item> items) {
        this.items = new ArrayList<>();
        this.items.addAll(items);
    }
}
