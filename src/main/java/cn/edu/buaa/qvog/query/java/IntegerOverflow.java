package cn.edu.buaa.qvog.query.java;

import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.dsl.lib.flow.DataFlowPredicate;
import cn.edu.buaa.qvog.engine.language.java.JavaQuery;
import cn.edu.buaa.qvog.query.java.lib.IntegerIncrease;
import cn.edu.buaa.qvog.query.java.lib.MaxIntegerDeclOrAssign;

public class IntegerOverflow extends JavaQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new IntegerOverflow()).close();
    }

    @Override
    public String getQueryName() {
        return "CWE-190 Integer Overflow";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new MaxIntegerDeclOrAssign())
                .from("sink", new IntegerIncrease())
                .where(DataFlowPredicate.builder()
                        .source("source")
                        .sink("sink")
                        .as("flow").exists())
                .select("source", "sink", "flow", "Potential Integer Overflow");
    }
}
