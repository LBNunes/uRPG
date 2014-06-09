/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-04-29 ymd
//
// X11 Licensed Code
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
/////////////////////////////////////////////////////////////////////////

package game;

import game.Classes.ClassID;
import game.Grid.GridArea;
import game.Grid.HexColors;
import game.WorldMapScene.WorldArea;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObjectTreeScene;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.MouseEvent;
import org.unbiquitous.uImpala.engine.io.MouseSource;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.observer.Event;
import org.unbiquitous.uImpala.util.observer.Observation;
import org.unbiquitous.uImpala.util.observer.Subject;

public class BattleScene extends GameObjectTreeScene {

    private enum TurnStage {
        BATTLE_START, PLAY_ANIMATIONS, FIND_NEXT_PLAYER, CHOOSE_ACTION,
        ACTION_ATTACK, ATTACK_TARGET, INFLICT_DAMAGE, ACTION_MOVE, MOVE_TARGET,
        ACTION_ABILITY, ABILITY_PICK, ABILITY_TARGET,
        ACTION_ITEM, ITEM_PICK, ITEM_TARGET,
        CHECK_END, VICTORY, DEFEAT, BATTLE_END
    }

    private Screen            screen;
    private MouseSource       mouse;

    private PlayerData        playerData;
    private ArrayList<Entity> units;
    private AnimationQueue    animationQueue;
    private Grid              grid;
    private TurnStage         currentStage;
    private TurnStage         nextStage;
    private Point             selectedHex;
    private Entity            currentEntity;
    private boolean           hasMoved;
    private boolean           hasActed;
    private ArrayList<Point>  hexList;

    private Button            moveButton;
    private Button            attackButton;
    private Button            abilityButton;
    private Button            itemButton;
    private Button            endButton;

