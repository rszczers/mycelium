import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

import Box2D.Box2DProcessing;


public class mycelium extends PApplet {
    private Box2DProcessing world;

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;
    private final static int GRID = 10;

    private boolean drawCells = true;
    private boolean drawGrids = true;
    private boolean drawVectorFields = true;
    private boolean drawLabels = true;
    private boolean calculatePhysics = true;
    private boolean toggleBoundaries = true;
    private boolean toggleForceField = false;
    private boolean drawfps = true;


    private static float[][][] cellColor = new float[GRID][GRID][3];


    /**
     * Definicje obiektów na scenie
     */
    private ArrayList<Tip> tips = new ArrayList<>();
    private ArrayList<BoundaryBox> boundaries = new ArrayList<>();

    private LSystem lsystem;
    private VectorField vf = new VectorField(GRID);

    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        frameRate(60);

        world = new Box2DProcessing(this, 10);
        world.createWorld();
        world.setGravity(0, 10);
        world.listenForCollisions();

        // określ kolory kwadratów w których pole jest takie samo.
        if (drawCells) {
            for (int i = 0; i < GRID; i++) {
                for (int j = 0; j < GRID; j++) {
                    for (int k = 0; k < 3; k++) {
                        cellColor[i][j][k] = 100 + vf.getBlock()[i][j].length();
                    }
                }
            }
        }

        // ograniczenia
        if (toggleBoundaries) {
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH/2, 5, WIDTH, 10));
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH/2, HEIGHT-5, WIDTH, 10));
            boundaries.add(new BoundaryBox(world, this,
                    5, HEIGHT/2, 10, HEIGHT));
            boundaries.add(new BoundaryBox(world, this,
                    WIDTH-5, HEIGHT/2, 10, HEIGHT));
        }
    }

    public void draw() {
        background(200);

        if  (calculatePhysics)
            world.step();

        if (drawCells)
            drawCells();
        if (drawGrids)
            drawGrid();
        if (drawVectorFields)
            drawVectorField(vf);
        if (drawLabels) {
            fill(16, 16, 153);
            textSize(32);
            text(mouseX/(WIDTH/GRID) + "; " + mouseY/(HEIGHT/GRID) + "\n" , width / 2, 60);
        }

        if (toggleBoundaries) {
            for (BoundaryBox t :
                    boundaries) {
                t.display();
            }
        }

        // Pętla aktualizująca położenie obiektów klasy Tip
        for (int i = 0; i < tips.size(); i++) {
            try {
                Tip t = tips.get(i);

                Vec2 coords = world.coordWorldToPixels(t.getBody().getPosition());
                float[] bp =  {coords.x, coords.y};
                int[] xy = c2vf((int)bp[0], (int) bp[1]);
//                System.out.println(xy[0] + "; " + xy[1]);
                if (toggleForceField)
                    t.applyForce(vf.getBlock()[xy[0]][xy[1]]);
//                System.out.println(vf.getBlock()[xy[0]][xy[1]]));
                t.display();
            } catch (ArrayIndexOutOfBoundsException e) {
                tips.get(i).killBody(); // usuń obiekt z systemu fizycznego
                tips.remove(i); //wyrzucanie obiektów, które wyleciały poza scenę
            }
        }


        if (drawfps) {
            fill(0);
            text(frameRate, 10, 60);
        }
    }

    /**
     * Dodawanie obiektów przez kliknięcie lewym przyciskiem myszki
     */
    public void mouseClicked() {
        tips.add(new Tip(world, new Vec2(mouseX, mouseY), new Ball(this, world, 20)));
    }
    public void beginContact(Contact c) {
//        Fixture f1 = c.getFixtureA();
//        Fixture f2 = c.getFixtureB();
//
//        Body b1 = f1.getBody();
//        Body b2 = f2.getBody();
//
//        Object o1 = b1.getUserData();
//        Object o2 = b2.getUserData();
//
//
//        if (o1.getClass() == Tip.class && o2.getClass() == Tip.class) {
//            Tip p1  = (Tip) o1;
//            //Tutaj można wywołać jakąś metodę p1
//            Tip p2 = (Tip) o2;
//            //Tutaj też.
//        }
    }

    /**
     * Mało mądra metoda do rysowania siatki
     */
    private void drawGrid() {
        stroke(90);
        for (int x = WIDTH/GRID; x < WIDTH; x += WIDTH/GRID) {
            line(x, 0, x, HEIGHT);
        }
        for (int y = HEIGHT/GRID; y < HEIGHT; y += HEIGHT/GRID) {
            line(0, y, WIDTH, y);
        }
        noStroke();
    }

    /**
     * Rysuje wektory pola wektorowego
     * @param vf
     */
    private void drawVectorField(VectorField vf) {
        stroke(127,0,0);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
                int[] c = vf2c(i, j);
                int x1 = c[0] + (WIDTH / (2 * GRID));
                int y1 = c[1] + (HEIGHT / (2 * GRID));
                int x2 = x1 + (int) (vf.getBlock()[i][j].x);
                int y2 = y1 + (int) (vf.getBlock()[i][j].y);
                line(x1, y1, x2, y2);

                PVector px2 = new PVector(x2, y2);
                PVector px1 = new PVector(x1, y1);
                PVector cx1 = px1.sub(px2).normalize().rotate(radians(15)).mult(10).add(px2);
                line(x2, y2, cx1.x, cx1.y);
                PVector cx2 = px1.sub(px2).rotate(radians(-30)).add(px2);
                line(x2, y2, cx2.x, cx2.y);

            }
        }
        noStroke();
    }

    /**
     * Zaznacza obszary, w których pole wektorowe zdefiniowane jest tak samo
     */
    private void drawCells() {
        rectMode(CORNER);
        int dw = WIDTH/GRID;
        int dh = HEIGHT/GRID;
        for (int x = 0; x < WIDTH; x += dw) {
            for (int y = 0; y < HEIGHT; y += dh) {
                fill(cellColor[x/dw][y/dh][0], cellColor[x/dw][y/dh][1], cellColor[x/dw][y/dh][2]);
                rect(x, y, WIDTH/GRID, HEIGHT/GRID);
            }
        }
    }

    /**
     * Zamienia współrzędne ekranu na współrzędne tablicy komórek
     * @param x
     * @param y
     * @return
     */
    private int[] c2vf(int x, int y) {
        return new int[] {x/(WIDTH/GRID), y/(HEIGHT/GRID)};
    }

    /**
     * Zamienia współrzędne tablicy komórek na współrzędne piksela lewego górnego rogu odpowiedniej komórki
     * @param x
     * @param y
     * @return
     */
    private int[] vf2c(int x, int y) {
        return new int[] {x*(WIDTH/GRID), y*(HEIGHT/GRID)};
    }
}
