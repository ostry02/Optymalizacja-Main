package model;

public class Evaluation {

    public record EvalResult(double dist, int noBus, double penCost, double attr, double attrScore, double total) {}

    public static EvalResult evaluate(int[] route, Instance instance, double penalty, double alpha) {
        return evaluate(route, instance, penalty, alpha, route.length);
    }

    // wersja liczaca fitness tylko dla pierwszych len atrakcji trasy
    public static EvalResult evaluate(int[] route, Instance instance, double penalty, double alpha, int len) {
        double dist = 0;
        int noBus = 0;
        double attr = 0;

        for (int i = 0; i < len - 1; i++) {
            dist += instance.distance(route[i], route[i + 1]);
            if (!instance.hasBus(route[i], route[i + 1])) noBus++;
        }
        for (int i = 0; i < len; i++)
            attr += instance.attractions.get(route[i]).attractiveness;

        double penCost = penalty * noBus;
        double attrScore = alpha * attr;
        double total = dist + penCost - attrScore;
        return new EvalResult(dist, noBus, penCost, attr, attrScore, total);
    }
}
