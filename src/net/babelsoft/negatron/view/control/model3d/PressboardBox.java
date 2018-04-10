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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * cardboard / pressboard box (boîte cartonnée / en carton compact) 纸盒
 * folding style (gabarit) 折叠纸盒的结构形式
 * http://www.since1878.com/folding-carton-styles.php
 * http://www.fortneypackages.com/products/folding-cartons/?t=cartons
 * http://www.imperialbox.net/custom-boxes-styles
 * http://www.colbertpkg.com/foldingcartons.html
 * https://boxtemplates.wordpress.com/
    Standard Reverse Tuck
    Standard Straight Tuck
    French Reverse Tuck
    Tuck Top Auto Bottom
    Airplane Style Straight Tuck

 * @author capan
 */
public abstract class PressboardBox extends MeshView {
    
    protected PressboardBox(
        float boxHeight, float boxWidth, float boxDepth,
        float[] texCoords, int[] faces, Image boxTexture
    ) {
        this(
            buildPoints(boxHeight, boxWidth, boxDepth),
            texCoords, faces, boxTexture
        );
    }
    
    protected PressboardBox(
        float[] points, float[] texCoords, int[] faces, Image boxTexture
    ) {
        // Specifies hard edges
        int[] faceSmoothingGroups = new int[points.length / 2]; // all automatically initialize to the value 0
        
        // Assemble mesh together
        TriangleMesh caseMesh = new TriangleMesh();
        caseMesh.getPoints().setAll(points);
        caseMesh.getTexCoords().setAll(texCoords);
        caseMesh.getFaces().setAll(faces);
        caseMesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);
        setMesh(caseMesh);
        
        // Add texture
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(boxTexture);
        setMaterial(material);
    }
    
    private static float[] buildPoints(
        float boxHeight, float boxWidth, float boxDepth
    ) {
        float hw = boxWidth / 2f;
        float hh = boxHeight / 2f;
        float hd = boxDepth / 2f;

        return new float[] {
            -hw, -hh, -hd,
             hw, -hh, -hd,
             hw,  hh, -hd,
            -hw,  hh, -hd,
            -hw, -hh,  hd,
             hw, -hh,  hd,
             hw,  hh,  hd,
            -hw,  hh,  hd
        };
    }
}
