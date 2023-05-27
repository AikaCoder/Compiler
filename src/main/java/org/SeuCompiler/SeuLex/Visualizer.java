package org.SeuCompiler.SeuLex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.SeuCompiler.SeuLex.FiniteAutomata.DFA;
import org.SeuCompiler.SeuLex.FiniteAutomata.FA;
import org.SeuCompiler.SeuLex.FiniteAutomata.NFA;
import org.SeuCompiler.SeuLex.FiniteAutomata.State;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Visualizer {
    private File printDirectory = new File(System.getProperty("user.dir")+File.separator+"doc"+File.separator+"result"+File.separator);
    private boolean printInfile = true;
    public void print(NFA nfa, String nfaName){
        StringBuilder builder = getBuilderFrom(nfa, nfaName);

        builder.append("\ntransform table\n").append(String.format("\t%-8s%-8s%-20s\n", "start", "char", "end"));
        nfa.getTransforms().getTransformMap().forEach((start, transform) -> transform.getMap().forEach(((faChar, ends) -> {
            StringBuilder endsBuilder = new StringBuilder();
            for(State end : ends){
                endsBuilder.append(end.getIndex()).append(", ");
            }
            builder.append(String.format("\t%-8d%-8s%-20s\n", start.getIndex(), faChar.getPrintedString(), endsBuilder));
        })));

        printBuilder(builder, nfaName);
    }

    public void print(DFA dfa, String dfaName){
        StringBuilder builder = getBuilderFrom(dfa, dfaName);

        builder.append("\ntransform table\n").append(String.format("\t%-8s%-8s%-20s\n", "start", "char", "end"));
        dfa.getTransforms().forEach((start, transform) -> transform.forEach(((faChar, end) ->
            builder.append(String.format("\t%-8d%-8s%-20d\n", start.getIndex(), faChar.getPrintedString(), end.getIndex()))
        )));

        printBuilder(builder, dfaName);
    }

    private void printBuilder(StringBuilder builder, String filename){
        if(printInfile){
            try{
                File file = new File(printDirectory, filename+".txt");
                if(!printDirectory.exists())
                    if(!printDirectory.mkdirs()) throw new IOException("未能新建"+printDirectory);
                if(file.exists())
                    if(!file.delete()) throw new IOException("未能删除"+file);
                if(!file.createNewFile()) throw new IOException("未能创建"+file);

                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(builder.toString());
                out.flush();
                out.close();
                System.out.println("print dfa "+filename+" in "+file);
            } catch (IOException e) {
                System.out.println("print dfa error: "+ e);
            }
        }
        else{
            System.out.println("======== print "+filename+" ========");
            System.out.print(builder);
        }
    }

    private @NotNull StringBuilder getBuilderFrom(FA fa, String name){
        StringBuilder builder = new StringBuilder("start states:\n");
        builder.append("\t").append(fa.getStartState().getIndex()).append("\n");

        builder.append("\naccept states:\n\t");
        for(State state : fa.getAcceptStates()) builder.append(state.getIndex()).append(", ");

        builder.append("\naccept -> action:\n").append(String.format("\t%-8s%-20s\n", "state", "action"));
        fa.getAcceptActionMap().forEach((state,action)->
                builder.append(String.format("\t%-8d%-20s\n", state.getIndex(), action.code()))
        );
        return builder;
    }


}
