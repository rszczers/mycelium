import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PVector;

import java.util.ArrayList;

public class mycelium extends PApplet {
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
    private static int GRID = 8;

    private boolean drawCells = true;
    private boolean drawGrids = true;
    private boolean drawVectorFields = true;
    private boolean drawLabels = true;

    private static float[][][] cellColor = new float[GRID][GRID][3];

    private ArrayList<Mover> movers = new ArrayList<Mover>();

    LSystem lsystem;
    VectorField vf = new VectorField(GRID);

    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        frameRate(60);
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                for (int k = 0; k < 3; k++) {
                    cellColor[i][j][k] = 100 + vf.getBlock()[i][j].dist(new PVector(0, 0, 0));
                }
            }
        }
    }

    public void draw() {
        background(200);
        //vf.setBlock(new int[] {mouseX/(WIDTH/GRID), mouseY/(HEIGHT/GRID)}, PVector.random2D());

        if (drawCells)
            drawCells();
        if (drawGrids)
            drawGrid();
        if (drawVectorFields)
            drawVectorField(vf);
        if (drawLabels) {
            fill(0, 102, 153);
            textSize(32);
            text(mouseX/(WIDTH/GRID) + "; " + mouseY/(HEIGHT/GRID) + "\n" , width / 2, 60);
        }

        // Pętla aktualizująca położenie obiektów klasy Mover
        for (int i = 0; i < movers.size(); i++) {
            try {
                Mover t = movers.get(i);
                int[] xy = c2vf((int) t.getLocation().x, (int) t.getLocation().y);
                t.applyForce(vf.getBlock()[xy[0]][xy[1]]);
                t.update();
                t.show();
            } catch (ArrayIndexOutOfBoundsException e) {
                movers.remove(i); //wyrzucanie obiektów, które wyleciały poza scenę
            }
        }
    }

    /**
     * Dodawanie obiektów przez kliknięcie lewym przyciskiem myszki
     */
    public void mouseClicked() {
        movers.add(new Mover(new PVector(mouseX, mouseY), 50, new Ball(this, 20)));
    }

    /**
     * Mało mądra metoda do rysowania siatki
     */
    private void drawGrid() {
        stroke(15);
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
        stroke(127,0,0);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
                int[] c = vf2c(i, j);
                int x1 = c[0] + (WIDTH / (2 * GRID));
                int y1 = c[1] + (HEIGHT / (2 * GRID));
                int x2 = x1 + (int) (vf.getBlock()[i][j].x * 50);
                int y2 = y1 + (int) (vf.getBlock()[i][j].y * 50);
                line(x1, y1, x2, y2);

                PVector px2 = new PVector(x2, y2);
                PVector px1 = new PVector(x1, y1);
                PVector cx1 = px1.sub(px2).normalize().rotate(radians(15)).mult(15).add(px2);
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
