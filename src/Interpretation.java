import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;

public interface Interpretation {
    BodyDef bd = new BodyDef();

    void display(Vec2 v, float phi);
    BodyDef getBodyDef();
    Shape getShape();
    FixtureDef getFixtureDef();
}