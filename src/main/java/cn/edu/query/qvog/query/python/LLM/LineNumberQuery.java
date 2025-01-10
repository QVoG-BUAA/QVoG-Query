package cn.edu.query.qvog.query.python.LLM;

import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.core.ioc.Environment;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.python.PythonQuery;
import cn.edu.engine.qvog.engine.language.python.lib.predicate.LineNumberMatch;
import cn.edu.query.qvog.query.python.CWE_022.TaintedPath;
import cn.edu.engine.qvog.engine.helper.JsonHelper;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class LineNumberQuery extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new TaintedPath()).close();
    }

    @Override
    public String getQueryName() {
        return "LLM: Line Number Query";
    }

    private static int[] convertJSONArrayToIntArray(JSONArray jsonArray) {
        int length = jsonArray.size();
        int[] intArray = new int[length];

        for (int i = 0; i < length; i++) {
            // 注意：这里假设JSONArray中的所有元素都是数字
            intArray[i] = Integer.parseInt(jsonArray.get(i).toString()) + 1;
        }

        return intArray;
    }

    @Override
    public CompleteQuery run() {
        try {
            JSONObject config = JsonHelper.load("./config.json");
            JSONObject llm = JsonHelper.tryGetObject(config, "llm");

            String path = JsonHelper.tryGetValue(llm, "path");
            String cwe = JsonHelper.tryGetValue(llm, "cwe");
            String file = JsonHelper.tryGetValue(llm, "file");

            JSONObject json = JsonHelper.load(path);
            JSONObject cweJson = JsonHelper.tryGetObject(json, cwe);
            JSONObject fileJson = JsonHelper.tryGetObject(cweJson, file);
            JSONArray source = JsonHelper.tryGetArray(fileJson, "pred_source");
            JSONArray sink = JsonHelper.tryGetArray(fileJson, "pred_sink");
            System.out.println(source.toString());
            System.out.println(sink.toString());
            return QueryDescriptor.open()
                    .from("source", new LineNumberMatch(convertJSONArrayToIntArray(source)))
                    .from("sink", new LineNumberMatch(convertJSONArrayToIntArray(sink)))
                    .where(TaintFlowPredicate.with()
                            .source("source")
                            .sink("sink")
                            .as("path").exists())
                    .select("source", "sink", "path");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
