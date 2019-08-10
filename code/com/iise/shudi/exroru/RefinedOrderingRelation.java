package com.iise.shudi.exroru;

public class RefinedOrderingRelation {

    public static double SDA_WEIGHT = 0.0;
    public static boolean IMPORTANCE = true;

    private Relation relation;
    private boolean adjacency;
    private double importance;

    public RefinedOrderingRelation(Relation r, boolean a, double i) {
        relation = r;
        adjacency = a;
        importance = i;
    }

    public double intersectionWithoutNever(RefinedOrderingRelation r) {
        if (relation == Relation.NEVER && r.relation == Relation.NEVER) {
            return 0;
        } else if (relation != r.relation) {
            return 0;
        } else if (adjacency != r.adjacency) {
            return SDA_WEIGHT * (IMPORTANCE ? Math.min(importance, r.importance) : 1);
        } else {
            return (IMPORTANCE ? Math.min(importance, r.importance) : 1);
        }
    }

    public double intersectionWithNever(RefinedOrderingRelation r) {
        if (relation != r.relation) {
            return 0;
        } else if (adjacency != r.adjacency) {
            return SDA_WEIGHT * (IMPORTANCE ? Math.min(importance, r.importance) : 1);
        } else {
            return (IMPORTANCE ? Math.min(importance, r.importance) : 1);
        }
    }

    public double unionWithoutNever(RefinedOrderingRelation r) {
        if (relation == Relation.NEVER && r.relation == Relation.NEVER) {
            return 0;
        } else if (relation == Relation.NEVER) {
            return (IMPORTANCE ? r.importance : 1);
        } else if (r.relation == Relation.NEVER) {
            return (IMPORTANCE ? r.importance : 1);
        } else {
            return (IMPORTANCE ? Math.max(importance, r.importance) : 1);
        }
    }

    public double unionWithNever(RefinedOrderingRelation r) {
        return (IMPORTANCE ? Math.max(importance, r.importance) : 1);
    }

    public boolean equals(Object o) {
        if (o instanceof RefinedOrderingRelation) {
            RefinedOrderingRelation r = (RefinedOrderingRelation) o;
            if (r.relation == relation && r.adjacency == adjacency) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        String s;
        switch (relation) {
            case ALWAYS:
                s = "A";
                break;
            case SOMETIMES:
                s = "S";
                break;
            case NEVER:
                s = "N";
                break;
            default:
                s = "U";
                break;
        }
        return s;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public boolean isAdjacency() {
        return adjacency;
    }

    public void setAdjacency(boolean adjacency) {
        this.adjacency = adjacency;
    }

    public double getImportance() {
        return (IMPORTANCE ? importance : 1);
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

}
