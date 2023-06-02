package org.SeuCompiler.Yacc.LR1Analyzer;

// 用于状态转移表中
public record ActionTableCell(ActionTableCellType type, int data) {
    // 用于ACTIONTableCell中表示操作类型
    public enum ActionTableCellType {
        SHIFT("shift"),
        REDUCE("reduce"),
        ACC("acc"),
        NONE("none");

        private final String type;

        ActionTableCellType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
