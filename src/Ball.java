import Box2D.Box2DProcessing;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PShader;

/**
 * Przykładowa klasa, definiuje wygląd obiektów Ball.
 */
public class Ball implements Interpretation {
    private int radius;
    private BodyDef bd;
    private CircleShape ps;
    private FixtureDef fd;
    private PApplet context;

    private PShader pointShader;
    private PShader newShader;
    private PImage cloud1;
    private PImage cloud2;
    private PImage cloud3;


    public Ball(PApplet context, Box2DProcessing world, int radius) {
        this.context = context;
        this.radius = radius;

        // Definicja BodyDef
        bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
//        bd.fixedRotation = true;
        bd.linearDamping = 0.5f;
        bd.angularDamping = 0.9f;
//        bd.bullet = true;

        // Definicja kształtu
        ps = new CircleShape();
        ps.setRadius(world.scalarPixelsToWorld(radius/2));

        // Definicja dowiązania ciała do kształtu
        fd = new FixtureDef();
        fd.shape = ps;
        fd.density = 0.7f;
        fd.friction = 0.3f;
        fd.restitution = 0.0f;

        //pointShader = context.loadShader("spritefrag.glsl", "spritevert.glsl");
        //pointShader.set("weight", 20.0f);
//        cloud1 = context.loadImage("cloud1.png");
//        cloud2 = context.loadImage("cloud2.png");
//        cloud3 = context.loadImage("cloud3.png");
        //pointShader.set("sprite", cloud3);

    }

    public void display(Vec2 v, float phi) {
        context.pushMatrix();
        context.translate(v.x, v.y);
        context.rotate(-phi);

            context.fill(255, 255, 255);
            context.ellipse(0, 0, radius, radius);
            context.stroke(0);
            context.strokeWeight(2);
            context.fill(0, 0, 255);
            context.line(0, 0, radius / 2, 0);
            context.strokeWeight(1);
//        context.noStroke();

        // Tutaj jest fragment chmurkowego szejdera
//        context.shader(pointShader, PApplet.POINTS);
//        context.strokeWeight(20);
//        context.strokeCap(PApplet.SQUARE);
//        context.stroke(255,255);
//        context.point(0, 0);
//        context.strokeWeight(10);
        //context.resetShader();
        context.popMatrix();

    }
    public int getRadius() {return radius;}

    public BodyDef getBodyDef() {
        return this.bd;
    }

    public CircleShape getShape() {
        return ps;
    }

    public FixtureDef getFixtureDef() {
        return fd;
    }

}
