package me.bechberger.jfr.wrap;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import me.bechberger.jfr.query.Table;
import me.bechberger.jfr.tool.ConfigOptions;
import me.bechberger.jfr.tool.QueryCommand;
import me.bechberger.jfr.util.UserDataException;
import me.bechberger.jfr.util.UserSyntaxException;
import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.FunctionNode;
import me.bechberger.jfr.wrap.nodes.OrderByNode;

/*
 * Contains much of the evaluation logic
 * and keeps track of everything during evaluation
 * Is a singleton to ensure only one instance
 * 
 * Keeps track of the path to the jfr file
 * Map of tables (key is the root node of the query)
 * Map of nonSelected (Tables which are assigned to smth and not to be printed)
 * Map of aggregates for a query
 * Map of aggregate columns for a query #TODO: check if necessary
 * Map of non aggregates for a query
 * Map of groupings in a query
 * State of evaluation for a query
 * Map of percentiles for a Field in a Query (Identifier filed is key of inner map)
 * Map of assignments for a query (Identifier is key)
 * Map of todos for a query (Root node is key)
 * Map of gc tables for a Time in a query (Uses TreeMap with Instant as key)
 * Root node of the query being currently evaluated
 */
public class Evaluator {
    private String pathToFile;
    private static Evaluator instance;
    private HashMap<AstNode, EvalTable> tables;
    private HashMap<AstNode, EvalTable> nonSelected;
    private Map<AstNode, List<FunctionNode>> aggregates;
    private Map<AstNode, List<AstNode>> nonAggregates;
    private Map<AstNode, List<AstNode>> groupings;
    public EvalState state = EvalState.INITIAL;
    private Map<AstNode, Map<AstNode, Object[]>> percentiles;
    private Map<String, AstNode> assignments;
    private Map<String, AstNode> todos;
    private Map<AstNode, TreeMap<Instant, Integer>> gcTables;
    private AstNode currentRoot;

    private Evaluator() {
        this.tables = new HashMap<AstNode, EvalTable>();
        this.nonSelected = new HashMap<AstNode, EvalTable>();
        this.aggregates = new HashMap<AstNode, List<FunctionNode>>();
        this.nonAggregates = new HashMap<AstNode, List<AstNode>>();
        this.groupings = new HashMap<AstNode, List<AstNode>>();
        this.assignments = new HashMap<String, AstNode>();
        this.percentiles = new HashMap<AstNode, Map<AstNode, Object[]>>();
        this.todos = new HashMap<String, AstNode>();
        this.gcTables = new HashMap<AstNode, TreeMap<Instant, Integer>>();
        this.currentRoot = null;
    }

    public void setFile(String path) {
        pathToFile = path;
    }

    public String getFile() {
        return pathToFile;
    }

    public void addTable(EvalTable table, AstNode root) {
        table.setQuery(root);
        tables.put(root, table);
    }

    /*
     * Moves a table to the nonSelected Map (called after assignment is evaluated)
     */
    public void moveNonSelected(AstNode root) {
        if (tables.containsKey(root)) {
            EvalTable table = tables.get(root);
            nonSelected.put(root, table);
            tables.remove(root);
        } else {
            throw new IllegalStateException("No table found for the given root node");
        }
    }

