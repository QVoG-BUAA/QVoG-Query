package cn.edu.query.qvog.query.python.CWE_079;

/*
# CWE-022 Tainted path

import os.path
from flask import Flask, request, abort

app = Flask(__name__)

@app.route("/user_picture1")
def user_picture1():
    filename = request.args.get('p')
    # BAD: This could read any file on the file system
    data = open(filename, 'rb').read()
    return data

@app.route("/user_picture2")
def user_picture2():
    base_path = '/server/static/images'
    filename = request.args.get('p')
    # BAD: This could still read any file on the file system
    data = open(os.path.join(base_path, filename), 'rb').read()
    return data

@app.route("/user_picture3")
def user_picture3():
    base_path = '/server/static/images'
    filename = request.args.get('p')
    #GOOD -- Verify with normalised version of path
    fullpath = os.path.normpath(os.path.join(base_path, filename))
    if not fullpath.startswith(base_path):
        raise Exception("not allowed")
    data = open(fullpath, 'rb').read()
    return data
 */

import cn.edu.engine.qvog.engine.core.graph.values.statements.IfStatement;
import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.language.python.PythonQuery;
import cn.edu.engine.qvog.engine.language.python.lib.predicate.ContainsFunctionCall;

public class XSS extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new XSS()).close();
    }

    @Override
    public String getQueryName() {
        return "CWE-079: XSS";
    }

    @Override
    public CompleteQuery run() {
        return QueryDescriptor.open()
                .from("source", new ContainsFunctionCall("request.*"))
                .from("sink", new ContainsFunctionCall("make_response"))
                .fromP("barrier", new ContainsFunctionCall("escape"))
                .where(TaintFlowPredicate.with()
                        .source("source")
                        .sink("sink")
                        .barrier("barrier")
                        .as("path").exists())
                .select("source", "sink", "path");
    }
}