import java.io.*;
import java.util.*;
import experiments.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Config cfg = new Config("config.properties");
        new File("results").mkdirs();

        long seed = cfg.getSeed();
        double penalty = cfg.getPenalty();
        double alpha = cfg.getAlpha();

        double pBus       = cfg.getPBus();
        double penaltyMin = cfg.getPenaltyMin();
        double penaltyMax = cfg.getPenaltyMax();

        long taguchiSeed = seed + 9000L;
        String[] datasets = cfg.getDatasets();

        for (int f = 0; f < datasets.length; f++) {

            Explorer.Selection sel = Explorer.load(cfg, datasets[f]);
            System.out.println("Start taguchi: " + sel.label());

            Taguchi.Result pso = Taguchi.runPSO(sel.pool(), sel.count(), penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed, sel.label());
            Taguchi.Result aco = Taguchi.runACO(sel.pool(), sel.count(), penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed, sel.label());

            saveHistory("results/results_" + sel.label() + ".csv", pso.history(), aco.history());
            Stats.save(sel.pool(), sel.label(), penalty, alpha, pso, aco);

            double[] p = pso.params();
            double optInertia= p[0];
            double optC1 = p[1];
            double optC2 = p[2];
            int    optParticles = (int) Math.round(p[3]);

            long varSeed = taguchiSeed + 1_000_000L;

            System.out.println("PSO warianty: " + sel.label());

            Taguchi.Result psoRepair = Taguchi.confirmPSOVariant(
                    sel.pool(), sel.count(), penalty, alpha,
                    optInertia, optC1, optC2, optParticles,
                    varSeed, "PSO_REPAIR",
                    true, pBus, false, penalty, penalty, false);

            Taguchi.Result psoAdaptive = Taguchi.confirmPSOVariant(
                    sel.pool(), sel.count(), penalty, alpha,
                    optInertia, optC1, optC2, optParticles,
                    varSeed + 1, "PSO_ADAPTIVE",
                    false, 0.0, true, penaltyMin, penaltyMax, false);

            Taguchi.Result psoGreedy = Taguchi.confirmPSOVariant(
                    sel.pool(), sel.count(), penalty, alpha,
                    optInertia, optC1, optC2, optParticles,
                    varSeed + 2, "PSO_GREEDY",
                    false, 0.0, false, penalty, penalty, true);

            Taguchi.Result psoAll = Taguchi.confirmPSOVariant(
                    sel.pool(), sel.count(), penalty, alpha,
                    optInertia, optC1, optC2, optParticles,
                    varSeed + 3, "PSO_ALL",
                    true, pBus, true, penaltyMin, penaltyMax, true);

            saveVariantHistory("results/pso_variants_" + sel.label() + ".csv",
                    pso, psoRepair, psoAdaptive, psoGreedy, psoAll);
            Stats.saveVariants(sel.pool(), sel.label(), penalty, alpha,
                    pso, psoRepair, psoAdaptive, psoGreedy, psoAll, aco);
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

    static void saveVariantHistory(String fileName,
                                   Taguchi.Result base, Taguchi.Result repair,
                                   Taguchi.Result adaptive, Taguchi.Result greedy,
                                   Taguchi.Result all) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("iteration,PSO_BASE,PSO_REPAIR,PSO_ADAPTIVE,PSO_GREEDY,PSO_ALL");
            double[] h0 = base.history();
            double[] h1 = repair.history();
            double[] h2 = adaptive.history();
            double[] h3 = greedy.history();
            double[] h4 = all.history();
            for (int i = 0; i < h0.length; i++)
                pw.printf("%d,%.6f,%.6f,%.6f,%.6f,%.6f%n",
                        i + 1, h0[i], h1[i], h2[i], h3[i], h4[i]);
        }
    }
}
