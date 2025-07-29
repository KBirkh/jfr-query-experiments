package me.bechberger.jfr.wrap;

public enum EvalState {
    INITIAL, // Initial state before any evaluation
    FROM, // Evaluating FROM clause
    WHERE, // Evaluating WHERE conditions
    GROUP_BY, // Evaluating GROUP BY conditions
    HAVING, // Evaluating HAVING conditions
    SELECT, // Evaluating SELECT expressions
    ORDER_BY, // Evaluating ORDER BY expressions
    LIMIT; // Evaluating LIMIT expressions
}