    public BattleScene(PlayerData playerData, WorldArea area) {

        this.playerData = playerData;

        // Initialize the screen manager
        screen = GameComponents.get(Screen.class);

        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));

        // TODO: Unpack player data
        // TODO: Create enemies
        units = new ArrayList<Entity>();
        Entity warrior = new Entity(assets, "Test Character", ClassID.WARRIOR, 1);
        warrior.Move(0, 3);
        units.add(warrior);
        Entity enemy = new Entity(assets, 001, 1);
        enemy.Move(10, 3);
        units.add(enemy);

        grid = new Grid(assets, area);

        currentStage = TurnStage.BATTLE_START;
        nextStage = TurnStage.FIND_NEXT_PLAYER;

        selectedHex = null;
        currentEntity = null;
        hasMoved = false;
        hasActed = false;
        hexList = new ArrayList<Point>();

        animationQueue = new AnimationQueue(assets);

        moveButton = new Button(assets,
                                Config.BUTTON_LOOK,
                                "Move",
                                Color.white,
                                (int) (Config.BUTTON_BASE_X - 2 * (Config.BUTTON_X_SPACING + Config.BUTTON_X_WIDTH)),
                                Config.BUTTON_BASE_Y);

        attackButton = new Button(
                                  assets,
                                  Config.BUTTON_LOOK,
                                  "Attack",
                                  Color.white,
                                  (int) (Config.BUTTON_BASE_X - 1 * (Config.BUTTON_X_SPACING + Config.BUTTON_X_WIDTH)),
                                  Config.BUTTON_BASE_Y);

        abilityButton = new Button(
                                   assets,
                                   Config.BUTTON_LOOK,
                                   "Ability",
                                   Color.white,
                                   (int) (Config.BUTTON_BASE_X + 0 * (Config.BUTTON_X_SPACING + Config.BUTTON_X_WIDTH)),
                                   Config.BUTTON_BASE_Y);

        itemButton = new Button(assets,
                                Config.BUTTON_LOOK,
                                "Item",
                                Color.white,
                                (int) (Config.BUTTON_BASE_X + 1 * (Config.BUTTON_X_SPACING + Config.BUTTON_X_WIDTH)),
                                Config.BUTTON_BASE_Y);

        endButton = new Button(assets,
                               Config.BUTTON_LOOK,
                               "End Turn",
                               Color.white,
                               (int) (Config.BUTTON_BASE_X + 2 * (Config.BUTTON_X_SPACING + Config.BUTTON_X_WIDTH)),
                               Config.BUTTON_BASE_Y);

        HideActionButtons();

        TextLog.instance.SetAssets(assets);
        TextLog.instance.Clear();
    }

    @SuppressWarnings("unused")
    private void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;
        selectedHex = grid.FindHexByPixel(e.getX(), e.getY());
        if (!grid.ValidHexPosition(selectedHex.x, selectedHex.y)) {
            selectedHex = null;
        }
    }

    protected void update() {
        if (screen.isCloseRequested()) {
            GameComponents.get(Game.class).quit();
        }
        grid.update();
        for (Entity e : units) {
            e.update();
        }

        TextLog.instance.Update();

        TurnLogic();
    }

    protected void render() {
        GameRenderers renderers = new GameRenderers();
        grid.render(renderers);
        for (Entity e : units) {
            grid.RenderAtHex(e.sp, e.pos.x, e.pos.y);
        }
        animationQueue.Render();

        moveButton.render(renderers);
        attackButton.render(renderers);
        abilityButton.render(renderers);
        itemButton.render(renderers);
        endButton.render(renderers);

        TextLog.instance.Render(Config.SCREEN_WIDTH / 2, (int) (0.9 * Config.SCREEN_HEIGHT), Corner.CENTER);
    }

    private void TurnLogic() {
        switch (currentStage) {
            case BATTLE_START:
                Stage_BattleStart();
                break;
            case PLAY_ANIMATIONS:
                Stage_PlayAnimations();
                break;
            case FIND_NEXT_PLAYER:
                Stage_FindNextPlayer();
                break;
            case CHOOSE_ACTION:
                Stage_ChooseAction();
                break;
            case ACTION_ATTACK:
                break;
            case ATTACK_TARGET:
                break;
            case INFLICT_DAMAGE:
                break;
            case ACTION_MOVE:
                Stage_ActionMove();
                break;
            case MOVE_TARGET:
                break;
            case ACTION_ABILITY:
                break;
            case ABILITY_PICK:
                break;
            case ABILITY_TARGET:
                break;
            case ACTION_ITEM:
                break;
            case ITEM_PICK:
                break;
            case ITEM_TARGET:
                break;
            case CHECK_END:
                Stage_CheckEnd();
                break;
            case DEFEAT:
                Stage_Defeat();
            case VICTORY:
                Stage_Victory();
                break;
            case BATTLE_END:
                break;
        }
    }

    private void Stage_BattleStart() {
        // Reset everyone's turn timer
        for (Entity e : units) {
            e.FullHeal();
            e.turnTimer = 0;
        }
        animationQueue.NewGroup();
        animationQueue.Push(Config.BATTLE_START, Config.BATTLE_START_FRAMES, Config.BATTLE_START_FPS,
                            new Point(Config.SCREEN_WIDTH / 2, Config.SCREEN_HEIGHT / 2));
        currentStage = TurnStage.PLAY_ANIMATIONS;
        nextStage = TurnStage.FIND_NEXT_PLAYER;
    }

    private void Stage_PlayAnimations() {
        animationQueue.Update();
        if (animationQueue.IsEmpty()) {
            currentStage = nextStage;
        }
    }

    private void Stage_FindNextPlayer() {
        int largestTimer = 99;
        Entity turnTaker = null;
        while (turnTaker == null) {
            for (Entity e : units) {
                if (e.turnTimer > largestTimer) {
                    largestTimer = e.turnTimer;
                    turnTaker = e;
                }
            }
            if (turnTaker == null) {
                for (Entity e : units) {
                    e.turnTimer += e.stats.spd;
                }
            }
        }
        ShowActionButtons();
        currentEntity = turnTaker;
        hasMoved = false;
        hasActed = false;
        grid.ClearColors();
        grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
        currentStage = TurnStage.CHOOSE_ACTION;
    }

    private void Stage_ChooseAction() {
        if (moveButton.WasPressed()) {
            if (hasMoved) {
                TextLog.instance.Print(currentEntity.name + " has already moved on this turn!", Color.white);
                moveButton.Reset();
            }
            else {
                hexList.clear();
                ListHexes(currentEntity.pos.x, currentEntity.pos.y, GridArea.CIRCLE, currentEntity.moveRange);
                HideActionButtons();
                grid.ColorHexes(hexList, HexColors.YELLOW, true);
                grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
                selectedHex = null;
                currentStage = TurnStage.ACTION_MOVE;
            }
        }
        if (attackButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                attackButton.Reset();
            }
            else {
                currentStage = TurnStage.ACTION_ATTACK;
                HideActionButtons();
            }
        }
        if (abilityButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                abilityButton.Reset();
            }
            else {
                currentStage = TurnStage.ACTION_ABILITY;
                HideActionButtons();
            }
        }
        if (itemButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                itemButton.Reset();
            }
            else {
                currentStage = TurnStage.ACTION_ITEM;
                HideActionButtons();
            }
        }
        if (endButton.WasPressed()) {
            HideActionButtons();
            if (hasActed && hasMoved) {
                currentEntity.turnTimer = 0;
            }
            else if (hasActed || hasMoved) {
                currentEntity.turnTimer = 30;
            }
            else {
                currentEntity.turnTimer = 60;
            }
            currentStage = TurnStage.FIND_NEXT_PLAYER;
        }
    }

    private void Stage_ActionMove() {
        if (selectedHex != null) {
            if (AlreadyListed(selectedHex.x, selectedHex.y)) {
                if (!HexOccupied(selectedHex.x, selectedHex.y)) {
                    currentEntity.Move(selectedHex.x, selectedHex.y);
                    hasMoved = true;
                    ShowActionButtons();
                    grid.ClearColors();
                    grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
                    currentStage = TurnStage.CHOOSE_ACTION;
                }
            }
        }
    }

    private void Stage_CheckEnd() {
        if (PlayerUnitsDead()) {
            currentStage = TurnStage.DEFEAT;
        }
        else if (EnemyUnitsDead()) {
            currentStage = TurnStage.VICTORY;
        }
        else {
            currentStage = TurnStage.FIND_NEXT_PLAYER;
        }
    }

    private void Stage_Defeat() {
        animationQueue.NewGroup();
        animationQueue.Push(Config.DEFEAT_TEXT, Config.DEFEAT_FRAMES, Config.DEFEAT_FPS,
                            new Point(Config.SCREEN_WIDTH / 2, Config.SCREEN_HEIGHT / 2));
        // TODO: Put consequences (if any) into effect

        currentStage = TurnStage.PLAY_ANIMATIONS;
        nextStage = TurnStage.BATTLE_END;
    }

    private void Stage_Victory() {

        animationQueue.NewGroup();
        animationQueue.Push(Config.VICTORY_TEXT, Config.VICTORY_FRAMES, Config.VICTORY_FPS,
                            new Point(Config.SCREEN_WIDTH / 2, Config.SCREEN_HEIGHT / 2));
        // TODO: Give items and job exp

        currentStage = TurnStage.PLAY_ANIMATIONS;
        nextStage = TurnStage.BATTLE_END;
    }

    private void ListHexes(int x, int y, GridArea area, int range) {

        if (!grid.ValidHexPosition(x, y) && area != GridArea.ALL)
            return;

        switch (area) {
            case SINGLE_HEX:
                if (!AlreadyListed(x, y)) {
                    hexList.add(new Point(x, y));
                }
                break;

            case CIRCLE:
                if (!AlreadyListed(x, y)) {
                    hexList.add(new Point(x, y));
                }
                if (range > 0) {
                    ListHexes(x, y + 1, area, range - 1);
                    ListHexes(x, y - 1, area, range - 1);
                    ListHexes(x + 1, y, area, range - 1);
                    ListHexes(x - 1, y, area, range - 1);

                    if (y % 2 == 1) {
                        ListHexes(x + 1, y + 1, area, range - 1);
                        ListHexes(x + 1, y - 1, area, range - 1);
                    }
                    else {
                        ListHexes(x - 1, y + 1, area, range - 1);
                        ListHexes(x - 1, y - 1, area, range - 1);
                    }
                }
                break;

            // Line with an inclination of +30°, starting in x, y. Complement specifies length.
            case DIAGONAL_UP_LINE:
                if (!AlreadyListed(x, y)) {
                    hexList.add(new Point(x, y));
                    if (range > 0) {
                        if (y % 2 == 1) {
                            ListHexes(x + 1, y - 1, area, range - 1);
                        }
                        else {
                            ListHexes(x, y - 1, area, range - 1);
                        }
                    }
                    // If it's less than 0, fill the hex (-1, +1)
                    else if (range < 0) {
                        if (y % 2 == 1) {
                            ListHexes(x, y + 1, area, range + 1);
                        }
                        else {
                            ListHexes(x - 1, y + 1, area, range + 1);
                        }
                    }
                }
                break;
            // Line with an inclination of -30°, starting in x, y. Complement specifies length.
            case DIAGONAL_DOWN_LINE:
                if (!AlreadyListed(x, y)) {
                    hexList.add(new Point(x, y));
                    if (range > 0) {
                        if (y % 2 == 1) {
                            ListHexes(x + 1, y + 1, area, range - 1);
                        }
                        else {
                            ListHexes(x, y + 1, area, range - 1);
                        }
                    }
                    // If it's less than 0, fill the hex (-1, +1)
                    else if (range < 0) {
                        if (y % 2 == 1) {
                            ListHexes(x, y - 1, area, range + 1);
                        }
                        else {
                            ListHexes(x - 1, y - 1, area, range + 1);
                        }
                    }
                }
                break;
            // Color the line going up or down, starting at x, y. Complement specifies length.
            case HORIZONTAL_LINE:
                if (!AlreadyListed(x, y)) {
                    hexList.add(new Point(x, y));
                    if (range > 0) {
                        ListHexes(x + 1, y, area, range - 1);
                    }
                    // If it's less than 0, fill the hex (-1, +0)
                    else if (range < 0) {
                        ListHexes(x - 1, y, area, range + 1);
                    }
                }
                break;

            case ALL:
                // TODO: Really, I shouldn't need this.
                break;
        }
    }

    private boolean AlreadyListed(int x, int y) {
        for (Point p : hexList) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    private boolean HexOccupied(int x, int y) {
        for (Entity e : units) {
            if (e.pos.x == x && e.pos.y == y) {
                return true;
            }
        }
        return false;
    }

    private boolean PlayerUnitsDead() {
        for (Entity e : units) {
            if (e.playerUnit && e.currentHP > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean EnemyUnitsDead() {
        for (Entity e : units) {
            if (!e.playerUnit && e.currentHP > 0) {
                return false;
            }
        }
        return true;
    }

    private void HideActionButtons() {
        moveButton.Hide();
        attackButton.Hide();
        abilityButton.Hide();
        itemButton.Hide();
        endButton.Hide();
    }

    private void ShowActionButtons() {
        moveButton.Show();
        attackButton.Show();
        abilityButton.Show();
        itemButton.Show();
        endButton.Show();
    }
}