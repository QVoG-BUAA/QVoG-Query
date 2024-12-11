package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.values.statements.FunctionDefStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.Literal;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.CrossFunctionTaintFlowPredicate;
import cn.edu.engine.qvog.engine.dsl.lib.flow.ControlFlowPredicate;
import cn.edu.engine.qvog.engine.dsl.lib.flow.HighPrecisionTaintFlow;
import cn.edu.engine.qvog.engine.dsl.lib.predicate.MatchAll;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;
import cn.edu.engine.qvog.engine.language.shared.predicate.ContainsFunctionCall;
//import cn.edu.query.qvog.query.arkts.CWE.CWE_022.TaintedPath;
import cn.edu.query.qvog.query.cxx.misuse.matchProblem.StrlenInMalloc;
import cn.edu.query.qvog.query.cxx.misuse.outdateProblem.Encrypt3DES;

/*
// Test.ets
import hilog from '@ohos.hilog';
@Entry
@Component
struct Index {
    build() {
            Column() {
                Button()
                    .onMouse(() => {
                        hilog.info(1001, 'Index', 'onScroll')
                })
            }
    }
}
 */


public class HighFrequencyHiLog extends ArkTSQuery{
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(HighFrequencyHiLog.class.getSimpleName(), new HighFrequencyHiLog())
                .close();
    }
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("onMouse")))
                .from("sink", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("info")))
                .fromP("barrier", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("pop()")))
                .where(ControlFlowPredicate.builder()
                        .source("source")
                        .sink("sink")
                        .barrier("barrier")
                        .as("flow").exists())
                .select("sink");
    }
}
