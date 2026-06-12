package experiments;

import java.io.*;
import model.*;

public class Stats {

    public static void saveVariants(Instance instance, String label, double penalty, double alpha,
                                    Taguchi.Result psoBase, Taguchi.Result psoRepair,
                                    Taguchi.Result psoAdaptive, Taguchi.Result psoBoth,
                                    Taguchi.Result aco) throws IOException {

        String filename = "results/stats_variants_" + label + ".csv";
        PrintWriter pw = new PrintWriter(new FileWriter(filename));
        pw.println("algo,fitness,time_ms,dist,noBus,penalty_cost,attr,attr_score,total,best_params");

        Taguchi.Result[] wyniki = {psoBase, psoRepair, psoAdaptive, psoBoth, aco};
        for (Taguchi.Result res : wyniki) {
            Evaluation.EvalResult e = res.eval();
            String params = "";
            for (int i = 0; i < res.factors().length; i++) {
                if (i > 0) params += "; ";
                params += res.factors()[i] + "=" + String.format("%.4f", res.params()[i]);
            }
            pw.printf("%s,%.6f,%d,%.4f,%d,%.4f,%.4f,%.4f,%.6f,%s%n",
                    res.algo(), e.total(), res.timeMs(), e.dist(), e.noBus(),
                    e.penCost(), e.attr(), e.attrScore(), e.total(), params);
        }

        pw.println();
        pw.printf("# penalty=%.4f  alpha=%.4f%n", penalty, alpha);
        pw.close();
    }

    public static void save(Instance instance, String label, double penalty, double alpha,
                            Taguchi.Result pso, Taguchi.Result aco) throws IOException {

        String filename = "results/stats_" + label + ".csv";
        PrintWriter pw = new PrintWriter(new FileWriter(filename));

        pw.println("algo,fitness,time_ms,dist,noBus,penalty_cost,attr,attr_score,total,best_params");

        Taguchi.Result[] wyniki = {pso, aco};
        for (Taguchi.Result res : wyniki) {
            Evaluation.EvalResult e = res.eval();

            String params = "";
            for (int i = 0; i < res.factors().length; i++) {
                if (i > 0) params += "; ";
                params += res.factors()[i] + "=" + String.format("%.4f", res.params()[i]);
            }

            pw.printf("%s,%.6f,%d,%.4f,%d,%.4f,%.4f,%.4f,%.6f,%s%n",
                    res.algo(), e.total(), res.timeMs(), e.dist(), e.noBus(),
                    e.penCost(), e.attr(), e.attrScore(), e.total(), params);
        }

        pw.println();
        pw.printf("# penalty=%.4f  alpha=%.4f%n", penalty, alpha);
        pw.close();
    }
}
