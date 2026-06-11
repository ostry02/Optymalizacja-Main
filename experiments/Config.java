package experiments;

import java.io.*;
import java.util.*;

public class Config {

    private final Properties props;

    public Config(String path) throws IOException {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }
    }

    public double getPenalty() { return Double.parseDouble(get("penalty")); }
    public double getAlpha() { return Double.parseDouble(get("alpha")); }

    // nazwy datasetow 
    public String[] getDatasets() {
        String[] parts = get("datasets").split(",");
        for (int i=0; i<parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    public String getDatasetFile(String label) { return get("dataset." + label + ".file"); }
    public int getDatasetCount(String label) { return Integer.parseInt(get("dataset." + label + ".count")); }
    public double getDatasetMinAttr(String label) { return Double.parseDouble(get("dataset." + label + ".minAttr")); }

    // PSO warianty
    public double getPBus()        { return Double.parseDouble(get("pso.pBus")); }
    public double getPenaltyMin()  { return Double.parseDouble(get("pso.penaltyMin")); }
    public double getPenaltyMax()  { return Double.parseDouble(get("pso.penaltyMax")); }

    // Taguchi
    public int getTaguchiReplications() { return Integer.parseInt(get("taguchi.replications")); }

    // liczba niezaleznych przebiegow do statystyk (mean/std/min/max)
    public int getStatsRuns() { return Integer.parseInt(get("stats.runs")); }

    private String get(String key) {
        String val = props.getProperty(key);
        return val.trim();
    }
}
