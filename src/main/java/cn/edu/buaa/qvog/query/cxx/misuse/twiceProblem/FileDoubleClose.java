package cn.edu.buaa.qvog.query.cxx.misuse.twiceProblem;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.CrossFunctionTaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsDefineLikeOperation;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class FileDoubleClose extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(FileDoubleClose.class.getSimpleName(), new FileDoubleClose())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("fclose"))
                .from("barrier", new ContainsDefineLikeOperation())
                .from("sink", new ContainsFunctionCall("fclose"))
                .where(CrossFunctionTaintFlowPredicate.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}
