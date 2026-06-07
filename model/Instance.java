package model;

import java.io.*;
import java.util.*;

public class Instance {

    public List<Attraction> attractions;
    public Set<Long> busEdges;
    public int n;

    public Instance(List<Attraction> attractions, Set<Long> busEdges) {
        this.attractions = attractions;
        this.busEdges = busEdges;
        this.n = attractions.size();
    }

    public double distance(int i,int j) {
        double dx = attractions.get(i).x - attractions.get(j).x;
        double dy = attractions.get(i).y - attractions.get(j).y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    // czy jest autobus miedzy i a j
    public boolean hasBus(int i,int j) {
        int lo, hi;
        if(i < j) { lo = i; hi = j; } else { lo = j; hi = i; }
        return busEdges.contains((long)lo * 100000 + hi);
    }

    public static Instance loadFromCSV(String path) throws IOException {
        List<Attraction> atr = new ArrayList<>();
        Set<Long> edges = new HashSet<>();

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
                int lo, hi;
                if(i < j) { lo = i; hi = j; } else { lo = j; hi = i; }
                edges.add((long)lo * 100000 + hi);
            }
        }
        br.close();

        return new Instance(atr, edges);
    }

    public String toString() {
        return "Instance{n=" + n + ", buses=" + busEdges.size() + "}";
    }
}
