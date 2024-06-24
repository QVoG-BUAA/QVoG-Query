package cn.edu.buaa.qvog.query.cxx.misuse.locationProblem.forget;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.CrossFunctionTaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsCxxSystemExit;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

// 32->8
public class SocketNotClose extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(SocketNotClose.class.getSimpleName(), new SocketNotClose())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open().useEntryExitVirtualNode(false, true)
                .from("source", new ContainsFunctionCall("socket"))
                .from("sink", new ContainsFunctionCall("close"))
                .where(CrossFunctionTaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .addSysExitAsSink(true)
                        .specialSink(new ContainsCxxSystemExit())
                        .as("path").exists())
                .select("source");
    }
}
