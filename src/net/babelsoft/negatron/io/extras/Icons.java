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
package net.babelsoft.negatron.io.extras;

import com.twelvemonkeys.image.ResampleOp;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.GrayFilter;
import net.babelsoft.negatron.io.configuration.Property;

/**
 *
 * @author capan
 */
public class Icons {
    
    public static final String EXTENSION = ".ico";
    public static final Image DEFAULT_ICON = new Image(
        Icons.class.getResourceAsStream("/net/babelsoft/negatron/resource/icon/Negatron.png")
    );
    
    private Icons() { }
    
    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    private static BufferedImage toBufferedImage(java.awt.Image img)
    {
       if (img instanceof BufferedImage)
           return (BufferedImage) img;

       // Create a buffered image with transparency
       BufferedImage bImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

       // Draw the image on to the buffered image
       Graphics2D bGr = bImg.createGraphics();
       bGr.drawImage(img, 0, 0, null);
       bGr.dispose();

       // Return the buffered image
       return bImg;
    }
    
    public static Image newImage(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return newImage(stream, 0, false);
        }
    }
    
    public static Image newImage(String name) throws IOException {
        return newImage(name, 0, false);
    }
    
    public static Image newImage(String name, int size) throws IOException {
        return newImage(name, size, false);
    }
    
    public static Image newGreyImage(String name, int size) throws IOException {
        return newImage(name, size, true);
    }
    
    public static Image newImage(String name, int size, boolean requestGrey) throws IOException {
        try (InputStream stream = Extras.newInputStream(name, Property.ICON, EXTENSION)) {
            return newImage(stream, size, requestGrey);
        }
    }
    
    public static Image newImage(Path path, int size) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return newImage(stream, size, false);
        }
    }
    
    public static Image newImage(ZipFile file, ZipEntry entry, int size) throws IOException {
        try (InputStream stream = Extras.newInputStream(file, entry)) {
            return newImage(stream, size, false);
        }
    }
    
    public static Image newImage(InputStream stream, int size, boolean requestGrey) throws IOException {
        if (stream != null) try (
            ImageInputStream imageStream = ImageIO.createImageInputStream(stream)
        ) {
            ImageReader reader = ImageIO.getImageReaders(imageStream).next();
            reader.setInput(imageStream);
            
            BufferedImage image = reader.read(0, null);
            if (size > 0) {
                BufferedImageOp resampler = new ResampleOp(size, size, ResampleOp.FILTER_TRIANGLE);
                image = resampler.filter(image, null);
            }
            if (requestGrey) {
                image = toBufferedImage(GrayFilter.createDisabledImage(image));
            }

            return SwingFXUtils.toFXImage(image, null);
        } else
            return null;
    }
}
