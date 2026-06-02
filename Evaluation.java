// funkcja celu = dystans + kara za brak autobusu - atrakcyjnosc
public class Evaluation {

    public static double evaluate(int[] route, Instance instance, double penalty, double alpha) {
        double dist = 0;
        int bezBusu = 0;
        double attr = 0;

        for (int i=0; i<route.length-1; i++) {
            dist += instance.distance(route[i], route[i+1]);
            if(!instance.hasBus(route[i], route[i+1])) {
                bezBusu++;
            }
        }

        for (int i=0; i<route.length;i++) {
            attr += instance.attractions.get(route[i]).attractiveness;
        }

        return dist + penalty*bezBusu - alpha*attr;
    }

    // szczegoly do wypisania
    public static String breakdown(int[] route, Instance instance, double penalty, double alpha) {
        double dist = 0;
        int bezBusu = 0;
        double attr = 0;

        for (int i=0; i<route.length-1; i++) {
            dist += instance.distance(route[i], route[i+1]);
            if(!instance.hasBus(route[i], route[i+1])) bezBusu++;
        }
        for(int i=0; i<route.length; i++) {
            attr += instance.attractions.get(route[i]).attractiveness;
        }

        double total = dist + penalty*bezBusu - alpha*attr;
        return "dist=" + String.format("%.2f", dist)
            + " bezBusu=" + bezBusu
            + " attr=" + String.format("%.2f", attr)
            + " SUMA=" + String.format("%.4f", total);
    }
}
