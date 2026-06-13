package algorithms;

import java.util.*;
import model.*;

public class PSO {

    int nParticles;
    int maxIter;
    double inertia;
    double c1;
    double c2;

    Instance instance;
    double penalty;
    double alpha;
    Random rng;
    int routeLen;

    boolean useRepair;
    double pBus;
    boolean adaptivePenalty;
    double penaltyMin;
    double penaltyMax;
    boolean useGreedyInit;   // start z zachlannej konstrukcji zamiast losowych permutacji

    int[] bestRoute;
    double bestFitness;
    Evaluation.EvalResult bestResult;
    double[] history;

    public PSO(Instance instance, double penalty, double alpha,
               int nParticles, int maxIter, double inertia, double c1, double c2,
               Random rng, int routeLen) {
        this(instance, penalty, alpha, nParticles, maxIter, inertia, c1, c2, rng, routeLen,
             false, 0.0, false, penalty, penalty, false);
    }

    public PSO(Instance instance, double penalty, double alpha,
               int nParticles, int maxIter, double inertia, double c1, double c2,
               Random rng, int routeLen,
               boolean useRepair, double pBus,
               boolean adaptivePenalty, double penaltyMin, double penaltyMax) {
        this(instance, penalty, alpha, nParticles, maxIter, inertia, c1, c2, rng, routeLen,
             useRepair, pBus, adaptivePenalty, penaltyMin, penaltyMax, false);
    }

    public PSO(Instance instance, double penalty, double alpha,
               int nParticles, int maxIter, double inertia, double c1, double c2,
               Random rng, int routeLen,
               boolean useRepair, double pBus,
               boolean adaptivePenalty, double penaltyMin, double penaltyMax,
               boolean useGreedyInit) {
        this.instance = instance;
        this.penalty = penalty;
        this.alpha = alpha;
        this.nParticles = nParticles;
        this.maxIter = maxIter;
        this.inertia = inertia;
        this.c1 = c1;
        this.c2 = c2;
        this.rng = rng;
        this.routeLen = routeLen;
        this.useRepair = useRepair;
        this.pBus = pBus;
        this.adaptivePenalty = adaptivePenalty;
        this.penaltyMin = penaltyMin;
        this.penaltyMax = penaltyMax;
        this.useGreedyInit = useGreedyInit;
    }

    public int[] run() {
        int n = instance.n;
        bestFitness = Double.MAX_VALUE;
        double[] hist = new double[maxIter];

        double initPenalty = adaptivePenalty ? penaltyMin : penalty;

        List<Particle> swarm = new ArrayList<>();
        for (int p = 0; p < nParticles; p++) {
            int[] pos = useGreedyInit ? greedyPerm() : randomPerm(n);
            Evaluation.EvalResult result = Evaluation.evaluate(pos, instance, initPenalty, alpha, routeLen);
            double fit = result.total();
            swarm.add(new Particle(pos, fit));
            if (fit < bestFitness) {
                bestFitness = fit;
                bestResult = result;
                bestRoute = new int[n];
                for (int i = 0; i < n; i++) bestRoute[i] = pos[i];
            }
        }

        for (int iter = 0; iter < maxIter; iter++) {
            double currentPenalty = adaptivePenalty
                ? penaltyMin + (penaltyMax - penaltyMin) * ((double) iter / maxIter)
                : penalty;

            for (int p = 0; p < swarm.size(); p++) {
                Particle particle = swarm.get(p);
                List<int[]> newVel = new ArrayList<>();

                // inercja - zachowaj swapy z prawdopodobienstwem w
                for (int s = 0; s < particle.velocity.size(); s++) {
                    if (rng.nextDouble() < inertia) newVel.add(particle.velocity.get(s));
                }

                // skladowa poznawcza idzie w strone personal best
                double r1 = rng.nextDouble();
                double probP = Math.min(1.0, c1 * r1);
                List<int[]> diffP = computeDiff(particle.position, particle.personalBest);
                for (int s = 0; s < diffP.size(); s++) {
                    if (rng.nextDouble() < probP) newVel.add(diffP.get(s));
                }

                // skladowa spoleczna idzie w strone global best
                double r2 = rng.nextDouble();
                double probG = Math.min(1.0, c2 * r2);
                List<int[]> diffG = computeDiff(particle.position, bestRoute);
                for (int s = 0; s < diffG.size(); s++) {
                    if (rng.nextDouble() < probG) newVel.add(diffG.get(s));
                }

                // limit rozmiaru predkosci - zapobiega eksplozji
                int maxVelSize = Math.max(1, routeLen / 2);
                if (newVel.size() > maxVelSize) {
                    Collections.shuffle(newVel, rng);
                    newVel = new ArrayList<>(newVel.subList(0, maxVelSize));
                }

                // dodaje swapy do nowej pozycji
                int[] newPos = new int[n];
                for (int i = 0; i < n; i++) newPos[i] = particle.position[i];
                for (int[] swap : newVel) {
                    int tmp = newPos[swap[0]];
                    newPos[swap[0]] = newPos[swap[1]];
                    newPos[swap[1]] = tmp;
                }

                // faza naprawcza: dla par bez polaczenia autobusowego proponuje swapy
                if (useRepair) applyRepairSwaps(newPos, newVel);

                particle.velocity = newVel;
                particle.position = newPos;

                Evaluation.EvalResult result = Evaluation.evaluate(newPos, instance, currentPenalty, alpha, routeLen);
                double fit = result.total();
                particle.tryUpdatePBest(newPos, fit);

                if (fit < bestFitness) {
                    bestFitness = fit;
                    bestResult = result;
                    bestRoute = new int[n];
                    for (int i = 0; i < n; i++) bestRoute[i] = newPos[i];
                }
            }
            hist[iter] = bestFitness;
        }

        history = hist;
        int[] result = new int[routeLen];
        for (int i = 0; i < routeLen; i++) result[i] = bestRoute[i];
        return result;
    }

