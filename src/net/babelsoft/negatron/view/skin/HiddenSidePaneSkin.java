/**
 * Copyright (c) 2014, 2015, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.babelsoft.negatron.view.skin;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.HiddenSidePane;
import net.babelsoft.negatron.view.control.TitledWindowPane;
import net.babelsoft.negatron.view.control.TitledWindowPane.DisplayMode;

/**
 * Heavily mmodified version of HiddenSidesPaneSkin from ControlsFX
 * @author capan
 */
public class HiddenSidePaneSkin extends SkinBase<HiddenSidePane> {

    private final StackPane stackPane;
    private final InvalidationListener displayModeListener;
    private Delegate onceOnAnimationEnded;
    
    public HiddenSidePaneSkin(HiddenSidePane pane) {
        super(pane);
        
        displayModeListener = observable -> animate();
        
        stackPane = new StackPane();
        getChildren().add(stackPane);
        updateStackPane();

        InvalidationListener rebuildListener = observable -> updateStackPane();
        pane.contentProperty().addListener(rebuildListener);
        pane.topProperty().addListener(rebuildListener);
        pane.rightProperty().addListener(rebuildListener);
        pane.bottomProperty().addListener(rebuildListener);
        pane.leftProperty().addListener(rebuildListener);
        
        for (Side side : Side.values()) {
            visibility[side.ordinal()] = new SimpleDoubleProperty(0);
            visibility[side.ordinal()].addListener(observable -> getSkinnable().requestLayout());
        }
        
        Rectangle clip = new Rectangle();
        clip.setX(0);
        clip.setY(0);
        clip.widthProperty().bind(getSkinnable().widthProperty());
        clip.heightProperty().bind(getSkinnable().heightProperty());

        getSkinnable().setClip(clip);
    }
    
    private final DoubleProperty[] visibility = new SimpleDoubleProperty[Side.values().length];
    private Timeline timeline;
    
    private void animate() {
        if (timeline != null)
            timeline.stop();

        KeyValue[] keyValues = new KeyValue[Side.values().length];
        for (Side side : Side.values()) {
            TitledWindowPane window = null;
            switch (side) {
                case TOP:
                    window = getSkinnable().getTop();
                    break;
                case RIGHT:
                    window = getSkinnable().getRight();
                    break;
                case BOTTOM:
                    window = getSkinnable().getBottom();
                    break;
                case LEFT:
                    window = getSkinnable().getLeft();
                    break;
            }
            
            double value = 0.0;
            if (window != null) {
                switch (window.getDisplayMode()) {
                    case INTERMEDIATE:
                        value = 0.5;
                        break;
                    case MAXIMISED:
                        value = 1.0;
                        break;
                }
                if (window.getOnceOnAnimationEnded() != null) {
                    onceOnAnimationEnded = window.getOnceOnAnimationEnded();
                    window.setOnceOnAnimationEnded(null);
                }
            }
            
            keyValues[side.ordinal()] = new KeyValue(visibility[side.ordinal()], value);
        }

        Duration duration = getSkinnable().getAnimationDuration() != null ?
            getSkinnable().getAnimationDuration() : Duration.millis(200)
        ;

        KeyFrame keyFrame = new KeyFrame(duration, e -> {
            if (onceOnAnimationEnded != null) {
                onceOnAnimationEnded.fire();
                onceOnAnimationEnded = null;
            }
        }, keyValues);
        timeline = new Timeline(keyFrame);
        timeline.play();
    }
    
    private void updateStackPane() {
        stackPane.getChildren().clear();
        TitledWindowPane top = getSkinnable().getTop();
        TitledWindowPane right = getSkinnable().getRight();
        TitledWindowPane bottom = getSkinnable().getBottom();
        TitledWindowPane left = getSkinnable().getLeft();

        if (getSkinnable().getContent() != null) {
            stackPane.getChildren().add(getSkinnable().getContent());
        }
        if (top != null) {
            stackPane.getChildren().add(top);
            top.setManaged(false);
            top.displayModeProperty().removeListener(displayModeListener);
            top.displayModeProperty().addListener(displayModeListener);
        }
        if (right != null) {
            stackPane.getChildren().add(right);
            right.setManaged(false);
            right.displayModeProperty().removeListener(displayModeListener);
            right.displayModeProperty().addListener(displayModeListener);
        }
        if (bottom != null) {
            stackPane.getChildren().add(bottom);
            bottom.setManaged(false);
            bottom.displayModeProperty().removeListener(displayModeListener);
            bottom.displayModeProperty().addListener(displayModeListener);
        }
        if (left != null) {
            stackPane.getChildren().add(left);
            left.setManaged(false);
            left.displayModeProperty().removeListener(displayModeListener);
            left.displayModeProperty().addListener(displayModeListener);
        }
    }

