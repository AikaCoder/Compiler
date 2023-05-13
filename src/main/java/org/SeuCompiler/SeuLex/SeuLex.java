package org.SeuCompiler.SeuLex;

import lombok.NoArgsConstructor;
import org.SeuCompiler.Exception.CompilerErrorCode;
import org.SeuCompiler.Exception.SeuCompilerException;
import org.SeuCompiler.SeuLex.FiniteAutomata.*;
import org.SeuCompiler.SeuLex.LexParser.LexParser;

import java.util.*;

@NoArgsConstructor
public class SeuLex {
    private DFA miniDFA;
    private List<State> stateList;   //将集合转换成列表, 便于后续在转换矩阵中确定位置
    public String analyseLex(String filePath) {
        LexParser parser = null;
        try {
            parser = new LexParser(filePath);
            NFA nfa = NFA.buildFromParser(parser);
            DFA dfa = new DFA(nfa);
            this.miniDFA = dfa.minimize();
            this.stateList = new ArrayList<>(miniDFA.getStates());
            List<State> starts = new ArrayList<>(this.miniDFA.getStartStates());
            if (starts.size() != 1)
                throw new SeuCompilerException(CompilerErrorCode.TOO_MANY_START_STATES);
            Collections.swap(stateList, 0, stateList.indexOf(starts.get(0)));   //把start State 换到第一个位置

        } catch (SeuCompilerException e) {
            if (e.getLineNum() != null) System.out.println("error at line" + e.getLineNum());
            System.out.println(e.getCode() + "  " + e.getDescription());
            if (e.getOtherInfo() != null) System.out.println(e.getOtherInfo() + '\n');
        }
        assert parser != null;
        return LexCode.CopyPartBegin +
                parser.getCopyPart() +
                LexCode.LexGenerationPartBegin +
                LexCode.preConfigs +
                genTransformMatrix() +
                genSwitchCase() +
                LexCode.genYYLex(genCaseAction()) +
                LexCode.yyless +
                LexCode.yymore +
                LexCode.CCodePartBegin +
                parser.getCCodePart();
    }
    private String genTransformMatrix(){
        StringBuilder res = new StringBuilder(String.format("const int _trans_mat[%d][128] = {\n", this.miniDFA.getStates().size()));
        this.miniDFA.getTransforms().forEach((begin, transform) -> {
            String[] targets = new String[128];
            Arrays.fill(targets, "-1"); //表示无法到达
            String otherTarget = "-1";      //记录读入 ANY 或 OTHER 后到达的状态
            for (Map.Entry<FAChar, State> entry : transform.entrySet()){
                FAChar faChar = entry.getKey();
                State end = entry.getValue();
                if(faChar.equalsTo(SpecialChar.OTHER) || faChar.equalsTo(SpecialChar.ANY))
                    otherTarget = String.valueOf(this.stateList.indexOf(end));
                else targets[faChar.getCharacter()] = String.valueOf(this.stateList.indexOf(end));
            }

            if(Objects.equals(otherTarget, "-1")){
                for(int i = 0; i < targets.length; i++){
                    if(Objects.equals(targets[i], "-1")) targets[i] = otherTarget;
                }
            }
            res.append(String.join(", ", targets)).append("\n");
        });
        res.append("};\n");
        return res.toString();
    }

    private String genSwitchCase(){
        StringBuilder res = new StringBuilder(String.format("const int _swi_case[%d] = { ", this.stateList.size()));
        for(int i = 0; i<this.stateList.size(); i++){
            if(miniDFA.getAcceptStates().contains(stateList.get(i)))
                res.append(i).append(", ");
            else res.append("-1, ");
        }
        res.append(" };\n");
        return res.toString();
    }

    private String genCaseAction(){
        StringBuilder res = new StringBuilder();
        for(State state : this.miniDFA.getStartStates()){
            int index = this.stateList.indexOf(state);
            res.append(String.format(
                """
                        case %d:
                            %s
                            break;
                """
                , index, this.miniDFA.getAcceptActionMap().get(state))
            );
        }
        res.append(
            """
                    default:
                        break;
            """
        );
        return res.toString();
    }

}
