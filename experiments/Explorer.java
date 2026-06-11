package experiments;

import java.io.*;
import model.*;

public class Explorer {

    public record Selection(Instance pool, int count, String label) {}

    public static Selection load(Config cfg, String label) throws IOException {
        String file = cfg.getDatasetFile(label);
        int count = cfg.getDatasetCount(label);
        double minAttr = cfg.getDatasetMinAttr(label);

        Instance full = Instance.loadFromCSV(file);

        // pula atrakcji spelniajacych prog atrakcyjnosci
        Instance pool = full.filter(minAttr);
        
        if (count >= pool.n) {
            count = pool.n;
            System.out.println(label + ": " + pool.n + " atrakcji spelnia prog.");
        }

        return new Selection(pool, count, label);
    }
}
