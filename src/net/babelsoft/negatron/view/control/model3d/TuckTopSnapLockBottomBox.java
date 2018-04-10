/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2018 BabelSoft S.A.S.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.babelsoft.negatron.view.control.model3d;

import javafx.scene.image.Image;

/**
 * tuck top snap lock bottom box (boîte à fond semi-automatique) 单插扣底盒
 * first-in closure panel 底凹, second-in flap 底衬, third-in closure panel 底凸
 * @author capan
 */
public class TuckTopSnapLockBottomBox extends PressboardBox {
    
    public TuckTopSnapLockBottomBox(
        float boxHeight, float boxWidth, float boxDepth,
        float glueFlapWidth, float tuckFlapWidth,
        float secondInFlapLength, float thirdInClosurePanelSmallBaseLength,
        Image boxTexture
    ) {
        super(
            buildPoints(
                boxHeight, boxWidth, boxDepth,
                thirdInClosurePanelSmallBaseLength
            ),
            buildTexCoords(
                boxHeight, boxWidth, boxDepth,
                glueFlapWidth, tuckFlapWidth,
                secondInFlapLength, thirdInClosurePanelSmallBaseLength
            ),
            buildFaces(), boxTexture
        );
    }
    
    private static float[] buildPoints(
        float boxHeight, float boxWidth, float boxDepth,
        float thirdInClosurePanelSmallBaseLength
    ) {
        float hw = boxWidth / 2f;
        float hh = boxHeight / 2f;
        float hd = boxDepth / 2f;
        float hl = thirdInClosurePanelSmallBaseLength / 2f;
        float hh2 = hh - 2f;

        return new float[] {
            -hw, -hh, -hd,
             hw, -hh, -hd,
             hw,  hh, -hd,
            -hw,  hh, -hd,
            -hw, -hh,  hd,
             hw, -hh,  hd,
             hw,  hh,  hd,
            -hw,  hh,  hd,
              0,  hh, -hd,
            -hl, hh2,   0,
              0,  hh,  hd,
             hl, hh2,   0
        };
    }
    
    private static float[] buildTexCoords(
        float boxHeight, float boxWidth, float boxDepth,
        float glueFlapWidth, float tuckFlapWidth,
        float secondInFlapLength, float thirdInClosurePanelSmallBaseLength
    ) {
        float foldingCartonWidth = boxHeight + boxDepth + tuckFlapWidth + secondInFlapLength;
        float foldingCartonLength = boxWidth * 2f + boxDepth * 2f + glueFlapWidth;
        float w = (boxWidth - thirdInClosurePanelSmallBaseLength) / 2f;
        float y1 = tuckFlapWidth / foldingCartonWidth;
        float y2 = (tuckFlapWidth + boxDepth) / foldingCartonWidth;
        float y3 = 1f - secondInFlapLength / foldingCartonWidth;
        float y4 = y3 + boxDepth / 2f / foldingCartonWidth;
        float x1 = boxDepth / 2f / foldingCartonLength;
        float x2 = boxDepth / foldingCartonLength;
        float x3 = (boxDepth + w) / foldingCartonLength;
        float x4 = (boxDepth + boxWidth / 2f) / foldingCartonLength;
        float x5 = (boxDepth + boxWidth - w) / foldingCartonLength;
        float x6 = (boxDepth + boxWidth) / foldingCartonLength;
        float x7 = (boxDepth * 1.5f + boxWidth) / foldingCartonLength;
        float x8 = (boxDepth * 2f + boxWidth) / foldingCartonLength;
        float x9 = (boxDepth * 2f + boxWidth + w) / foldingCartonLength;
        float x10 = (boxDepth * 2f + boxWidth * 1.5f) / foldingCartonLength;
        float x11 = (boxDepth * 2f + boxWidth * 2f - w) / foldingCartonLength;
        float x12 = 1f - glueFlapWidth / foldingCartonLength;
            
        return new float[] {
              0, y2,
             x2, y2,
             x2, y1,
             x6, y1,
             x6, y2,
             x8, y2,
            x12, y2,
            x12, y3,
            x11, y4,
            x10, y3,
             x9, y4,
             x8, y3,
             x7, y4,
             x6, y3,
             x5, y4,
             x4, y3,
             x3, y4,
             x2, y3,
             x1, y4,
              0, y3
        };
    }
    
    private static int[] buildFaces() {
        return new int[] {
             0,  5,    2,  7,    1,  6,
             2,  7,    0,  5,    3, 11,
             1,  0,    6, 17,    5,  1,
             6, 17,    1,  0,    2, 19,
             5,  1,    7, 13,    4,  4,
             7, 13,    5,  1,    6, 17,
             4,  4,    3, 11,    0,  5,
             3, 11,    4,  4,    7, 13,
             /*** snap lock bottom ***/
             3, 11,    9, 10,    8,  9,
            11,  8,    8,  9,    9, 10,
             8,  9,   11,  8,    2,  7,
             2, 19,   11, 18,    6, 17,
             6, 17,   11, 16,   10, 15,
             9, 14,   10, 15,   11, 16,
            10, 15,    9, 14,    7, 13,
             7, 13,    9, 12,    3, 11,
             /***     tuck top     ***/
             4,  4,    1,  2,    5,  1,
             1,  2,    4,  4,    0,  3
        };
    }
}
