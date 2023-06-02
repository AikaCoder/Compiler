package org.SeuCompiler.Yacc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.SeuCompiler.Yacc.Grammar.GrammarSymbol;
import org.SeuCompiler.Yacc.Grammar.LR1DFA;
import org.SeuCompiler.Yacc.Grammar.LR1Item;
import org.SeuCompiler.Yacc.LR1Analyzer.ActionTableCell;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1Analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Visualizer {
    String resultDir = null;

    /**
     * 设定输出目录, 如果不指定, 则输出到user下的doc文件
     * @param dir
     */
    public void setResultDir(String dir){
        this.resultDir = dir;
    }

     /**
     * 实现了LR(1)分析表的可视化功能
     * 将ACTION和GOTO表格转换为JSON数据并存储在本地文件中
     * 如果viewNow参数为true，则打开本地浏览器并显示分析表格的可视化结果
     **/
    public void visualizeACTIONGOTOTable(LR1Analyzer lr1Analyzer, boolean viewNow) {
        //通过LR1Analyzer获取ACTION和GOTO表格的数据
        List<String> ACTIONHead = new ArrayList<>();
        List<String> GOTOHead = new ArrayList<>();
        //将表头ACTIONHead和GOTOHead分别转换为List<String>类型
        for(GrammarSymbol symbol : lr1Analyzer.getSymbols()){
            if(symbol.isType(GrammarSymbol.GrammarSymbolType.NON_TERMINAL)) GOTOHead.add(symbol.content());
            else ACTIONHead.add(symbol.content());
        }
        int[] colUsage = new int[ACTIONHead.size()];
        //根据表格数据生成对应的List<List<String>>类型的ACTIONTable和GOTOTable
        List<List<String>> ACTIONTable = new ArrayList<>();
        for (int i = 0; i < lr1Analyzer.getACTIONTable().size(); i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < lr1Analyzer.getACTIONTable().get(i).size(); j++) {
                ActionTableCell cell = lr1Analyzer.getACTIONTable().get(i).get(j);
                switch (cell.type().getType()) {
                    case "acc" -> {
                        row.add("acc");
                        colUsage[j] = 1;
                    }
                    case "none" -> row.add("");
                    case "reduce" -> {
                        String producer = lr1Analyzer.getProducers().get(cell.data()).toString().trim();
                        row.add("r(" + producer + ")");
                        colUsage[j] = 1;
                    }
                    case "shift" -> {
                        row.add("s" + cell.data());
                        colUsage[j] = 1;
                    }
                }
            }
            ACTIONTable.add(row);
        }
        List<List<String>> GOTOTable = new ArrayList<>();
        for (int i = 0; i < lr1Analyzer.getGOTOTable().size(); i++) {
            List<String> row = new ArrayList<>();
            for (int cell : lr1Analyzer.getGOTOTable().get(i)) {
                if (cell == -1) {
                    row.add("");
                } else {
                    row.add(Integer.toString(cell));
                }
            }
            GOTOTable.add(row);
        }
        //删除不需要的列，将表头和表格数据存储在dumpObject中
        for (int col = colUsage.length - 1; col >= 0; col--) {
            if (colUsage[col] == 0) {
                ACTIONHead.remove(col);
                for (List<String> row : ACTIONTable) {
                    row.remove(col);
                }
            }
        }
        Map<String, List<?>> dumpObject = new HashMap<>();
        dumpObject.put("ACTIONHead", ACTIONHead);
        dumpObject.put("GOTOHead", GOTOHead);
        dumpObject.put("ACTIONTable", ACTIONTable);
        dumpObject.put("GOTOTable", GOTOTable);
        //将dumpObject转换为JSON字符串形式，并存储在本地文件data.js中
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String dumpJSON = gson.toJson(dumpObject);

        //E:\Desktop\newSEUCompile\SEUCompiler
        //String s = System.getProperty("user.dir");

        String VisualizerPath = Paths.get(System.getProperty("user.dir"), "doc").toString();
        if(this.resultDir!=null){
            VisualizerPath = this.resultDir;
        }
        try {
            FileWriter file = new FileWriter(Paths.get(VisualizerPath, "./data.js").toString());
            file.write("window._seulex_data = " + dumpJSON);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //如果viewNow为true，则打开本地浏览器并显示分析表格的可视化结果
        if (viewNow) {
            try {
                Runtime.getRuntime().exec("cmd /c start " + Paths.get(VisualizerPath, "./index.html"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
