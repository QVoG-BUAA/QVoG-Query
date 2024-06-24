package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.FunctionDefStatement;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsUnaryOperator;

public class CalculateArraySize extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute("CalculateArraySize", new CalculateArraySize())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value instanceof FunctionDefStatement)
                .from("sink", new ContainsUnaryOperator("sizeof"))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .as("path").exists()
                ).select("source", "sink");
    }
}
