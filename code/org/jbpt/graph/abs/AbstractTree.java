package org.jbpt.graph.abs;

import org.jbpt.hypergraph.abs.IVertex;
import org.jbpt.utils.DotSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Abstract tree implementation.<br/><br/>
 * <p>
 * Implemented as a directed graph.
 * For every edge of the graph source vertex represents parent vertex and target vertex represents child vertex.
 *
 * @param <V> Vertex template.
 * @author Artem Polyvyanyy
 */
public class AbstractTree<V extends IVertex> extends AbstractDirectedGraph<IDirectedEdge<V>, V> implements ITree<V> {
    protected V root = null;

    /**
     * Empty constructor - for technical purposes.
     */
    protected AbstractTree() {
    }

    /**
     * Constructor of the abstract tree.
     *
     * @param root Vertex to use as root of this tree.
     */
    public AbstractTree(V root) {
        this.root = root;
    }

    @Override
    public V getRoot() {
        return this.root;
    }

    @Override
    public V reRoot(V v) {
        if (v == null || !this.getVertices().contains(v)) return this.root;
        if (v.equals(this.root)) return this.root;
        this.root = v;

        Queue<V> queue = new ConcurrentLinkedQueue<V>();
        queue.add(this.root);
        Set<V> visited = new HashSet<V>();
        visited.add(this.root);

        while (!queue.isEmpty()) {
            V c = queue.poll();
            Collection<V> adjVs = this.getAdjacent(c);
            adjVs.removeAll(visited);

            for (V a : adjVs) {
                super.removeEdges(super.getEdges(c, a));
                super.removeEdges(super.getEdges(a, c));

                super.addEdge(c, a);
                visited.add(a);
                queue.add(a);
            }
        }

        return this.root;
    }

    @Override
    public Set<V> getChildren(V v) {
        // TODO: super.getDirectSuccessors(v) must return a set of vertices
        return new HashSet<V>(super.getDirectSuccessors(v));
    }

    @Override
    public Set<V> getChildrenRecursively(V v) {
        Set<V> result = new HashSet<V>();
        for (V s : super.getDirectSuccessors(v))
            result.addAll(getChildrenRecursively(s));
        return result;
    }

    @Override
    public V getParent(V v) {
        return super.getFirstDirectPredecessor(v);
    }

    @Override
    public V addChild(V p, V c) {
        if (!super.getVertices().contains(p)) return null;
        IDirectedEdge<V> e = super.addEdge(p, c);

        return (e == null) ? null : c;
    }

    @Override
    public boolean isRoot(V v) {
        if (this.root == null) return false;
        return this.root.equals(v);
    }

    @Override
    public String toDOT() {
        return new DotSerializer().serialize(this, false);
    }

    @Override
    public V getLCA(V v1, V v2) {
        if (v1 == null || v2 == null) return null;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return null;

        if (v1.equals(v2)) return v1;

        List<V> path1 = this.getDownwardPath(this.getRoot(), v1);
        List<V> path2 = this.getDownwardPath(this.getRoot(), v2);

        V result = null;
        for (int i = 0; i < path1.size(); i++) {
            if (i >= path2.size()) break;
            if (path1.get(i).equals(path2.get(i))) result = path1.get(i);
            else break;
        }

        return result;
    }

    @Override
    public V getLCA(Collection<V> vertices) {
        if (vertices.isEmpty()) return null;
        if (!this.getVertices().containsAll(vertices)) return null;

        Set<List<V>> paths = new HashSet<List<V>>();
        int shortest = Integer.MAX_VALUE;
        for (V v : vertices) {
            List<V> path = this.getDownwardPath(this.getRoot(), v);
            paths.add(path);
            shortest = Math.min(shortest, path.size());
        }

        V result = this.getRoot();
        for (int i = 0; i < shortest; i++) {
            Set<V> currentLevel = new HashSet<V>();
            for (List<V> path : paths)
                currentLevel.add(path.get(i));

            if (currentLevel.size() <= 1)
                result = paths.iterator().next().get(i);
            else
                break;
        }
        return result;
    }


    @Override
    public boolean isChild(V v1, V v2) {
        if (v1 == null || v2 == null) return false;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return false;

        if (v1.equals(v2)) return false;

        return this.getDirectSuccessors(v2).contains(v1);
    }

    @Override
    public boolean isParent(V v1, V v2) {
        if (v1 == null || v2 == null) return false;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return false;

        if (v1.equals(v2)) return false;

        return this.getDirectPredecessors(v2).contains(v1);
    }

    @Override
    public boolean isDescendant(V v1, V v2) {
        if (v1 == null || v2 == null) return false;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return false;

        if (v1.equals(v2)) return false;
        return this.getDownwardPath(this.getRoot(), v1).contains(v2);
    }

    @Override
    public boolean isAncestor(V v1, V v2) {
        if (v1 == null || v2 == null) return false;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return false;

        if (v1.equals(v2)) return false;
        return this.getDownwardPath(this.getRoot(), v2).contains(v1);
    }

    @Override
    public List<V> getDownwardPath(V v1, V v2) {
        List<V> result = new ArrayList<V>();

        if (v1 == null || v2 == null) return result;
        if (!this.getVertices().contains(v1) ||
                !this.getVertices().contains(v2)) return result;

        V v = v2;
        result.add(v);
        while (!this.getDirectPredecessors(v).isEmpty() && !result.contains(v1)) {
            v = this.getFirstDirectPredecessor(v);
            result.add(v);
        }

        if (!result.contains(v1)) return new ArrayList<V>();
        Collections.reverse(result);
        return result;
    }
}
