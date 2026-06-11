import java.io.*;
import java.util.*;
import experiments.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Config cfg = new Config("config.properties");
        new File("results").mkdirs();

        long seed = cfg.getSeed();
        double penalty = cfg.getPenalty();
        double alpha = cfg.getAlpha();

        long taguchiSeed = seed + 9000L;
        String[] datasets = cfg.getDatasets();

        for (int f = 0; f < datasets.length; f++) {

            Explorer.Selection sel = Explorer.load(cfg, datasets[f]);

            System.out.println("Start taguchi: " + sel.label());

            Taguchi.Result pso = Taguchi.runPSO(sel.pool(), sel.count(), penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f, sel.label());
            Taguchi.Result aco = Taguchi.runACO(sel.pool(), sel.count(), penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f, sel.label());

            saveHistory("results/results_" + sel.label() + ".csv", pso.history(), aco.history());
            Stats.save(sel.pool(), sel.label(), penalty, alpha, pso, aco);
        }

        System.out.println("Wyniki zapisano w results");
    }

    static void saveHistory(String fileName, double[] psoHist, double[] acoHist) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("iteration,fitness_pso,fitness_aco");
            for (int i = 0; i < psoHist.length; i++)
                pw.println((i + 1) + "," + String.format("%.6f", psoHist[i])
                        + "," + String.format("%.6f", acoHist[i]));
        }
    }
}
