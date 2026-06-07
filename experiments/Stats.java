package experiments;

import java.io.*;
import java.util.*;
import model.*;
import algorithms.*;

public class Stats {

    private Stats() {}

    public static void run(Instance instance, double penalty, double alpha,
                           Config cfg, int nRuns, long baseSeed) throws IOException {

        double[] psoResults = new double[nRuns];
        double[] acoResults = new double[nRuns];

        for (int r = 0; r < nRuns; r++) {
            PSO pso = new PSO(instance, penalty, alpha,
                    cfg.getPsoParticles(), cfg.getPsoIterations(),
                    cfg.getPsoInertia(), cfg.getPsoC1(), cfg.getPsoC2(),
                    new Random(baseSeed + r));
            pso.run();
            psoResults[r] = pso.getBestFitness();

            ACO aco = new ACO(instance, penalty, alpha,
                    cfg.getAcoAnts(), cfg.getAcoIterations(),
                    cfg.getAcoAlpha(), cfg.getAcoBeta(), cfg.getAcoEvaporation(), cfg.getAcoQ(),
                    new Random(baseSeed + r + 10000));
            aco.run();
            acoResults[r] = aco.getBestFitness();
        }

        printSummary(instance.n, nRuns, psoResults, acoResults);
        saveCsv(instance.n, psoResults, acoResults);
    }

    private static void printSummary(int n, int nRuns, double[] pso, double[] aco) {
        System.out.println("\n--- Stats n=" + n + " (" + nRuns + " runs) ---");
        System.out.printf("%-6s  %8s  %8s  %8s  %8s%n", "", "mean", "std", "min", "max");
        System.out.printf("%-6s  %8.4f  %8.4f  %8.4f  %8.4f%n",
                "PSO", mean(pso), std(pso), min(pso), max(pso));
        System.out.printf("%-6s  %8.4f  %8.4f  %8.4f  %8.4f%n",
                "ACO", mean(aco), std(aco), min(aco), max(aco));
    }

    private static void saveCsv(int n, double[] pso, double[] aco) throws IOException {
        String filename = "results/stats_n" + n + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("run,fitness_pso,fitness_aco");
            for (int i = 0; i < pso.length; i++)
                pw.printf("%d,%.6f,%.6f%n", i + 1, pso[i], aco[i]);

            pw.println();
            pw.printf("mean,%.6f,%.6f%n", mean(pso), mean(aco));
            pw.printf("std,%.6f,%.6f%n", std(pso), std(aco));
            pw.printf("min,%.6f,%.6f%n", min(pso), min(aco));
            pw.printf("max,%.6f,%.6f%n", max(pso), max(aco));
        }
        System.out.println("Saved: " + filename);
    }

    private static double mean(double[] v) {
        double s = 0;
        for (double x : v) s += x;
        return s / v.length;
    }

    private static double std(double[] v) {
        double m = mean(v);
        double s = 0;
        for (double x : v) s += (x - m) * (x - m);
        return Math.sqrt(s / v.length);
    }

    private static double min(double[] v) {
        double m = v[0];
        for (double x : v) if (x < m) m = x;
        return m;
    }

    private static double max(double[] v) {
        double m = v[0];
        for (double x : v) if (x > m) m = x;
        return m;
    }
}
