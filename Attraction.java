public class Attraction {
    int id;
    String name;
    double x;
    double y;
    double attractiveness;

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
