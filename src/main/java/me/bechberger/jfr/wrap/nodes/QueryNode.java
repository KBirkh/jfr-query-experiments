package me.bechberger.jfr.wrap.nodes;

import me.bechberger.jfr.wrap.EvalRow;
import me.bechberger.jfr.wrap.EvalState;
import me.bechberger.jfr.wrap.Evaluator;

/*
 * Represents a query in the AST
 * Contains all the clauses in a query
 */
public class QueryNode extends AstNode {
    private SelectNode select;
    private FromNode from;
    private WhereNode where;
    private GroupByNode groupBy;
    private HavingNode having;
    private OrderByNode orderBy;
    private LimitNode limit;
    public boolean hasAt;

    public QueryNode() {

    }

    public QueryNode(boolean hasAt) {
        this.hasAt = hasAt;
    }

    public void setSelect(AstNode select) {
        if (this.select == null) {
            this.select = (SelectNode) select;
        } else {
            throw new IllegalStateException("QueryNode can only have one SelectNode");
        }
    }

    public void setFrom(FromNode from) {
        if (this.from == null) {
            this.from = from;
        } else {
            throw new IllegalStateException("QueryNode can only have one FromNode");
        }
    }

    public void setWhere(WhereNode where) {
        if (this.where == null) {
            this.where = where;
        } else {
            throw new IllegalStateException("QueryNode can only have one WhereNode");
        }
    }

    public void setGroupBy(GroupByNode groupBy) {
        if (this.groupBy == null) {
            this.groupBy = groupBy;
        } else {
            throw new IllegalStateException("QueryNode can only have one GroupByNode");
        }
    }

    public void setHaving(HavingNode having) {
        if (this.having == null) {
            this.having = having;
        } else {
            throw new IllegalStateException("QueryNode can only have one HavingNode");
        }
    }

    public void setOrderBy(OrderByNode orderBy) {
        if (this.orderBy == null) {
            this.orderBy = orderBy;
        } else {
            throw new IllegalStateException("QueryNode can only have one OrderByNode");
        }
    }

    public void setLimit(LimitNode limit) {
        if (this.limit == null) {
            this.limit = limit;
        } else {
            throw new IllegalStateException("QueryNode can only have one LimitNode");
        }
    }

    public AstNode getHaving() {
        return having;
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String dent = "  ".repeat(indent);
        sb.append("\n").append(dent).append(this.getClass().getSimpleName()).append(":");
        sb.append("\n").append(dent).append("  Has @: ").append(hasAt);
        if (select != null) {
            sb.append(select.toString(indent + 1));
        }
        if (from != null) {
            sb.append(from.toString(indent + 1));
        }
        if (where != null) {
            sb.append(where.toString(indent + 1));
        }
        if (groupBy != null) {
            sb.append(groupBy.toString(indent + 1));
        }
        if (having != null) {
            sb.append(having.toString(indent + 1));
        }
        if (orderBy != null) {
            sb.append(orderBy.toString(indent + 1));
        }
        if (limit != null) {
            sb.append(limit.toString(indent + 1));
        }
        return sb.toString();
    }
    
    /*
     * Contains the "query execution plan"
     * if a clause does not exist skip its evaluation
     * Plan:
     * 1. Evaluate FromNode
     * 2. Evaluate the nonAggregates (GC correlations)
     * 3. Evaluate WhereNode
     * 4. find the aggregates
     * 5. Group the table
     * 6. Evaluate the HavingNode
     * 7. Evaluate Ordering
     * 8. Evaluate Limit
     * 9. Perform projection
     * 10. set isEvaluated flag to true
     */
    @Override
    public Object eval(AstNode root) {
        if(isEvaluated) {
            return null;
        }
        Evaluator evaluator = Evaluator.getInstance();
        evaluator.state = EvalState.FROM;
        if(from.eval(root).equals(-1)) {
            return null;
        }
        evaluator.state = EvalState.WHERE;
        select.evalNonAggregates(root);
        if(where != null) {
            where.evalNonAggregates(root);
            where.eval(root);
        }
        evaluator.state = EvalState.GROUP_BY;
        select.findAggregates(root);
        if(groupBy != null) {
            ((GroupByNode) groupBy).eval(new EvalRow(), root);
        }
        evaluator.group(root);
        if(having != null) {
            evaluator.state = EvalState.HAVING;
            ((HavingNode) having).eval(root);
        }
        if(orderBy != null) {
            evaluator.state = EvalState.ORDER_BY;
            orderBy.eval(root);
        }
        if(limit != null) {
            evaluator.state = EvalState.LIMIT;
            limit.eval(root);
        }
        evaluator.state = EvalState.SELECT;
        select.eval(root);

        this.isEvaluated = true;
        return null;
    }
    

}
