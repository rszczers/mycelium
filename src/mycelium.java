import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import java.util.ArrayList;

import Box2D.Box2DProcessing;
import processing.opengl.PShader;


public class mycelium extends PApplet {
    private Box2DProcessing world;

    private final static int WIDTH = 1200;
    private final static int HEIGHT = 800;
    private final static int GRID = 20;

    private boolean drawCells = false;
    private boolean drawGrids = true;
    private boolean drawVectorFields = false;
    private boolean drawLabels = true;
    private boolean calculatePhysics = true;
    private boolean toggleBoundaries = false;
    private boolean toggleForceField = false;
    private boolean toggleGravity = true;
    private boolean drawfps = true;


    private static float[][][] cellColor = new float[GRID][GRID][3];

    /**
     * Definicje obiektów na scenie
     */
//    private ArrayList<Tip> tips = new ArrayList<>();
    private Fungus fungi;
    private ArrayList<BoundaryBox> boundaries = new ArrayList<>();
    private VectorField vf = new VectorField(GRID);
    private ArrayList<Tip> tcoll;


    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }
    public void settings() {
        size(WIDTH, HEIGHT, P3D);
    }

    public void setup() {
        frameRate(60);
        background(0);

        world = new Box2DProcessing(this, 10);
        world.createWorld();
        if (toggleGravity) {
            world.setGravity(0, 10);
        } else {
            world.setGravity(0, 0);
        }

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

        if (drawCells)
            drawCells();
        if (drawGrids)
            drawGrid();
        if (drawVectorFields)
            drawVectorField(vf);

        fungi = new Fungus(world, this, new Vec2(width/2, height/2));

    }

    public void draw() {

        if  (calculatePhysics)
            world.step();

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


        tcoll = fungi.getTips();

        for (int i = 0; i < tcoll.size(); i++) {
            try {
                Tip t = tcoll.get(i);
                Vec2 coords = world.coordWorldToPixels(t.getBody().getPosition());
                float[] bp =  {coords.x, coords.y};
                int[] xy = c2vf((int)bp[0], (int)bp[1]);
                if (toggleForceField)
                    t.applyForce(vf.getBlock()[xy[0]][xy[1]]);
                t.display();
            } catch (ArrayIndexOutOfBoundsException e) {
                tcoll.get(i).killBody(); // usuń obiekt z systemu fizycznego
                tcoll.remove(i); //wyrzucanie obiektów, które wyleciały poza scenę
            }
        }

        if (drawfps) {
            fill(0);
            text(frameRate, 10, 60);
        }

        fungi.grow();
        fungi.display();

    }


    /**
     * Dodawanie obiektów przez kliknięcie lewym przyciskiem myszki
     */
    public void mouseClicked() {
        fungi.addRoot(new Vec2(mouseX, mouseY));
//        tips.add(new Tip(world, , new Ball(this, world, 20)));
    }


    public void beginContact(Contact c) {

        Fixture f1 = c.getFixtureA();
        Fixture f2 = c.getFixtureB();

        Body b1 = f1.getBody();
        Body b2 = f2.getBody();

        Object o1 = b1.getUserData();
        Object o2 = b2.getUserData();


        if (o1.getClass() == CollisionShape.class && o2.getClass() == CollisionShape.class) {
            CollisionShape p1  = (CollisionShape) o1;
            //Tutaj można wywołać jakąś metodę p1
            CollisionShape p2 = (CollisionShape) o2;
            System.out.println("CCC");
            //Tutaj też.
        }

        if (o1.getClass() == Tip.class) {
            Tip p1  = (Tip) o1;
            //Tutaj można wywołać jakąś metodę p1
            p1.getOwner().kill();
            System.out.println("Tip-tip1");
            //Tutaj też.
        }
        if (o2.getClass() == Tip.class) {
            Tip p1  = (Tip) o2;
            //Tutaj można wywołać jakąś metodę p1
            p1.getOwner().kill();
            System.out.println("Tip-tip2");
            //Tutaj też.
        }

//        if (o1.getClass() == Tip.class && o2.getClass() == CollisionShape.class) {
//            Tip t1  = (Tip) o1;
//            //Tutaj można wywołać jakąś metodę p1
//            CollisionShape t2 = (CollisionShape) o2;
//            //Tutaj też.
//            System.out.println("Tip-Shape");
//        }
    }

    /**
     * Mało mądra metoda do rysowania siatki
     */
    private void drawGrid() {
        stroke(90);
        strokeWeight(1);
        for (int x = WIDTH/GRID; x < WIDTH; x += WIDTH/GRID) {
            line(x, 0, x, HEIGHT);
        }
        for (int y = HEIGHT/GRID; y < HEIGHT; y += HEIGHT/GRID) {
            line(0, y, WIDTH, y);
        }
    }

    /**
     * Rysuje wektory pola wektorowego
     * @param vf
     */
    private void drawVectorField(VectorField vf) {
        stroke(1,0,0);
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
                strokeWeight(1);
                stroke(1);
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
