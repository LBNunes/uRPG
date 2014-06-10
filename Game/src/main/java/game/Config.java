/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Luísa Bontempo Nunes
//     Created on 2014-05-24 ymd
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

public class Config {

    public static final String  WINDOW_TITLE           = "uRPG";
    public static final int     SCREEN_WIDTH           = 1280;
    public static final int     SCREEN_HEIGHT          = 720;
    public static final boolean FULLSCREEN             = false;
    public static final String  WINDOW_ICON            = null;

    public static final String  SAND_BG                = "img/sandbg.jpg";
    public static final String  GRASS_BG               = "img/grassbg.jpg";
    public static final String  CLEAR_HEX              = "img/clearhex.png";
    public static final String  RED_HEX                = "img/redhex.png";
    public static final String  GREEN_HEX              = "img/greenhex.png";
    public static final String  BLUE_HEX               = "img/bluehex.png";
    public static final String  YELLOW_HEX             = "img/yellowhex.png";

    public static final String  BATTLE_START           = "img/battlestart.png";
    public static final int     BATTLE_START_FRAMES    = 8;
    public static final float   BATTLE_START_FPS       = 6.0f;
    public static final String  VICTORY_TEXT           = "img/victory.png";
    public static final int     VICTORY_FRAMES         = 1;
    public static final float   VICTORY_FPS            = 0.2f;
    public static final String  DEFEAT_TEXT            = "img/defeat.png";
    public static final int     DEFEAT_FRAMES          = 1;
    public static final float   DEFEAT_FPS             = 0.2f;

    public static final String  ITEM_DATA              = "data/item";
    public static final String  CLASS_DATA             = "data/class";
    public static final String  ENEMY_DATA             = "data/enemy";
    public static final String  PLAYER_NAMES           = "data/names";
    public static final String  ENEMY_NAMES            = "data/enemynames";

    public static final String  BUTTON_LOOK            = "img/button.png";
    public static final String  BUTTON_FONT            = "font/seguisb.ttf";
    public static final int     BUTTON_BASE_X          = SCREEN_WIDTH / 2;
    public static final int     BUTTON_BASE_Y          = (int) (SCREEN_HEIGHT * 0.05);
    public static final int     BUTTON_X_SPACING       = 20;
    public static final int     BUTTON_X_WIDTH         = 150;

    public static final String  DAMAGE_ANIMATION_FONT  = "font/edo.ttf";
    public static final float   DAMAGE_ANIMATION_HOVER = 30;
    public static final float   DAMAGE_ANIMATION_TIME  = 1500;

    public static final float   CRITICAL_FACTOR        = 1.5f;

    public static final String  LOG_FONT               = "font/seguisb.ttf";
    public static final int     LOG_FONT_SIZE          = 16;
    public static final int     LOG_CAP                = 6;
    public static final float   LOG_EXPIRE_TIME        = 8.0f;
}
