package me.bechberger.jfr.wrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.FunctionNode;
import me.bechberger.jfr.wrap.nodes.IdentifierNode;
import me.bechberger.jfr.wrap.nodes.OrderByNode;


public class Evaluator {
    private static Evaluator instance;
    private HashMap<AstNode, EvalTable> tables;
    private List<FunctionNode> aggregates;
    private List<AstNode> groupings;
    public EvalState state = EvalState.INITIAL;
    private Map<AstNode, Object[]> percentiles;
    private Map<AstNode, AstNode> assignments;

    private Evaluator() {
        this.tables = new HashMap<AstNode, EvalTable>();
        this.aggregates = new ArrayList<FunctionNode>();
        this.groupings = new ArrayList<AstNode>();
        this.assignments = new HashMap<AstNode, AstNode>();
    }

    public void addTable(EvalTable table, AstNode root) {
        table.setQuery(root);
        tables.put(root, table);
    }

    public void addToTable(EvalTable table, AstNode root) {
        if(tables.isEmpty()) {
            addTable(table, root);
        } else {
            EvalTable tab1 = tables.values().stream().findFirst().orElse(null);
            tables.clear();
            ArrayList<Column> resCol = new ArrayList<Column>();
            for(Column col : tab1.getColumns()) {
                resCol.add(col);
            }
            for(Column col : table.getColumns()) {
                resCol.add(col);
            }
            EvalTable res = new EvalTable(resCol, new ArrayList<EvalRow>());
            for(EvalRow a : tab1.rows) {
                for(EvalRow b : table.rows) {
                    EvalRow newRow = new EvalRow();
                    for(Map.Entry<String, Object> entry : a.getFields().entrySet()) {
                        newRow.addField(entry.getKey(), entry.getValue());
                    }
                    for(Map.Entry<String, Object> entry : b.getFields().entrySet()) {
                        newRow.addField(entry.getKey(), entry.getValue());
                    }
                    res.addRow(newRow);
                }
            }

            tables.put(root, res);

        }
    
    }

    public EvalTable getTable(AstNode root) {
        return tables.get(root);
    }

    public void switchTable(AstNode root, EvalTable table) {
        if (tables.containsKey(root)) {
            tables.replace(root, table);
        } else {
            tables.put(root, table);
        }
    }

    

    public static Evaluator getInstance() {
        if(instance == null) {
            instance = new Evaluator();
        }
        return instance;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<AstNode, EvalTable> entry : tables.entrySet()) {
            EvalTable table = entry.getValue();
            sb.append("Rows:\n");
            for (EvalRow row : table.rows) {
                sb.append(row).append("\n");
            }
            sb.append("\nColumns: ");
            for (Column column : table.columns) {
                sb.append(column.getFullName()).append(", ");
            }
        }
        return sb.toString();
    }

    public void addAggregate(AstNode aggregate) {
        aggregates.add((FunctionNode) aggregate);
    }

    public void addGrouping(AstNode grouping) {
        groupings.add(grouping);
    }

    public void group(AstNode root) {
        // Ensure there is at least one table and groupings are defined
        if (tables.isEmpty() || groupings.isEmpty()) {
            return;
        }

        // Get the first table
        EvalTable table = tables.get(root);
        if (table == null) {
            throw new IllegalStateException("No table available for grouping.");
        }

        // Group rows by the fields in the groupings list
        Map<List<Object>, List<EvalRow>> groupedRows = table.rows.stream()
            .collect(Collectors.groupingBy(row -> groupings.stream()
                .map(grouping -> grouping.eval(row, root)) // Evaluate each grouping field
                .collect(Collectors.toList())));    // Collect grouping field values into a list

        // Create a new list of rows for the grouped table
        List<EvalRow> newRows = new ArrayList<>();
        for (Map.Entry<List<Object>, List<EvalRow>> entry : groupedRows.entrySet()) {
            List<Object> groupKey = entry.getKey();
            List<EvalRow> group = entry.getValue();

            // Create a new row for the group
            EvalRow newRow = new EvalRow();

            // Add grouping fields to the new row
            for (int i = 0; i < groupings.size(); i++) {
                String groupingFieldName = groupings.get(i).toString(); // Use the grouping field's string representation
                newRow.addField(groupingFieldName, groupKey.get(i));
            }

            // Evaluate aggregate functions and add them to the new row
            for (FunctionNode aggregate : aggregates) {
                Object aggregateValue = aggregate.eval(group, root); // Evaluate the aggregate function for the group
                String aggregateFieldName = aggregate.getName(); // Use the aggregate function's string representation
                newRow.addField(aggregateFieldName, aggregateValue);
            }

            // Add the new row to the list of new rows
            newRows.add(newRow);
        }

        // Create a new table with the grouped rows
        List<Column> newColumns = new ArrayList<>();
        for (AstNode grouping : groupings) {
            newColumns.add(new Column(grouping.getName(), null)); // Add grouping columns
        }
        for (AstNode aggregate : aggregates) {
            newColumns.add(new Column(aggregate.getName(), null)); // Add aggregate columns
        }
        EvalTable newTable = new EvalTable(newColumns, newRows);

        // Replace the existing table with the new grouped table
        tables.clear();
        tables.put(root, newTable);
    }

    private void addPercentile(AstNode node, Object[] percentiles) {
        if (this.percentiles == null) {
            this.percentiles = new HashMap<>();
        }
        this.percentiles.put(node, percentiles);
    }

    public Object[] getPercentiles(AstNode node, AstNode root) {
        if (this.percentiles == null) {
            return calculatePercentiles(node, root);
        }
        return this.percentiles.get(node);
    }

    private Object[] calculatePercentiles(AstNode node, AstNode root) {
        EvalTable table = tables.get(root);
        if (table == null || table.rows.isEmpty()) {
            return new Object[] {0.0, 0.0, 0.0, 0.0, 0.0}; // Default percentiles if no data
        }
        OrderByNode orderByNode = new OrderByNode();
        orderByNode.addOrder(node);
        orderByNode.addDirection("ASC");
        List<EvalRow> ordered = orderByNode.evalForPercentile(root);
        int index999 = (int) (table.rows.size() * 0.999);
        int index99 = (int) (table.rows.size() * 0.99);
        int index95 = (int) (table.rows.size() * 0.95);
        int index90 = (int) (table.rows.size() * 0.9);
        int index50 = (int) (table.rows.size() * 0.5);
        EvalRow row999 = ordered.get(index999);
        EvalRow row99 = ordered.get(index99);
        EvalRow row95 = ordered.get(index95);
        EvalRow row90 = ordered.get(index90);
        EvalRow row50 = ordered.get(index50);
        Object[] percentiles = new Object[5];
        percentiles[0] = node.eval(row999, root);
        percentiles[1] = node.eval(row99, root);
        percentiles[2] = node.eval(row95, root);
        percentiles[3] = node.eval(row90, root);
        percentiles[4] = node.eval(row50, root);
        addPercentile(node, percentiles);
        return percentiles;
    }

    public Object getAssignment(IdentifierNode identifierNode) {
        return assignments.get(identifierNode);
    }

    public void addAssignment(AstNode identifier, AstNode node) {
        if (identifier == null || node == null) {
            throw new IllegalArgumentException("Identifier and node cannot be null");
        }
        if (!(identifier instanceof IdentifierNode)) {
            throw new IllegalArgumentException("Identifier must be an instance of IdentifierNode");
        }
        assignments.put(identifier, node);
    }

}

