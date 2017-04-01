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

public class CollisionShape {
    private int[] color;
    private PApplet context;
    private Box2DProcessing world;
    private PShape dShape;
    private PolygonShape pShape;
    private Body body;
    private BodyDef bd;
    private Vec2[] top;
    private Vec2[] bottom;

    public CollisionShape(PApplet context, Box2DProcessing world, Vec2[] last, Vec2[] next, int[] color) {
        this.context = context;
        this.world = world;
        this.color = color;
        this.dShape = createDisplayShapes(last, next);
        this.pShape = createCollisionShape(last, next);
        this.top = next.clone();
        this.bottom = last.clone();

        this.bd = new BodyDef();
        bd.type = BodyType.STATIC;
        Vec2 middle = next[0].add(top[1]).mul(0.5f);
        bd.position.set(middle);
        this.body = world.createBody(bd);
        this.body.createFixture(pShape, 1.0f);

        body.setUserData(this);
    }
    /**
     * Generuj shape dla kolizji
     * @return
     */
    private PolygonShape createCollisionShape(Vec2[] last, Vec2[] next) {
        Vec2 middle = next[0].add(last[1]).mul(0.5f);
        Vec2[] vertices = {
                last[0].sub(middle),
                last[1].sub(middle),
                next[1].sub(middle),
                next[0].sub(middle),
        };
//        System.out.println("top[0] = " + last[0] + ", top[1] = " + last[1] + ", next[0] = " + next[0] + ", next[1] = " + next[1]);
        PolygonShape ps = new PolygonShape();
        ps.set(vertices, vertices.length);
        return ps;
    }

    /**
     * Wygeneruj PShape z czterech punktów
     * @return
     */
    private PShape createDisplayShapes(Vec2[] last_world, Vec2[] next_world) {
        Vec2[] last = last_world.clone();
        Vec2[] next = next_world.clone();

        for (int i = 0; i < last.length; i++) {
            last[i] = world.coordWorldToPixels(last[i]);
            next[i] = world.coordWorldToPixels(next[i]);
//            System.out.println("top[" + i + "] = " + top[i] + ", \t " + "next[" + i + "] = " + next[i]);
        }

        PShape ps = context.createShape();
        context.pushMatrix();
        ps.beginShape();
            ps.fill(color[0], color[1], color[2], 127);
            ps.stroke(0);
            ps.strokeWeight(1);
            ps.vertex(next[0].x, next[0].y);
            ps.vertex(next[1].x, next[1].y);
            ps.vertex(last[1].x, last[1].y);
            ps.vertex(last[0].x, last[0].y);
//                ps.setVisible(false);
        ps.endShape(PApplet.CLOSE);
        context.shape(ps);
        context.popMatrix();
        return ps;
    }

    public Vec2[] getLeftVerticies(){
        Vec2[] left_vec = new Vec2[2];
        left_vec[0] = world.coordWorldToPixels(top[0]);
        left_vec[1] = world.coordWorldToPixels(bottom[0]);
        return left_vec;
    }

    public Vec2[] getRightVerticies(){
        Vec2[] right_vec = new Vec2[2];
        right_vec[0] = world.coordWorldToPixels(top[0]);
        right_vec[1] = world.coordWorldToPixels(bottom[0]);
        return right_vec;
    }

    public Vec2[] getLeft(){
        Vec2[] left = new Vec2[2];
        left[0] = bottom[0];
        left[1] = top[0];
        return left;
    }

    public Vec2[] getRight(){
        Vec2[] right = new Vec2[2];
        right[0] = top[1];
        right[1] = bottom[1];
        return right;
    }

    public int[] getColor() {
        return color;
    }

    public PolygonShape getCollisionShape() {
        return pShape;
    }

    public PShape getDisplayShape() {
        return dShape;
    }

    public Body getBody() {
        return body;
    }

    public Vec2 getPosition() {
        return body.getPosition();
    }

    public Vec2[] getTop() {
        return top;
    }

    public Vec2[] getBottom() {
        return bottom;
    }

    public void display() {
        context.shape(dShape);
    }
}
