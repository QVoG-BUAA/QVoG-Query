package cn.edu.buaa.qvog.query.cxx.misuse.matchProblem;

import cn.edu.buaa.qvog.engine.core.graph.types.PointerType;
import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.Reference;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsBinaryOperator;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsUnaryOperator;

public class CalculateArrayOffset extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute("CalculateArrayOffset", new CalculateArrayOffset())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsBinaryOperator(
                        expression -> expression instanceof Reference reference
                                && reference.getType() instanceof PointerType,
                        expression -> new ContainsUnaryOperator("sizeof").test(expression),
                        null))
                .select("source");
    }
}
