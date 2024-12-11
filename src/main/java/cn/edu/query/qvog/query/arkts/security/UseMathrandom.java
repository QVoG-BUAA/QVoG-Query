package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.Literal;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;
import cn.edu.query.qvog.query.cxx.misuse.matchProblem.StrlenInMalloc;
import cn.edu.query.qvog.query.cxx.misuse.outdateProblem.Encrypt3DES;

public class UseMathrandom extends ArkTSQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(UseMathrandom.class.getSimpleName(), new UseMathrandom())
                .close();
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(
                        v -> v instanceof CallExpression callExpression &&
                                callExpression.getFunction().getName().contains("random")))
                .select("source");
    }
}


/*
function generateRandom(min: number, max: number){
    let tmp: number = Math.random();
    return Math.floor(tmp * (max - min) + min);
}

function getRandomStudent(students: []){
    let num: number = generateRandom(0, students.length);
    return students[num];
}
*/