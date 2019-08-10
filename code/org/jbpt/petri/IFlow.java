package org.jbpt.petri;

import org.jbpt.graph.abs.IDirectedEdge;

/**
 * Interface to a flow relation of a Petri net.
 *
 * @param <N> Node template.
 * @author Artem Polyvyanyy
 */
public interface IFlow<N extends INode> extends IDirectedEdge<N> {
}