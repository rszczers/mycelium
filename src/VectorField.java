import Box2D.Box2DProcessing;
import org.jbox2d.common.Vec2;

import java.util.Random;

/**
 * Created by rszczers on 10.03.17.
 */
public class VectorField {
    private Vec2[][] block;
    private Box2DProcessing world;

    public VectorField(int n, Box2DProcessing world) {
        block = new Vec2[n][n];
        this.world = world;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                block[i][j] = new Vec2(0.0f, -20.0f);
//                block[i][j] = new Vec2(new Random().nextFloat() * 2 - 1, new Random().nextFloat() * 2 - 1);
//                block[i][j] = new Vec2(new Random().nextFloat() * 2 - 1, new Random().nextFloat() * 2 - 1);
//                block[i][j].normalize();
//                block[i][j].mulLocal(20);
            }
        }
    }

    public void setBlock(int[] coord, Vec2 v) {
        block[coord[0]][coord[1]] = v;
    }

    public void standardBlock(int[] coord, Vec2 v, float forceValue){
        v.normalize();
        v.mulLocal(forceValue);
        block[coord[0]][coord[1]].addLocal(v);
//        block[coord[0]][coord[1]].normalize();
//        block[coord[0]][coord[1]].mulLocal(forceValue);
    }

    public Vec2[][] getBlock() {
        return block;
    }
}

