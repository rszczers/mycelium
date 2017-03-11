import org.jbox2d.common.Vec2;

import java.util.Random;

/**
 * Created by rszczers on 10.03.17.
 */
public class VectorField {
    private Vec2[][] block;

    public VectorField(int n) {
        Random random = new Random();
        block = new Vec2[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                block[i][j] = new Vec2(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1).mulLocal(50);
            }
        }
    }

    public void setBlock(int[] coord, Vec2 v) {
        block[coord[0]][coord[1]] = v;
    }

    public Vec2[][] getBlock() {
        return block;
    }
}

