import org.jbox2d.common.Vec2;

import java.util.Random;

/**
 * Created by rszczers on 10.03.17.
 */
public class VectorField {
    private Vec2[][] block;

    public VectorField(int n) {
        block = new Vec2[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                block[i][j] = new Vec2(new Random().nextFloat() * 2 - 1, new Random().nextFloat() * 2 - 1);
                block[i][j].normalize();
                block[i][j].mulLocal(20);
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

