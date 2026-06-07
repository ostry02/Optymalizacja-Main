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

    public String[] getInstances() {
        String[] parts = get("instances").split(",");
        for (int i=0; i<parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    // PSO
    public int getPsoParticles() { return Integer.parseInt(get("pso.particles")); }
    public int getPsoIterations() { return Integer.parseInt(get("pso.iterations")); }
    public double getPsoInertia() { return Double.parseDouble(get("pso.inertia")); }
    public double getPsoC1() { return Double.parseDouble(get("pso.c1")); }
    public double getPsoC2() { return Double.parseDouble(get("pso.c2")); }

    // ACO
    public int getAcoAnts() { return Integer.parseInt(get("aco.ants")); }
    public int getAcoIterations() { return Integer.parseInt(get("aco.iterations")); }
    public double getAcoAlpha() { return Double.parseDouble(get("aco.alpha")); }
    public double getAcoBeta() { return Double.parseDouble(get("aco.beta")); }
    public double getAcoEvaporation() { return Double.parseDouble(get("aco.evaporation")); }
    public double getAcoQ() { return Double.parseDouble(get("aco.q")); }

    // Taguchi
    public int getTaguchiReplications() { return Integer.parseInt(get("taguchi.replications")); }

    // Stats
    public int getStatsRuns() { return Integer.parseInt(get("stats.runs")); }

    private String get(String key) {
        String val = props.getProperty(key);
        return val.trim();
    }
}
