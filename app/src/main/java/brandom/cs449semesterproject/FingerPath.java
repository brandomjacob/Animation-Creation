package brandom.cs449semesterproject;

import android.graphics.Path;

public class FingerPath {
    //Values for color, width and path of line.
    private int color;
    private int strokeWidth;
    private Path path;

    public int get_color()
    {
        return color;
    }

    public int get_strokeWidth()
    {
        return strokeWidth;
    }

    public Path get_path()
    {
        return path;
    }

    //Constructor
    public FingerPath(int color, int strokeWidth, Path path)
    {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}