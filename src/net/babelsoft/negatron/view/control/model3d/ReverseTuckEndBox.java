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
 * reverse tuck end box (boîte à pattes rentrantes alternées) 双插盒 鸳鸯盒
 * @author capan
 */
public class ReverseTuckEndBox extends PressboardBox {
    
    public ReverseTuckEndBox(
        float boxHeight, float boxWidth, float boxDepth,
        float glueFlapWidth, float tuckFlapWidth, Image boxTexture
    ) {
        super(
            boxHeight, boxWidth, boxDepth,
            buildTexCoords(
                boxHeight, boxWidth, boxDepth, glueFlapWidth, tuckFlapWidth
            ),
            buildFaces(), boxTexture
        );
    }
    
    private static float[] buildTexCoords(
        float boxHeight, float boxWidth, float boxDepth,
        float glueFlapWidth, float tuckFlapWidth
    ) {
        float foldingCartonWidth = boxHeight + boxDepth * 2f + tuckFlapWidth * 2f;
        float foldingCartonLength = boxWidth * 2f + boxDepth * 2f + glueFlapWidth;
        float y1 = tuckFlapWidth / foldingCartonWidth;
        float y2 = (tuckFlapWidth + boxDepth) / foldingCartonWidth;
        float y3 = 1f - y2;
        float y4 = 1f - y1;
        float x1 = boxDepth / foldingCartonLength;
        float x2 = (boxDepth + boxWidth) / foldingCartonLength;
        float x3 = (boxDepth * 2f + boxWidth) / foldingCartonLength;
        float x4 = 1f - glueFlapWidth / foldingCartonLength;
            
        return new float[] {
             0, y2,
            x1, y2,
            x1, y1,
            x2, y1,
            x2, y2,
            x3, y2,
            x4, y2,
            x4, y3,
            x4, y4,
            x3, y4,
            x3, y3,
            x2, y3,
            x1, y3,
             0, y3
        };
    }
    
    private static int[] buildFaces() {
        return new int[] {
            0,  5,   2,  7,   1,  6,
            2,  7,   0,  5,   3, 10,
            1,  0,   6, 12,   5,  1,
            6, 12,   1,  0,   2, 13,
            5,  1,   7, 11,   4,  4,
            7, 11,   5,  1,   6, 12,
            4,  4,   3, 10,   0,  5,
            3, 10,   4,  4,   7, 11,
            3, 10,   6,  8,   2,  7,
            6,  8,   3, 10,   7,  9,
            4,  4,   1,  2,   5,  1,
            1,  2,   4,  4,   0,  3
        };
    }
}
