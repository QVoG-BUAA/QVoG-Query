package cn.edu.buaa.qvog.query.cxx.misuse.locationProblem.forget;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsCxxSystemExit;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsMemoryAllocFunction;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

// 24 *p, 2 ptr1, ptr2 3
public class MemoryNotFree extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute("MemoryNotFreed", new MemoryNotFree())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsMemoryAllocFunction())
                .from("sink", new ContainsFunctionCall("free"))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .setFlowSensitive(false)
                        .addSysExitAsSink(true)
                        .specialSink(new ContainsCxxSystemExit())
                        .as("path").exists())
                .select("source");
    }
}
