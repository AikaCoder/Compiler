package org.SeuCompiler.SeuLex.Regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MatchedItems {
    protected String rawStr;
    protected List<MatchedRecord> allItem = new ArrayList<>();

    /**
     * 对给定的字符串和正则规则, 返回所有的匹配结果
     * @param input 待匹配字符串
     * @param pattern 匹配正则
     */
    public MatchedItems(String input, Pattern pattern){
        rawStr = input;
        Matcher matcher = pattern.matcher(input);
        List<MatchedRecord> res = new ArrayList<>();
        while(matcher.find()){
            MatchedRecord t = new MatchedRecord(matcher.group(0), matcher.start(), matcher.end());
            res.add(t);
        }
        allItem = res;
    }

    public List<MatchedRecord> getAllItem() {
        return allItem;
    }

    public String getRawStr() {
        return rawStr;
    }

    /**
     * 对所有匹配项, 如果其在给定container的某一匹配项的范围内, 则将其移除
     * @param container 给定的另外一个匹配结果
     */
    public void removeItemContainedIn(MatchedItems container){
        Iterator<MatchedRecord> iterator = allItem.iterator();
        while(iterator.hasNext()){
            MatchedRecord former = iterator.next();
            for(MatchedRecord latter : container.getAllItem()){
                if(former.insideRangeOf(latter)){
                    iterator.remove();
                }
            }
        }
    }

   }