    /*
     * Computes cross product
     * #TODO: check if used at any point
     */
    public void addToTable(EvalTable table, AstNode root) {
        if(!tables.containsKey(root)) {
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
        if(!tables.containsKey(root)) {
            return nonSelected.get(root);
        }
        return tables.get(root);
    }
    
    /*
     * Switch a table with a key with another one, preserving the key
     */
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
    
    /*
     * Destroy evaluator instance for testing purposes
     */
    public void destruct() {
        instance = null;
    }

    public void addTodo(String todo, AstNode root) {
        todos.put(todo, root);
    }

    /*
     * Evaluate all nodes in the Todo map
     */
    public void evalTodos() {
        todos.forEach((String, node) -> node.eval(node));
    }

    /*
     * #TODO: check in here if aggregateColumns is necessary
     */
    public void addAggregate(AstNode aggregate, AstNode root) {
        if(aggregates.get(root) == null) {
            aggregates.put(root, new ArrayList<FunctionNode>());
        }
        if(aggregates.get(root).contains(aggregate)) {
            // Avoid adding the same aggregate multiple times
        } else {
            aggregates.get(root).add((FunctionNode) aggregate);
        }
    }

    public void addNonAggregate(AstNode nonAggregate, AstNode root) {
        if(nonAggregates.get(root) == null) {
            nonAggregates.put(root, new ArrayList<AstNode>());
        }
        if(nonAggregates.get(root).contains(nonAggregate)) {
            // Avoid adding the same non-aggregate multiple times
        } else {
            nonAggregates.get(root).add(nonAggregate);
        }
    }
    
    public void addGrouping(AstNode grouping, AstNode root) {
        if (groupings.get(root) == null) {
            groupings.put(root, new ArrayList<AstNode>());
        }
        groupings.get(root).add(grouping);
    }
    
    /*
     * Grouping logic
     * Leverages the Collectors.groupingBy method provided by Java
     */
    public void group(AstNode root) {
        // Ensure there is at least one table and groupings are defined
        if (tables.isEmpty() || (aggregates.isEmpty() && groupings.isEmpty())) {
            return;
        }
        
        // Get the table
        EvalTable table = tables.get(root);
        
        Map<List<Object>, List<EvalRow>> groupedRows = new LinkedHashMap<>();
        if(!groupings.isEmpty()) {
            // Group rows by the fields in the groupings list
            groupedRows = table.rows.stream()
            .collect(Collectors.groupingBy(row -> groupings.get(root).stream()
            .map(grouping -> grouping.eval(row, root)) // Evaluate each grouping field
            .collect(Collectors.toList()),
            LinkedHashMap::new,
            Collectors.toList()));    // Collect grouping field values into a list
        } else {
            groupedRows.put(new ArrayList<Object>(), table.rows); // If no groupings, use all rows as a single group
        }
            
        // Create a new list of rows for the grouped table
        List<EvalRow> newRows = new ArrayList<>();
        for (Map.Entry<List<Object>, List<EvalRow>> entry : groupedRows.entrySet()) {
            List<Object> groupKey = entry.getKey();
            List<EvalRow> group = entry.getValue();
            
            // Create a new row for the group
            EvalRow newRow = new EvalRow();
            
            if(!groupings.isEmpty()) {
                // Add grouping fields to the new row
                for (int i = 0; i < groupings.get(root).size(); i++) {
                    String groupingFieldName = groupings.get(root).get(i).getName(); // Use the grouping field's string representation
                    newRow.addField(groupingFieldName, groupKey.get(i));
                }
            }
            
            if(aggregates.get(root) == null || aggregates.get(root).isEmpty()) {
                // If no aggregates are defined, just add the first row's fields
                for (EvalRow row : group) {
                    for (Map.Entry<String, Object> entryField : row.getFields().entrySet()) {
                        if (!newRow.getFields().containsKey(entryField.getKey())) {
                            newRow.addField(entryField.getKey(), entryField.getValue());
                        }
                    }
                }
                newRows.add(newRow);
                continue;
            } else {
                // Evaluate aggregate functions and add them to the new row
                for (FunctionNode aggregate : aggregates.get(root)) {
                    Object aggregateValue = aggregate.eval(group, root); // Evaluate the aggregate function for the group
                    String aggregateFieldName = aggregate.getName(); // Use the aggregate function's string representation
                    newRow.addField(aggregateFieldName, aggregateValue);
                }
            }
            
            // Add the new row to the list of new rows
            newRows.add(newRow);
        }
        
        // Create a new table with the grouped rows
        List<Column> newColumns = new ArrayList<>();
        if(!groupings.isEmpty()) {
            for (AstNode grouping : groupings.get(root)) {
                newColumns.add(new Column(grouping.getName(), null)); // Add grouping columns
            }    
        }

        if(aggregates.get(root) == null || aggregates.get(root).isEmpty()) {
            // If no aggregates are defined, just add the original columns
            EvalTable originalTable = tables.get(root);
            for (Column col : originalTable.getColumns()) {
                if (!newColumns.stream().anyMatch(c -> c.getFullName().equals(col.getFullName()))) {
                    newColumns.add(col); // Add original columns that are not grouping or aggregate columns
                }
            }
        } else {
            for (AstNode aggregate : aggregates.get(root)) {
                newColumns.add(new Column(aggregate.getName(), null)); // Add aggregate columns
            }
        }
        EvalTable newTable = new EvalTable(newColumns, newRows);
        newTable.setGrouped(true);
        
        // Replace the existing table with the new grouped table
        tables.remove(root);
        tables.put(root, newTable);
    }
    
    private void addPercentile(AstNode node, Object[] percentiles, AstNode root) {
        if(this.percentiles.get(root) == null) {
            this.percentiles.put(root, new HashMap<AstNode, Object[]>());
        }
        this.percentiles.get(root).put(node, percentiles);
    }
    
    /*
     * Called from funcionNode to check if some field is in the percentile range
     * If percentiles for that node in that query were already calculated
     * get the values form the existing table
     * else calculate them
     */
    public Object[] getPercentiles(AstNode node, AstNode root) {
        if (this.percentiles.get(root) == null) {
            return calculatePercentiles(node, root);
        }
        return this.percentiles.get(root).get(node);
    }
    
    /*
     * Order a copy of the table corresponding to the root node by the node for which
     * to evaluate the percentiles
     * 
     * Calculate indexes (inclusive) for the 99.9, 99, 95, 90 and 50 percentiles
     * Get the rows at those indexes and evaluate the node for each of them
     * Create an array with the values of the percentiles
     * Add the array to the percentiles map for the node and root node
     * Return the array
     */
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
        addPercentile(node, percentiles, root);
        return percentiles;
    }
    
