import java.io.*;
import java.util.*;

public class Main {

    static final long   SEED    = 42L;
    static final double PENALTY = 10.0;
    static final double ALPHA   = 1.0;

    // PSO parameters
    static final int    PSO_PARTICLES = 30;
    static final int    PSO_ITER      = 200;
    static final double PSO_INERTIA   = 0.7;
    static final double PSO_C1        = 1.5;
    static final double PSO_C2        = 1.5;

    // ACO parameters
    static final int    ACO_ANTS  = 30;
    static final int    ACO_ITER  = 200;
    static final double ACO_ALPHA = 1.0;  // pheromone exponent
    static final double ACO_BETA  = 2.0;  // heuristic exponent
    static final double ACO_EVAP  = 0.1;  // evaporation rate
    static final double ACO_Q     = 100.0;

    static final String[] FILES = {
        "data/instance_5.csv",
        "data/instance_8.csv",
        "data/instance_12.csv",
        "data/instance_20.csv",
        "data/instance_30.csv"
    };

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);

        System.out.println("=== TSP - Atrakcje Wroclawia ===");
        System.out.println("Seed=" + SEED + "  Penalty=" + PENALTY + "  Alpha=" + ALPHA);
        System.out.println();

        for (int f = 0; f < FILES.length; f++) {
            String file = FILES[f];
            System.out.println("--- " + file + " ---");

            Instance inst = Instance.loadFromCSV(file);
            System.out.println(inst);

            // --- PSO ---
            PSO  pso    = new PSO(inst, PENALTY, ALPHA,
                                  PSO_PARTICLES, PSO_ITER, PSO_INERTIA, PSO_C1, PSO_C2,
                                  new Random(SEED + f));
            long t0     = System.currentTimeMillis();
            int[] psoBest = pso.run();
            long psoMs  = System.currentTimeMillis() - t0;

            System.out.printf("[PSO] fitness=%.4f  time=%d ms%n", pso.getBestFitness(), psoMs);
            System.out.println("[PSO] " + Evaluation.breakdown(psoBest, inst, PENALTY, ALPHA));
            System.out.println("[PSO] route: " + routeNames(psoBest, inst));

            // --- ACO ---
            ACO  aco    = new ACO(inst, PENALTY, ALPHA,
                                  ACO_ANTS, ACO_ITER, ACO_ALPHA, ACO_BETA, ACO_EVAP, ACO_Q,
                                  new Random(SEED + f + 100));
            t0          = System.currentTimeMillis();
            int[] acoBest = aco.run();
            long acoMs  = System.currentTimeMillis() - t0;

            System.out.printf("[ACO] fitness=%.4f  time=%d ms%n", aco.getBestFitness(), acoMs);
            System.out.println("[ACO] " + Evaluation.breakdown(acoBest, inst, PENALTY, ALPHA));
            System.out.println("[ACO] route: " + routeNames(acoBest, inst));

            // --- save combined CSV ---
            String outName = "results_n" + inst.n + ".csv";
            saveHistory(outName, pso.getHistory(), aco.getHistory());
            System.out.println("Saved: " + outName);
            System.out.println();
        }

        System.out.println("=== Done ===");

        // Taguchi parameter tuning
        System.out.println("\n\n=== Taguchi L9 - parameter tuning ===");
        int taguchiReps = 5;
        long taguchiSeed = SEED + 9000L;

        for (int f = 0; f < FILES.length; f++) {
            Instance inst = Instance.loadFromCSV(FILES[f]);
            Taguchi.runPSO(inst, PENALTY, ALPHA, taguchiReps, taguchiSeed + f);
            Taguchi.runACO(inst, PENALTY, ALPHA, taguchiReps, taguchiSeed + f);
        }

        System.out.println("\n=== Taguchi done ===");
    }

    /** Returns route as a string of attraction names. */
    static String routeNames(int[] route, Instance inst) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.length; i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(inst.attractions.get(route[i]).name);
        }
        return sb.toString();
    }

    /** Saves PSO and ACO fitness histories to a single CSV file. */
    static void saveHistory(String fileName, double[] psoHist, double[] acoHist) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        pw.println("iteration,fitness_pso,fitness_aco");
        for (int i = 0; i < psoHist.length; i++)
            pw.println((i + 1) + "," + String.format("%.6f", psoHist[i])
                               + "," + String.format("%.6f", acoHist[i]));
        pw.close();
    }
}
