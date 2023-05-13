package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DFA{
    protected Set<State> startStates = new HashSet<>();
    protected Set<State> acceptStates = new HashSet<>();
    protected Set<State> states = new HashSet<>();
    protected Set<FAString> alphabet = new HashSet<>();
    protected Map<State, Map<FAString, State>> transforms = new HashMap<>();
    private final Map<State, Action> acceptActionMap = new HashMap<>();
    
    public DFA copy(){
        DFA dfa = new DFA();
        dfa.alphabet.addAll(this.alphabet);

        Map<State, State> oldNewMap = new HashMap<>();
        for (State s : this.states) {
            State newState = new State();
            if (this.startStates.contains(s)) dfa.startStates.add(newState);
            if (this.acceptStates.contains(s)) dfa.acceptStates.add(newState);
            dfa.states.add(newState);
            oldNewMap.put(s, newState);
        }

        this.transforms.forEach((begin, transform) -> {
            Map<FAString, State> tempMap = new HashMap<>();
            transform.forEach((str, end) -> tempMap.put(str, oldNewMap.get(end)));
            this.transforms.put(oldNewMap.get(begin), tempMap);
        });
        this.acceptActionMap.forEach((state, action) -> {
            dfa.acceptActionMap.put(oldNewMap.get(state), action);
        });
        return dfa;
    }
    
    /**
     * 使用NFA构建DFA
     * @param nfa 用来初始化的NFA
     */
    public DFA(@NotNull NFA nfa){
        //----------初始化(starts, alphabet)-----------
        this();
        if(nfa.startStates.size() == 0) return;
        //记录dfa的状态集合和新状态的对应关系
        List<Set<State>> oldStatesList = new ArrayList<>();
        List<State> newStateList = new ArrayList<>();
        Set<State> oldStarts = nfa.epsilonClosure(nfa.startStates);
        State newStart = new State();
        oldStatesList.add(oldStarts);
        newStateList.add(newStart);
        this.alphabet.addAll(nfa.getAlphabet());
        this.startStates.add(newStart);    //定义一个新的状态为起始状态
        this.states.add(newStart);

        //--------合并等价类(states, transform)---------
        for(int i = 0; i < newStateList.size(); i++){
            AtomicBoolean addAny = new AtomicBoolean(false);
            //对每一个dfa新状态, 取对应的nfa旧状态集, 并得到其在输入不同字符后等到的新集合(进行了epsilon后扩展)
            Set<State> oldBegins = oldStatesList.get(i);
            State newBegin = newStateList.get(i);
            Transform expand = nfa.expandTransformWithEpsilon(oldBegins);
            expand.getMap().forEach((str, states) -> {
                if(states.isEmpty()) return;
                if(oldStatesList.contains(states)){
                    this.transforms.put(newBegin, Map.of(str, newStateList.get(oldStatesList.indexOf(states))));
                }else{
                    State newEnd = new State();
                    oldStatesList.add(states);
                    newStateList.add(newEnd);
                    this.transforms.put(newBegin, Map.of(str, newEnd));
                }

                if(str.equalsToSpChar(SpecialChar.ANY)) addAny.set(true);
            });
            //处理输入字符为ANY的情况
            if(addAny.get()){
                Map<FAString, State> temp = this.transforms.get(newBegin);
                State targetOfAny = temp.get(SpecialChar.ANY.toFAString());
                temp.entrySet().removeIf(entry -> entry.getValue().equals(targetOfAny));//删除所有指向target of any的转换
                if(temp.isEmpty()) temp.put(SpecialChar.ANY.toFAString(), targetOfAny); //如果所有转换都被删去了, 说明所有状态都是接收ANY字符
                else temp.put(SpecialChar.OTHER.toFAString(), targetOfAny); //Other表示除有效字符外的字符
            }
        }

        this.states.addAll(newStateList);

        //---------处理accept (acceptStates, acceptActionMap)---------
        for(int i = 0; i < newStateList.size(); i++){
            State s = newStateList.get(i);
            Set<State> refers = oldStatesList.get(i);
            refers.forEach(refer -> {
                if(nfa.acceptStates.contains(refer)){
                    //如果nfa的起始状态的epsilon闭包包含可接受状态
                    //则将对应的order最小(出现最早, 优先级最高)的动作作为newState对应的动作存放到acceptActionMap中
                    Action action = this.acceptActionMap.get(s); //初始为null, 但几次循环后可能会有值
                    Action compare = nfa.getAcceptActionMap().get(refer);
                    if(action == null){
                        this.acceptStates.add(s);
                        this.acceptActionMap.put(s, compare);
                    }else if(action.getOrder() > compare.getOrder()){
                        this.acceptActionMap.put(s, compare);
                    }   //else action存在, 且 不用改变action
                }
            });
        }
    }

    /**
     * 最小化DFA
     * @return 最小化之后的DFA
     */
    private DFA minimize(){
        if(this.alphabet.contains(SpecialChar.ANY.toFAString())) return this;   //不考虑any
        //------------初始化-----------
        //根据可接受状态划分成terminal与nonTerminal
        Set<State> terminals = new HashSet<>(this.acceptStates);
        Set<State> nonTerminals = new HashSet<>();
        this.states.forEach(s ->{
            if(!terminals.contains(s)) nonTerminals.add(s);
        });

        List<Set<State>> divisions = new ArrayList<>();  //用于存放不同状态划分的散列
        //因为不同的accept state可能对应不同的action/regex,
        //所以这里不能像一般情况一样将所有的terminal合起来作为一种情况, 而要分开来进行划分
        terminals.forEach(ter -> divisions.add(Set.of(ter)));
        //nonTerminal 初始化时nonTerminal本身作为一种划分放入divisions中
        divisions.add(nonTerminals);

        //------------拆分-----------
        boolean dividable = true;
        List<Set<State>> completedDivisions = new ArrayList<>();    //已经划分完毕(即无法再分)的划分的集合
        while(dividable){
            dividable = false;
            List<Set<State>> newDivs = new ArrayList<>();

            Iterator<Set<State>> setIterator = divisions.listIterator();    //因为要删除元素, 所以选用迭代器(也可以用反向删除)
            while(setIterator.hasNext()){   //对于divisions中的每一个划分
                Set<State> divSet = setIterator.next();
                if(divSet.size() <= 1){ //只有一个元素, divSet划分完毕
                    completedDivisions.add(divSet);
                    setIterator.remove();
                    continue;
                }

                //divSet数量大于等于2, 有可能可以继续划分
                Set<State> newDivision = new HashSet<>();   //被拆分出来的新划分
                Iterator<State> stateIterator = divSet.iterator();
                State standard = stateIterator.next();  //随机 取一个元素作为参考
                while (stateIterator.hasNext()){
                    State s = stateIterator.next();
                    if(!transforms.get(standard).equals(transforms.get(s))){
                        //目标State的转移方式与参考State不同, 因此需要进行划分
                        newDivision.add(s);
                        stateIterator.remove();
                        dividable = true;   //有新划分出现, 因此还需要继续最外层循环
                    }
                }
                //上面的循环结束后, divSet中所有元素(State)的Transform是一样的, 因此也已经划分完毕.
                completedDivisions.add(divSet);
                setIterator.remove();

                if(newDivision.size()>0) newDivs.add(newDivision);  //有新划分
            }
            divisions.addAll(newDivs);
        }
        completedDivisions.addAll(divisions);   //结束后将可能还有的划分放入complete divisions里面

        //------------划分结束, 重构DFA-----------
        Map<State, State> oldNewMap = new HashMap<>(); //新旧状态映射表
        completedDivisions.forEach(div ->{
            State newState = new State();   //每一个div对应一个新状态
            div.forEach(state ->{oldNewMap.put(state, newState);});
        });

        DFA dfa = new DFA();
        dfa.alphabet.addAll(this.alphabet);
        this.states.forEach(s ->{
            if(this.startStates.contains(s)) dfa.startStates.add(oldNewMap.get(s));
            if(this.acceptStates.contains(s)) dfa.acceptStates.add(oldNewMap.get(s));
            dfa.states.add(oldNewMap.get(s));
        });

        this.transforms.forEach((begin, transform) -> {
            Map<FAString, State> tempMap = new HashMap<>();
            transform.forEach((str, end) -> tempMap.put(str, oldNewMap.get(end)));
            this.transforms.put(oldNewMap.get(begin), tempMap);
        });

        this.acceptActionMap.forEach((state, action) -> {
            dfa.acceptActionMap.put(oldNewMap.get(state), action);
        });
        return dfa;
    }

    public boolean test(String str) {
        for (State start : this.startStates) {  //虽然理论上说构造出来的DFA只有一个start...
            State current = start;
            int matchedCount = 0;
            Stack<State> candidates = new Stack<>();

            for (; matchedCount < str.length(); matchedCount++){
                FAString currentChar = new FAString(str.substring(matchedCount, matchedCount + 1));
                if (!this.alphabet.contains(currentChar)
                        && !this.alphabet.contains(SpecialChar.ANY.toFAString()))//字母表没有该字符, 且不存在ANY转移
                    return false;
                State newState = this.transforms.get(current).get(currentChar);
                matchedCount++;
                if(newState != null && !candidates.contains(newState))
                    candidates.add(newState);
                if(candidates.isEmpty()) break;
                else current = candidates.pop();
            }

            if (matchedCount == str.length() && this.acceptStates.contains(current)) {
                return true;
            }
        }
        return false;
    }

}
