package inkball;

import java.util.List;

import processing.core.PApplet;
import processing.data.JSONObject;

public class Capture {
    private PApplet p;
    private App app;
    private int levelNumber;
    private JSONObject config;
    private LevelLoader levelLoader;

    public Capture(PApplet p, App app, JSONObject config, int levelNumber) {
        this.p = p;
        this.app = app;
        this.config = config;
        this.levelNumber = levelNumber;
        this.levelLoader = new LevelLoader(config);
    }

    public void checkCollisionWithHoles(Ball ball, List<Level.Hole> holes) {
        for (Level.Hole hole : holes) {
            float holeCenterX = (hole.x + 1) * App.CELLSIZE;
            float holeCenterY = (hole.y + 1) * App.CELLSIZE + App.TOPBAR;
            float dx = holeCenterX - ball.getX();
            float dy = holeCenterY - ball.getY();
            float distanceToHole = (float) Math.sqrt(dx * dx + dy * dy);

            // Apply attraction when within range of 32 pixels
            if (distanceToHole < 32) {
                float attractionStrength = 0.005f; // Attraction strength factor
                ball.applyAttraction(dx, dy, attractionStrength);

                // Shrink the ball as it gets closer to the hole
                ball.shrink(distanceToHole);

                // Capture the ball if within close proximity to the center
                if (distanceToHole < 5) {
                    captureBall(ball, hole);
                    ball.setCaptured(true);
                    return;
                }
            }
        }
    }

    public void captureBall(Ball ball, Level.Hole hole) {
        String ballColor = ball.getColour();
        String holeColor = hole.getColour();

        // Fetch score modifiers from config
        float scoreIncreaseModifier = levelLoader.getScoreIncreaseModifier(levelNumber);
        float scoreDecreaseModifier = levelLoader.getScoreDecreaseModifier(levelNumber);

        // Fetch score increase and decrease values for the ball color
        JSONObject scoreIncreaseConfig = config.getJSONObject("score_increase_from_hole_capture");
        JSONObject scoreDecreaseConfig = config.getJSONObject("score_decrease_from_wrong_hole");

        // Handle score based on ball color and hole color
        int scoreIncrease = getIntFromConfig(scoreIncreaseConfig, ballColor, 0);
        int scoreDecrease = getIntFromConfig(scoreDecreaseConfig, ballColor, 0);

        // Handle grey holes and balls
        if (ballColor.equals("grey") || holeColor.equals("grey") || ballColor.equals(holeColor)) {
            // Successful capture, increase the score
            int scoreToAdd = (int) (scoreIncrease * scoreIncreaseModifier);
            App.increaseScore(scoreToAdd);
        } else {
            // Unsuccessful capture, decrease the score and requeue the ball
            int scoreToSubtract = (int) (scoreDecrease * scoreDecreaseModifier);
            App.decreaseScore(scoreToSubtract);
            app.requeueBall(ball); // Requeue the ball if it wasn't captured successfully
        }
    }

    // Helper method to safely retrieve values from config with default
    private int getIntFromConfig(JSONObject config, String key, int defaultValue) {
        if (config.hasKey(key)) {
            return config.getInt(key);
        }
        return defaultValue;
    }
}
