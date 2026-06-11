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

        // dane wejsciowe uzytkownika (dataset + filtr + liczba atrakcji)
        Explorer.Selection sel = Explorer.run();

        System.out.println("Start taguchi");
        long taguchiSeed = seed + 9000L;

        // Taguchi strojone na puli wybranej przez uzytkownika
        Taguchi.Result pso = Taguchi.runPSO(sel.pool(), sel.count(), penalty, alpha,
                cfg.getTaguchiReplications(), taguchiSeed, sel.label());
        Taguchi.Result aco = Taguchi.runACO(sel.pool(), sel.count(), penalty, alpha,
                cfg.getTaguchiReplications(), taguchiSeed, sel.label());

        // najlepsze rozwiazanie -> statystyki
        saveHistory("results/results_" + sel.label() + ".csv", pso.history(), aco.history());
        Stats.save(sel.pool(), sel.label(), penalty, alpha, pso, aco);

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
}
