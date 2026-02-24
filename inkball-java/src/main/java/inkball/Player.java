package inkball;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class Player {
    private PApplet p;
    private List<List<float[]>> lines;
    private List<float[]> currentLine;

    public Player(PApplet p) {
        this.p = p;
        lines = new ArrayList<>();
        currentLine = new ArrayList<>();
    }

    public void startNewLine() {
        currentLine = new ArrayList<>();
        lines.add(currentLine);
    }

    public void addLinePoint(float x, float y) {
        if (currentLine != null) {
            currentLine.add(new float[]{x, y});
        }
    }

    public void renderLines() {
        p.stroke(0);
        p.strokeWeight(10);
        p.noFill();

        for (List<float[]> line : lines) {
            if (line.size() > 1) {
                for (int i = 0; i < line.size() - 1; i++) {
                    float[] p1 = line.get(i);
                    float[] p2 = line.get(i + 1);
                    p.line(p1[0], p1[1], p2[0], p2[1]);
                }
            }
        }
    }

    public void removeLineAt(float x, float y) {
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            List<float[]> line = lines.get(lineIndex);
            for (int i = 0; i < line.size() - 1; i++) {
                float[] p1 = line.get(i);
                float[] p2 = line.get(i + 1);
                if (isPointNearLine(x, y, p1, p2)) {
                    lines.remove(lineIndex);
                    return;
                }
            }
        }
    }

    public void removeLine(List<float[]> line) {
        lines.remove(line);
    }

    private boolean isPointNearLine(float x, float y, float[] p1, float[] p2) {
        float distance = distanceToSegment(x, y, p1[0], p1[1], p2[0], p2[1]);
        return distance < 10;
    }

    public static float distanceToSegment(float x, float y, float x1, float y1, float x2, float y2) {
        float A = x - x1;
        float B = y - y1;
        float C = x2 - x1;
        float D = y2 - y1;

        float dot = A * C + B * D;
        float len_sq = C * C + D * D;
        float param = (len_sq != 0) ? dot / len_sq : -1;

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

    public List<List<float[]>> getLines() {
        return lines;
    }
}
