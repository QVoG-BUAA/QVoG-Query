package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;

public class CodeInjection extends ArkTSQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(CodeInjection.class.getSimpleName(), new CodeInjection())
                .close();
    }

    @Override
    public String getQueryName() {
        return "CWE-094: Code Injection";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("input", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("getQueryParameter")))
                .fromP("exec", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("eval")))
                .fromP("barrier", value -> value.toStream().anyMatch(v -> v instanceof IfStatement))
                .where(TaintFlowPredicate.with()
                        .source("input")
                        .sink("exec")
                        .barrier("barrier")
                        .as("path").exists())
                .select("input", "exec");
    }
}

/*
import router from '@system.router';

function runCode(){
    const code = router.getQueryParameter('code');
    eval(code);
}
*/
