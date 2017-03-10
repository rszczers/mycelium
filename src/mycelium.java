import processing.core.PApplet;
import processing.core.PVector;

public class mycelium extends PApplet {
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
    private static int GRID = 8;
    private boolean drawCells = true;
    private boolean drawGrids = true;
    private boolean drawVectorFields = true;

    private static float[][][] cellColor = new float[GRID][GRID][3];

    LSystem lsystem;
    VectorField vf = new VectorField(GRID);

    public static void main(String[] args) {
        PApplet.main("mycelium", args);
    }
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public void setup() {
        frameRate(30);
        for (int i = 0; i < GRID; i++) {
            for (int j = 0; j < GRID; j++) {
                for (int k = 0; k < 3; k++) {
                    cellColor[i][j][k] = 100 + vf.getBlock()[i][j].dist(new PVector(0, 0, 0));
                }
            }
        }
    }

    public void draw() {
        background(180);
        //vf.setBlock(new int[] {mouseX/(WIDTH/GRID), mouseY/(HEIGHT/GRID)}, PVector.random2D());

        if (drawCells)
            drawCells();
        if (drawGrids)
            drawGrid();
        if (drawVectorFields)
            drawVectorField(vf);

        fill(0, 102, 153);
        text(mouseX/(WIDTH/GRID) + "; " + mouseY/(HEIGHT/GRID) + "\n" , width / 2, 60);
    }

    private void drawGrid() {
        stroke(15);
        for (int x = WIDTH/GRID; x < WIDTH; x += WIDTH/GRID) {
            line(x, 0, x, HEIGHT);
        }
        for (int y = HEIGHT/GRID; y < HEIGHT; y += HEIGHT/GRID) {
            line(0, y, WIDTH, y);
        }
    }

    private void drawVectorField(VectorField vf) {
        stroke(127,0,0);
        for (int i = 0; i < vf.getBlock().length; i++) {
            for (int j = 0; j < vf.getBlock().length; j++) {
                int[] c = vf2c(i, j);
                int x1 = c[0] + (WIDTH / (2 * GRID));
                int y1 = c[1] + (HEIGHT / (2 * GRID));
                int x2 = x1 + (int) vf.getBlock()[i][j].x;
                int y2 = y1 + (int) vf.getBlock()[i][j].y;
                line(x1, y1, x2, y2);
            }
        }
        noStroke();
    }

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

    private int[] c2vf(int x, int y) {
        return new int[] {x/(WIDTH/GRID), y/(HEIGHT/GRID)};
    }


    private int[] vf2c(int x, int y) {
        return new int[] {x*(WIDTH/GRID), y*(HEIGHT/GRID)};
    }

}
