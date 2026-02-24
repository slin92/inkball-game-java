package inkball;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;

public class Level {
    private char[][] layout;
    private int rows;
    private int cols;
    private List<int[]> spawners;
    private List<Hole> holes;
    private List<Ball> immediateBalls;

    public Level(String layoutFilePath, PApplet p, JSONObject config, int levelNumber) {
        spawners = new ArrayList<>();
        holes = new ArrayList<>();
        immediateBalls = new ArrayList<>();
        loadLevel(layoutFilePath, p);
    }

    public class Hole {
        public int x, y;
        private String colour;

        public Hole(int x, int y, String colour) {
            this.x = x;
            this.y = y;
            this.colour = colour;
        }

        public String getColour() {
            return this.colour;
        }
    }

    private void loadLevel(String layoutFilePath, PApplet p) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(layoutFilePath));
            String line;
            int row = 0;
            List<char[]> levelRows = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                levelRows.add(line.toCharArray());
                row++;
            }

            rows = row;
            cols = levelRows.get(0).length;
            layout = new char[rows][cols];

            for (int i = 0; i < rows; i++) {
                layout[i] = levelRows.get(i);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parseTiles(p);
    }

    private void parseTiles(PApplet p) {
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                char tile = layout[row][col];

                if (tile == 'S') {
                    spawners.add(new int[]{col, row});
                } else if (tile == 'H') {
                    if (col + 1 < layout[row].length && Character.isDigit(layout[row][col + 1])) {
                        String holeColour = getBallColourString(Character.getNumericValue(layout[row][col + 1]));
                        holes.add(new Hole(col, row, holeColour));
                        col++;
                    }
                } else if (tile == 'B') {
                    if (col + 1 < layout[row].length && Character.isDigit(layout[row][col + 1])) {
                        int ballColour = Character.getNumericValue(layout[row][col + 1]);
                        PImage ballImage = getBallImage(ballColour, p);
                        int ballX = col * App.CELLSIZE;
                        int ballY = row * App.CELLSIZE + App.TOPBAR;

                        Ball newBall = new Ball(ballX, ballY, getBallColourString(ballColour), ballImage, p);
                        immediateBalls.add(newBall);
                    }
                }
            }
        }
    }

    private String getBallColourString(int colour) {
        if (colour == 1) return "orange";
        else if (colour == 2) return "blue";
        else if (colour == 3) return "green";
        else if (colour == 4) return "yellow";
        else return "gray";
    }

    private PImage getBallImage(int colour, PApplet p) {
        if (colour == 1) return p.loadImage("src/main/resources/inkball/ball1.png");
        else if (colour == 2) return p.loadImage("src/main/resources/inkball/ball2.png");
        else if (colour == 3) return p.loadImage("src/main/resources/inkball/ball3.png");
        else if (colour == 4) return p.loadImage("src/main/resources/inkball/ball4.png");
        else return p.loadImage("src/main/resources/inkball/ball0.png");
    }

    public int getColourIndex(String colour) {
        if (colour.equals("orange")) return 1;
        else if (colour.equals("blue")) return 2;
        else if (colour.equals("green")) return 3;
        else if (colour.equals("yellow")) return 4;
        else return 0;
    }

    public List<int[]> getSpawners() {
        return spawners;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public List<Ball> getImmediateBalls() {
        return immediateBalls;
    }

    public List<int[]> getWallPositions() {
        List<int[]> wallPositions = new ArrayList<>();
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                char tile = layout[row][col];
                if (tile == 'X' || tile == '1' || tile == '2' || tile == '3' || tile == '4') {
                    wallPositions.add(new int[]{col, row});
                }
            }
        }
        return wallPositions;
    }

    public String[] getWallColours() {
        List<String> wallColours = new ArrayList<>();
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                char tile = layout[row][col];
                if (tile == '1') wallColours.add("orange");
                else if (tile == '2') wallColours.add("blue");
                else if (tile == '3') wallColours.add("green");
                else if (tile == '4') wallColours.add("yellow");
                else if (tile == 'X') wallColours.add("gray");
            }
        }
        return wallColours.toArray(new String[0]);
    }
}
