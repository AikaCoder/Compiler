package org.SeuCompiler.Yacc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1DFA;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1Item;
import org.SeuCompiler.Yacc.LR1Analyzer.ACTIONTableCell;
import org.SeuCompiler.Yacc.LR1Analyzer.LR1Analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Visualiser {
    /**
     * 实现了LR(1)分析表的可视化功能
     * 将ACTION和GOTO表格转换为JSON数据并存储在本地文件中
     * 如果viewNow参数为true，则打开本地浏览器并显示分析表格的可视化结果
     */
    public static void visualizeACTIONGOTOTable(LR1Analyzer lr1Analyzer, boolean viewNow) {
        //通过LR1Analyzer获取ACTION和GOTO表格的数据
        List<String> ACTIONHead = new ArrayList<>();
        //将表头ACTIONHead和GOTOHead分别转换为List<String>类型
        for (int i : lr1Analyzer.getACTIONReverseLookup()) {
            ACTIONHead.add(lr1Analyzer.getSymbolString(i));
        }
        List<String> GOTOHead = new ArrayList<>();
        for (int i : lr1Analyzer.getGOTOReverseLookup()) {
            GOTOHead.add(lr1Analyzer.getSymbolString(i));
        }
        int[] colUsage = new int[ACTIONHead.size()];
        //根据表格数据生成对应的List<List<String>>类型的ACTIONTable和GOTOTable
        List<List<String>> ACTIONTable = new ArrayList<>();
        for (int i = 0; i < lr1Analyzer.getACTIONTable().size(); i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < lr1Analyzer.getACTIONTable().get(i).size(); j++) {
                ACTIONTableCell cell = lr1Analyzer.getACTIONTable().get(i).get(j);
                switch (cell.type().getType()) {
                    case "acc" -> {
                        row.add("acc");
                        colUsage[j] = 1;
                    }
                    case "none" -> row.add("");
                    case "reduce" -> {
                        String producer = lr1Analyzer.formatPrintProducer(lr1Analyzer.getProducers().get(cell.data())).trim();
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
        String VisualizerPath = Paths.get(System.getProperty("user.dir"), "../../../enhance/TableVisualizer").toString();
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
                Runtime.getRuntime().exec("start " + Paths.get(VisualizerPath, "./index.html"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将LR(1)分析器生成的LR(1)自动机可视化为一个图形
     * 并将该图形以JSON格式保存到本地文件中
     * 如果设置了viewNow参数为true，则还会在浏览器中显示该图形。
     */
    public void visualizeGOTOGraph(LR1DFA lr1dfa, LR1Analyzer lr1Analyzer, boolean viewNow) {
        List<List<String>> nodes = new ArrayList<>();
        List<List<String>> edges = new ArrayList<>();
        //构建节点和边的列表
        // 设置点（项目集）
        for (int i = 0; i < lr1dfa.getStates().size(); i++) {
            String topPart = "I" + i + "\n=======\n";
            List<String> stateLines = new ArrayList<>();
            boolean kernelItem = true;
            for (LR1Item item : lr1dfa.getStates().get(i).getItems()) {
                String leftPart = "";
                leftPart += lr1Analyzer.getSymbols().get(item.rawProducer().lhs()).content();
                leftPart += " -> ";
                int j = 0;
                for (; j < item.rawProducer().rhs().size(); j++) {
                    if (j == item.dotPosition()) leftPart += "●";
                    leftPart += lr1Analyzer.getSymbolString(item.rawProducer().rhs().get(j)) + " ";
                }
                if (j == item.dotPosition()) leftPart = leftPart.substring(0, leftPart.length() - 1) + "●";
                leftPart += " § ";
                String lookahead = lr1Analyzer.getSymbolString(item.lookahead());
                int sameLeftPos = -1;
                for (int k = 0; k < stateLines.size(); k++) {
                    if (stateLines.get(k).startsWith(leftPart)) {
                        sameLeftPos = k;
                        break;
                    }
                }
                if (sameLeftPos != -1) {
                    String originalLine = stateLines.get(sameLeftPos);
                    String newLine = originalLine.substring(0, originalLine.lastIndexOf(" ")) + "/" + lookahead;
                    stateLines.set(sameLeftPos, newLine);
                } else {
                    stateLines.add(leftPart + lookahead);
                }
                if (kernelItem) {
                    leftPart = "-------\n";
                    lookahead = "";
                    stateLines.add(leftPart + lookahead);
                    kernelItem = false;
                }
            }
            StringBuilder stateStringBuilder = new StringBuilder();
            stateStringBuilder.append(topPart);
            for (String line : stateLines) {
                stateStringBuilder.append(line).append("\n");
            }
            List<String> node = new ArrayList<>();
            node.add("K" + i);
            node.add(stateStringBuilder.toString().trim());
            node.add("#FFFFFF");
            nodes.add(node);
        }

        // 设置边（迁移）
        for (int i = 0; i < lr1dfa.getStates().size(); i++) {
            //分别是to和alpha
            for (Map<String, Integer> transition : lr1dfa.getAdjList().get(i)) {
                List<String> edge = new ArrayList<>();
                edge.add("K" + i);
                edge.add("K" + transition.keySet());
                edge.add("K" + i + "_" + transition.get("to"));
                edge.add(lr1Analyzer.getSymbols().get(transition.get("alpha")).content());
                edges.add(edge);
            }
        }
        //将节点和边缓存为JSON数据并将其存储到本地文件中。
        List<List<Object>> dumpObject = new ArrayList<>();
        dumpObject.add(new ArrayList<>(List.of("nodes", nodes)));
        dumpObject.add(new ArrayList<>(List.of("edges", edges)));
        Gson gson = new Gson();
        String json = gson.toJson(dumpObject);

        // 存储JSON数据到本地文件
        try (FileWriter writer = new FileWriter("./data.js")) {
            writer.write("window._seulex_shape = 'rect';\nlet data = ");
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //如果设置了viewNow参数为true，则启动浏览器显示该图形
        if (viewNow) {
            try {
                Runtime.getRuntime().exec("cmd /c start index.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
