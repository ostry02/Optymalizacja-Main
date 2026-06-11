package experiments;

import java.io.*;
import java.util.*;
import model.*;
import algorithms.*;

public class Taguchi {

    // tablica ortogonalna L9(3^4) - 9 eksperymentow, 4 czynniki, 3 poziomy kazdego
    private static final int[][] L9 = {
        {0, 0, 0, 0},
        {0, 1, 1, 1},
        {0, 2, 2, 2},
        {1, 0, 1, 2},
        {1, 1, 2, 0},
        {1, 2, 0, 1},
        {2, 0, 2, 1},
        {2, 1, 0, 2},
        {2, 2, 1, 0},
    };

    // PSO factor levels
    private static final double[] PSO_INERTIA = {0.4, 0.7, 0.9};
    private static final double[] PSO_C1 = {0.3, 0.5, 0.8};
    private static final double[] PSO_C2 = {0.3, 0.5, 0.8};
    private static final int[] PSO_PARTICLES = {15, 30, 50};
    private static final String[] PSO_FACTORS = {"inertia", "c1", "c2", "nParticles"};

    // ACO factor levels
    private static final double[] ACO_ALPHA_LEV = {0.5, 1.0, 2.0};
    private static final double[] ACO_BETA_LEV = {1.0, 2.0, 3.0};
    private static final double[] ACO_EVAP_LEV = {0.05, 0.1, 0.2};
    private static final int[] ACO_ANTS = {15, 30, 50};
    private static final String[] ACO_FACTORS = {"alphaAco", "betaAco", "evapRate", "nAnts"};

    private static final int PSO_ITER = 100;
    private static final int ACO_ITER = 100;

    private Taguchi() {}

    public record Result(String algo, String[] factors, double[] params,
                         double fitness, Evaluation.EvalResult eval,
                         long timeMs, double[] history) {}

    public static Result runPSO(Instance instance, int routeLen, double penalty, double alpha,
                              int replications, String label) throws IOException {

        double[][] results = new double[9][replications];

        for (int exp = 0; exp < 9; exp++) {
            int[] row = L9[exp];
            double inertia = PSO_INERTIA[row[0]];
            double c1 = PSO_C1[row[1]];
            double c2 = PSO_C2[row[2]];
            int nParticles = PSO_PARTICLES[row[3]];

            for (int r = 0; r < replications; r++) {
                PSO pso = new PSO(instance, penalty, alpha,
                        nParticles, PSO_ITER, inertia, c1, c2,
                        new Random(), routeLen);
                pso.run();
                results[exp][r] = pso.getBestFitness();
            }
        }

        double[] sn = computeSN(results);
        int[] best = bestLevels(sn);
        saveCsv("results/taguchi_pso_" + label + ".csv", PSO_FACTORS,
                new double[][]{PSO_INERTIA, PSO_C1, PSO_C2, toDouble(PSO_PARTICLES)},
                sn, results);

        double optInertia = PSO_INERTIA[best[0]];
        double optC1 = PSO_C1[best[1]];
        double optC2 = PSO_C2[best[2]];
        int optParticles = PSO_PARTICLES[best[3]];

        PSO confirm = new PSO(instance, penalty, alpha,
                optParticles, PSO_ITER, optInertia, optC1, optC2,
                new Random(), routeLen);
        long t0 = System.currentTimeMillis();
        confirm.run();
        long ms = System.currentTimeMillis() - t0;

        return new Result("PSO", PSO_FACTORS,
                new double[]{optInertia, optC1, optC2, optParticles},
                confirm.getBestFitness(), confirm.getBestResult(), ms, confirm.getHistory());
    }

    // uruchamia potwierdzenie PSO z konkretnymi parametrami i flagami wariantu
    public static Result confirmPSOVariant(Instance instance, int routeLen, double penalty, double alpha,
                                           double inertia, double c1, double c2, int nParticles,
                                           String algoName,
                                           boolean useRepair, double pBus,
                                           boolean adaptivePenalty, double penaltyMin, double penaltyMax,
                                           boolean useGreedyInit) {
        PSO pso = new PSO(instance, penalty, alpha, nParticles, PSO_ITER, inertia, c1, c2,
                          new Random(), routeLen,
                          useRepair, pBus, adaptivePenalty, penaltyMin, penaltyMax, useGreedyInit);
        long t0 = System.currentTimeMillis();
        pso.run();
        long ms = System.currentTimeMillis() - t0;
        return new Result(algoName, PSO_FACTORS,
                          new double[]{inertia, c1, c2, nParticles},
                          pso.getBestFitness(), pso.getBestResult(), ms, pso.getHistory());
    }

