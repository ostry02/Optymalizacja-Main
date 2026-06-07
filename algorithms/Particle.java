package algorithms;

import java.util.*;

// pojedyncza czastka w roju
public class Particle {
    public int[] position;
    public List<int[]> velocity;
    public int[] personalBest;
    public double personalBestFit;

    public Particle(int[] pos,double fit) {
        this.position = new int[pos.length];
        for (int i=0; i<pos.length; i++) this.position[i] = pos[i];

        this.velocity = new ArrayList<>();

        this.personalBest = new int[pos.length];
        for (int i=0;i<pos.length;i++) this.personalBest[i] = pos[i];

        this.personalBestFit = fit;
    }

    public void tryUpdatePBest(int[] pos, double fit) {
        if(fit < personalBestFit) {
            for (int i=0; i<pos.length; i++) personalBest[i] = pos[i];
            personalBestFit = fit;
        }
    }
}
