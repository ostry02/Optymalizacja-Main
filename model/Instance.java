package model;

import java.io.*;
import java.util.*;

public class Instance {

    public List<Attraction> attractions;
    public Set<String> busEdges;
    public int n;

    public Instance(List<Attraction> attractions, Set<String> busEdges) {
        this.attractions = attractions;
        this.busEdges = busEdges;
        this.n = attractions.size();
    }

    public double distance(int i,int j) {
        double dx = attractions.get(i).x - attractions.get(j).x;
        double dy = attractions.get(i).y - attractions.get(j).y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public boolean hasBus(int i, int j) {
        return busEdges.contains(i + "," + j);
    }

    // Pula atrakcji o atrakcyjnosci >= minAttr (z przemapowanymi indeksami i krawedziami).
    // To z tej puli algorytm wybiera najlepszy podzbior do odwiedzenia.
    public Instance filter(double minAttr) {
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < attractions.size(); i++)
            if (attractions.get(i).attractiveness >= minAttr) chosen.add(i);

        Map<Integer, Integer> remap = new HashMap<>();
        List<Attraction> subAttr = new ArrayList<>();
        for (int newIdx = 0; newIdx < chosen.size(); newIdx++) {
            int oldIdx = chosen.get(newIdx);
            remap.put(oldIdx, newIdx);
            subAttr.add(attractions.get(oldIdx));
        }

        Set<String> subEdges = new HashSet<>();
        for (int oi : chosen) {
            for (int oj : chosen) {
                if (oi != oj && hasBus(oi, oj))
                    subEdges.add(remap.get(oi) + "," + remap.get(oj));
            }
        }

        return new Instance(subAttr, subEdges);
    }

    public static Instance loadFromCSV(String path) throws IOException {
        List<Attraction> atr = new ArrayList<>();
        Set<String> edges = new HashSet<>();

        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int mode = 0;

        while((line = br.readLine()) != null) {
            line = line.trim();
            if(line.length() == 0) continue;

            if(line.startsWith("# Attractions")) { mode = 1; continue; }
            if(line.startsWith("# Bus Edges")) { mode = 2; continue; }
            if(line.startsWith("#")) continue;

            String[] parts = line.split(",");

            if(mode == 1) {
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                double x = Double.parseDouble(parts[2].trim());
                double y = Double.parseDouble(parts[3].trim());
                double a = Double.parseDouble(parts[4].trim());
                atr.add(new Attraction(id, name, x, y, a));
            }
            else if(mode == 2) {
                int i = Integer.parseInt(parts[0].trim());
                int j = Integer.parseInt(parts[1].trim());
                edges.add(i + "," + j);
                edges.add(j + "," + i);
            }
        }
        br.close();

        return new Instance(atr, edges);
    }

    public String toString() {
        return "Instance{n=" + n + ", buses=" + busEdges.size() + "}";
    }
}
