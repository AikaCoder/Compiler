package org.SeuCompiler.SeuLex.Regex;

/**
 * 正则匹配结果
 * @param content 匹配到的字符串
 * @param head 匹配结果在原字符串开始的位置, pos >= head
 * @param tail 匹配结果在原字符串结束的位置, pos < tail
 */
record MatchedRecord(String content, int head, int tail) {
    public boolean insideRangeOf(int pos) {
        return pos >= head && pos < tail;
    }
    public boolean insideRangeOf(MatchedRecord another){
        boolean res = another.head() < head && tail < another.tail();
        return res;
    }
}
