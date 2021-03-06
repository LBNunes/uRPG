/////////////////////////////////////////////////////////////////////////
//
// Copyright (c) Lu�sa Bontempo Nunes
//     Created on 2014-06-04 ymd
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

public class Stats {

    public int HP;
    public int MP;
    public int atk;
    public int def;
    public int mag;
    public int res;
    public int spd;

    public Stats() {
        HP = 0;
        MP = 0;
        atk = 0;
        def = 0;
        mag = 0;
        res = 0;
    }

    public Stats(int HP, int MP, int atk, int def, int mag, int res, int spd) {
        SetAll(HP, MP, atk, def, mag, res, spd);
    }

    public void SetAll(int HP, int MP, int atk, int def, int mag, int res, int spd) {
        this.HP = HP;
        this.MP = MP;
        this.atk = atk;
        this.def = def;
        this.mag = mag;
        this.res = res;
        this.spd = spd;
    }
}
