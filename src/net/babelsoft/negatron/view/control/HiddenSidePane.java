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
package net.babelsoft.negatron.view.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Duration;
import net.babelsoft.negatron.view.skin.HiddenSidePaneSkin;

/**
 * Heavily modified version of HiddenSidesPane from ControlsFX
 * @author capan
 */
public class HiddenSidePane extends Control {

    /**
     * Constructs a new pane with the given content node and the four side
     * nodes. Each one of the side nodes may be null.
     * 
     * @param content
     *            the primary node that will fill the entire width and height of
     *            the pane
     * @param top
     *            the hidden node on the top side
     * @param right
     *            the hidden node on the right side
     * @param bottom
     *            the hidden node on the bottom side
     * @param left
     *            the hidden node on the left side
     */
    public HiddenSidePane(Node content, TitledWindowPane top, TitledWindowPane right, TitledWindowPane bottom, TitledWindowPane left) {
        setContent(content);
        setTop(top);
        setRight(right);
        setBottom(bottom);
        setLeft(left);
    }

    /**
     * Constructs a new pane with no content and no side nodes.
     */
    public HiddenSidePane() {
        this(null, null, null, null, null);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new HiddenSidePaneSkin(this);
    }
    
    // Content node support.

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$

    /**
     * The property that is used to store a reference to the content node. The
     * content node will fill the entire width and height of the pane.
     * 
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    /**
     * Returns the value of the content node property.
     * 
     * @return the content node property value
     */
    public final Node getContent() {
        return contentProperty().get();
    }

    /**
     * Sets the value of the content node property.
     * 
     * @param content
     *            the new content node
     */
    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    // Top node support.

    private final ObjectProperty<TitledWindowPane> top = new SimpleObjectProperty<>(this, "top"); //$NON-NLS-1$

    /**
     * The property used to store a reference to the node shown at the top side
     * of the pane.
     * 
     * @return the hidden node at the top side of the pane
     */
    public final ObjectProperty<TitledWindowPane> topProperty() {
        return top;
    }

    /**
     * Returns the value of the top node property.
     * 
     * @return the top node property value
     */
    public final TitledWindowPane getTop() {
        return topProperty().get();
    }

    /**
     * Sets the value of the top node property.
     * 
     * @param top
     *            the top node value
     */
    public final void setTop(TitledWindowPane top) {
        topProperty().set(top);
    }

    // Right node support.

    /**
     * The property used to store a reference to the node shown at the right
     * side of the pane.
     * 
     * @return the hidden node at the right side of the pane
     */
    private final ObjectProperty<TitledWindowPane> right = new SimpleObjectProperty<>(this, "right"); //$NON-NLS-1$

    /**
     * Returns the value of the right node property.
     * 
     * @return the right node property value
     */
    public final ObjectProperty<TitledWindowPane> rightProperty() {
        return right;
    }

    /**
     * Returns the value of the right node property.
     * 
     * @return the right node property value
     */
    public final TitledWindowPane getRight() {
        return rightProperty().get();
    }

    /**
     * Sets the value of the right node property.
     * 
     * @param right
     *            the right node value
     */
    public final void setRight(TitledWindowPane right) {
        rightProperty().set(right);
    }

    // Bottom node support.

    /**
     * The property used to store a reference to the node shown at the bottom
     * side of the pane.
     * 
     * @return the hidden node at the bottom side of the pane
     */
    private final ObjectProperty<TitledWindowPane> bottom = new SimpleObjectProperty<>(this, "bottom"); //$NON-NLS-1$

    /**
     * Returns the value of the bottom node property.
     * 
     * @return the bottom node property value
     */
    public final ObjectProperty<TitledWindowPane> bottomProperty() {
        return bottom;
    }

    /**
     * Returns the value of the bottom node property.
     * 
     * @return the bottom node property value
     */
    public final TitledWindowPane getBottom() {
        return bottomProperty().get();
    }

    /**
     * Sets the value of the bottom node property.
     * 
     * @param bottom
     *            the bottom node value
     */
    public final void setBottom(TitledWindowPane bottom) {
        bottomProperty().set(bottom);
    }

    // Left node support.

    /**
     * The property used to store a reference to the node shown at the left side
     * of the pane.
     * 
     * @return the hidden node at the left side of the pane
     */
    private final ObjectProperty<TitledWindowPane> left = new SimpleObjectProperty<>(this, "left"); //$NON-NLS-1$

    /**
     * Returns the value of the left node property.
     * 
     * @return the left node property value
     */
    public final ObjectProperty<TitledWindowPane> leftProperty() {
        return left;
    }

    /**
     * Returns the value of the left node property.
     * 
     * @return the left node property value
     */
    public final TitledWindowPane getLeft() {
        return leftProperty().get();
    }

    /**
     * Sets the value of the left node property.
     * 
     * @param left
     *            the left node value
     */
    public final void setLeft(TitledWindowPane left) {
        leftProperty().set(left);
    }
    
    // slide in / slide out duration

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(
        this, "animationDuration", Duration.millis(200)
    ); //$NON-NLS-1$

    /**
     * Returns the animation duration property. The value of this property
     * determines the fade in time for a hidden side to become visible.
     * 
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    /**
     * Returns the animation delay
     * 
     * @return animation delay
     */
    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * Set the animation delay
     * 
     * @param duration
     *            animation duration
     */
    public final void setAnimationDuration(Duration duration) {
        animationDuration.set(duration);
    }
}
