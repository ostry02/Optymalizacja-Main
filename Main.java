import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);

        Config cfg = new Config("config.properties");

        long seed = cfg.getSeed();
        double penalty = cfg.getPenalty();
        double alpha = cfg.getAlpha();

        System.out.println("=== TSP - Atrakcje Wroclawia ===");
        System.out.println("Seed=" + seed + "  Penalty=" + penalty + "  Alpha=" + alpha);
        System.out.println();

        String[] files = cfg.getInstances();

        for (int f = 0; f < files.length; f++) {
            System.out.println("--- " + files[f] + " ---");

            Instance inst = Instance.loadFromCSV(files[f]);
            System.out.println(inst);

            // PSO
            PSO pso = new PSO(inst, penalty, alpha,
                    cfg.getPsoParticles(), cfg.getPsoIterations(),
                    cfg.getPsoInertia(), cfg.getPsoC1(), cfg.getPsoC2(),
                    new Random(seed + f));
            long t0 = System.currentTimeMillis();
            int[] psoBest = pso.run();
            long psoMs = System.currentTimeMillis() - t0;

            System.out.printf("[PSO] fitness=%.4f  time=%d ms%n", pso.getBestFitness(), psoMs);
            System.out.println("[PSO] " + Evaluation.breakdown(psoBest, inst, penalty, alpha));
            System.out.println("[PSO] route: " + routeNames(psoBest, inst));

            // ACO
            ACO aco = new ACO(inst, penalty, alpha,
                    cfg.getAcoAnts(), cfg.getAcoIterations(),
                    cfg.getAcoAlpha(), cfg.getAcoBeta(), cfg.getAcoEvaporation(), cfg.getAcoQ(),
                    new Random(seed + f + 100));
            t0 = System.currentTimeMillis();
            int[] acoBest = aco.run();
            long acoMs = System.currentTimeMillis() - t0;

            System.out.printf("[ACO] fitness=%.4f  time=%d ms%n", aco.getBestFitness(), acoMs);
            System.out.println("[ACO] " + Evaluation.breakdown(acoBest, inst, penalty, alpha));
            System.out.println("[ACO] route: " + routeNames(acoBest, inst));

            String outName = "results_n" + inst.n + ".csv";
            saveHistory(outName, pso.getHistory(), aco.getHistory());
            System.out.println("Saved: " + outName);
            System.out.println();
        }

        System.out.println("=== Done ===");

        // Stats
        System.out.println("\n\n=== Statistical comparison (" + cfg.getStatsRuns() + " runs) ===");
        for (int f = 0; f < files.length; f++) {
            Instance inst = Instance.loadFromCSV(files[f]);
            Stats.run(inst, penalty, alpha, cfg, cfg.getStatsRuns(), seed + 5000L + f);
        }

        // Taguchi
        System.out.println("\n\n=== Taguchi L9 - parameter tuning ===");
        long taguchiSeed = seed + 9000L;

        for (int f = 0; f < files.length; f++) {
            Instance inst = Instance.loadFromCSV(files[f]);
            Taguchi.runPSO(inst, penalty, alpha, cfg.getTaguchiReplications(), taguchiSeed + f);
            Taguchi.runACO(inst, penalty, alpha, cfg.getTaguchiReplications(), taguchiSeed + f);
        }

        System.out.println("\n=== Taguchi done ===");
    }

    static String routeNames(int[] route, Instance inst) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.length; i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(inst.attractions.get(route[i]).name);
        }
        return sb.toString();
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