    @Override
    protected void layoutChildren(
        double contentX, double contentY, double contentWidth, double contentHeight
    ) {

        /*
         * Layout the stackpane in a normal way (equals
         * "lay out the content node", the only managed node)
         */
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        // layout the unmanaged side nodes

        TitledWindowPane bottom = getSkinnable().getBottom();
        if (bottom != null) {
            DisplayMode displayMode = bottom.getDisplayMode();
            DisplayMode previousDisplayMode = bottom.getPreviousDisplayMode();
            double visibilityValue = visibility[Side.BOTTOM.ordinal()].get();
            double prefHeight = contentHeight;
            double offset = visibilityValue;
            
            if (displayMode == DisplayMode.MAXIMISED ||
                displayMode == DisplayMode.INTERMEDIATE && previousDisplayMode == DisplayMode.MAXIMISED && visibilityValue != 0.5 ||
                displayMode == DisplayMode.HIDDEN && (previousDisplayMode == DisplayMode.INTERMEDIATE || previousDisplayMode == DisplayMode.MAXIMISED) && visibilityValue != 0.0
            ) {
                offset *= prefHeight;
            } else {
                prefHeight *= 0.5;
                offset *= contentHeight;
            }
            
            bottom.resizeRelocate(
                contentX, contentY + (contentHeight - offset), contentWidth, prefHeight
            );
            bottom.setVisible(visibilityValue > 0);
        }

        Node left = getSkinnable().getLeft();
        if (left != null) {
            double prefWidth = contentWidth;
            double offset = prefWidth * visibility[Side.LEFT.ordinal()].get();
            left.resizeRelocate(
                contentX - (prefWidth - offset), contentY, prefWidth, contentHeight
            );
            left.setVisible(visibility[Side.LEFT.ordinal()].get() > 0);
        }

        Node right = getSkinnable().getRight();
        if (right != null) {
            double prefWidth = contentWidth;
            double offset = prefWidth * visibility[Side.RIGHT.ordinal()].get();
            right.resizeRelocate(
                contentX + contentWidth - offset, contentY, prefWidth, contentHeight
            );
            right.setVisible(visibility[Side.RIGHT.ordinal()].get() > 0);
        }

        TitledWindowPane top = getSkinnable().getTop();
        if (top != null) {
            DisplayMode displayMode = top.getDisplayMode();
            DisplayMode previousDisplayMode = top.getPreviousDisplayMode();
            double visibilityValue = visibility[Side.TOP.ordinal()].get();
            double prefHeight = contentHeight;
            double offset = visibilityValue * contentHeight;
            
            if (
                displayMode == DisplayMode.MAXIMISED && previousDisplayMode == DisplayMode.HIDDEN ||
                displayMode == DisplayMode.HIDDEN && previousDisplayMode == DisplayMode.MAXIMISED
            ) {
                top.resizeRelocate(
                    contentX, contentY - (prefHeight - offset), contentWidth, prefHeight
                );
            } else if (
                displayMode == DisplayMode.INTERMEDIATE && previousDisplayMode == DisplayMode.HIDDEN ||
                displayMode == DisplayMode.HIDDEN && previousDisplayMode == DisplayMode.INTERMEDIATE
            ) {
                prefHeight *= 0.5;
                
                top.resizeRelocate(
                    contentX, contentY - (prefHeight - offset), contentWidth, prefHeight
                );
            } else if (
                displayMode == DisplayMode.MAXIMISED && previousDisplayMode == DisplayMode.INTERMEDIATE ||
                displayMode == DisplayMode.INTERMEDIATE && previousDisplayMode == DisplayMode.MAXIMISED
            ) {
                prefHeight *= visibilityValue;
            
                top.resizeRelocate(
                    contentX, contentY, contentWidth, prefHeight
                );
            }
            
            top.setVisible(visibilityValue > 0);
        }
    }
}
