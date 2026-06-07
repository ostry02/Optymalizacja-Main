package experiments;

import java.io.*;
import model.*;

public class Stats {

    private Stats() {}

    public static void save(Instance instance, double penalty, double alpha,
                            Evaluation.EvalResult psoResult, long psoMs,
                            Evaluation.EvalResult acoResult, long acoMs) throws IOException {
        saveCsv(instance.n, psoMs, psoResult, acoMs, acoResult, penalty, alpha);
    }

    private static void saveCsv(int n,
                                 long psoMs, Evaluation.EvalResult p,
                                 long acoMs, Evaluation.EvalResult a,
                                 double penalty, double alpha) throws IOException {
        String filename = "results/stats_n" + n + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("algo,fitness,time_ms,dist,noBus,penalty_cost,attr,attr_score,total");
            pw.printf("PSO,%.6f,%d,%.4f,%d,%.4f,%.4f,%.4f,%.6f%n",
                    p.total(), psoMs, p.dist(), p.noBus(), p.penCost(), p.attr(), p.attrScore(), p.total());
            pw.printf("ACO,%.6f,%d,%.4f,%d,%.4f,%.4f,%.4f,%.6f%n",
                    a.total(), acoMs, a.dist(), a.noBus(), a.penCost(), a.attr(), a.attrScore(), a.total());
            pw.println();
            pw.printf("# penalty=%.4f  alpha=%.4f%n", penalty, alpha);
        }
        System.out.println("Saved: " + filename);
    }
}