    // pojedynczy przebieg ACO z konkretnymi parametrami (do agregacji wielu runow)
    public static Result confirmACO(Instance instance, int routeLen, double penalty, double alpha,
                                    double alphaAco, double betaAco, double evap, int nAnts,
                                    String algoName) {
        ACO aco = new ACO(instance, penalty, alpha, nAnts, ACO_ITER, alphaAco, betaAco, evap, 100.0,
                          new Random(), routeLen);
        long t0 = System.currentTimeMillis();
        aco.run();
        long ms = System.currentTimeMillis() - t0;
        return new Result(algoName, ACO_FACTORS,
                          new double[]{alphaAco, betaAco, evap, nAnts},
                          aco.getBestFitness(), aco.getBestResult(), ms, aco.getHistory());
    }

    public static Result runACO(Instance instance, int routeLen, double penalty, double alpha,
                              int replications, String label) throws IOException {
        double[][] results = new double[9][replications];

        for (int exp = 0; exp < 9; exp++) {
            int[] row = L9[exp];
            double alphaAco = ACO_ALPHA_LEV[row[0]];
            double betaAco = ACO_BETA_LEV[row[1]];
            double evap = ACO_EVAP_LEV[row[2]];
            int nAnts = ACO_ANTS[row[3]];

            for (int r = 0; r < replications; r++) {
                ACO aco = new ACO(instance, penalty, alpha,
                        nAnts, ACO_ITER, alphaAco, betaAco, evap, 100.0,
                        new Random(), routeLen);
                aco.run();
                results[exp][r] = aco.getBestFitness();
            }
        }

        double[] sn = computeSN(results);
        int[] best = bestLevels(sn);
        saveCsv("results/taguchi_aco_" + label + ".csv", ACO_FACTORS,
                new double[][]{ACO_ALPHA_LEV, ACO_BETA_LEV, ACO_EVAP_LEV, toDouble(ACO_ANTS)},
                sn, results);

        double optAlpha = ACO_ALPHA_LEV[best[0]];
        double optBeta = ACO_BETA_LEV[best[1]];
        double optEvap = ACO_EVAP_LEV[best[2]];
        int optAnts = ACO_ANTS[best[3]];

        ACO confirm = new ACO(instance, penalty, alpha,
                optAnts, ACO_ITER, optAlpha, optBeta, optEvap, 100.0,
                new Random(), routeLen);
        long t0 = System.currentTimeMillis();
        confirm.run();
        long ms = System.currentTimeMillis() - t0;

        return new Result("ACO", ACO_FACTORS,
                new double[]{optAlpha, optBeta, optEvap, optAnts},
                confirm.getBestFitness(), confirm.getBestResult(), ms, confirm.getHistory());
    }

    private static double[] computeSN(double[][] results) {
        double[] sn = new double[results.length];
        for (int exp = 0; exp < results.length; exp++) {
            double sumSq = 0;
            for (double y : results[exp]) sumSq += y * y;
            sn[exp] = -10.0 * Math.log10(sumSq / results[exp].length);
        }
        return sn;
    }

    private static int[] bestLevels(double[] sn) {
        int nFactors = L9[0].length;
        double[][] meanSN = new double[nFactors][3];
        int[][] count = new int[nFactors][3];

        for (int exp = 0; exp < 9; exp++)
            for (int f = 0; f < nFactors; f++) {
                int lv = L9[exp][f];
                meanSN[f][lv] += sn[exp];
                count[f][lv]++;
            }

        for (int f = 0; f < nFactors; f++)
            for (int lv = 0; lv < 3; lv++)
                if (count[f][lv] > 0) meanSN[f][lv] /= count[f][lv];

        int[] best = new int[nFactors];
        for (int f = 0; f < nFactors; f++)
            for (int lv = 1; lv < 3; lv++)
                if (meanSN[f][lv] > meanSN[f][best[f]]) best[f] = lv;

        return best;
    }

    private static void saveCsv(String filename, String[] factors, double[][] levels,
                                double[] sn, double[][] results) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print("exp," + factors[0] + "," + factors[1] + "," + factors[2] + "," + factors[3]
                    + ",mean_fitness,sn_ratio");
            for (int r = 0; r < results[0].length; r++) pw.print(",run" + (r + 1));
            pw.println();

            for (int exp = 0; exp < 9; exp++) {
                int[] row = L9[exp];
                double mean = Arrays.stream(results[exp]).average().orElse(0);
                pw.printf("%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f",
                        exp + 1,
                        levels[0][row[0]], levels[1][row[1]],
                        levels[2][row[2]], levels[3][row[3]],
                        mean, sn[exp]);
                for (double y : results[exp]) pw.printf(",%.4f", y);
                pw.println();
            }
        }
    }

    private static double[] toDouble(int[] arr) {
        double[] d = new double[arr.length];
        for (int i = 0; i < arr.length; i++) d[i] = arr[i];
        return d;
    }
}
