import processing.core.PApplet;

/**
 * Przykładowa klasa, definiuje wygląd obiektów Ball.
 */
public class Ball implements Interpretation {
    private int radius;
    PApplet context;

    public Ball(PApplet context, int radius) {
        this.context = context;
        this.radius = radius;
    }

    public void show(int x, int y) {
        context.fill(255, 60, 0);
        context.ellipse(x, y, radius, radius);
        context.noStroke();
    }
}
