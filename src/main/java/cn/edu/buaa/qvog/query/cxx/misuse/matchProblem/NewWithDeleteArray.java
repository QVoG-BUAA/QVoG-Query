package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.cxx.CxxDeleteExpression;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.cxx.CxxNewExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;

// 2
public class NewWithDeleteArray extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(NewWithDeleteArray.class.getSimpleName(), new NewWithDeleteArray())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(v -> v instanceof CxxNewExpression newExpr
                        && newExpr.getVectored()))
                .from("sink", value -> value.toStream().anyMatch(v -> v instanceof CxxDeleteExpression deleteExpr
                        && !deleteExpr.getVectored()))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}
