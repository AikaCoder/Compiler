package org.SeuCompiler.SeuLex.FiniteAutomata;

import java.util.*;

public class DFA extends FiniteAutomata {
    private Map<State, Action> acceptActionMap;

    public DFA(){
        super();
        this.startStates = new ArrayList<>();
        this.acceptStates = new ArrayList<>();
        this.states = new ArrayList<>();
        this.alphabet = new ArrayList<>();
        this.transformAdjList = new ArrayList<>();
        this.acceptActionMap = new HashMap<>();
    }

    public Map<State, Action> getAcceptActionMap() {
        return acceptActionMap;
    }

    /**
     * 最小化
     */
    public void minimize(){
        if(this.alphabet.contains("any")) return;
        List<List<State>> stateLists = new ArrayList<>();
        List<State> terminalStates = new ArrayList<>(this.acceptStates);
        List<State> nonTerminalStates = new ArrayList<>();
        List<State> copyOfOriginalStates = new ArrayList<>(this.states);

        for (State terminalState : terminalStates) {
            List<State> tempList = new ArrayList<>();
            tempList.add(terminalState);
            stateLists.add(tempList);
        }
        for (State copyOfOriginalState : copyOfOriginalStates) {
            if (!this.acceptStates.contains(copyOfOriginalState))
                nonTerminalStates.add(copyOfOriginalState);
        }
        if(nonTerminalStates.size() != 0){
            stateLists.add(nonTerminalStates);
        }

        boolean splitFlag = true;
        List<List<State>> newStateLists = new ArrayList<>();
        while(splitFlag){
            splitFlag = false;
            List<State> newSet = new ArrayList<>();
            for(int k = stateLists.size() - 1; k >= 0; k--){
                List<State> s = stateLists.get(k);
                List<State> copy_s = new ArrayList<>(s);
                if(s.size() <= 1){
                    newStateLists.add(s);
                    stateLists.remove(k);
                    splitFlag = true;
                    break;
                }
                else{
                    for(int i = s.size() - 1; i>=0; i--){
                        State standard = s.get(0);
                        List<Transform> tr1 = this.getTransforms(standard, null);
                        List<Transform> tr2 = this.getTransforms(s.get(i), null);
                        if(new HashSet<>(tr1).containsAll(tr2) && new HashSet<>(tr2).containsAll(tr1)){
                            newSet.add(s.get(i));
                            copy_s.remove(i);
                            splitFlag = true;
                        }
                    }
                    stateLists.remove(k);
                    newStateLists.add(copy_s);
                    if(newSet.size() > 0) stateLists.add(newSet);
                    break;
                }
            }
        }
        stateLists.addAll(newStateLists);
        //重构DFA
        List<Integer> reducedStates = new ArrayList<>();
        Map<Integer, Integer> oldNewMap = new HashMap<>();
        for(int i = 0; i < stateLists.size(); i++){
            for(int j = 0; j < stateLists.get(i).size(); j++){
                Integer indexOld = this.states.indexOf(stateLists.get(i).get(j));
                if(stateLists.get(i).size() == 1)
                    oldNewMap.put(indexOld, i);
                else{
                    if(j > 0)
                        reducedStates.add(indexOld);
                    oldNewMap.put(indexOld, i);
                }
            }
        }
        List<State> newStates = this.states.stream()
                .filter((State s) -> !reducedStates.contains(this.states.indexOf(s)))
                .toList();
        List<List<Transform>> newTrans = this.transformAdjList.stream()
                .filter((List<Transform> tfList) -> !reducedStates.contains(this.transformAdjList.indexOf(tfList)))
                .toList();
        for(List<Transform> tfList : newTrans){
            for(Transform tf:tfList){
                Integer temp = tf.getTarget();
                tf.setTarget(oldNewMap.get(temp));
            }
        }
        this.states = newStates;
        this.transformAdjList = newTrans;
    }

