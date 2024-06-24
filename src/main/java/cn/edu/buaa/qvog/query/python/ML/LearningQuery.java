package cn.edu.buaa.qvog.query.python.ML;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.dsl.lib.predicate.PairMatch;
import cn.edu.buaa.qvog.engine.language.python.PythonQuery;
import cn.edu.buaa.qvog.engine.ml.PredictTypes;

public class LearningQuery extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new LearningQuery()).close();
    }

    @Override
    public String getQueryName() {
        return "Machine Learning Query";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", PredictTypes.Source)
                .from("sink", PredictTypes.Sink)
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .as("path").exists())
                .where(p -> p.where(new PairMatch("source", "sink")))
                .select("source", "sink", "path");
    }
}
