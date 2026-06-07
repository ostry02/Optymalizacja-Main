package experiments;

import java.io.*;
import model.*;

public class Stats {

    private Stats() {}

    public static void save(Instance instance, double penalty, double alpha,
                            int[] psoBest, double psoFitness, long psoMs,
                            int[] acoBest, double acoFitness, long acoMs) throws IOException {

        double[] psoB = breakdown(psoBest, instance, penalty, alpha);
        double[] acoB = breakdown(acoBest, instance, penalty, alpha);

        printSummary(instance.n, psoFitness, psoMs, psoB, acoFitness, acoMs, acoB);
        saveCsv(instance.n, psoFitness, psoMs, psoB, acoFitness, acoMs, acoB, penalty, alpha);
    }

    private static double[] breakdown(int[] route, Instance instance, double penalty, double alpha) {
        double dist = 0;
        int noBus = 0;
        double attr = 0;
        for (int i = 0; i < route.length - 1; i++) {
            dist += instance.distance(route[i], route[i + 1]);
            if (!instance.hasBus(route[i], route[i + 1])) noBus++;
        }
        for (int i = 0; i < route.length; i++)
            attr += instance.attractions.get(route[i]).attractiveness;
        double total = dist + penalty * noBus - alpha * attr;
        return new double[]{dist, noBus, penalty * noBus, attr, alpha * attr, total};
    }

    private static void printSummary(int n,
                                     double psoFit, long psoMs, double[] p,
                                     double acoFit, long acoMs, double[] a) {
        System.out.println("\n--- Stats n=" + n + " ---");
        System.out.printf("%-4s  %10s  %8s  %8s  %5s  %10s  %8s  %10s  %10s%n",
                "algo", "fitness", "time_ms", "dist", "noBus", "penCost", "attr", "attrScore", "total");
        System.out.printf("%-4s  %10.4f  %8d  %8.2f  %5.0f  %10.2f  %8.2f  %10.2f  %10.4f%n",
                "PSO", psoFit, psoMs, p[0], p[1], p[2], p[3], p[4], p[5]);
        System.out.printf("%-4s  %10.4f  %8d  %8.2f  %5.0f  %10.2f  %8.2f  %10.2f  %10.4f%n",
                "ACO", acoFit, acoMs, a[0], a[1], a[2], a[3], a[4], a[5]);
    }

    private static void saveCsv(int n,
                                 double psoFit, long psoMs, double[] p,
                                 double acoFit, long acoMs, double[] a,
                                 double penalty, double alpha) throws IOException {
        String filename = "results/stats_n" + n + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("algo,fitness,time_ms,dist,noBus,penalty_cost,attr,attr_score,total");
            pw.printf("PSO,%.6f,%d,%.4f,%.0f,%.4f,%.4f,%.4f,%.6f%n",
                    psoFit, psoMs, p[0], p[1], p[2], p[3], p[4], p[5]);
            pw.printf("ACO,%.6f,%d,%.4f,%.0f,%.4f,%.4f,%.4f,%.6f%n",
                    acoFit, acoMs, a[0], a[1], a[2], a[3], a[4], a[5]);
            pw.println();
            pw.printf("# penalty=%.4f  alpha=%.4f%n", penalty, alpha);
        }
        System.out.println("Saved: " + filename);
    }
}
