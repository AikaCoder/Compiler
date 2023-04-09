package org.SeuCompiler.SeuLex.Regex;

import org.SeuCompiler.Exception.LexCodeEnum;
import org.SeuCompiler.Exception.SeuCompilerException;

import java.util.HashMap;
import java.util.regex.Pattern;

class MatchedInsideBrace extends MatchedItems {

    /**
     * 返回所有在{}内的匹配项
     *
     * @param input   待匹配字符串
     */
    public MatchedInsideBrace(String input) {
        super(input, Pattern.compile("(?<!\\\\)\\{(\\\\.|[^\\\\{}])*}"));
    }

    /**
     * 将给定字符串中匹配项 按说给映射关系, 映射到一般的正则表达式.
     * @param map 替代映射关系
     */
    public String SubstituteByMap(HashMap<String, String> map){
        int offset = 0;
        String result = rawStr;
        for(MatchedRecord item : allItem){
            String former = result.substring(0, item.head()+offset);
            String latter = result.substring(item.tail()+offset);

            String substitute = map.get(item.content());
            if(substitute == null){
                SeuCompilerException exception = new SeuCompilerException(LexCodeEnum.NO_CORRESPONDING_USER_DEFINE);
                exception.setOtherInfo("简写 "+item.content()+" 未在前面找到对应定义.");
                throw exception;
            }

            result = former + map.get(item.content()) + latter;
            offset += map.get(item.content()).length() - item.content().length();
        }
        return result;
    }

}
