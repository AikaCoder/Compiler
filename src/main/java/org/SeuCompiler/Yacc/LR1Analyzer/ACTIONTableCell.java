package org.SeuCompiler.Yacc.LR1Analyzer;

// 用于状态转移表中
public record ACTIONTableCell(ACTIONTableCellType type, int data) {
    // 用于ACTIONTableCell中表示操作类型
    public enum ACTIONTableCellType {
        SHIFT("shift"),
        REDUCE("reduce"),
        ACC("acc"),
        NONE("none");

        private final String type;

        ACTIONTableCellType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
