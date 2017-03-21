import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import processing.core.PApplet;
import processing.core.PShape;

/**
 * Created by rszczers on 21.03.17.
 */

public class collisionShape {
    private int[] color;
    private PApplet context;
    private Box2DProcessing world;
    private PShape dShape;
    private PolygonShape pShape;
    private Body body;
    private BodyDef bd;

    public collisionShape(PApplet context, Box2DProcessing world, Vec2[] last, Vec2[] next) {
        this.context = context;
        this.world = world;
        this.color = new int[] {127, 127, 127, 127};
        this.dShape = createDisplayShapes(last, next);
        this.pShape = createCollisionShape(last, next);

        this.bd = new BodyDef();
        bd.type = BodyType.STATIC;
        Vec2 middle = next[0].add(last[1]).mul(0.5f);
        bd.position.set(world.coordPixelsToWorld(middle));
        this.body = world.createBody(bd);
    }

    /**
     * Generuj shape dla kolizji
     * @return
     */
    private PolygonShape createCollisionShape(Vec2[] last, Vec2[] next) {
        Vec2[] vertices = {
                next[0],
                last[1],
                last[0],
                next[1]
        };
        PolygonShape ps = new PolygonShape();
        ps.set(vertices, vertices.length);
        return ps;
    }

    /**
     * Wygeneruj PShape z czterech punktów
     * @return
     */
    private PShape createDisplayShapes(Vec2[] last, Vec2[] next) {
        PShape ps = context.createShape();
        context.pushMatrix();
        ps.beginShape();
            ps.fill(color[0], color[1], color[2], color[3]);
            ps.stroke(0);
            ps.strokeWeight(0);
            ps.vertex(next[1].x, next[1].y);
            ps.vertex(next[0].x, next[0].y);
            ps.vertex(last[0].x, last[0].y);
            ps.vertex(last[1].x, last[1].y);
//                ps.setVisible(false);
        ps.endShape(PApplet.CLOSE);
        context.shape(ps);
        context.popMatrix();
        return ps;
    }

    public int[] getColor() {
        return color;
    }

    public PolygonShape getpShape() {
        return pShape;
    }

    public PShape getdShape() {
        return dShape;
    }

    public Body getBody() {
        return body;
    }
}
