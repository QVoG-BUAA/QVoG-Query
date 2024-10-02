# QVoG Query

QVoG Query Library

---

## Project Setup

This project depends on the QVoG-Engine. You need to place the `QVoGine-1.0.jar` built by it in the `lib` directory of the project.

In IDEA, execute `maven install` to build the project. The generated jar package will be located in the `target` directory.

- We can create the following file structure to run in terminal.

```text
config.json
QVoGine-1.0.jar
- lib
    - Query-1.0.jar
```

## Quick Start

### Naming Format

Queries for each language should be placed under the `query.<language>` package. `query` must directly contain `<language>`, though there can be any other packages outside `query` or under `language`.

### Vulnerability Pattern Classes

For each vulnerability pattern, you need to write a separate class to represent a query rule. By default, the class name will be used as the query name, but you can customize it by overriding `getQueryName()`. The core logic of the query resides in the `run()` method.

```java
public class CustomQuery extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new CustomQuery()).close();
    }

    @Override
    public String getQueryName() {
        return "A custom query";
    }
    
    @Override
    public CompleteQuery run() {
        // ...
    }
}
```

The `main` method is used for testing during development. If you wish to use it, you need to add a `config.json` in the root directory of the project. The specific format can be found in QVoG-Engine.

### Query Writing

You can use the Fluent API to write queries. The basic usage is as follows.

```java
@Override
public CompleteQuery run() {
    return QueryDesciptor.open()  // Open a query
        .from("source", CallExpression.class)  // Select all nodes containing CallExpression as "source"
        .from("sink", new ContainsFunctionCall("open")) // Select all nodes satisfying the Predicate as "sink"
        .fromP("barrier", value -> value.toStream()
             .anyMatch(v -> v instanceof IfStatement))  // Build "barrier" with the specified condition
        .where(TaintFlowPredicate.with()  // Begin building a TaintFlowPredicate
             .source("source")     // Use "source" as the Source table
             .sink("sink")         // Use "sink" as the Sink table
             .barrier("barrier")   // Use "barrier" as the Barrier table
             .as("path")           // Store the path results in the "path" table with "source", "sink", "path" columns
             .exists())            // Return the built FlowPredicate
        .select("source", "sink", "path");     // Select the "source", "sink", and "path" columns
}
```

The possible outputs are as follows.

| source                                        | sink                                                         | path     |
| --------------------------------------------- | ------------------------------------------------------------ | -------- |
| (prod.py:59) filename = request.args.get('p') | (prod.py:61) data = open(filename, 'rb').read()              | 59 -> 61 |
| (prod.py:67) filename = request.args.get('p') | (prod.py:69) data = open(os.path.join(base_path, filename), 'rb... | 67 -> 69 |

## Advanced Query Writing

### from

The `from` clause is used to select specified data from a graph database, forming a table (`ITable`). The `from` clause can be chained together to obtain a collection of multiple tables (`ITableSet`).

Each `from` statement represents a table of type `DataTable`, but there are two types of contents: one is a general `DataColumn`, and the other is a `PredicateColumn`. `DataColumn` contains data, while `PredicateColumn` determines whether elements are in the table through a `Predicate`.

Each table contains exactly one column, with the table name and column name being the same, both set to the `alias` parameter of the `from`.

> You can obtain a table containing a `PredicateColumn` using `fromP`.

The tables obtained from `from` utilize indexing, enabling quick checks to determine whether elements are present in the tables.

> In the `from` clause, you can manually specify a `cacheTag` to cache the results.

### where

The `where` clause is used to filter or combine data from the tables obtained through the `from` clause.

#### Filter where

> Related classes are located in the `cn.edu.engine.qvog.engine.dsl.fluent.filter` package.

To filter a particular table, the core component is `IFilterPredicate`. The general usage is as follows: you can construct an `IFilterPredicate` at the point of use, which uses `IRowPredicate` to evaluate each row. The latter can further utilize `IValuePredicate` to assess the columns within a row.

```java
QueryDescriptor.open()
    .from("call", CallExpression.class)
    .where(q -> q.onTable("call")                     // Filter on the "call" table
           .where(new ContainsFunctionCall("app.run") // The value satisfies a certain predicate
                  .onColumn("call")))                 // Use the predicate on the "call" column
    .select("call", "Flask app run found!")
    .display();
```

When the query is simple, consisting of only one `from`, the `where` clause can be simplified.

```java

QueryDescriptor.open()
        .from("call", CallExpression.class)
        .where(q -> q.onTable("call")                   // Filter on the "call" table
        .where(new ContainsFunctionCall("app.run")))    // The value of the only column in the table satisfies a certain predicate
        .select("call", "Flask app run found!")
        .display();
```

We can also simplify this by using an `IValuePredicate`, where we need to use the `whereP` method.

```java
QueryDescriptor.open()
    .from("call", CallExpression.class)
    .whereP(new ContainsFunctionCall("app.run")) // The value of the only column in the unique table satisfies a certain predicate
    .select("call", "Flask app run found!")
    .display();
```

#### Join where

> The relevant interfaces are located in `cn.edu.engine.qvog.engine.dsl.fluent.flow`, and the related classes are in `cn.edu.engine.qvog.engine.dsl.lib.flow`.

This feature performs reachability analysis (flow analysis) across multiple tables and synthesizes the results into a single table. Currently, it supports ordinary Data Flow, Control Flow, and Taint Flow. The underlying traversal supports three methods: DFS (Depth-First Search), Hamiltonian Path, and Eulerian Path. The core of this feature is the `IFlowPredicate` interface.

The basic implementation of flows is as follows, with variations depending on the specific `IFlowPredicate` implementation:

1. Obtain the tables corresponding to Source, Sink, and Barrier.
2. Traverse from each `Value` in the Source. If a path can be found from the Source to the Sink without passing through any Barriers, then add the Source, Sink, and Path to the result table.

> The Source must be a table containing `DataColumn` and cannot be a table with `PredicateColumn`. There are no restrictions on Barrier and Sink.

If the Barrier is omitted, by default, all nodes are **not** Barriers; if the Sink is omitted, by default, all nodes are **Sinks**.

After using `FlowPredicate`, the specified Source, Sink, and Barrier tables will be removed from the table obtained by `From`, and a new table will be added, with its name specified by `as`. The column names in the new table will be the same as the original table names, and the column name for the path will also be the same as the table name. For example, in the following query:

```java
QueryDescriptor.open()
                .from("input", new ContainsFunctionCall("request.*"))
                .fromP("exec", new ContainsFunctionCall("exec").or(new ContainsFunctionCall("eval")))
                .where(TaintFlowPredicate.with()
                        .source("input")
                        .sink("exec")
                        .as("path").exists())
                .select("input", "exec", "path")
                .display();
```

The possible outputs are as follows.

| input                                        | exec                    | path           |
| -------------------------------------------- | ----------------------- | -------------- |
| (prod.py:92) code = request.args.get('code') | (prod.py:93) exec(code) | 92 -> 93       |
| (prod.py:92) code = request.args.get('code') | (prod.py:94) eval(code) | 92 -> 93 -> 94 |

### select

The `Select` operation is used to pick specified columns in order from the results obtained by the `from` and `where` clauses. The `Select` can only be used when there is a single table in the query. If there are multiple tables, an `IFlowPredicate` must be used to perform a Join.
