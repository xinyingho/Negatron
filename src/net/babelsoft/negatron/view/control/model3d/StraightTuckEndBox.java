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
import javafx.scene.transform.Rotate;

/**
 * dust/tuck flap (rabat/languette) 内舌/插舌, top/bottom lid (couvercle/fond) 上下盖, folding carton (étui/boîte pliante) 折叠纸盒
 * http://www.ecopack-sarl.com/index.php?id=9&r=ecopack_produits_par_types_boites
 * http://www.cartonnages-larre.fr/index.php
 * straight tuck end box (boîte à pattes rentrantes opposées) 双插盒 对插盒
 * @author capan
 */
public class StraightTuckEndBox extends PressboardBox {
    
    public StraightTuckEndBox(
        float boxHeight, float boxWidth, float boxDepth,
        float glueFlapWidth, float tuckFlapWidth, boolean lieDown,
        Image boxTexture
    ) {
        super(
            boxHeight, boxWidth, boxDepth,
            buildTexCoords(
                boxHeight, boxWidth, boxDepth, glueFlapWidth, tuckFlapWidth
            ),
            buildFaces(), boxTexture
        );
        
        if (lieDown)
            getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
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
            x3, y3,
            x2, y3,
            x2, y4,
            x1, y4,
            x1, y3,
             0, y3
        };
    }
    
    private static int[] buildFaces() {
        return new int[] {
            0,  5,   2,  7,   1,  6,
            2,  7,   0,  5,   3,  8,
            1,  0,   6, 12,   5,  1,
            6, 12,   1,  0,   2, 13,
            5,  1,   7,  9,   4,  4,
            7,  9,   5,  1,   6, 12,
            4,  4,   3,  8,   0,  5,
            3,  8,   4,  4,   7,  9,
            3, 10,   6, 12,   2, 11,
            6, 12,   3, 10,   7,  9,
            4,  4,   1,  2,   5,  1,
            1,  2,   4,  4,   0,  3
        };
    }
}
