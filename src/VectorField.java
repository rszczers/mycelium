import processing.core.PApplet;
/**
 * Created by rszczers on 10.03.17.
 */
public class VectorField {
    private float[][] block;

    public VectorField(int n) {
       block = new float[n][n];
    }

    public void setBlock(int[] coord, float v) {
        block[coord[0]][coord[1]] = v;
    }

    public float[][] getBlock() {
        return block;
    }
}

