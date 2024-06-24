package cn.edu.buaa.qvog.query.cxx.misuse.locationProblem.premature;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.CrossFunctionTaintFlowPredicate;
import cn.edu.buaa.qvog.engine.language.cxx.CxxQuery;
import cn.edu.buaa.qvog.engine.language.cxx.lib.ContainsMysqlUseFunction;
import cn.edu.buaa.qvog.engine.language.shared.predicate.ContainsFunctionCall;

// 2
public class MysqlConnUseAfterClose extends CxxQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(MysqlConnUseAfterClose.class.getSimpleName(), new MysqlConnUseAfterClose())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("mysql_close"))
                .from("barrier", new ContainsFunctionCall("mysql_real_connect"))
                .from("sink", new ContainsMysqlUseFunction())
                .where(CrossFunctionTaintFlowPredicate.with().barrier("barrier")
                        .as("path").exists())
                .select("source", "sink");
    }
}
