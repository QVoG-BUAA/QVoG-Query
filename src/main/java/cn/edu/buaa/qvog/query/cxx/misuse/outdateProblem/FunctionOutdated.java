package cn.edu.buaa.qvog.query.cxx.misuse.outdateProblem;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

public class FunctionOutdated extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(FunctionOutdated.class.getSimpleName(), new FunctionOutdated())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("gets")
                        .or(new ContainsFunctionCall("gmtime"))
                        .or(new ContainsFunctionCall("sprintf"))
                        .or(new ContainsFunctionCall("strcpy")))
                .select("source");
    }
}
