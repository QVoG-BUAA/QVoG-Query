package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.AssignExpression;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.HighPrecisionTaintFlow;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class StrlenAfterMalloc extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(StrlenAfterMalloc.class.getSimpleName(), new StrlenAfterMalloc())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("malloc"))
                .from("barrier", e -> e instanceof CallExpression || e instanceof AssignExpression)
                .from("sink", new ContainsFunctionCall("strlen"))
                .where(HighPrecisionTaintFlow.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}
