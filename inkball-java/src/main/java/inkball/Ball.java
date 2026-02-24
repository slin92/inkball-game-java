package inkball;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;

public class Ball {
    private float x, y, vx, vy;
    private int radius = 16;
    private String colour;
    private PImage image;
    private PApplet p;
    private boolean isCaptured = false;

    public Ball(float x, float y, String colour, PImage image, PApplet p) {
        this.x = x - 15;
        this.y = y - 10;
        this.colour = colour;
        this.image = image;
        this.p = p;
        this.vx = (p.random(1) < 0.5) ? 2 : -2;
        this.vy = (p.random(1) < 0.5) ? 2 : -2;
    }

    public void move(List<int[]> wallPositions, String[] wallColours, List<Level.Hole> holes, Player player) {
        if (isCaptured) return;

        x += vx;
        y += vy;

        // Handle screen edge collision
        if (x <= 0 || x >= App.WIDTH - radius * 2) {
            vx = -vx;
        }
        if (y <= App.TOPBAR || y >= App.HEIGHT - radius * 2) {
            vy = -vy;
        }

        // Handle collision with walls
        for (int i = 0; i < wallPositions.size(); i++) {
            int[] pos = wallPositions.get(i);
            int wallX = pos[0] * App.CELLSIZE - 15;
            int wallY = pos[1] * App.CELLSIZE + App.TOPBAR - 10;

            if (isCollidingWithTile(wallX, wallY)) {
                resolveWallCollision(wallX, wallY, wallColours[i]);
            }
        }

        // Handle collision with player-drawn lines
        checkCollisionWithLines(player);
    }

    private boolean isCollidingWithTile(int tileX, int tileY) {
        return x + radius > tileX && x - radius < tileX + App.CELLSIZE &&
               y + radius > tileY && y - radius < tileY + App.CELLSIZE;
    }

    private void resolveWallCollision(int wallX, int wallY, String wallColour) {
        float overlapX = Math.min(x + radius - wallX, wallX + App.CELLSIZE - (x - radius));
        float overlapY = Math.min(y + radius - wallY, wallY + App.CELLSIZE - (y - radius));

        if (overlapX < overlapY) {
            vx = -vx;
            x += (vx > 0) ? overlapX : -overlapX;
        } else {
            vy = -vy;
            y += (vy > 0) ? overlapY : -overlapY;
        }

        // Only walls should change the ball's color
        if (!wallColour.equals("gray")) {
            this.colour = wallColour;
            this.image = getBallImageByColour(colour);
        }
    }

    private void checkCollisionWithLines(Player player) {
        List<List<float[]>> linesToRemove = new ArrayList<>();

        for (List<float[]> line : player.getLines()) {
            for (int i = 0; i < line.size() - 1; i++) {
                float[] p1 = line.get(i);
                float[] p2 = line.get(i + 1);

                if (isCollidingWithLine(p1, p2)) {
                    float[] normal = calculateNormal(p1[0], p1[1], p2[0], p2[1]);
                    float[] newVelocity = reflect(new float[]{vx, vy}, normal);
                    vx = newVelocity[0];
                    vy = newVelocity[1];
                    linesToRemove.add(line);
                    break;
                }
            }
        }

        // Remove lines that have been hit
        for (List<float[]> line : linesToRemove) {
            player.removeLine(line);
        }
    }

    private boolean isCollidingWithLine(float[] p1, float[] p2) {
        float ballNextX = this.x + this.vx;
        float ballNextY = this.y + this.vy;

        float distanceP1Ball = distanceToSegment(ballNextX, ballNextY, p1[0], p1[1], p2[0], p2[1]);
        float distanceP2Ball = distanceToSegment(ballNextX, ballNextY, p2[0], p2[1], p1[0], p1[1]);
        float segmentLength = distanceBetweenPoints(p1[0], p1[1], p2[0], p2[1]);

        return (distanceP1Ball + distanceP2Ball) < (segmentLength + radius);
    }

    private float[] calculateNormal(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        return new float[]{-dy / length, dx / length}; 
    }

    private float[] reflect(float[] velocity, float[] normal) {
        float dotProduct = velocity[0] * normal[0] + velocity[1] * normal[1];
        return new float[]{velocity[0] - 2 * dotProduct * normal[0], velocity[1] - 2 * dotProduct * normal[1]};
    }

    public void applyAttraction(float dx, float dy, float attractionStrength) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            vx += attractionStrength * (dx / distance);
            vy += attractionStrength * (dy / distance);
        }
    }

    public void shrink(float distanceToHole) {
        float shrinkFactor = distanceToHole / 32;
        radius = (int) (16 * shrinkFactor);  // Shrink relative to distance
    }

    public void display() {
        if (!isCaptured) {
            p.image(image, x, y - 5, radius * 2, radius * 2);
        }
    }

    private float distanceBetweenPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private float distanceToSegment(float x, float y, float x1, float y1, float x2, float y2) {
        float A = x - x1;
        float B = y - y1;
        float C = x2 - x1;
        float D = y2 - y1;

        float dot = A * C + B * D;
        float lenSq = C * C + D * D;
        float param = (lenSq != 0) ? dot / lenSq : -1;

        float xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        float dx = x - xx;
        float dy = y - yy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setCaptured(boolean captured) {
        this.isCaptured = captured;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getColour() {
        return colour;
    }

    public int getRadius() {
        return radius;
    }

    private PImage getBallImageByColour(String colour) {
        switch (colour) {
            case "orange":
                return p.loadImage("src/main/resources/inkball/ball1.png");
            case "blue":
                return p.loadImage("src/main/resources/inkball/ball2.png");
            case "green":
                return p.loadImage("src/main/resources/inkball/ball3.png");
            case "yellow":
                return p.loadImage("src/main/resources/inkball/ball4.png");
            default:
                return p.loadImage("src/main/resources/inkball/ball0.png");
        }
    }
}
