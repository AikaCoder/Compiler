package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.SeuCompiler.Exception.CompilerErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.LexNode.LexChar;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public final class DFA extends FA{
    private Map<State, Map<LexChar, State>> transforms = new HashMap<>();
    
    /**
     * 使用NFA构建DFA
     * @param nfa 用来初始化的NFA
     */
    public DFA(@NotNull NFA nfa) throws SeuCompilerException {
        this();

        System.out.println("building DFA from NFA ...");
        int index = 0;

        //----------初始化(starts, alphabet)-----------
        //用两个List记录dfa的状态集合和新状态的对应关系
        List<Set<State>> oldStatesList = new ArrayList<>();
        List<State> newStateList = new ArrayList<>();
        Set<State> oldStarts = nfa.getTransforms().epsilonClosure(Set.of(nfa.getStartState()));
        printEquivalenceClass(index, oldStarts,"ε-Closure");
        State newStart = new State(index++);

        oldStatesList.add(oldStarts);
        newStateList.add(newStart);
        this.startState = newStart;    //定义一个新的状态为起始状态
        this.states.add(newStart);

        //--------合并等价类(states, transform)---------
        for(int i = 0; i < newStateList.size(); i++){
            boolean addAny = false;
            //对每一个dfa新状态, 取对应的nfa旧状态集, 并得到其在输入不同字符后等到的新集合(进行了epsilon后扩展)
            Set<State> oldBegins = oldStatesList.get(i);
            State newBegin = newStateList.get(i);
            Transform expand = nfa.getTransforms().expandTransformWithEpsilon(oldBegins);
            for (Map.Entry<LexChar, Set<State>> e : expand.getMap().entrySet()) {
                LexChar str = e.getKey();
                Set<State> oldEnds = e.getValue();
                if (oldEnds.isEmpty()) continue;
                if (oldStatesList.contains(oldEnds)) {
                    addTransform(newBegin, str, newStateList.get(oldStatesList.indexOf(oldEnds)));
                } else {
                    oldStatesList.add(oldEnds);
                    printEquivalenceClass(index, oldEnds, "ε-Closure");
                    State newEnd = new State(index++);
                    newStateList.add(newEnd);
                    addTransform(newBegin, str, newEnd);
                }

                if (str.equalsTo(SpecialChar.ANY)) addAny = true;
            }
            //处理输入字符为ANY的情况
            if(addAny){
                Map<LexChar, State> temp = this.transforms.get(newBegin);
                State targetOfAny = temp.get(SpecialChar.ANY.toFAChar());
                temp.entrySet().removeIf(entry -> entry.getValue().equals(targetOfAny));//删除所有指向target of any的转换
                if(temp.isEmpty()) temp.put(SpecialChar.ANY.toFAChar(), targetOfAny); //如果所有转换都被删去了, 说明所有状态都是接收ANY字符
                else temp.put(SpecialChar.OTHER.toFAChar(), targetOfAny); //Other表示除有效字符外的字符
            }
        }

        this.states.addAll(newStateList);

        //---------处理accept (acceptStates, acceptActionMap)---------
        for(int i = 0; i < newStateList.size(); i++){
            State s = newStateList.get(i);
            Set<State> refers = oldStatesList.get(i);
            refers.forEach(refer -> {
                if(nfa.getAcceptStates().contains(refer)){
                    Action actionNow = this.acceptActionMap.get(s);
                    Action compare = nfa.getAcceptActionMap().get(refer);
                    Action smaller = compare.lessThan(actionNow) ? compare : actionNow;
                    this.acceptStates.add(s);
                    this.acceptActionMap.put(s, smaller);
                }
            });
        }

        System.out.println("build DFA complete.");
    }

    /**
     * 最小化DFA
     * @return 最小化之后的DFA
     */
    public DFA minimize() throws SeuCompilerException {
        System.out.println("minimizing DFA...");
        for (Map.Entry<State, Map<LexChar, State>> e : this.transforms.entrySet()) {
            Map<LexChar, State> lexCharStateMap = e.getValue();
            if (lexCharStateMap.containsKey(SpecialChar.ANY.toFAChar())) {
                System.out.println("stopped because not supported minimizing DFA with [any] defined.");
                return this;//不考虑any todo
            }
        }

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
        for (State ter : terminals)
            divisions.add(new HashSet<>(Set.of(ter)));
        //nonTerminal 初始化时nonTerminal本身作为一种划分放入divisions中
        divisions.add(nonTerminals);

        //------------拆分-----------
        System.out.println("divide state class by is accept action and transforms.");
        boolean dividable = true;
        int index = 0;
        List<Set<State>> completedDivisions = new ArrayList<>();    //已经划分完毕(即无法再分)的划分的集合
        while(dividable){
            dividable = false;
            List<Set<State>> newDivs = new ArrayList<>();

            Iterator<Set<State>> setIterator = divisions.listIterator();    //因为要删除元素, 所以选用迭代器(也可以用反向删除)
            while(setIterator.hasNext()){   //对于divisions中的每一个划分
                Set<State> divSet = setIterator.next();
                if(divSet.size() <= 1){ //只有一个元素, divSet划分完毕
                    completedDivisions.add(divSet);
                    printEquivalenceClass(index++, divSet, "minimize");
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
                printEquivalenceClass(index++, divSet, "minimize");
                setIterator.remove();

                if(newDivision.size()>0) newDivs.add(newDivision);  //有新划分
            }
            divisions.addAll(newDivs);
        }
        completedDivisions.addAll(divisions);   //结束后将可能还有的划分放入complete divisions里面

        //------------划分结束, 重构DFA-----------
        index = 0;
        DFA dfa = new DFA();
        Map<State, State> oldNewMap = new HashMap<>(); //新旧状态映射表
        for (Set<State> div : completedDivisions) {
            if(div.contains(this.startState)){
                State newState = new State(index++);
                dfa.startState = newState;
                oldNewMap.put(this.startState, newState);
                break;
            }
        }
        for (Set<State> div : completedDivisions) {
            if(div.contains(this.startState)) continue;
            State newState = new State(index++);   //每一个div对应一个新状态
            div.forEach(state -> oldNewMap.put(state, newState));
        }
        this.states.forEach(s ->{
            if(this.acceptStates.contains(s)) dfa.acceptStates.add(oldNewMap.get(s));
            dfa.states.add(oldNewMap.get(s));
        });
        this.transforms.forEach((begin, transform) -> {
            Map<LexChar, State> tempMap = new HashMap<>();
            transform.forEach((str, end) -> tempMap.put(str, oldNewMap.get(end)));
            dfa.transforms.put(oldNewMap.get(begin), tempMap);
        });
        this.acceptActionMap.forEach((state, action) -> dfa.acceptActionMap.put(oldNewMap.get(state), action));

        for (Map.Entry<State, Map<LexChar, State>> entry : dfa.transforms.entrySet()) {
            Map<LexChar, State> value = entry.getValue();
            if (value.containsKey(SpecialChar.EPSILON.toFAChar()))
                throw new SeuCompilerException(CompilerErrorCode.DFA_CONTAINS_EPSILON);
        }

        System.out.println("minimize complete");
        return dfa;
    }

    private void addTransform(State begin, LexChar lexChar, State end) throws SeuCompilerException {
        if( ! this.transforms.containsKey(begin)){
            this.transforms.put(begin,new HashMap<>( new HashMap<>(Map.of(lexChar, end))));
            return;
        }
        Map<LexChar, State> transform = this.transforms.get(begin);
        if(transform.containsKey(lexChar)){
            if( ! transform.get(lexChar).equals(end))
                throw new SeuCompilerException(CompilerErrorCode.MULTIPLE_TRANSFORM_FRO_ONE_INPUT);
            return;
        }
        transform.put(lexChar, end);
    }

    private void printEquivalenceClass(int index, Set<State> states, String type){
        StringBuilder builder = new StringBuilder(type).append("_class_").append(index).append(":\t{");
        for(State state : states){
            builder.append(state.getIndex()).append(", ");
        }
        builder.append("}");
        System.out.println(builder);
    }
}
