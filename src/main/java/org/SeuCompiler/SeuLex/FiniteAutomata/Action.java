package org.SeuCompiler.SeuLex.FiniteAutomata;

/**
 * 用在_acceptActionMap，表示一段动作代码及其出现次序
 */
public class Action {
    private int order;
    private String code;

    public Action(int order, String code){
        this.order = order;
        this.code = code;
    }
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
