import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PGraphics;

public interface Interpretation {

    void display(Vec2 v, float phi, PGraphics layer);
    BodyDef getBodyDef();
    Shape getShape();
    FixtureDef getFixtureDef();
}