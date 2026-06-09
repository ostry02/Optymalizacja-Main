import java.io.*;
import java.util.*;
import model.*;
import experiments.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Config cfg = new Config("config.properties");
        new File("results").mkdirs();

        long seed = cfg.getSeed();
        double penalty = cfg.getPenalty();
        double alpha = cfg.getAlpha();

        Explorer.run(cfg, penalty, alpha, seed);
        System.out.println("Start taguchi");
        runBatch(cfg, penalty, alpha, seed);
    }

    static void runBatch(Config cfg, double penalty, double alpha, long seed) throws IOException {
        String[] files = cfg.getInstances();
        long taguchiSeed = seed + 9000L;

        for (int f = 0; f < files.length; f++) {
            Instance inst = Instance.loadFromCSV(files[f]);

            Taguchi.Result pso = Taguchi.runPSO(inst, penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f);
            Taguchi.Result aco = Taguchi.runACO(inst, penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f);

            saveHistory("results/results_n" + inst.n + ".csv", pso.history(), aco.history());
            Stats.save(inst, penalty, alpha, pso, aco);
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
