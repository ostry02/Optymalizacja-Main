import java.util.*;

// -------------------------------------------------------
// ACO.java – Ant Colony Optimization for the TSP variant
//
// Algorithm (elitist variant):
//   1. Initialise pheromone matrix tau[i][j] = TAU0
//   2. Each iteration: every ant builds a tour using
//      roulette-wheel selection based on:
//        p[i][j] proportional to tau[i][j]^alphaAco * eta[i][j]^betaAco
//      where eta[i][j] = (1 + attractiveness[j]) / dist(i,j)
//   3. Evaporation: tau[i][j] *= (1 - evaporationRate)
//   4. Only the best ant of the iteration deposits pheromone:
//        tau[i][j] += Q / tour_cost
// -------------------------------------------------------
public class ACO {

    // --- algorithm parameters ---
    private final int    nAnts;
    private final int    maxIter;
    private final double alphaAco;        // pheromone exponent
    private final double betaAco;         // heuristic exponent
    private final double evaporationRate; // evaporation coefficient rho
    private final double Q;               // pheromone deposit constant

    // --- problem context ---
    private final Instance instance;
    private final double   penalty;
    private final double   alpha;
    private final Random   rng;

    // --- internal matrices ---
    private double[][] tau; // pheromone matrix
    private double[][] eta; // heuristic matrix (computed once)

    // --- results ---
    private int[]    bestRoute;
    private double   bestFitness;
    private double[] history;

    private static final double TAU0 = 1.0;  // initial pheromone level
    private static final double EPS  = 1e-6; // guard against division by zero

    public ACO(Instance instance, double penalty, double alpha,
               int nAnts, int maxIter, double alphaAco, double betaAco,
               double evaporationRate, double Q, Random rng) {
        this.instance        = instance;
        this.penalty         = penalty;
        this.alpha           = alpha;
        this.nAnts           = nAnts;
        this.maxIter         = maxIter;
        this.alphaAco        = alphaAco;
        this.betaAco         = betaAco;
        this.evaporationRate = evaporationRate;
        this.Q               = Q;
        this.rng             = rng;
    }

    /** Runs the algorithm and returns the best route found. */
    public int[] run() {
        int n = instance.n;
        bestFitness = Double.MAX_VALUE;
        history     = new double[maxIter];

        initTau(n);
        initEta(n);

        for (int iter = 0; iter < maxIter; iter++) {
            int[]  iterBestRoute = null;
            double iterBestFit   = Double.MAX_VALUE;

            // each ant builds a tour
            for (int ant = 0; ant < nAnts; ant++) {
                int[]  tour = buildTour(n);
                double fit  = Evaluation.evaluate(tour, instance, penalty, alpha);
                if (fit < iterBestFit) { iterBestFit = fit; iterBestRoute = tour; }
                if (fit < bestFitness) {
                    bestFitness = fit;
                    bestRoute   = Arrays.copyOf(tour, n);
                }
            }

            // evaporation + deposit by iteration-best ant
            evaporate(n);
            if (iterBestRoute != null) deposit(iterBestRoute, iterBestFit);

            history[iter] = bestFitness;
        }
        return Arrays.copyOf(bestRoute, n);
    }

    public double   getBestFitness() { return bestFitness; }
    public double[] getHistory()     { return Arrays.copyOf(history, history.length); }

    // --- matrix initialisation ---

    private void initTau(int n) {
        tau = new double[n][n];
        for (double[] row : tau) Arrays.fill(row, TAU0);
    }

    /** eta[i][j] = (1 + attractiveness[j]) / dist(i,j) — closer and more attractive is better. */
    private void initEta(int n) {
        eta = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    eta[i][j] = (1.0 + instance.attractions.get(j).attractiveness)
                                / Math.max(instance.distance(i, j), EPS);
    }

    // --- tour construction ---

    /** Builds a complete tour for one ant using roulette-wheel selection. */
    private int[] buildTour(int n) {
        boolean[] visited = new boolean[n];
        int[]     tour    = new int[n];
        int       current = rng.nextInt(n);
        tour[0] = current;
        visited[current] = true;

        for (int step = 1; step < n; step++) {
            double[] weight = new double[n];
            double   sum    = 0;
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    weight[j] = Math.pow(tau[current][j], alphaAco)
                              * Math.pow(eta[current][j], betaAco);
                    sum += weight[j];
                }
            }

            // roulette-wheel selection
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
            // fallback: pick first unvisited (floating-point edge case)
            if (next == -1)
                for (int j = 0; j < n; j++)
                    if (!visited[j]) { next = j; break; }

            tour[step] = next;
            visited[next] = true;
            current = next;
        }
        return tour;
    }

    // --- pheromone update ---

    private void evaporate(int n) {
        double factor = 1.0 - evaporationRate;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tau[i][j] *= factor;
    }

    /** Deposits pheromone along tour edges: tau[i][j] += Q / cost. */
    private void deposit(int[] tour, double cost) {
        if (cost <= 0) return;
        double amount = Q / cost;
        for (int i = 0; i < tour.length - 1; i++) {
            tau[tour[i]][tour[i + 1]] += amount;
            tau[tour[i + 1]][tour[i]] += amount; // undirected graph
        }
    }
}