    /**
     * 使用NFA构建DFA
     * @param nfa
     */
    public DFA(NFA nfa){
        this();
        if(nfa.startStates.size() == 0) return;

        List<List<State>> stateSets = new ArrayList<>(){{add(nfa.epsilonClosure(nfa.startStates));}};
        this.alphabet = nfa.alphabet;
        this.startStates = new ArrayList<>(){{add(new State());}};
        this.transformAdjList = new ArrayList<>();
        stateSets.get(0).forEach((State s) -> {
            if(!nfa.acceptStates.contains(s)) return;
            Action action = this.acceptActionMap.get(this.startStates.get(0));
            Action compare = nfa.getAcceptActionMap().get(s);
            if(action == null){
                this.acceptStates = new ArrayList<>(){{add(startStates.get(0));}};
                this.acceptActionMap.put(this.startStates.get(0), nfa.getAcceptActionMap().get(s));
            }else if(action.getOrder() > compare.getOrder()){   //优先度更低
                this.acceptActionMap.put(this.startStates.get(0), compare);
            }
        });
        this.states = new ArrayList<>(){{add(startStates.get(0));}};

        //遍历 DFA在第i个状态读入第j个字母的状态
        for(int i = 0; i < this.states.size(); i++){
            int anyTargetState = -1;    //any边状态
            for(int j = 0; j < this.alphabet.size(); j++){
                List<State> newStateSet = nfa.epsilonClosure(nfa.move(stateSets.get(i), j));
                if(newStateSet.isEmpty()) continue;

                boolean isTheSameSet = false;
                int targetState = 0;
                for(List<State> stateSet:stateSets){
                    if(new HashSet<>(newStateSet).containsAll(stateSet)
                        && new HashSet<>(stateSet).containsAll(newStateSet))
                    {
                        isTheSameSet = true;
                        break;
                    }
                    targetState++;
                }
                if(!isTheSameSet){//targetState = stateSets.length();
                    State newState = new State();
                    stateSets.add(newStateSet);
                    this.states.add(new State());
                    this.transformAdjList.add(new ArrayList<>());
                    newStateSet.forEach((State s) ->{
                        if(!nfa.acceptStates.contains(s)) return;
                        Action action = this.acceptActionMap.get(newState);
                        Action compare = nfa.getAcceptActionMap().get(s);
                        if(action == null){
                            this.acceptStates = new ArrayList<>(){{add(startStates.get(0));}};
                            this.acceptActionMap.put(this.startStates.get(0), nfa.getAcceptActionMap().get(s));
                        }else if(action.getOrder() > compare.getOrder()){   //优先度更低
                            this.acceptActionMap.put(this.startStates.get(0), compare);
                        }
                    });
                }

                if(this.alphabet.get(j) == SpAlpha.ANY.getStr()){
                    this.transformAdjList.get(i).add(new Transform(SpAlpha.ANY.getValue(), targetState));
                    anyTargetState = targetState;
                }else{
                    this.transformAdjList.get(i).add(new Transform(j, targetState));
                }

            }
            if(anyTargetState != -1){
                for(int index = this.transformAdjList.get(i).size(); index >= 0; index--){
                    if(this.transformAdjList.get(i).get(index).getTarget() == anyTargetState)
                        this.transformAdjList.get(i).remove(index);
                }
                if(this.transformAdjList.get(i).isEmpty())
                    this.transformAdjList.get(i).add(new Transform(SpAlpha.ANY.getValue(), anyTargetState));
                else
                    this.transformAdjList.get(i).add(new Transform(SpAlpha.ANY.getValue(), anyTargetState));
            }
        }
    }

    /**
     * 使用DFA检测字符串
     * @param str
     * @return
     */
    public boolean test(String str) {
        for (State start : this.startStates) {
            State current = start;
            int matchedCount = 0;
            Stack<State> candidates = new Stack<>();

            for (; matchedCount < str.length(); matchedCount++){
                String currentChar = str.substring(matchedCount, matchedCount + 1);
                if (!this.alphabet.contains(currentChar)
                        && !this.alphabet.contains(SpAlpha.ANY.getStr()))//字母表没有该字符, 且不存在ANY转移
                    return false;

                State newState = this.expand(current, this.alphabet.indexOf(currentChar));
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
    State expand(State state, int alpha){
        List<Transform> trs = this.getTransforms(state, null);
        int otherTarget = -1;
        for(Transform tr : trs){
            if(
                tr.getAlpha() == alpha
                    || (tr.getAlpha() == SpAlpha.ANY.getValue()
                        && !Objects.equals(this.alphabet.get(alpha), "\n")
                )
            ){
                return this.states.get(tr.getTarget());
            } else if (tr.getAlpha() == SpAlpha.OTHER.getValue()) {
                otherTarget = tr.getTarget();
            }
        }
        return otherTarget == -1? null : this.states.get(otherTarget);
    }

    void link(List<State> from, State to, int alpha){
        for(State s:from){
            List<Transform> trs = this.getTransforms(s, null);
            trs.add(new Transform(alpha, this.states.indexOf(to)));
            this.setTransforms(s, trs);
        }
    }
}
