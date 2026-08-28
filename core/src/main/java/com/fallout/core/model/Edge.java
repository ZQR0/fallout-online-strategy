package com.fallout.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Static edge between two {@link MapPoint}s on the world map.
 *
 * <p>The map graph is undirected: an edge (A,B) is equivalent to (B,A).
 * {@code MapPoint} ids are UUIDs (see {@code MapPoint#id}). The runtime
 * adjacency marker in Redis {@code game:{sessionId}:edges} is a set of
 * strings formatted as {@code "nodeA:nodeB"}; this POJO mirrors the
 * {@code map_edge} row shape (see resilient-squishing-newt.md).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Edge {

    /** First endpoint of the edge. */
    private UUID sourceNodeId;

    /** Second endpoint of the edge. Must differ from {@link #sourceNodeId}. */
    private UUID targetNodeId;

    /** Geometric length of the edge (used for movement cost calculations). */
    private double length;

    /** Whether the edge can be traversed in both directions. */
    private boolean bidirectional;

    /**
     * Multiplier applied to travel time / cost on this edge.
     * {@code 1.0} means "no modifier"; values &gt; 1 slow the traveller down.
     */
    private double travelModifier;
}
