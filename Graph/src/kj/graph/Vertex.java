package kj.graph;

import java.util.Set;

/**
 * This class represents one vertex of a graph.
 * It is a general interface configurable with
 * the special class of Vertex and Edge for 
 * method return values.
 * @author jakob190590
 *
 * @param <V> special class of Vertex
 * @param <E> special class of Edge
 */
public interface Vertex<V extends Vertex<?, ?>, E extends Edge<?>> {
	
	/**
	 * Returns a collection of the edges from this vertex.
	 * Changes in the result will change the graph!
	 * @return the edges
	 */
	Set<E> getEdges();
	
	/**
	 * Computes the direct neighbors of the vertex.
	 * Changes in the result have no effect on the graph.
	 * @return the direct neighbors
	 */
	Set<V> getNeighbors();
		
}
