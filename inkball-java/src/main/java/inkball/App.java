package inkball;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class App extends PApplet {

    public static final int CELLSIZE = 32;
    public static final int TOPBAR = 64;
    public static final int WIDTH = 576;
    public static final int HEIGHT = 640;
    public static final int FPS = 30;

    private static int score = 0;
    public static Random random = new Random();

    private LevelLoader levelLoader;
    private Level currentLevel;
    private Capture capture;
    private int remainingTime;
    private int spawnIntervalCounter;
    private List<Ball> balls;
    private List<String> ballColours;
    private List<String> spawnQueue;
    private int currentLevelNumber = 1;
    private boolean isPaused = false;
    private Player player;

    public PImage[] ballImages, holeImages;
    public PImage spawnerImage, wallImage, wall1Image, wall2Image, wall3Image, wall4Image, tileImage;
    private JSONObject config;

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(FPS);
        config = loadJSONObject("config.json");
        levelLoader = new LevelLoader(config);
        loadAssets();
        initializeGame();
    }

    private void loadAssets() {
        spawnerImage = loadImage("src/main/resources/inkball/entrypoint.png");
        tileImage = loadImage("src/main/resources/inkball/tile.png");
        wallImage = loadImage("src/main/resources/inkball/wall0.png");
        wall1Image = loadImage("src/main/resources/inkball/wall1.png");
        wall2Image = loadImage("src/main/resources/inkball/wall2.png");
        wall3Image = loadImage("src/main/resources/inkball/wall3.png");
        wall4Image = loadImage("src/main/resources/inkball/wall4.png");

        ballImages = new PImage[5];
        ballImages[0] = loadImage("src/main/resources/inkball/ball0.png");
        ballImages[1] = loadImage("src/main/resources/inkball/ball1.png");
        ballImages[2] = loadImage("src/main/resources/inkball/ball2.png");
        ballImages[3] = loadImage("src/main/resources/inkball/ball3.png");
        ballImages[4] = loadImage("src/main/resources/inkball/ball4.png");

        holeImages = new PImage[5];
        holeImages[0] = loadImage("src/main/resources/inkball/hole0.png");
        holeImages[1] = loadImage("src/main/resources/inkball/hole1.png");
        holeImages[2] = loadImage("src/main/resources/inkball/hole2.png");
        holeImages[3] = loadImage("src/main/resources/inkball/hole3.png");
        holeImages[4] = loadImage("src/main/resources/inkball/hole4.png");
    }

    private void initializeGame() {
        String layoutFilePath = levelLoader.getLayoutFile(currentLevelNumber);
        currentLevel = new Level(layoutFilePath, this, config, currentLevelNumber);
        capture = new Capture(this, this, config, currentLevelNumber);
        remainingTime = levelLoader.getTimeLimit(currentLevelNumber);
        ballColours = new ArrayList<>(Arrays.asList(levelLoader.getBallColours(currentLevelNumber)));
        spawnQueue = new ArrayList<>(ballColours);
        spawnIntervalCounter = levelLoader.getSpawnInterval(currentLevelNumber) * FPS;
        balls = new ArrayList<>(currentLevel.getImmediateBalls());
        player = new Player(this);
    }

    @Override
    public void draw() {
        background(211, 211, 211);
        drawGameElements();
        
        if (remainingTime > 0 && !isPaused) {
            handleGameLogic();
            if (frameCount % FPS == 0) { 
                remainingTime--;
            }
        } else if (remainingTime == 0) {
            displayTimesUp();
            return;
        }
    }

    private void displayTimesUp() {
        fill(0);
        textSize(20);   
        textAlign(CENTER);
        text("=== TIME'S UP ===", WIDTH / 2, TOPBAR / 2);
        noLoop(); 
    }

    private void drawGameElements() {
        noStroke();
        fill(211);
        rect(0, 0, WIDTH, TOPBAR);
        drawTiles();       
        drawSpawners();    
        drawWalls();       
        drawHoles();       
        displayTopBar();
    }

    private void drawSpawners() {
        List<int[]> spawners = currentLevel.getSpawners();
        for (int[] spawner : spawners) {
            int x = spawner[0] * CELLSIZE;
            int y = spawner[1] * CELLSIZE + TOPBAR + 5;
            image(spawnerImage, x, y, CELLSIZE, CELLSIZE);
        }
    }

    private void drawTiles() {
        for (int row = 0; row < HEIGHT / CELLSIZE; row++) {
            for (int col = 0; col < WIDTH / CELLSIZE; col++) {
                int x = col * CELLSIZE;
                int y = row * CELLSIZE + TOPBAR + 5;
                image(tileImage, x, y, CELLSIZE, CELLSIZE);
            }
        }
    }

    private void drawWalls() {
        List<int[]> wallPositions = currentLevel.getWallPositions();
        String[] wallColours = currentLevel.getWallColours();
        for (int i = 0; i < wallPositions.size(); i++) {
            int[] pos = wallPositions.get(i);
            int x = pos[0] * CELLSIZE;
            int y = pos[1] * CELLSIZE + TOPBAR + 5;
            switch (wallColours[i]) {
                case "gray":
                    image(wallImage, x, y, CELLSIZE, CELLSIZE);
                    break;
                case "orange":
                    image(wall1Image, x, y, CELLSIZE, CELLSIZE);
                    break;
                case "blue":
                    image(wall2Image, x, y, CELLSIZE, CELLSIZE);
                    break;
                case "green":
                    image(wall3Image, x, y, CELLSIZE, CELLSIZE);
                    break;
                case "yellow":
                    image(wall4Image, x, y, CELLSIZE, CELLSIZE);
                    break;
            }
        }
    }

    private void drawHoles() {
        for (Level.Hole hole : currentLevel.getHoles()) {
            int holeX = hole.x * CELLSIZE;
            int holeY = hole.y * CELLSIZE + TOPBAR + 5;
            PImage holeImage = holeImages[currentLevel.getColourIndex(hole.getColour())];
            image(holeImage, holeX, holeY, CELLSIZE * 2, CELLSIZE * 2);
        }
    }

    private void handleGameLogic() {
        if (!isPaused) {
            for (Iterator<Ball> iterator = balls.iterator(); iterator.hasNext(); ) {
                Ball ball = iterator.next();
                if (!ball.isCaptured()) {
                    ball.move(currentLevel.getWallPositions(), currentLevel.getWallColours(), currentLevel.getHoles(), player);
                    capture.checkCollisionWithHoles(ball, currentLevel.getHoles());
                    if (ball.getRadius() <= 0) {
                        iterator.remove();
                    } else {
                        ball.display();
                    }
                }
            }

            if (spawnIntervalCounter > 0) {
                spawnIntervalCounter--;
            } else {
                spawnNextBall();
                spawnIntervalCounter = levelLoader.getSpawnInterval(currentLevelNumber) * FPS;
            }
        } else {
            for (Ball ball : balls) {
                ball.display();
            }
        }
        player.renderLines();
    }

    private void displayTopBar() {
        fill(0);
        rect(5, 5, 180, 40);

        for (int i = 0; i < Math.min(spawnQueue.size(), 5); i++) {
            PImage ballImage = getBallImage(spawnQueue.get(i));
            image(ballImage, 10 + i * 35, 10, 30, 30);
        }

        fill(0);
        textSize(20);
        text(String.format("%.1f", spawnIntervalCounter / (float) FPS), 200, 30);
        displayScore();
        displayRemainingTime();

        if (isPaused) {
            textAlign(CENTER, CENTER);
            text("*** PAUSED ***", WIDTH / 2, 30);
        }
    }

    private void displayRemainingTime() {
        fill(0);
        textSize(20);
        if (remainingTime > 0) {
            text("Time: " + remainingTime, WIDTH - 100, 50);
        }
    }

    public void displayScore() {
        fill(0);
        textSize(20);
        text("Score: " + getScore(), WIDTH - 100, 30);
    }

    public static int getScore() {
        return score;
    }

    public static void increaseScore(int scoreToAdd) {
        score += scoreToAdd;
    }

    public static void decreaseScore(int scoreToSubtract) {
        score -= scoreToSubtract;
    }

    public void requeueBall(Ball ball) {
        spawnQueue.add(ball.getColour());
    }

    private PImage getBallImage(String colour) {
        switch (colour) {
            case "orange":
                return ballImages[1];
            case "blue":
                return ballImages[2];
            case "green":
                return ballImages[3];
            case "yellow":
                return ballImages[4];
            default:
                return ballImages[0];
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == ' ') {
            isPaused = !isPaused;
        } else if (event.getKey() == 'r') {
            initializeGame();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == LEFT) {
            player.startNewLine();
        } else if (e.getButton() == RIGHT) {
            player.removeLineAt(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getButton() == LEFT && e.getY() > TOPBAR && e.getY() < HEIGHT && e.getX() > 0 && e.getX() < WIDTH) {
            player.addLinePoint(e.getX(), e.getY());
        }
    }

    public void spawnNextBall() {
        if (!spawnQueue.isEmpty()) {
            String nextColour = spawnQueue.remove(0);
            PImage ballImage = getBallImage(nextColour);
            List<int[]> spawners = currentLevel.getSpawners();
            int[] spawner = spawners.get(random.nextInt(spawners.size()));
            Ball newBall = new Ball(spawner[0] * CELLSIZE, spawner[1] * CELLSIZE + TOPBAR, nextColour, ballImage, this);
            balls.add(newBall);
        }
    }

    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
