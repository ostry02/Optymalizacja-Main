package experiments;

import java.io.*;
import java.util.*;
import model.*;

public class Explorer {

    // wybor uzytkownika: pula atrakcji po filtrze, liczba odwiedzanych, etykieta datasetu
    public record Selection(Instance pool, int count, String label) {}

    public static Selection run() throws IOException {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nWybierz dataset:");
            System.out.println("  1) 100 dense");
            System.out.println("  2) 100 sparse");
            System.out.println("  3) 200 dense");
            System.out.println("  4) 200 sparse");
            System.out.println("  5) 500 dense");
            System.out.println("  6) 500 sparse");
            System.out.print("> ");
            String choice = sc.nextLine().trim();

            String file, label;
            switch (choice) {
                case "1": file = "data/instance_100_dense.csv";  label = "100_dense";  break;
                case "2": file = "data/instance_100_sparse.csv"; label = "100_sparse"; break;
                case "3": file = "data/instance_200_dense.csv";  label = "200_dense";  break;
                case "4": file = "data/instance_200_sparse.csv"; label = "200_sparse"; break;
                case "5": file = "data/instance_500_dense.csv";  label = "500_dense";  break;
                case "6": file = "data/instance_500_sparse.csv"; label = "500_sparse"; break;
                default:
                    System.out.println("Nieprawidlowy wybor.");
                    continue;
            }

            Instance full = Instance.loadFromCSV(file);

            System.out.print("Ile atrakcji chcesz odwiedzic: ");
            int count = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Minimalna atrakcyjnosc (np. 6.0): ");
            double minAttr = Double.parseDouble(sc.nextLine().trim());

            // pula atrakcji spelniajacych prog atrakcyjnosci
            Instance pool = full.filter(minAttr);
            if (pool.n == 0) {
                System.out.printf("Brak atrakcji o atrakcyjnosci >= %.1f.%n", minAttr);
                continue;
            }
            if (count >= pool.n) {
                count = pool.n;
                System.out.println(pool.n + " atrakcji spelnia prog.");
            }

            return new Selection(pool, count, label);
        }
    }
}
