package me.bechberger.jfr.wrap.nodes;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.EvalState;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a function call in the abstract syntax tree.
 * It contains the function name, its type, and a list of arguments.
 * The function can be an aggregate function like SUM, AVG, COUNT, etc.
 * It also supports special functions like BEFORE_GC, AFTER_GC, and NEAR_GC
 * Different functions are used in different parts of the query
 */
public class FunctionNode extends AstConditional {
    private String name;
    private FunctionType type;
    private List<AstNode> arguments;


    public FunctionNode(String name, List<AstNode> arguments) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
        this.arguments = arguments;
    }

    public FunctionNode() {
        arguments = new ArrayList<AstNode>();
    }

    public FunctionNode(String name, AstNode... arguments) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
        this.arguments = new ArrayList<>();
        for (AstNode arg : arguments) {
            if (arg == null) {
                throw new IllegalArgumentException("Function argument cannot be null");
            }
            this.arguments.add(arg);
        }
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        this.name = name;
        setType();
    }

    // Used to determine the type of function that this node represents
    private void setType() {
        switch(name.toUpperCase()) {
            case "SUM":
                this.type = FunctionType.SUM;
                break;
            case "AVG":
                this.type = FunctionType.AVG;
                break;
            case "MEDIAN":
                this.type = FunctionType.MEDIAN;
                break;
            case "COUNT":
                this.type = FunctionType.COUNT;
                break;
            case "MIN":
                this.type = FunctionType.MIN;
                break;
            case "MAX": 
                this.type = FunctionType.MAX;
                break;
            case "P50":
                this.type = FunctionType.P50;
                break;
            case "P90":
                this.type = FunctionType.P90;
                break;
            case "P95":
                this.type = FunctionType.P95;
                break;
            case "P99":
                this.type = FunctionType.P99;
                break;
            case "P999":
                this.type = FunctionType.P999;
                break;
            case "BEFOREGC":
                this.type = FunctionType.BEFORE_GC;
                break;
            case "AFTERGC":
                this.type = FunctionType.AFTER_GC;
                break;
            case "NEARGC":
                this.type = FunctionType.NEAR_GC;
                break;
            default:
                throw new IllegalArgumentException("Unknown function type: " + name);

        }
    }

    // Adds functions that act as aggregates to the aggregates in the evaluator
    @Override
    public void findAggregates(AstNode root) {
        switch(type) {
            case SUM:
            case AVG:
            case COUNT:
            case MIN:
            case MAX:
                Evaluator evaluator = Evaluator.getInstance();
                evaluator.addAggregate(this, root);
                break;
            case P50:
            case P90:
            case P95:
            case P99:
            case P999:
            case BEFORE_GC:
            case AFTER_GC:
            case NEAR_GC:
                break;
            default:
                throw new IllegalArgumentException("Unknown function type: " + type);
        }
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(": ").append(name);
        if (arguments != null && !arguments.isEmpty()) {
            sb.append("\n").append(dent).append("  Arguments:");
            for (AstNode arg : arguments) {
                sb.append(arg.toString(indent + 2));
            }
        } else {
            sb.append("\n").append(dent).append("  No arguments");
        }
        return sb.toString();
    }

    public void addArgument(AstNode expression) {
        arguments.add(expression);
    }

    /*
     * Delegates the evaluation of the node to different methods
     * depending on which phase is currently being processed
     */
    @Override
    public Object eval(Object obj, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if(evaluator.state == EvalState.GROUP_BY) {
            if(obj instanceof EvalRow) {
                return evalWhere((EvalRow) obj, root);
            }
            return evalGroup(obj, root);
        } else if(evaluator.state == EvalState.WHERE) {
            return evalWhere((EvalRow) obj, root);
        } else {
            return evalHaving((EvalRow) obj, root);
        }
    }

    /*
     * Evaluates functions used in the Having clause
     * For the percentile functions it delegates to the evalWhere fucntion as they are always relative to those
     * The GC correlations cannot be used in this context
     * #TODO: check for problems concerning the delegation to evalWhere when the values have not yet been calculated
     */
    private Object evalHaving(EvalRow row, AstNode root) {
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        switch (type) {
            case SUM:
            case AVG:
            case COUNT:
            case MIN:
            case MAX:
                return row.getFields().get(this.getName());
            case P50:
            case P90:
            case P95:
            case P99:
            case P999:
                return evalWhere(row, root);
            case BEFORE_GC:
            case AFTER_GC:
            case NEAR_GC:
                // These functions are not evaluated in HAVING context
                throw new UnsupportedOperationException("Function " + name + " cannot be used in HAVING clause");
            default:
                throw new IllegalArgumentException("Unknown function type: " + type);
        }
    } 

    private Object evalWhere(EvalRow row, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        switch (type) {
            case SUM:
            case AVG:
            case COUNT:
            case MIN:
            case MAX:
                // These functions are not evaluated in WHERE context
                throw new UnsupportedOperationException("Function " + name + " cannot be used in WHERE clause");
            case P50:
                Comparable<Object> toCompare = (Comparable<Object>) arguments.get(0).eval(row, root);
                if(((Comparable<Object>) evaluator.getPercentiles(arguments.get(0), root)[4]).compareTo(toCompare) > 0) {
                    return false;
                } else return true;
            case P90:
                toCompare = (Comparable<Object>) arguments.get(0).eval(row, root);
                if(((Comparable<Object>) evaluator.getPercentiles(arguments.get(0), root)[3]).compareTo(toCompare) > 0) {
                    return false;
                } else return true;
            case P95:
                toCompare = (Comparable<Object>) arguments.get(0).eval(row, root);
                if(((Comparable<Object>) evaluator.getPercentiles(arguments.get(0), root)[2]).compareTo(toCompare) > 0) {
                return false;
                } else return true;
            case P99:
                toCompare = (Comparable<Object>) arguments.get(0).eval(row, root);
                if(((Comparable<Object>) evaluator.getPercentiles(arguments.get(0), root)[1]).compareTo(toCompare) > 0) {
                    return false;
                } else return true;
            case P999:
                toCompare = (Comparable<Object>) arguments.get(0).eval(row, root);
                if(((Comparable<Object>) evaluator.getPercentiles(arguments.get(0), root)[0]).compareTo(toCompare) > 0) {
                    return false;
                } else return true;
            case BEFORE_GC:
                return evalBeforeGC(row, arguments.get(0), root);
            case AFTER_GC:
                return evalAfterGC(row, arguments.get(0), root);
            case NEAR_GC:
                return evalNearGC(row, arguments.get(0), root);
            default:
                throw new IllegalArgumentException("Unknown function type: " + type);
        }
    }

    private Object evalGroup(Object rows, AstNode root) {
        // Only called from Evaluator -> assume rows is a List<EvalRow>
        List<EvalRow> evalRows = (List<EvalRow>) rows;
    
        // Dispatch to specific function type handler
        switch (type) {
            case SUM:
                return evalSum(evalRows, root);
            case AVG:
                return evalAvg(evalRows, root);
            case COUNT:
                return evalCount(evalRows);
            case MIN:
                return evalMin(evalRows, root);
            case MAX:
                return evalMax(evalRows, root);
            // Should already be evaluated by this time
            case P90:
            case P95:
            case P99:
            case P999:
            case BEFORE_GC:
            case AFTER_GC:
            case NEAR_GC:
                return evalRows;
            default:
                throw new IllegalArgumentException("Unknown function type: " + type);
        }
    }

    private Object evalSum(List<EvalRow> rows, AstNode root) {
        AstNode arg = arguments.get(0);
        if (rows.isEmpty()) {
            return 0;
        }
    
        if (arg.eval(rows.get(0), root) instanceof Number) {
            return rows.stream()
                .mapToDouble(row -> ((Number) arg.eval(row, root)).doubleValue())
                .sum();
        } else if (arg.eval(rows.get(0), root) instanceof Duration) {
            return rows.stream()
                .map(row -> (Duration) arg.eval(row, root))
                .reduce(Duration.ZERO, Duration::plus);
        } else {
            throw new IllegalArgumentException("SUM can only be applied to Number or Duration types");
        }
    }

    private Object evalAvg(List<EvalRow> rows, AstNode root) {
        AstNode arg = arguments.get(0);
        if (rows.isEmpty()) {
            return 0.0;
        }
        if (arg.eval(rows.get(0), root) instanceof Number) {
            return rows.stream()
                .mapToDouble(row -> ((Number) arg.eval(row, root)).doubleValue())
                .average()
                .orElse(0.0);
        } else if (arg.eval(rows.get(0), root) instanceof Duration) {
            Duration total = rows.stream()
                .map(row -> (Duration) arg.eval(row, root))
                .reduce(Duration.ZERO, Duration::plus);
            return total.dividedBy(rows.size());
        } else {
            throw new IllegalArgumentException("AVG can only be applied to Number or Duration types");
        }
    } 

    private Object evalCount(List<EvalRow> rows) {
        return rows.size();
    }

    private Object evalMin(List<EvalRow> rows, AstNode root) {
        AstNode arg = arguments.get(0);
        if(rows.isEmpty()) {
            return 0.0;
        }
        Object type = arg.eval(rows.get(0), root);
        if (type instanceof Number) {
            return rows.stream()
                .mapToDouble(row -> ((Number) arg.eval(row, root)).doubleValue())
                .min()
                .orElse(Double.NaN);
        } else if (type instanceof Duration) {
            return rows.stream()
                .map(row -> (Duration) arg.eval(row, root))
                .min(Duration::compareTo)
                .orElse(Duration.ZERO);
        } else if(type instanceof Instant) {
            return rows.stream()
                .map(row -> (Instant) arg.eval(row, root))
                .min(Instant::compareTo)
                .orElse(Instant.ofEpochMilli(0));
        } else {
            throw new IllegalArgumentException("MIN can only be applied to Number or Duration types");
        }
    }

    private Object evalMax(List<EvalRow> rows, AstNode root) {
        AstNode arg = arguments.get(0);
        if(rows.isEmpty()) {
            return 0.0;
        }
        if (arg.eval(rows.get(0), root) instanceof Number) {
            return rows.stream()
                .mapToDouble(row -> ((Number) arg.eval(row, root)).doubleValue())
                .max()
                .orElse(Double.NaN);
        } else if (arg.eval(rows.get(0), root) instanceof Duration) {
            return rows.stream()
                .map(row -> (Duration) arg.eval(row, root))
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
        } else {
            throw new IllegalArgumentException("MAX can only be applied to Number or Duration types");
        }
    }

    private Object evalBeforeGC(EvalRow row, AstNode identifier, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        Object ts = identifier.eval(row, root);
        if (!(ts instanceof Instant)) {
            throw new IllegalArgumentException("Identifier must evaluate to Instant for BEFORE_GC function");
        }
        Instant timestamp = (Instant) ts;
        Object[] result = evaluator.evalGC(timestamp, root);
        return result[0];
    }

    private Object evalAfterGC(EvalRow row, AstNode identifier, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        Object ts = identifier.eval(row, root);
        if (!(ts instanceof Instant)) {
            throw new IllegalArgumentException("Identifier must evaluate to Instant for AFTER_GC function");
        }
        Instant timestamp = (Instant) ts;
        Object[] result = evaluator.evalGC(timestamp, root);
        return result[1];
    }

    private Object evalNearGC(EvalRow row, AstNode identifier, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if (row == null) {
            throw new IllegalArgumentException("EvalRow cannot be null");
        }
        Object ts = identifier.eval(row, root);
        if (!(ts instanceof Instant)) {
            throw new IllegalArgumentException("Identifier must evaluate to Instant for NEAR_GC function");
        }
        Instant timestamp = (Instant) ts;
        Object[] result = evaluator.evalGC(timestamp, root);
        return result[2];
    }

    public FunctionType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name + "(" + String.join(", ", arguments.stream().map(AstNode::getName).toArray(String[]::new)) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FunctionNode)) return false;
        FunctionNode that = (FunctionNode) obj;
        return this.getName().equals(that.getName());
    }
}
