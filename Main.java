import java.io.*;
import java.util.*;

public class Main {

    static final long SEED = 42L;
    static final double PENALTY = 10.0;
    static final double ALPHA = 1.0;

    // parametry PSO
    static final int N_PARTICLES = 30;
    static final int MAX_ITER = 200;
    static final double INERTIA = 0.7;
    static final double C1 = 1.5;
    static final double C2 = 1.5;

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
        System.out.println("Seed=" + SEED + " Penalty=" + PENALTY + " Alpha=" + ALPHA);
        System.out.println();

        for(int f=0;f<FILES.length; f++) {
            String file = FILES[f];
            System.out.println("--- " + file + " ---");

            Instance inst = Instance.loadFromCSV(file);
            System.out.println(inst);

            long start = System.currentTimeMillis();

            PSO pso = new PSO(inst, PENALTY, ALPHA, N_PARTICLES, MAX_ITER, INERTIA, C1, C2, new Random(SEED));
            int[] best = pso.run();

            long czas = System.currentTimeMillis() - start;

            System.out.println("Best fitness: " + String.format("%.4f", pso.getBestFitness()));
            System.out.println(Evaluation.breakdown(best, inst, PENALTY, ALPHA));

            // wypisuje trase
            System.out.print("Trasa: ");
            for (int i=0; i<best.length; i++) {
                if(i > 0) System.out.print(" -> ");
                System.out.print(inst.attractions.get(best[i]).name);
            }
            System.out.println();
            System.out.println("Czas: " + czas + " ms");

            // zapis historii do csv
            String outName = "results_" + inst.n + ".csv";
            saveHistory(pso.getHistory(), outName);
            System.out.println("Zapisano: " + outName);
            System.out.println();
        }

        System.out.println("=== Koniec ===");
    }

    static void saveHistory(double[] history, String fileName) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        pw.println("iteration,best_fitness");
        for (int i=0; i<history.length;i++) {
            pw.println((i+1) + "," + String.format("%.6f", history[i]));
        }
        pw.close();
    }
}
