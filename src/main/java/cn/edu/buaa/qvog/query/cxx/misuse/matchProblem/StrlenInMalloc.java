package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;

public class StrlenInMalloc extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(StrlenInMalloc.class.getSimpleName(), new StrlenInMalloc())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression
                                && "strlen".equals(callExpression.getFunction().getName())
                                && ((CallExpression) v).getParent() instanceof CallExpression parentCall
                                && "malloc".equals(parentCall.getFunction().getName())))
                .select("source");
    }
}
