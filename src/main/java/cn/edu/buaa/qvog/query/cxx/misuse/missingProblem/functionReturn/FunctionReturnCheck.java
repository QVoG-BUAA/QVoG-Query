package cn.edu.buaa.qvog.query.cxx.misuse.missingProblem.functionReturn;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.DeclarationStatement;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.AssignExpression;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.HighPrecisionTaintFlow;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsCxxSystemExit;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class FunctionReturnCheck extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(FunctionReturnCheck.class.getSimpleName(), new FunctionReturnCheck())
                .close();

    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> {
                    if (value instanceof AssignExpression || value instanceof DeclarationStatement
                            || value instanceof CallExpression) {
                        return new ContainsFunctionCall("fgets")
                                .or(new ContainsFunctionCall("fopen"))
                                .or(new ContainsFunctionCall("malloc"))
                                .or(new ContainsFunctionCall("scanf"))
                                .or(new ContainsFunctionCall("socket")).test(value);
                    }
                    return false;
                })
                .from("barrier", expr -> expr instanceof IfStatement)
                .from("sink", e -> e instanceof CallExpression
                        || e instanceof AssignExpression
                        || e instanceof DeclarationStatement)
                .where(HighPrecisionTaintFlow.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .specialSink(new ContainsCxxSystemExit())
                        .as("path").exists())
                .select("source", "sink");
    }
}
