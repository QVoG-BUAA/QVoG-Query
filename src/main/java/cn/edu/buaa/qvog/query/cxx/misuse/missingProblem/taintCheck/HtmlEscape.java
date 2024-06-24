package cn.edu.buaa.qvog.query.cxx.misuse.missingProblem.taintCheck;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.HighPrecisionTaintFlow;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsDefineLikeOperation;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class HtmlEscape extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(HtmlEscape.class.getSimpleName(), new HtmlEscape())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("getenv"))
                .from("barrier", new ContainsDefineLikeOperation())
                .from("sink", new ContainsFunctionCall("puts"))
                .where(HighPrecisionTaintFlow.with()
                        .source("source")
                        .barrier("barrier")
                        .sink("sink")
                        .as("path").exists())
                .select("source", "sink");
    }
}