package game;

import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class Button extends GameObject {
    private Screen      screen;
    private MouseSource mouse;
    private Sprite      look;
    private Text        text;
    private Point       pos;
    private Point       textPos;
    private Rect        box;
    private boolean     pressed;

    public Button(AssetManager assets, String buttonLook, String buttonText, int x, int y) {
        screen = GameComponents.get(Screen.class);
        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));

        look = assets.newSprite(buttonLook);
        text = assets.newText(Config.BUTTON_FONT, buttonText);
        pos = new Point(x + look.getWidth() / 2, y + look.getHeight() / 2);
        textPos = new Point(x + look.getWidth() / 2, y + look.getHeight() / 2);
        pressed = false;

        box = new Rect(x, y, look.getWidth(), look.getHeight());
    }

    @Override
    protected void update() {

    }

    @Override
    protected void render(GameRenderers renderers) {
        look.render(screen, pos.x, pos.y, Corner.CENTER);
        text.render(screen, textPos.x, textPos.y, Corner.CENTER);
    }

    @Override
    protected void wakeup(Object... args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void destroy() {
        // TODO Auto-generated method stub

    }

    public void Hide() {
        visible = false;
        frozen = false;
    }

    public void Show() {
        visible = true;
        frozen = true;
    }

    public boolean WasPressed() {
        return pressed;
    }

    public void Reset() {
        pressed = false;
    }

    public void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;
        if (box.IsInside(e.getX(), e.getY())) {
            pressed = true;
        }
    }
}
