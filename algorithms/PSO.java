package algorithms;

import java.util.*;
import model.*;

// PSO dla permutacji - pozycja to permutacja, predkosc to lista swapow
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

    int[] bestRoute;
    double bestFitness;
    Evaluation.EvalResult bestResult;
    double[] history;

    public PSO(Instance instance, double penalty, double alpha,
               int nParticles, int maxIter, double inertia, double c1, double c2,
               Random rng) {
        this.instance = instance;
        this.penalty = penalty;
        this.alpha = alpha;
        this.nParticles = nParticles;
        this.maxIter = maxIter;
        this.inertia = inertia;
        this.c1 = c1;
        this.c2 = c2;
        this.rng = rng;
    }

    public int[] run() {
        int n = instance.n;
        bestFitness = Double.MAX_VALUE;
        double[] hist = new double[maxIter];

        // tworze roj losowych permutacji
        List<Particle> swarm = new ArrayList<>();
        for (int p=0; p<nParticles; p++) {
            int[] pos = randomPerm(n);
            Evaluation.EvalResult result = Evaluation.evaluate(pos, instance, penalty, alpha);
            double fit = result.total();
            swarm.add(new Particle(pos, fit));
            if(fit < bestFitness) {
                bestFitness = fit;
                bestResult = result;
                bestRoute = new int[n];
                for(int i=0; i<n; i++) bestRoute[i] = pos[i];
            }
        }

        // glowna petla
        for (int iter=0; iter<maxIter;iter++) {
            for (int p = 0; p<swarm.size(); p++) {
                Particle particle = swarm.get(p);
                List<int[]> newVel = new ArrayList<>();

                // inercja - zachowaj swapy z prawdopodobienstwem w
                for (int s=0; s<particle.velocity.size();s++) {
                    if(rng.nextDouble() < inertia) {
                        newVel.add(particle.velocity.get(s));
                    }
                }

                // skladowa poznawcza - w strone personal best
                double r1 = rng.nextDouble();
                List<int[]> diffP = computeDiff(particle.position, particle.personalBest);
                for(int s=0; s<diffP.size(); s++) {
                    if(rng.nextDouble() < c1*r1) newVel.add(diffP.get(s));
                }

                // skladowa spoleczna - w strone global best
                double r2 = rng.nextDouble();
                List<int[]> diffG = computeDiff(particle.position, bestRoute);
                for (int s=0; s<diffG.size(); s++) {
                    if(rng.nextDouble() < c2*r2) newVel.add(diffG.get(s));
                }

                particle.velocity = newVel;

                // aplikuje swapy do nowej pozycji
                int[] newPos = new int[n];
                for (int i=0; i<n; i++) newPos[i] = particle.position[i];
                for (int s=0; s<newVel.size(); s++) {
                    int[] swap = newVel.get(s);
                    int tmp = newPos[swap[0]];
                    newPos[swap[0]] = newPos[swap[1]];
                    newPos[swap[1]] = tmp;
                }
                particle.position = newPos;

                Evaluation.EvalResult result = Evaluation.evaluate(newPos, instance, penalty, alpha);
                double fit = result.total();
                particle.tryUpdatePBest(newPos, fit);

                if(fit < bestFitness) {
                    bestFitness = fit;
                    bestResult = result;
                    bestRoute = new int[n];
                    for (int i=0;i<n;i++) bestRoute[i] = newPos[i];
                }
            }
            hist[iter] = bestFitness;
        }

        history = hist;
        int[] result = new int[n];
        for (int i=0; i<n; i++) result[i] = bestRoute[i];
        return result;
    }

    public double getBestFitness() { return bestFitness; }
    public Evaluation.EvalResult getBestResult() { return bestResult; }
    public double[] getHistory() { return history; }

    // losowa permutacja przez tasowanie
    private int[] randomPerm(int n) {
        int[] p = new int[n];
        for (int i=0; i<n; i++) p[i] = i;
        for (int i=n-1; i>0; i--) {
            int j = rng.nextInt(i+1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        return p;
    }

    // minimalna sekwencja swapow zeby zamienic src w target
    public static List<int[]> computeDiff(int[] src,int[] target) {
        int[] work = new int[src.length];
        for (int i=0; i<src.length;i++) work[i] = src[i];

        int[] posOf = new int[work.length];
        for(int i=0; i<work.length; i++) posOf[work[i]] = i;

        List<int[]> swaps = new ArrayList<>();
        for (int i=0; i<work.length;i++) {
            if(work[i] != target[i]) {
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
