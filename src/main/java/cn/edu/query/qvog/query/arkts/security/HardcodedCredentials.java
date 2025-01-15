package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.*;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;
import cn.edu.query.qvog.query.cxx.misuse.matchProblem.StrlenInMalloc;
import cn.edu.query.qvog.query.cxx.misuse.outdateProblem.Encrypt3DES;

public class HardcodedCredentials extends ArkTSQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(HardcodedCredentials.class.getSimpleName(), new HardcodedCredentials())
                .close();
    }

    public boolean hardcodedCredential(String content){
        content = content.toLowerCase();
        return content.contains("password") || content.contains("username");
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("hardcodedCredential", value -> value.toStream().anyMatch(
                        v -> v instanceof AssignExpression expression && expression.getValue() instanceof Literal literal
                && hardcodedCredential(literal.toString())))
                .fromP("output", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("log")))
                .where(TaintFlowPredicate.with()
                        .source("hardcodedCredential")
                        .sink("output")
                        .as("path").exists())
                .select("hardcodedCredential");
    }
}

/*
function connectToDatabase(dbPassword: string) {
    const url = 'jdbc:mysql://localhost:3306/mydb?user=root&password='
    + dbPassword;
    // 连接数据库的逻辑
    console.log('Connecting to database:', url);
}
 */
