package cn.edu.query.qvog.query.arkts.security;

import cn.edu.engine.qvog.engine.core.graph.types.StringType;
import cn.edu.engine.qvog.engine.core.graph.values.statements.FunctionDefStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.engine.qvog.engine.core.graph.values.statements.expressions.Literal;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.ArkTS.ArkTSQuery;
import cn.edu.query.qvog.query.cxx.misuse.CxxQueryHelper;

public class ForEachThree extends ArkTSQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance()
                .execute(ForEachThree.class.getSimpleName(), new ForEachThree())
                .close();
    }

    @Override
    public String getQueryName() {
        return "ForEach";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", value -> value.toStream().anyMatch(
                        e -> e instanceof FunctionDefStatement functionDefStatement &&
                                functionDefStatement.getFunction().getName().contains("ForeachTest.%AM0") &&
                                functionDefStatement.getArguments().size() <3))
                .select("source");
    }
}

/*
@Entry
@Component
struct ForeachTest {
    private data: string[] = ['1', '2', '3'];
    build() {
        RelativeContainer() {
            List() {
                // ForEach缺少第三个参数，告警
                ForEach(this.data, (item: string, index: number) => {
                    ListItem() {
                        Text(item);
                    }
                })
            }
            .width('100%')
            .height('100%')
        }
        .height('100%')
        .width('100%')
    }
}
*/

