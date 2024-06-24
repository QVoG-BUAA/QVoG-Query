package cn.edu.buaa.qvog.query.python.CWE_377;

/*
def write_results(results):
    filename = tempfile.mktemp()
    with open(filename, "w+") as f:
        f.write(results)
    print("Results written to", filename)
    filename = os.tempnam()
 */

import cn.edu.buaa.qvog.engine.core.graph.values.statements.expressions.CallExpression;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.buaa.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.buaa.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.buaa.qvog.engine.language.python.PythonQuery;
import cn.edu.buaa.qvog.engine.language.python.lib.predicate.ContainsFunctionCall;

public class InsecureTempFile extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new InsecureTempFile()).close();
    }

    @Override
    public String getQueryName() {
        return "CWE-377: Insecure Temp File";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("call", CallExpression.class)
                .whereP(new ContainsFunctionCall("tempfile.mktemp")
                        .or(new ContainsFunctionCall("os.t*mpnam")))
                .select("call", "InsecureTempFile");
    }
}
