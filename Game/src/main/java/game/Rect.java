package game;

public class Rect {
    public int x;
    public int y;
    public int w;
    public int h;

    public Rect(int _x, int _y, int _w, int _h) {
        x = _x;
        y = _y;
        w = _w;
        h = _h;
    }

    public boolean IsInside(int px, int py) {
        if (px >= x + w || px < x)
            return false;
        if (py >= y + h || py < y)
            return false;
        return true;
    }
}
