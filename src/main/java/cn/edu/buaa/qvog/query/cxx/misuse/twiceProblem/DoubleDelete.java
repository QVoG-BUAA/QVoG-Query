package cn.edu.buaa.qvog.query.cxx.misuse.twiceProblem;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.cxx.CxxDeleteExpression;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.cxx.CxxNewExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.CrossFunctionTaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsDefineLikeOperation;

public class DoubleDelete extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(DoubleDelete.class.getSimpleName(), new DoubleDelete())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(v -> v instanceof CxxDeleteExpression))
                .from("barrier", new ContainsDefineLikeOperation()
                        .or(value -> value.toStream().anyMatch(v -> v instanceof CxxNewExpression)))
                .from("sink", value -> value.toStream().anyMatch(v -> v instanceof CxxDeleteExpression))
                .where(CrossFunctionTaintFlowPredicate.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}
