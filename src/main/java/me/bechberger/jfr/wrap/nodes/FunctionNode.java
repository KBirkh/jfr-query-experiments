package me.bechberger.jfr.wrap.nodes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.EvalState;
import me.bechberger.jfr.wrap.Evaluator;


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

    private void setType() {
        switch(name) {
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
            case "p50":
                this.type = FunctionType.P50;
                break;
            case "p90":
                this.type = FunctionType.P90;
                break;
            case "p95":
                this.type = FunctionType.P95;
                break;
            case "p99":
                this.type = FunctionType.P99;
                break;
            case "p999":
                this.type = FunctionType.P999;
                break;
            case "beforeGC":
                this.type = FunctionType.BEFORE_GC;
                break;
            case "afterGC":
                this.type = FunctionType.AFTER_GC;
                break;
            case "nearGc":
                this.type = FunctionType.NEAR_GC;
                break;
            default:
                throw new IllegalArgumentException("Unknown function type: " + name);

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

    @Override
    public Object eval(Object obj, AstNode root) {
        Evaluator evaluator = Evaluator.getInstance();
        if(evaluator.state == EvalState.GROUP_BY) {
            return evalGroup(obj, root);
        } else if(evaluator.state == EvalState.WHERE) {
            return evalWhere((EvalRow) obj, root);
        } else {
            return evalHaving((EvalRow) obj, root);
        }
    }

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
                return evalBeforeGC(List.of(row));
            case AFTER_GC:
                return evalAfterGC(List.of(row));
            case NEAR_GC:
                return evalNearGC(List.of(row));
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
            case AFTER_GC:
            case NEAR_GC:
                // Placeholder for GC related logic
                return null;
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
        if (arg.eval(rows.get(0), root) instanceof Number) {
            return rows.stream()
                .mapToDouble(row -> ((Number) arg.eval(row, root)).doubleValue())
                .min()
                .orElse(Double.NaN);
        } else if (arg.eval(rows.get(0), root) instanceof Duration) {
            return rows.stream()
                .map(row -> (Duration) arg.eval(row, root))
                .min(Duration::compareTo)
                .orElse(Duration.ZERO);
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

    private Object evalBeforeGC(List<EvalRow> rows) {
        // Placeholder for before GC logic
        return null;
    }

    private Object evalAfterGC(List<EvalRow> rows) {
        // Placeholder for after GC logic
        return null;
    }

    private Object evalNearGC(List<EvalRow> rows) {
        // Placeholder for near GC logic
        return null;
    }

    public FunctionType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name + "(" + String.join(", ", arguments.stream().map(AstNode::getName).toArray(String[]::new)) + ")";
    }
}
