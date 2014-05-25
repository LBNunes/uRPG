package game;

public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        x = 0;
        y = 0;
    }

    public void Add(Point p) {
        x += p.x;
        y += p.y;
    }

    public void Subtract(Point p) {
        x -= p.x;
        y -= p.y;
    }

    public void Multiply(float f) {
        x = (int) (x * f);
        y = (int) (y * f);
    }

    public Point Plus(Point p) {
        return new Point(x + p.x, y + p.y);
    }

    public Point Minus(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public Point Times(float f) {
        return new Point((int) (x * f), (int) (y * f));
    }
}
