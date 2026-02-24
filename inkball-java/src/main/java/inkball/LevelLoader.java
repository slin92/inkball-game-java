package inkball;

import processing.data.JSONArray;
import processing.data.JSONObject;

public class LevelLoader {
    private JSONObject config;

    public LevelLoader(JSONObject config) {
        this.config = config;
    }

    /**
     * Get the layout file path for the given level number.
     */
    public String getLayoutFile(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);  // Arrays are 0-indexed
        return levelConfig.getString("layout");
    }

    /**
     * Get the time limit for the given level.
     */
    public int getTimeLimit(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);
        return levelConfig.getInt("time");
    }

    /**
     * Get the spawn interval for the given level.
     */
    public int getSpawnInterval(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);
        return levelConfig.getInt("spawn_interval");
    }

    /**
     * Get the ball colors for the given level.
     */
    public String[] getBallColours(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);
        JSONArray ballColoursArray = levelConfig.getJSONArray("balls");
        return ballColoursArray.getStringArray();
    }

    /**
     * Get the score increase modifier for the given level.
     */
    public float getScoreIncreaseModifier(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);
        return (float) levelConfig.getDouble("score_increase_from_hole_capture_modifier");
    }

    /**
     * Get the score decrease modifier for the given level.
     */
    public float getScoreDecreaseModifier(int levelNumber) {
        JSONArray levelsArray = config.getJSONArray("levels");
        JSONObject levelConfig = levelsArray.getJSONObject(levelNumber - 1);
        return (float) levelConfig.getDouble("score_decrease_from_wrong_hole_modifier");
    }

    public int getTotalLevels() {
            return config.getJSONArray("levels").size();
        }
}
