package org.SeuCompiler.Yacc.LR1Analyzer;

import lombok.Getter;

import java.util.Objects;

/**
 * LR1项目
 * A->a, $就是一条项目
 * 将多个展望符的，拆分成不同的项目，每个项目只有一个展望符号
 *
 * @param producer    在LR1Analyzer._producers中的下标
 * @param rawProducer 历史遗留产物
 * @param dotPosition producer.rhs的点号位置，规定0号位为最左（所有符号之前）位置
 * @param lookahead   展望符（终结符）
 */

@Getter
public record LR1Item(int producer, LR1Producer rawProducer, int dotPosition, int lookahead) {
    public LR1Item(int producer, LR1Producer rawProducer, int dotPosition, int lookahead) {
        this.producer = producer;
        this.dotPosition = dotPosition;
        this.lookahead = lookahead;
        this.rawProducer = new LR1Producer(rawProducer.lhs(), rawProducer.rhs(), rawProducer.action());
    }

    // 深拷贝
    public static LR1Item copy(LR1Item item, boolean go) {
        return new LR1Item(
                item.producer,
                item.rawProducer,
                item.dotPosition,
                item.lookahead + (go ? 1 : 0)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LR1Item lr1Item)) return false;
        return producer == lr1Item.producer && dotPosition == lr1Item.dotPosition && lookahead == lr1Item.lookahead;
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, dotPosition, lookahead);
    }

    public boolean dotAtLast() {
        return this.dotPosition == this.rawProducer.rhs().size();
    }
}
