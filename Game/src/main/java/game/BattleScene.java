/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Lu�sa Bontempo Nunes
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
import game.Item.ItemSlot;
import game.WorldMapScene.WorldArea;

import java.util.ArrayList;

import org.unbiquitous.uImpala.engine.core.Game;
import org.unbiquitous.uImpala.engine.core.GameComponents;
import org.unbiquitous.uImpala.engine.core.GameObjectTreeScene;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.KeyboardEvent;
import org.unbiquitous.uImpala.engine.io.KeyboardSource;
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
        ACTION_ATTACK, ACTION_MOVE, ACTION_ABILITY, ABILITY_PICK, ITEM_PICK, ACTION_ITEM,
        CHECK_END, VICTORY, DEFEAT, BATTLE_END
    }

    private Screen            screen;
    private MouseSource       mouse;
    private KeyboardSource    keyboard;
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
    private boolean           escKeyPressed;

    public BattleScene(PlayerData playerData, WorldArea area) {

        this.playerData = playerData;

        // Initialize the screen manager
        screen = GameComponents.get(Screen.class);

        mouse = screen.getMouse();
        mouse.connect(MouseSource.EVENT_BUTTON_DOWN, new Observation(this, "OnButtonDown"));
        keyboard = screen.getKeyboard();
        keyboard.connect(KeyboardSource.EVENT_KEY_DOWN, new Observation(this, "OnKeyDown"));
        keyboard.connect(KeyboardSource.EVENT_KEY_UP, new Observation(this, "OnKeyUp"));

        escKeyPressed = false;

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
                Stage_ActionAttack();
                break;
            case ACTION_MOVE:
                Stage_ActionMove();
                break;
            case ABILITY_PICK:
                Stage_AbilityPick();
                break;
            case ACTION_ABILITY:
                Stage_ActionAbility();
                break;
            case ITEM_PICK:
                Stage_ItemPick();
                break;
            case ACTION_ITEM:
                Stage_ActionItem();
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
                if (e.turnTimer > largestTimer && e.currentHP > 0) {
                    largestTimer = e.turnTimer;
                    turnTaker = e;
                }
            }
            if (turnTaker == null) {
                for (Entity e : units) {
                    if (e.currentHP > 0) {
                        e.turnTimer += e.stats.spd;
                    }
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

        if (endButton.WasPressed() || (hasActed && hasMoved)) {
            HideActionButtons();
            if (hasActed && hasMoved) {
                currentEntity.turnTimer = 0;
            }
            else if (hasActed || hasMoved) {
                currentEntity.turnTimer = 20;
            }
            else {
                currentEntity.turnTimer = 40;
            }
            currentStage = TurnStage.FIND_NEXT_PLAYER;
        }

        else if (moveButton.WasPressed()) {
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
        else if (attackButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                attackButton.Reset();
            }
            else {
                hexList.clear();
                ListHexes(currentEntity.pos.x, currentEntity.pos.y, GridArea.CIRCLE,
                          currentEntity.equipment.Get(ItemSlot.WEAPON).GetRange());
                HideActionButtons();
                grid.ColorHexes(hexList, HexColors.RED, true);
                grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
                selectedHex = null;
                currentStage = TurnStage.ACTION_ATTACK;
            }
        }
        else if (abilityButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                abilityButton.Reset();
            }
            else {
                currentStage = TurnStage.ABILITY_PICK;
                HideActionButtons();
            }
        }
        else if (itemButton.WasPressed()) {
            if (hasActed) {
                TextLog.instance.Print(currentEntity.name + " has already acted on this turn!", Color.white);
                itemButton.Reset();
            }
            else {
                currentStage = TurnStage.ITEM_PICK;
                HideActionButtons();
            }
        }
    }

    private void Stage_ActionMove() {
        if (selectedHex != null) {
            if (AlreadyListed(selectedHex.x, selectedHex.y)) {
                if (HexOccupied(selectedHex.x, selectedHex.y) == null) {
                    currentEntity.Move(selectedHex.x, selectedHex.y);
                    hasMoved = true;
                    grid.ClearColors();
                    if (!(hasMoved && hasActed)) {
                        ShowActionButtons();
                        grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
                    }
                    currentStage = TurnStage.CHOOSE_ACTION;
                }
            }
        }
        else if (escKeyPressed) {
            ShowActionButtons();
            grid.ClearColors();
            grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
            currentStage = TurnStage.CHOOSE_ACTION;
        }
    }

    private void Stage_ActionAttack() {
        if (selectedHex != null) {
            if (AlreadyListed(selectedHex.x, selectedHex.y)) {
                Entity target = HexOccupied(selectedHex.x, selectedHex.y);
                if (target != null && target.playerUnit != currentEntity.playerUnit) {
                    int damage = Classes.GetPhysicalFactor(currentEntity, target);
                    Point damagePos = grid.FindHexPosition(target.pos.x, target.pos.y);
                    damagePos.y -= Grid.hexRadius / 2;
                    if (Classes.IsCritical(currentEntity)) {
                        damage *= Config.CRITICAL_FACTOR;
                        animationQueue.Push(damage, true, damage >= 0 ? Color.red : Color.green, damagePos);
                        PrintDamage(damage, true, currentEntity, target);
                    }
                    else {
                        animationQueue.Push(damage, false, damage >= 0 ? Color.red : Color.green, damagePos);
                        PrintDamage(damage, false, currentEntity, target);
                    }
                    hasActed = true;
                    grid.ClearColors();
                    if (!(hasMoved && hasActed)) {
                        ShowActionButtons();
                        grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
                    }
                    currentStage = TurnStage.PLAY_ANIMATIONS;
                    nextStage = TurnStage.CHECK_END;
                }
            }
        }
        else if (escKeyPressed) {
            ShowActionButtons();
            grid.ClearColors();
            grid.ColorArea(currentEntity.pos.x, currentEntity.pos.y, GridArea.SINGLE_HEX, HexColors.BLUE, 0);
            currentStage = TurnStage.CHOOSE_ACTION;
        }
    }

    private void Stage_AbilityPick() {
        currentStage = TurnStage.CHOOSE_ACTION;
    }

    private void Stage_ActionAbility() {

    }

    private void Stage_ItemPick() {
        currentStage = TurnStage.CHOOSE_ACTION;
    }

    private void Stage_ActionItem() {

    }

    private void Stage_CheckEnd() {
        if (PlayerUnitsDead()) {
            currentStage = TurnStage.DEFEAT;
        }
        else if (EnemyUnitsDead()) {
            currentStage = TurnStage.VICTORY;
        }
        else {
            currentStage = TurnStage.CHOOSE_ACTION;
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

            // Line with an inclination of +30�, starting in x, y. Complement specifies length.
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
            // Line with an inclination of -30�, starting in x, y. Complement specifies length.
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

    private Entity HexOccupied(int x, int y) {
        for (Entity e : units) {
            if (e.pos.x == x && e.pos.y == y) {
                return e;
            }
        }
        return null;
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

    private void PrintDamage(int damage, boolean critical, Entity attacker, Entity target) {
        String message = new String();
        message += attacker.name + " the " + attacker.className + " attacks! ";
        if (damage > 0) {
            message += target.name + " the " + target.className + " takes " + damage + " damage";
        }
        else if (damage < 0) {
            message += target.name + " the " + target.className + " is healed by " + damage + " hitpoints";
        }
        else {
            message += target.name + " the " + target.className + " takes no damage";
        }
        if (critical) {
            message += "(Critical Hit!)";
        }
        TextLog.instance.Print(message, Color.white);
    }

    // private void PrintDamage(int damage, Ability ability, Entity caster, Entity target) {
    // String message = new String();
    // }

    @SuppressWarnings("unused")
    private void OnButtonDown(Event event, Subject subject) {
        MouseEvent e = (MouseEvent) event;
        selectedHex = grid.FindHexByPixel(e.getX(), e.getY());
        if (!grid.ValidHexPosition(selectedHex.x, selectedHex.y)) {
            selectedHex = null;
        }
    }

    @SuppressWarnings("unused")
    private void OnKeyDown(Event event, Subject subject) {
        KeyboardEvent e = (KeyboardEvent) event;
        System.out.println(e.getKey());
        if (e.getKey() == 1) {
            escKeyPressed = true;
        }
    }

    @SuppressWarnings("unused")
    private void OnKeyUp(Event event, Subject subject) {
        KeyboardEvent e = (KeyboardEvent) event;
        System.out.println(e.getKey());
        if (e.getKey() == 1) {
            escKeyPressed = false;
        }
    }
}