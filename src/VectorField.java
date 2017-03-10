import processing.core.PVector;

/**
 * Created by rszczers on 10.03.17.
 */
public class VectorField {
    private PVector[][] block;

    public VectorField(int n) {
        block = new PVector[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                block[i][j] = PVector.random2D();
            }
        }
    }

    public void setBlock(int[] coord, PVector v) {
        block[coord[0]][coord[1]] = v;
    }

    public PVector[][] getBlock() {
        return block;
    }
}

