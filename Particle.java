import java.util.*;

// pojedyncza czastka w roju
public class Particle {
    int[] position;
    List<int[]> velocity;
    int[] personalBest;
    double personalBestFit;

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
