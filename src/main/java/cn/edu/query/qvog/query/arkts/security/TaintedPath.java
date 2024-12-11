package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.values.statements.FunctionDefStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.Literal;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;
import cn.edu.engine.qvog.engine.language.shared.predicate.ContainsFunctionCall;
//import cn.edu.query.qvog.query.arkts.CWE.CWE_022.TaintedPath;
import cn.edu.query.qvog.query.cxx.misuse.matchProblem.StrlenInMalloc;
import cn.edu.query.qvog.query.cxx.misuse.outdateProblem.Encrypt3DES;


public class TaintedPath extends ArkTSQuery{
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(TaintedPath.class.getSimpleName(), new TaintedPath())
                .close();
    }

    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("getQueryParameter")))
                .from("sink", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("openSync")))
                .fromP("barrier", value -> value.toStream().anyMatch(v -> v instanceof IfStatement))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .barrier("barrier")
                        .as("path").exists())
                .select("sink");
    }
}
