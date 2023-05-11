package org.SeuCompiler.SeuLex.FiniteAutomata;

/**
 * 自动机状态转换
 */
class Transform {
    private int alpha; // 边上的字母（转换的条件）在this._alphabets中的下标，特殊下标见enum SpAlpha
    private int target; // 目标状态在this._states中的下标

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public boolean same(Transform another) {
        return (this.alpha == another.alpha && this.target == another.target);
    }
}
