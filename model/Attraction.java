package model;

public class Attraction {
    public int id;
    public String name;
    public double x;
    public double y;
    public double attractiveness;

    public Attraction(int id, String name, double x, double y, double attractiveness) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.attractiveness = attractiveness;
    }

    public String toString() {
        return name + "(" + x + "," + y + ")";
    }
}
