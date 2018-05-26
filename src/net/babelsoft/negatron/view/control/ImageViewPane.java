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
package net.babelsoft.negatron.view.control;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.util.Strings;

/**
 * 
 * @author akouznet, capan
 */
public class ImageViewPane extends Region {
    
    private final ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<>();
    private String dragCopyPath;
    private Delegate onDropCompleted;

    public ImageViewPane() {
        this(new ImageView());
    }
    
    public ImageViewPane(Image image) {
        this((ImageView) null);
        setImage(image, true);
    }
    
    public ImageViewPane(ImageView imageView) {
        imageViewProperty.addListener((o, oldValue, newValue) -> {
            if (oldValue != null)
                getChildren().remove(oldValue);
            if (newValue != null)
                getChildren().add(newValue);
        });
        imageViewProperty.set(imageView);
        
        final DropShadow ds = new DropShadow(10, Color.RED);
        final Function<Dragboard, Boolean> isAcceptable =
            dragBoard -> Strings.isValid(dragCopyPath) && dragBoard.hasUrl() && dragBoard.getUrl().matches("[\\s\\S]+\\.(png|jpg)")
        ;
        setOnDragOver(event -> {
            if (isAcceptable.apply(event.getDragboard()))
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });
        setOnDragEntered(event -> {
            if (isAcceptable.apply(event.getDragboard()))
                setEffect(ds);
            event.consume();
        });
        setOnDragExited(event -> {
            if (isAcceptable.apply(event.getDragboard()))
                setEffect(null);
            event.consume();
        });
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (isAcceptable.apply(db)) try {
                String url = db.getUrl();
                int index = url.lastIndexOf(".");
                String extension = url.substring(index);
                
                Path targetPath = Paths.get(dragCopyPath + extension);
                File targetDir = targetPath.getParent().toFile();
                if (!targetDir.exists())
                    targetDir.mkdirs();
                
                Path tmpPath;
                if (extension.endsWith("jpg"))
                    tmpPath = Paths.get(dragCopyPath + ".png");
                else
                    tmpPath = Paths.get(dragCopyPath + ".jpg");
                Files.deleteIfExists(tmpPath);
                
                Files.copy(Paths.get(new URL(url.replace(" ", "%20")).toURI()), targetPath, StandardCopyOption.REPLACE_EXISTING);
                if (onDropCompleted != null)
                    onDropCompleted.fire();
                success = true;
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(ImageViewPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    public ObjectProperty<ImageView> imageViewProperty() {
        return imageViewProperty;
    }
    
    public ImageView getImageView() {
        return imageViewProperty.get();
    }
    
    public void setImageView(ImageView imageView) {
        this.imageViewProperty.set(imageView);
    }
    
    public void setImage(Image image) {
        PixelReader reader = image.getPixelReader();
        if (reader != null) {
            // crude interpolated extension of the original image
            /*Stop[] stops = new Stop[] {
                new Stop(0, reader.getColor(0, 0)),
                new Stop(0, reader.getColor((int) image.getWidth() - 1, (int) image.getHeight() - 1))
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);*/

            boolean isBig = image.getWidth() >= 5 || image.getHeight() >= 5;
            
            Color topLeftColour = isBig ? reader.getColor(5, 5) : reader.getColor(0, 0);
            Color topRightColour = isBig ? reader.getColor((int) image.getWidth() - 6, 5) : reader.getColor(0, 0);
            Color bottomLeftColour = isBig ? reader.getColor(5, (int) image.getHeight() - 6) : reader.getColor(0, 0);
            Color bottomRightColour = isBig ? reader.getColor((int) image.getWidth() - 6, (int) image.getHeight() - 6) : reader.getColor(0, 0);

            if (topLeftColour == topRightColour || bottomLeftColour == bottomRightColour)
                setStyle("-fx-background-color: linear-gradient("
                    + "to bottom, " + toRgbaCode(topLeftColour) + ", " + toRgbaCode(bottomRightColour) +
                ")");
            else if (topLeftColour == bottomLeftColour || topRightColour == bottomRightColour)
                setStyle("-fx-background-color: linear-gradient("
                    + "to right, " + toRgbaCode(topLeftColour) + ", " + toRgbaCode(bottomRightColour) +
                ")");
            else
                setStyle("-fx-background-color: linear-gradient("
                    + "to bottom right, " +
                    toRgbaCode(topLeftColour) + " 0%, " +
                    toRgbaCode(bottomLeftColour) + " 50%, " +
                    toRgbaCode(bottomRightColour) + " 100%" +
                ")");
        } else // an error occured while ImageIO tried to decode the current image file to a raw bitmap, so put a specific background to warn users
            setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, white 0%, white 50%, red 60%)");
        
        setImage(image, true);
    }
    
    public void setImage(Image image, boolean preserveRatio) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(preserveRatio);
        imageView.setPickOnBounds(true);
        setImageView(imageView);
    }

    public void setDragCopyPath(String dragCopyPath) {
        this.dragCopyPath = dragCopyPath;
    }
    
    public void setOnDropCompleted(Delegate delegate) {
        onDropCompleted = delegate;
    }
    
    // --- tooltip
    /**
     * The ToolTip for this control.
     */
    public final ObjectProperty<Tooltip> tooltipProperty() {
        if (tooltip == null) {
            tooltip = new ObjectPropertyBase<Tooltip>() {
                private Tooltip old = null;
                @Override protected void invalidated() {
                    Tooltip t = get();
                    // install / uninstall
                    if (t != old) {
                        if (old != null) {
                            Tooltip.uninstall(ImageViewPane.this, old);
                        }
                        if (t != null) {
                            Tooltip.install(ImageViewPane.this, t);
                        }
                        old = t;
                    }
                }

                @Override
                public Object getBean() {
                    return ImageViewPane.this;
                }

                @Override
                public String getName() {
                    return "tooltip";
                }
            };
        }
        return tooltip;
    }
    private ObjectProperty<Tooltip> tooltip;
    public final void setTooltip(Tooltip value) { tooltipProperty().setValue(value); }
    public final Tooltip getTooltip() { return tooltip == null ? null : tooltip.getValue(); }

    @Override
    protected void layoutChildren() {
        ImageView imageView = imageViewProperty.get();
        if (imageView != null) {
            imageView.setFitWidth(getWidth());
            imageView.setFitHeight(getHeight());
            layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
        super.layoutChildren();
    }
    
    private String toRgbaCode(Color color) {
        return String.format(
            "rgba(%d,%d,%d,%f)",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 ),
            color.getOpacity()
        );
    }
}
