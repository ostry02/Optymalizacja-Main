package experiments;

import java.io.*;
import java.util.*;
import model.*;

public class Stats {

    // zagregowane wyniki z wielu przebiegow: mean/std/min/max fitnessu
    // oraz usrednione skladowe i historia zbieznosci
    public record Agg(String algo, int runs, double mean, double std, double min, double max,
                      double meanDist, double meanNoBus, double meanPenalty, double meanAttr,
                      long meanTimeMs, double[] meanHistory, double[] params, String[] factors) {}

    public static Agg aggregate(String algo, List<Taguchi.Result> runs) {
        int n = runs.size();
        double[] fits = new double[n];
        double sumDist = 0, sumNoBus = 0, sumPen = 0, sumAttr = 0;
        long sumTime = 0;
        int h = runs.get(0).history().length;
        double[] meanHist = new double[h];

        for (int i = 0; i < n; i++) {
            Taguchi.Result r = runs.get(i);
            Evaluation.EvalResult e = r.eval();
            fits[i] = e.total();
            sumDist += e.dist();
            sumNoBus += e.noBus();
            sumPen += e.penCost();
            sumAttr += e.attr();
            sumTime += r.timeMs();
            double[] hist = r.history();
            for (int t = 0; t < h; t++) meanHist[t] += hist[t] / n;
        }

        double mean = 0;
        for (double f : fits) mean += f;
        mean /= n;
        double var = 0;
        for (double f : fits) var += (f - mean) * (f - mean);
        double std = Math.sqrt(var / n);
        double min = fits[0], max = fits[0];
        for (double f : fits) { if (f < min) min = f; if (f > max) max = f; }

        Taguchi.Result first = runs.get(0);
        return new Agg(algo, n, mean, std, min, max,
                sumDist / n, sumNoBus / n, sumPen / n, sumAttr / n,
                sumTime / n, meanHist, first.params(), first.factors());
    }

    private static String paramStr(Agg a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.factors().length; i++) {
            if (i > 0) sb.append("; ");
            sb.append(a.factors()[i]).append("=").append(String.format("%.4f", a.params()[i]));
        }
        return sb.toString();
    }

    private static void writeRows(String filename, double penalty, double alpha, List<Agg> aggs) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("algo,runs,mean,std,min,max,mean_dist,mean_noBus,mean_penalty,mean_attr,mean_time_ms,best_params");
            for (Agg a : aggs) {
                pw.printf("%s,%d,%.6f,%.6f,%.6f,%.6f,%.4f,%.4f,%.4f,%.4f,%d,%s%n",
                        a.algo(), a.runs(), a.mean(), a.std(), a.min(), a.max(),
                        a.meanDist(), a.meanNoBus(), a.meanPenalty(), a.meanAttr(),
                        a.meanTimeMs(), paramStr(a));
            }
            pw.println();
            pw.printf("# penalty=%.4f  alpha=%.4f%n", penalty, alpha);
        }
    }

    public static void save(String label, double penalty, double alpha, Agg pso, Agg aco) throws IOException {
        writeRows("results/stats_" + label + ".csv", penalty, alpha, List.of(pso, aco));
    }

    public static void saveVariants(String label, double penalty, double alpha, List<Agg> aggs) throws IOException {
        writeRows("results/stats_variants_" + label + ".csv", penalty, alpha, aggs);
    }
}
