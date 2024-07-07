package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.InverseControlAndCgFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

// 2
public class RandAndSrand extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(RandAndSrand.class.getSimpleName(), new RandAndSrand())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("rand"))
                .from("sink", new ContainsFunctionCall("srand"))
                .where(InverseControlAndCgFlowPredicate.builder()
                        .source("source")
                        .sink("sink")
                        .setFlowSensitive(false)
                        .as("path").exists())
                .select("source");
    }
}
