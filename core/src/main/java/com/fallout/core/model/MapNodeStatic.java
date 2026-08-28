package com.fallout.core.model;

import com.fallout.core.enums.MapPointsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Static metadata of a {@link MapPoint} that the engine keeps in
 * Redis hash {@code game:{sessionId}:static:nodes}.
 *
 * <p>Runtime owner / garrison / fortification live elsewhere (Redis
 * {@code game:{sessionId}:node:{nodeId}}) and are intentionally not part
 * of this view.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MapNodeStatic {

    private UUID nodeId;
    private String name;
    private double x;
    private double y;
    private MapPointsType type;
    private int maxGarrison;
    private int baseFortification;
}