    public Object getAssignment(String identifier) {
        return assignments.get(identifier);
    }
    
    public void addAssignment(String identifier, AstNode node) {
        if (identifier == null || node == null) {
            throw new IllegalArgumentException("Identifier and node cannot be null");
        }
        assignments.put(identifier, node);
    }

    public void setRoot(AstNode root) {
        this.currentRoot = root;
    }

    public AstNode getCurrentRoot() {
        return this.currentRoot;
    }

    /*
     * Evaluates the GC correlation for a given timestamp
     * if the table for the GCs is not present start a
     * new query with all GCs in the file, convert it to a EvalTable
     * then take the startTime and gcId from that table and cache those
     * in a TreeMap.
     * 
     * Then take the given timestamp and get the floorEntry and ceilingEntry
     * for previous and next gc.
     * Then calculate the closest entry to the timestamp via the difference
     * Return the gcId for each of those in an array
     */
    public Object[] evalGC(Instant timestamp, AstNode root) {
        if (gcTables == null) {
            gcTables = new HashMap<>();
        }
        if (!gcTables.containsKey(root)) {
            EvalTable evalTable = new EvalTable(new ArrayList<Column>(), new ArrayList<EvalRow>());
            QueryCommand queryCommand = new QueryCommand();
            queryCommand.setView("SELECT startTime, gcId FROM GarbageCollection");
            queryCommand.setFile(getFile());
            queryCommand.setConfigOptions(new ConfigOptions());
            try {
                Table table = queryCommand.call();
                evalTable = TableUtils.toEvalTable(table);
            } catch (UserSyntaxException | UserDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for(EvalRow row : evalTable.rows) {
                Instant startTime = (Instant) row.getFields().get("startTime");
                int gcId = (int) row.getFields().get("gcId");
                gcTables.computeIfAbsent(root, k -> new TreeMap<>()).put(startTime, gcId);
            }
        }
        TreeMap<Instant, Integer> treemap = gcTables.get(root);
        if (treemap == null) {
            throw new IllegalStateException("No GC table found for the given root node");
        }
        Map.Entry<Instant, Integer> floorEntry = treemap.floorEntry(timestamp);
        Map.Entry<Instant, Integer> ceilingEntry = treemap.ceilingEntry(timestamp);
        Map.Entry<Instant, Integer> closestEntry;
        if(floorEntry == null && ceilingEntry == null) {
            return new Object[] {null, null, null};
        } else if(floorEntry == null) {
            return new Object[] {null, ceilingEntry.getValue(), ceilingEntry.getValue()};
        } else if (ceilingEntry == null) {
            return new Object[] {floorEntry.getValue(), null, floorEntry.getValue()};
        } else {
            // Choose the closest entry based on the timestamp
            closestEntry = Duration.between(timestamp, floorEntry.getKey()).compareTo(Duration.between(timestamp, ceilingEntry.getKey())) < 0 ? floorEntry : ceilingEntry;
        }
        Integer beforeGc = floorEntry.getValue();
        Integer afterGc = ceilingEntry.getValue();
        Integer nearGc = closestEntry.getValue();
        return new Object[] {beforeGc, afterGc, nearGc};
    }
    
    /*
     * Used in the projection stage
     * Removes a specified column from the table
     * belonging to the given root node
     */
    public void removeCol(Column col, AstNode root) {
        if (col == null) {
            throw new IllegalArgumentException("Column cannot be null");
        }
        EvalTable table = tables.get(root);
        if (table == null) {
            throw new IllegalStateException("No table found for the given root node");
        }
        String toSearch =  col.getFullName();
        // Clone Column list, as it might be immutable in some cases #TODO: check if this is still true
        ArrayList<Column> mutable = new ArrayList<Column>(table.getColumns());
        mutable.removeIf(column -> column.getFullName().equals(toSearch));
        table.setColumns(mutable);
        for (EvalRow row : table.rows) {
            row.getFields().remove(toSearch);
        }
    }

    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<AstNode, EvalTable> entry : tables.entrySet()) {
            EvalTable table = entry.getValue();
            sb.append(table.toString());
        }
        return sb.toString();
    }

    /*
     * If there are any non-aggregates (GC correlation)
     * add a column for each, evaluate the value for each row
     * and insert it into the new column
     */
    public void evalNonAggregates(AstNode root) {
        EvalTable table = tables.get(root);
        List<AstNode> nonAggregatesList = nonAggregates.get(root);
        if (table == null || nonAggregatesList == null) {
            return; // No non-aggregates to evaluate
        }
        for (AstNode nonAggregate : nonAggregatesList) {
            table.addColumn(new Column(nonAggregate.getName(), null));
            for (EvalRow row : table.rows) {
                Object value = nonAggregate.eval(row, root);
                row.addField(nonAggregate.getName(), value);
            }
        }
    }
    
}

