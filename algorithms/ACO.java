package algorithms;

import java.util.*;
import model.*;

public class ACO {

    private final int nAnts;
    private final int maxIter;
    private final double alphaAco;
    private final double betaAco;
    private final double evaporationRate;
    private final double Q;

    private final Instance instance;
    private final double penalty;
    private final double alpha;
    private final Random rng;

    private double[][] tau;
    private double[][] eta;

    private int[] bestRoute;
    private double bestFitness;
    private double[] history;

    private static final double TAU0 = 1.0;
    private static final double EPS = 1e-6;

    public ACO(Instance instance, double penalty, double alpha,
               int nAnts, int maxIter, double alphaAco, double betaAco,
               double evaporationRate, double Q, Random rng) {
        this.instance = instance;
        this.penalty = penalty;
        this.alpha = alpha;
        this.nAnts = nAnts;
        this.maxIter = maxIter;
        this.alphaAco = alphaAco;
        this.betaAco = betaAco;
        this.evaporationRate = evaporationRate;
        this.Q = Q;
        this.rng = rng;
    }

    public int[] run() {
        int n = instance.n;
        bestFitness = Double.MAX_VALUE;
        history = new double[maxIter];

        initTau(n);
        initEta(n);

        for (int iter = 0; iter < maxIter; iter++) {
            int[] iterBestRoute = null;
            double iterBestFit = Double.MAX_VALUE;

            for (int ant = 0; ant < nAnts; ant++) {
                int[] tour = buildTour(n);
                double fit = Evaluation.evaluate(tour, instance, penalty, alpha);
                if (fit < iterBestFit) {
                    iterBestFit = fit;
                    iterBestRoute = tour;
                }
                if (fit < bestFitness) {
                    bestFitness = fit;
                    bestRoute = Arrays.copyOf(tour, n);
                }
            }

            evaporate(n);
            if (iterBestRoute != null) deposit(iterBestRoute, iterBestFit);

            history[iter] = bestFitness;
        }
        return Arrays.copyOf(bestRoute, n);
    }

    public double getBestFitness() { return bestFitness; }
    public double[] getHistory() { return Arrays.copyOf(history, history.length); }

    private void initTau(int n) {
        tau = new double[n][n];
        for (double[] row : tau) Arrays.fill(row, TAU0);
    }

    private void initEta(int n) {
        eta = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    eta[i][j] = (1.0 + instance.attractions.get(j).attractiveness)
                            / Math.max(instance.distance(i, j), EPS);
    }

    private int[] buildTour(int n) {
        boolean[] visited = new boolean[n];
        int[] tour = new int[n];
        int current = rng.nextInt(n);
        tour[0] = current;
        visited[current] = true;

        for (int step = 1; step < n; step++) {
            double[] weight = new double[n];
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    weight[j] = Math.pow(tau[current][j], alphaAco)
                            * Math.pow(eta[current][j], betaAco);
                    sum += weight[j];
                }
            }

            int next = -1;
            if (sum > 0) {
                double threshold = rng.nextDouble() * sum;
                double cumulative = 0;
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        cumulative += weight[j];
                        if (cumulative >= threshold) { next = j; break; }
                    }
                }
            }
            if (next == -1)
                for (int j = 0; j < n; j++)
                    if (!visited[j]) { next = j; break; }

            tour[step] = next;
            visited[next] = true;
            current = next;
        }
        return tour;
    }

    private void evaporate(int n) {
        double factor = 1.0 - evaporationRate;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tau[i][j] *= factor;
    }

    private void deposit(int[] tour, double cost) {
        if (cost <= 0) return;
        double amount = Q / cost;
        for (int i = 0; i < tour.length - 1; i++) {
            tau[tour[i]][tour[i + 1]] += amount;
            tau[tour[i + 1]][tour[i]] += amount;
        }
    }
}
