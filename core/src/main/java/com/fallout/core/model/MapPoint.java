package com.fallout.core.model;

import com.fallout.core.enums.MapPointsType;

// Точка на карте
public class MapPoint {

    private String id;
    private String name;
    private double x;
    private double y;
    private MapPointsType type;

    public MapPoint(String id, String name, double x, double y, MapPointsType type) {
        setId(id);
        setName(name);
        setX(x);
        setY(y);
        setType(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Id parameter cannot be null or blank");
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
