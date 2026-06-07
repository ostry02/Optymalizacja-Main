package model;

public class Evaluation {

    public static double evaluate(int[] route, Instance instance, double penalty, double alpha) {
        double dist = 0;
        int noBus = 0;
        double attr = 0;

        for (int i=0; i<route.length-1; i++) {
            dist += instance.distance(route[i], route[i+1]);
            if(!instance.hasBus(route[i], route[i+1])) {
                noBus++;
            }
        }

        for (int i=0; i<route.length;i++) {
            attr += instance.attractions.get(route[i]).attractiveness;
        }

        return dist + penalty*noBus - alpha*attr;
    }

}
