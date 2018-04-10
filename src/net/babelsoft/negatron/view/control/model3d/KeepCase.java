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

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Boîtier DVD standard Amaray (keep case or plastic case)
 * jaquette recto ou recto-verso (inlay, insert, wraparound cover)
 * livret (booklet)
 * rond/rondelle/label de DVD (disc label)
 * gabarit pour jaquette : format à plat 273 * 184 mm, format plié 130/129,5 (face arrière et avant) * 184 mm (13/14 mm pour la tranche), textes distants de 2 mm du bord
 * @author capan
 */
public class KeepCase extends Group {
    
    public KeepCase(
        float caseHeight, float caseWidth, float caseDepth, Color caseColour,
        float frontCoverLength, float frontCoverWidth, float spineWidth, Image coverTexture
    ) {
        Group keepCase = buildCase(
            caseHeight, caseWidth, caseDepth, caseColour,
            frontCoverLength, frontCoverWidth
        );
        MeshView wraparoundCover = buildCover(
            frontCoverLength, frontCoverWidth, spineWidth, coverTexture
        );
        
        float gap = (spineWidth - caseDepth) / 2f;
        float coverTranslateX = (frontCoverWidth - caseWidth) / 2f - gap / 2f;
        wraparoundCover.setTranslateX(coverTranslateX);
        
        getChildren().addAll(wraparoundCover, keepCase);
        setTranslateX(gap / 2f); // center the overall 3d object
    }
    
    private Group buildCaseElement(float height, float width, float depth, PhongMaterial material) {
        Box b1 = new Box(width - 2f, height, depth);
        b1.setTranslateX(-1f);
        Box b2 = new Box(2f, height - 4f, depth);
        b2.setTranslateX(width / 2f - 1f);
        Cylinder c1 = new Cylinder(2f, depth);
        c1.getTransforms().addAll(
            new Translate(width / 2f - 2f, height / 2f - 2f, 0),
            new Rotate(90, Rotate.X_AXIS)
        );
        Cylinder c2 = new Cylinder(2f, depth);
        c2.getTransforms().addAll(
            new Translate(width / 2f - 2f, (height / 2f - 2f) * -1f, 0),
            new Rotate(90, Rotate.X_AXIS)
        );
        
        b1.setMaterial(material);
        b2.setMaterial(material);
        c1.setMaterial(material);
        c2.setMaterial(material);
        
        Group element = new Group();
        element.getChildren().addAll(b1, b2, c1, c2);
        return element;
    }
    
    private Group buildCase(
        float caseHeight, float caseWidth, float caseDepth, Color caseColour,
        float frontCoverLength, float frontCoverWidth
    ) {
        PhongMaterial material = new PhongMaterial(caseColour.brighter().brighter());
        material.setSpecularColor(caseColour.brighter().brighter());
        float elementDepth = 1f;
        
        Group ex1 = buildCaseElement(caseHeight, caseWidth, elementDepth, material);
        ex1.setTranslateZ((caseDepth - elementDepth) / 2f);
        Group ex2 = buildCaseElement(caseHeight, caseWidth, elementDepth, material);
        ex2.setTranslateZ((caseDepth - elementDepth) / -2f);
        Box ex3 = new Box(elementDepth, caseHeight, caseDepth);
        ex3.setMaterial(material);
        ex3.setTranslateX((caseWidth - elementDepth) / -2f);
        
        Group in1 = buildCaseElement(frontCoverLength, frontCoverWidth, (caseDepth - elementDepth) / 2f, material);
        in1.setTranslateZ(caseDepth / 4f);
        Group in2 = buildCaseElement(frontCoverLength, frontCoverWidth, (caseDepth - elementDepth) / 2f, material);
        in2.setTranslateZ(caseDepth / -4f);
        
        PhongMaterial coreMaterial = new PhongMaterial(caseColour);
        coreMaterial.setSpecularColor(caseColour);
        Group core = buildCaseElement(frontCoverLength - elementDepth * 2f, frontCoverWidth - elementDepth * 2f, elementDepth * 2f, coreMaterial);
        
        Group g = new Group();
        g.getChildren().addAll(ex1, ex2, ex3, in1, in2, core);
        
        return g;
    }
    
