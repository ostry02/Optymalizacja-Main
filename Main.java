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

        double busBonus   = cfg.getAcoBusBonus();
        double lookahead  = cfg.getAcoLookahead();

        long taguchiSeed = seed + 9000L;
        int runs = cfg.getStatsRuns();
        String[] datasets = cfg.getDatasets();

        for (int f = 0; f < datasets.length; f++) {

            Explorer.Selection sel = Explorer.load(cfg, datasets[f]);
            int L = sel.count();
            System.out.println("Start taguchi: " + sel.label());

            // Taguchi: dobor parametrow (zapisuje taguchi_*.csv)
            Taguchi.Result psoTune = Taguchi.runPSO(sel.pool(), L, penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f, sel.label());
            Taguchi.Result acoTune = Taguchi.runACO(sel.pool(), L, penalty, alpha,
                    cfg.getTaguchiReplications(), taguchiSeed + f, sel.label());

            // seedy dla 10 przebiegow: warianty PSO wspoldziela te same (porownanie sparowane), ACO osobno
            long psoSeed = seed + 100_000L * (f + 1);
            long acoSeed = seed + 100_000L * (f + 1) + 50_000L;

            double[] pp = psoTune.params();
            double inertia = pp[0], c1 = pp[1], c2 = pp[2];
            int particles = (int) Math.round(pp[3]);

            double[] ap = acoTune.params();
            double aAlpha = ap[0], aBeta = ap[1], aEvap = ap[2];
            int aAnts = (int) Math.round(ap[3]);

            System.out.println(runs + " przebiegow / wariant: " + sel.label());

            List<Stats.Agg> aggs = new ArrayList<>();
            Stats.Agg base = Stats.aggregate("PSO", manyPSO(sel.pool(), L, penalty, alpha,
                    inertia, c1, c2, particles, "PSO", false, 0.0, false, penalty, penalty, false, psoSeed, runs));
            Stats.Agg repair = Stats.aggregate("PSO_REPAIR", manyPSO(sel.pool(), L, penalty, alpha,
                    inertia, c1, c2, particles, "PSO_REPAIR", true, pBus, false, penalty, penalty, false, psoSeed, runs));
            Stats.Agg adaptive = Stats.aggregate("PSO_ADAPTIVE", manyPSO(sel.pool(), L, penalty, alpha,
                    inertia, c1, c2, particles, "PSO_ADAPTIVE", false, 0.0, true, penaltyMin, penaltyMax, false, psoSeed, runs));
            Stats.Agg greedy = Stats.aggregate("PSO_GREEDY", manyPSO(sel.pool(), L, penalty, alpha,
                    inertia, c1, c2, particles, "PSO_GREEDY", false, 0.0, false, penalty, penalty, true, psoSeed, runs));
            // wariant ze wszystkimi ulepszeniami: repair + adaptive + greedy
            Stats.Agg all = Stats.aggregate("PSO_ALL", manyPSO(sel.pool(), L, penalty, alpha,
                    inertia, c1, c2, particles, "PSO_ALL", true, pBus, true, penaltyMin, penaltyMax, true, psoSeed, runs));
            Stats.Agg acoAgg = Stats.aggregate("ACO", manyACO(sel.pool(), L, penalty, alpha,
                    aAlpha, aBeta, aEvap, aAnts, "ACO", false, 0.0, 0.0, acoSeed, runs));
            // connectivity-guided ACO
            Stats.Agg acoCG = Stats.aggregate("ACO_CG", manyACO(sel.pool(), L, penalty, alpha,
                    aAlpha, aBeta, aEvap, aAnts, "ACO_CG", true, busBonus, lookahead, acoSeed, runs));

            aggs.add(base); aggs.add(repair); aggs.add(adaptive);
            aggs.add(greedy); aggs.add(all); aggs.add(acoAgg); aggs.add(acoCG);

            Stats.save(sel.label(), penalty, alpha, base, acoAgg);
            Stats.saveVariants(sel.label(), penalty, alpha, aggs);
            saveAcoCompare("results/aco_compare_" + sel.label() + ".csv",
                    acoAgg.meanHistory(), acoCG.meanHistory());

            saveHistory("results/results_" + sel.label() + ".csv",
                    base.meanHistory(), acoAgg.meanHistory());
            saveVariantHistory("results/pso_variants_" + sel.label() + ".csv",
                    base.meanHistory(), repair.meanHistory(), adaptive.meanHistory(),
                    greedy.meanHistory(), all.meanHistory());
        }

        System.out.println("Wyniki zapisano w results");
    }

    // uruchamia wariant PSO `runs` razy z roznymi seedami (baseSeed + i)
    static List<Taguchi.Result> manyPSO(model.Instance pool, int L, double penalty, double alpha,
            double inertia, double c1, double c2, int particles, String name,
            boolean useRepair, double pBus, boolean adaptive, double penaltyMin, double penaltyMax,
            boolean useGreedy, long baseSeed, int runs) {
        List<Taguchi.Result> list = new ArrayList<>();
        for (int i = 0; i < runs; i++)
            list.add(Taguchi.confirmPSOVariant(pool, L, penalty, alpha, inertia, c1, c2, particles,
                    baseSeed + i, name, useRepair, pBus, adaptive, penaltyMin, penaltyMax, useGreedy));
        return list;
    }

    static List<Taguchi.Result> manyACO(model.Instance pool, int L, double penalty, double alpha,
            double aAlpha, double aBeta, double aEvap, int aAnts, String name,
            boolean connectivityGuided, double busBonus, double lookahead, long baseSeed, int runs) {
        List<Taguchi.Result> list = new ArrayList<>();
        for (int i = 0; i < runs; i++)
            list.add(Taguchi.confirmACO(pool, L, penalty, alpha, aAlpha, aBeta, aEvap, aAnts,
                    baseSeed + i, name, connectivityGuided, busBonus, lookahead));
        return list;
    }
    static void saveAcoCompare(String fileName, double[] base, double[] cg) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("iteration,ACO,ACO_CG");
            for (int i = 0; i < base.length; i++)
                pw.printf("%d,%.6f,%.6f%n", i + 1, base[i], cg[i]);
        }
    }

    static void saveHistory(String fileName, double[] psoHist, double[] acoHist) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("iteration,fitness_pso,fitness_aco");
            for (int i = 0; i < psoHist.length; i++)
                pw.println((i + 1) + "," + String.format("%.6f", psoHist[i])
                        + "," + String.format("%.6f", acoHist[i]));
        }
    }

    // historie sa juz usrednione po wszystkich przebiegach (mean per iteracja)
    static void saveVariantHistory(String fileName,
                                   double[] h0, double[] h1, double[] h2,
                                   double[] h3, double[] h4) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("iteration,PSO_BASE,PSO_REPAIR,PSO_ADAPTIVE,PSO_GREEDY,PSO_ALL");
            for (int i = 0; i < h0.length; i++)
                pw.printf("%d,%.6f,%.6f,%.6f,%.6f,%.6f%n",
                        i + 1, h0[i], h1[i], h2[i], h3[i], h4[i]);
        }
    }
}
