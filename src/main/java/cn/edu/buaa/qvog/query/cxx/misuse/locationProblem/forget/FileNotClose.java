package cn.edu.buaa.qvog.query.cxx.misuse.locationProblem.forget;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsCxxSystemExit;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

/**
 * 9 file1 failed
 * 3 file2 failed
 */
public class FileNotClose extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute("FileNotClose", new FileNotClose())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("fopen"))
                .from("sink", new ContainsFunctionCall("fclose"))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .addSysExitAsSink(true)
                        .specialSink(new ContainsCxxSystemExit())
                        .as("path").exists())
                .select("source");
    }
}
