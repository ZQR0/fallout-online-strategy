package com.fallout.core.model;

import com.fallout.core.enums.MapPointsType;

import java.util.UUID;

// Точка на карте
public class MapPoint {

    private UUID id;
    private String name;
    private double x;
    private double y;
    private MapPointsType type;

    public MapPoint(UUID id, String name, double x, double y, MapPointsType type) {
        setId(id);
        setName(name);
        setX(x);
        setY(y);
        setType(type);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        if (id == null) throw new IllegalArgumentException("Id parameter cannot be null");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name parameter cannot be null or blank");
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public MapPointsType getType() {
        return type;
    }

    public void setType(MapPointsType type) {
        this.type = type;
    }
}