    // dla kazdej pary sasiadow bez bezposredniego autobusu szuka w trasie
    // atrakcji polaczonej autobusowo i z prawdopodobienstwem pBus proponuje swap
    private void applyRepairSwaps(int[] pos, List<int[]> vel) {
        for (int i = 0; i < routeLen - 1; i++) {
            if (!instance.hasBus(pos[i], pos[i + 1])) {
                for (int j = i + 2; j < routeLen; j++) {
                    if (instance.hasBus(pos[i], pos[j]) && rng.nextDouble() < pBus) {
                        vel.add(new int[]{i + 1, j});
                        int tmp = pos[i + 1];
                        pos[i + 1] = pos[j];
                        pos[j] = tmp;
                        break;
                    }
                }
            }
        }
    }

    public double getBestFitness() { return bestFitness; }
    public Evaluation.EvalResult getBestResult() { return bestResult; }
    public double[] getHistory() { return history; }

    // zachlanna konstrukcja trasy
    private int[] greedyPerm() {
        int n = instance.n;
        boolean[] used = new boolean[n];
        int[] perm = new int[n];

        int start = rng.nextInt(n);
        perm[0] = start;
        used[start] = true;

        for (int k = 1; k < routeLen; k++) {
            int cur = perm[k - 1];
            int best = -1;
            double bestScore = -Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (used[j]) continue;
                double d = Math.max(instance.distance(cur, j), 1e-6);
                double score = (instance.hasBus(cur, j) ? 100.0 : 0.0)
                             + (1 + instance.attractions.get(j).attractiveness) / d;
                if (score > bestScore) {
                    bestScore = score;
                    best = j;
                }
            }
            perm[k] = best;
            used[best] = true;
        }

        int idx = routeLen;
        for (int j = 0; j < n; j++)
            if (!used[j]) perm[idx++] = j;

        return perm;
    }

    // losowa permutacja przez tasowanie
    private int[] randomPerm(int n) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        return p;
    }

    // minimalna sekwencja swapow
    public static List<int[]> computeDiff(int[] src, int[] target) {
        int[] work = new int[src.length];
        for (int i = 0; i < src.length; i++) work[i] = src[i];

        int[] posOf = new int[work.length];
        for (int i = 0; i < work.length; i++) posOf[work[i]] = i;

        List<int[]> swaps = new ArrayList<>();
        for (int i = 0; i < work.length; i++) {
            if (work[i] != target[i]) {
                int j = posOf[target[i]];
                swaps.add(new int[]{i, j});
                posOf[work[i]] = j;
                posOf[work[j]] = i;
                int tmp = work[i];
                work[i] = work[j];
                work[j] = tmp;
            }
        }
        return swaps;
    }
}
