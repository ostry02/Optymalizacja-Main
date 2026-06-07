import java.io.*;
import java.util.*;
import model.*;
import algorithms.*;
import experiments.*;

public class Main {

    public static void main(String[] args) throws IOException {
        // config
        Locale.setDefault(Locale.US);
        Config cfg = new Config("config.properties");
        new File("results").mkdirs();

        long seed = cfg.getSeed();
        double penalty = cfg.getPenalty();
        double alpha = cfg.getAlpha();

        System.out.println("Seed=" + seed + " Penalty=" + penalty + " Alpha=" + alpha);

        String[] files = cfg.getInstances();

        for (int f = 0; f < files.length; f++) {
            Instance inst = Instance.loadFromCSV(files[f]);

            // PSO
            PSO pso = new PSO(inst, penalty, alpha,
                    cfg.getPsoParticles(), cfg.getPsoIterations(),
                    cfg.getPsoInertia(), cfg.getPsoC1(), cfg.getPsoC2(),
                    new Random(seed + f));
            long t0 = System.currentTimeMillis();
            int[] psoBest = pso.run();
            long psoMs = System.currentTimeMillis() - t0;

            // ACO
            ACO aco = new ACO(inst, penalty, alpha,
                    cfg.getAcoAnts(), cfg.getAcoIterations(),
                    cfg.getAcoAlpha(), cfg.getAcoBeta(), cfg.getAcoEvaporation(), cfg.getAcoQ(),
                    new Random(seed + f + 100));
            t0 = System.currentTimeMillis();
            int[] acoBest = aco.run();
            long acoMs = System.currentTimeMillis() - t0;

            String outName = "results/results_n" + inst.n + ".csv";
            saveHistory(outName, pso.getHistory(), aco.getHistory());
            System.out.println("Saved: " + outName);

            Stats.save(inst, penalty, alpha,
                    psoBest, pso.getBestFitness(), psoMs,
                    acoBest, aco.getBestFitness(), acoMs);

            System.out.println();
        }


        // Taguchi
        // System.out.println("\n\n=== Taguchi L9 - parameter tuning ===");
        // long taguchiSeed = seed + 9000L;

        // for (int f = 0; f < files.length; f++) {
        //     Instance inst = Instance.loadFromCSV(files[f]);
        //     Taguchi.runPSO(inst, penalty, alpha, cfg.getTaguchiReplications(), taguchiSeed + f);
        //     Taguchi.runACO(inst, penalty, alpha, cfg.getTaguchiReplications(), taguchiSeed + f);
        // }

        // System.out.println("\n=== Taguchi done ===");
    }

    static void saveHistory(String fileName, double[] psoHist, double[] acoHist) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        pw.println("iteration,fitness_pso,fitness_aco");
        for (int i = 0; i < psoHist.length; i++)
            pw.println((i + 1) + "," + String.format("%.6f", psoHist[i])
                    + "," + String.format("%.6f", acoHist[i]));
        pw.close();
    }
}
