package experiments;

import java.io.*;
import java.util.*;
import model.*;
import algorithms.*;

public class Explorer {

    public static void run(Config cfg, double penalty, double alpha, long seed) throws IOException {
        Scanner sc = new Scanner(System.in);
        Boolean i = true;

        while (i==true) {
            System.out.print("\nWybierz dataset [50/200]");
            String choice = sc.nextLine().trim();

            String file;
            if (choice.equals("50")) file = "data/instance_50.csv";
            else file = "data/instance_200.csv";

            Instance full = Instance.loadFromCSV(file);

            System.out.print("Ile atrakcji chcesz zobaczyc: ");
            int count = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Minimalna atrakcyjnosc (np. 6.0): ");
            double minAttr = Double.parseDouble(sc.nextLine().trim());

            // pula atrakcji spelniajacych prog atrakcyjnosci
            Instance pool = full.filter(minAttr);
            if (pool.n == 0) {
                System.out.printf("Brak atrakcji o atrakcyjnosci >= %.1f.%n", minAttr);
                continue;
            }
            if (count > pool.n) {
                count = pool.n;
                System.out.println(pool.n +" atrakcji spelnia");
            }

            optimize(pool, count, cfg, penalty, alpha, seed);
            i= false;
        }
    }

    private static void optimize(Instance pool, int count, Config cfg,
                                 double penalty, double alpha, long seed) {
        PSO pso = new PSO(pool, penalty, alpha,
                cfg.getPsoParticles(), cfg.getPsoIterations(),
                cfg.getPsoInertia(), cfg.getPsoC1(), cfg.getPsoC2(), new Random(seed), count);
        int[] psoRoute = pso.run();

        ACO aco = new ACO(pool, penalty, alpha,
                cfg.getAcoAnts(), cfg.getAcoIterations(),
                cfg.getAcoAlpha(), cfg.getAcoBeta(), cfg.getAcoEvaporation(), cfg.getAcoQ(),
                new Random(seed), count);
        int[] acoRoute = aco.run();

        int[] best = pso.getBestFitness() <= aco.getBestFitness() ? psoRoute : acoRoute;

        System.out.printf("%nPSO fitness = %.4f, ACO fitness = %.4f%n",
                pso.getBestFitness(), aco.getBestFitness());

    }
}
