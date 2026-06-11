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

    public long getSeed() { return Long.parseLong(get("seed")); }
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

    // Taguchi
    public int getTaguchiReplications() { return Integer.parseInt(get("taguchi.replications")); }

    private String get(String key) {
        String val = props.getProperty(key);
        return val.trim();
    }
}