    private MeshView buildCover(float frontCoverLength, float frontCoverWidth, float spineWidth, Image coverTexture) {
        float hw = frontCoverWidth / 2f;
        float hh = frontCoverLength / 2f;
        float hd = spineWidth / 2f;
        float t  = 1f; // paper sleeve thickness

        float[] points = {
            -hw,   -hh, -hd,
             hw,   -hh, -hd,
             hw,    hh, -hd,
            -hw,    hh, -hd,
            
            -hw+t, -hh, -hd+t,
             hw,   -hh, -hd+t,
             hw,    hh, -hd+t,
            -hw+t,  hh, -hd+t,

            -hw+t, -hh,  hd-t,
             hw,   -hh,  hd-t,
             hw,    hh,  hd-t,
            -hw+t,  hh,  hd-t,
            
            -hw,   -hh,  hd,
             hw,   -hh,  hd,
             hw,    hh,  hd,
            -hw,    hh,  hd
        };
        
        /* Determine texture critical points
         *  ______________________________
         * |     back | spine | front     |
         * |          |       |           |
         * |          |       |           |
         *            |       |
         *           x1       x2
         * <------ coverTotalLength ------>
         */
        float coverTotalLength = frontCoverWidth * 2f + spineWidth;
        float x1 = frontCoverWidth / coverTotalLength;
        float x2 = 1f - x1;
            
        float[] texCoords = {
             0, 0,
            x1, 0,
            x2, 0,
             1, 0,
             1, 1,
            x2, 1,
            x1, 1,
             0, 1
        };
        
        int[] faces = {
             0, 2,    2, 4,    1, 3,
             2, 4,    0, 2,    3, 5,
             1, 0,    6, 0,    5, 0,
             6, 0,    1, 0,    2, 0,
             5, 0,    7, 0,    4, 0,
             7, 0,    5, 0,    6, 0,
             4, 0,   11, 0,    8, 0,
            11, 0,    4, 0,    7, 0,
             8, 0,   10, 0,    9, 0,
            10, 0,    8, 0,   11, 0,
             9, 0,   14, 0,   13, 0,
            14, 0,    9, 0,   10, 0,
            13, 0,   15, 6,   12, 1,
            15, 6,   13, 0,   14, 7,
            12, 1,    3, 5,    0, 2,
             3, 5,   12, 1,   15, 6,
            
             3, 0,    6, 0,    2, 0,
             6, 0,    3, 0,    7, 0,
             3, 0,   11, 0,    7, 0,
            11, 0,    3, 0,   15, 0,
            11, 0,   14, 0,   10, 0,
            14, 0,   11, 0,   15, 0,
            
             4, 0,    1, 0,    5, 0,
             1, 0,    4, 0,    0, 0,
            12, 0,    4, 0,    8, 0,
             4, 0,   12, 0,    0, 0,
            12, 0,    9, 0,   13, 0,
             9, 0,   12, 0,    8, 0
        };
        
        // Specifies hard edges
        int[] faceSmoothingGroups = new int[28]; // all automatically initialised to value 0
        
        // Assemble mesh together
        TriangleMesh coverMesh = new TriangleMesh();
        coverMesh.getPoints().setAll(points);
        coverMesh.getTexCoords().setAll(texCoords);
        coverMesh.getFaces().setAll(faces);
        coverMesh.getFaceSmoothingGroups().setAll(faceSmoothingGroups);
        MeshView coverMeshView = new MeshView(coverMesh);
        
        // Add texture
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(coverTexture);
        coverMeshView.setMaterial(material);
        
        return coverMeshView;
    }
}
