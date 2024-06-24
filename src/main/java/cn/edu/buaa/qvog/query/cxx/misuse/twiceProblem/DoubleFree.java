package cn.edu.buaa.qvog.query.cxx.misuse.twiceProblem;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsMemoryAllocFunction;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class DoubleFree extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(DoubleFree.class.getSimpleName(), new DoubleFree())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("free"))
                .from("barrier", new ContainsMemoryAllocFunction())
                .from("sink", new ContainsFunctionCall("free"))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}
