/**
 * Copyright (c) 2013, 2015 ControlsFX
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
package net.babelsoft.negatron.view.control.adapter;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import net.babelsoft.negatron.util.Duplicatable;

import java.lang.ref.WeakReference;
import java.util.Collection;

/**
 * Convenience class for users of the {@link Action} API. Primarily this class
 * is used to conveniently create UI controls from a given Action (this is
 * necessary for now as there is no built-in support for Action in JavaFX
 * UI controls at present).
 *
 * <p>Some of the methods in this class take a {@link Collection} of
 * {@link Action actions}. In these cases, it is likely they are designed to
 * work with {@link ActionGroup action groups}. For examples on how to work with
 * these methods, refer to the {@link ActionGroup} class documentation.
 *
 * @see Action
 * @see ActionGroup
 */
@SuppressWarnings("deprecation")
public class ActionUtils {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    private ActionUtils() {
        // no-op
    }

    /***************************************************************************
     *                                                                         *
     * Action API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Action text behavior.
     * Defines uniform action's text behavior for multi-action controls such as toolbars and menus
     */
    public enum ActionTextBehavior {
        /**
         * Text is shown as usual on related control
         */
        SHOW,

        /**
         * Text is not shown on the related control
         */
        HIDE,
    }


    /**
     * Takes the provided {@link Action} and returns a {@link Button} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link Button} should bind to.
     * @param textBehavior Defines {@link ActionTextBehavior}
     * @return A {@link Button} that is bound to the state of the provided
     *      {@link Action}
     */
    public static Button createButton(final Action action, final ActionTextBehavior textBehavior) {
        return configure(new Button(), action, textBehavior);
    }

    /**
     * Takes the provided {@link Action} and returns a {@link Button} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link Button} should bind to.
     * @return A {@link Button} that is bound to the state of the provided
     *      {@link Action}
     */
    public static Button createButton(final Action action) {
        return configure(new Button(), action, ActionTextBehavior.SHOW);
    }

    /**
     * Takes the provided {@link Action} and binds the relevant properties to
     * the supplied {@link Button}. This allows for the use of Actions
     * within custom Button subclasses.
     *
     * @param action The {@link Action} that the {@link Button} should bind to.
     * @param button The {@link ButtonBase} that the {@link Action} should be bound to.
     * @param textBehavior Defines {@link ActionTextBehavior}
     * @return The {@link ButtonBase} that was bound to the {@link Action}.
     */
    public static ButtonBase configureButton(final Action action, ButtonBase button, final ActionTextBehavior textBehavior) {
        return configure(button, action, textBehavior);
    }

    /**
     * Takes the provided {@link Action} and binds the relevant properties to
     * the supplied {@link Button}. This allows for the use of Actions
     * within custom Button subclasses.
     *
     * @param action The {@link Action} that the {@link Button} should bind to.
     * @param button The {@link ButtonBase} that the {@link Action} should be bound to.
     * @return The {@link ButtonBase} that was bound to the {@link Action}.
     */
    public static ButtonBase configureButton(final Action action, ButtonBase button) {
        return configureButton(action, button, ActionTextBehavior.SHOW);
    }

    /**
     * Removes all bindings and listeners which were added when the supplied
     * {@link ButtonBase} was bound to an {@link Action} via one of the methods
     * of this class.
     *
     * @param button a {@link ButtonBase} that was bound to an {@link Action}
     */
    public static void unconfigureButton(ButtonBase button) {
        unconfigure(button);
    }

    /**
     * Takes the provided {@link Action} and returns a {@link Hyperlink} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link Hyperlink} should bind to.
     * @return A {@link Hyperlink} that is bound to the state of the provided
     *      {@link Action}
     */
    public static Hyperlink createHyperlink(final Action action) {
        return configure(new Hyperlink(), action, ActionTextBehavior.SHOW);
    }

    /**
     * Takes the provided {@link Action} and returns a {@link ToggleButton} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link ToggleButton} should bind to.
     * @param textBehavior Defines {@link ActionTextBehavior}
     * @return A {@link ToggleButton} that is bound to the state of the provided
     *      {@link Action}
     */
    public static ToggleButton createToggleButton(final Action action, final ActionTextBehavior textBehavior ) {
        return configure(new ToggleButton(), action, textBehavior);
    }

    /**
     * Takes the provided {@link Action} and returns a {@link ToggleButton} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link ToggleButton} should bind to.
     * @return A {@link ToggleButton} that is bound to the state of the provided
     *      {@link Action}
     */
    public static ToggleButton createToggleButton( final Action action ) {
        return createToggleButton( action, ActionTextBehavior.SHOW );
    }

    /**
     * Takes the provided {@link Action} and returns a {@link CheckBox} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link CheckBox} should bind to.
     * @return A {@link CheckBox} that is bound to the state of the provided
     *      {@link Action}
     */
    public static CheckBox createCheckBox(final Action action) {
        return configure(new CheckBox(), action, ActionTextBehavior.SHOW);
    }

    /**
     * Takes the provided {@link Action} and returns a {@link RadioButton} instance
     * with all relevant properties bound to the properties of the Action.
     *
     * @param action The {@link Action} that the {@link RadioButton} should bind to.
     * @return A {@link RadioButton} that is bound to the state of the provided
     *      {@link Action}
     */
    public static RadioButton createRadioButton(final Action action) {
        return configure(new RadioButton(), action, ActionTextBehavior.SHOW);
    }



    /***************************************************************************
     *                                                                         *
     * ActionGroup API                                                         *
     *                                                                         *
     **************************************************************************/


    /**
     * Action representation of the generic separator. Adding this action anywhere in the
     * action tree serves as indication that separator has be created in its place.
     * See {@link ActionGroup} for example of action tree creation
     */
    public static Action ACTION_SEPARATOR = new Action(null, null) {
        @Override public String toString() {
            return "Separator";  //$NON-NLS-1$
        }
    };

    public static Action ACTION_SPAN = new Action(null, null) {
        @Override public String toString() {
            return "Span";  //$NON-NLS-1$
        }
    };
    

    /***************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private static Node copyNode( Node node ) {
        if ( node instanceof ImageView ) {
            return new ImageView( ((ImageView)node).getImage());
        } else if ( node instanceof Duplicatable<?> ) {
            return (Node) ((Duplicatable<?>)node).duplicate();
        } else {
            return null;
        }
    }

    // Carry over action style classes changes to the styleable
    // Binding as not a good solution since it wipes out existing styleable classes
    private static void bindStyle(final Styleable styleable, final Action action ) {
        styleable.getStyleClass().addAll( action.getStyleClass() );
        action.getStyleClass().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while(c.next()) {
                    if (c.wasRemoved()) {
                        styleable.getStyleClass().removeAll(c.getRemoved());
                    }
                    if (c.wasAdded()) {
                        styleable.getStyleClass().addAll(c.getAddedSubList());
                    }
                }
            }
        });
    }

    private static <T extends ButtonBase> T configure(final T btn, final Action action, final ActionTextBehavior textBehavior) {
        if (action == null) {
            throw new NullPointerException("Action can not be null"); //$NON-NLS-1$
        }

        // button bind to action properties

        bindStyle(btn,action);

        //btn.textProperty().bind(action.textProperty());
        if ( textBehavior == ActionTextBehavior.SHOW ) {
            btn.textProperty().bind(action.textProperty());
        }
        btn.disableProperty().bind(action.disabledProperty());


        btn.graphicProperty().bind(new ObjectBinding<Node>() {
            { bind(action.graphicProperty()); }

            @Override protected Node computeValue() {
                return copyNode(action.graphicProperty().get());
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                super.removeListener(listener);
                unbind(action.graphicProperty());
            }
        });


        // add all the properties of the action into the button, and set up
        // a listener so they are always copied across
        btn.getProperties().putAll(action.getProperties());
        action.getProperties().addListener(new ButtonPropertiesMapChangeListener<>(btn, action));

        // tooltip requires some special handling (i.e. don't have one when
        // the text property is null
        btn.tooltipProperty().bind(new ObjectBinding<Tooltip>() {
            private Tooltip tooltip = new Tooltip();
            private StringBinding textBinding = new When(action.longTextProperty().isEmpty()).then(action.textProperty()).otherwise(action.longTextProperty());

            {
                bind(textBinding);
                tooltip.textProperty().bind(textBinding);
            }

            @Override protected Tooltip computeValue() {
                String longText =  textBinding.get();
                return longText == null || textBinding.get().isEmpty() ? null : tooltip;
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                super.removeListener(listener);
                unbind(action.longTextProperty());
                tooltip.textProperty().unbind();
            }
        });



        // Handle the selected state of the button if it is of the applicable type

        if ( btn instanceof ToggleButton ) {
            ((ToggleButton)btn).selectedProperty().bindBidirectional(action.selectedProperty());
        }

        // Just call the execute method on the action itself when the action
        // event occurs on the button
        btn.setOnAction(action);

        return btn;
    }

    private static void unconfigure(final ButtonBase btn) {
        if (btn == null || !(btn.getOnAction() instanceof Action)) {
            return;
        }

        Action action = (Action) btn.getOnAction();

        btn.styleProperty().unbind();
        btn.textProperty().unbind();
        btn.disableProperty().unbind();
        btn.graphicProperty().unbind();

        action.getProperties().removeListener(new ButtonPropertiesMapChangeListener<>(btn, action));

        btn.tooltipProperty().unbind();

        if (btn instanceof ToggleButton) {
            ((ToggleButton) btn).selectedProperty().unbindBidirectional(action.selectedProperty());
        }

        btn.setOnAction(null);
    }

    private static class ButtonPropertiesMapChangeListener<T extends ButtonBase> implements MapChangeListener<Object, Object> {

        private final WeakReference<T> btnWeakReference;
        private final Action action;

        private ButtonPropertiesMapChangeListener(T btn, Action action) {
            btnWeakReference = new WeakReference<>(btn);
            this.action = action;
        }

        @Override public void onChanged(MapChangeListener.Change<?, ?> change) {
            T btn = btnWeakReference.get();
            if (btn == null) {
                action.getProperties().removeListener(this);
            } else {
                btn.getProperties().clear();
                btn.getProperties().putAll(action.getProperties());
            }
        }

        @Override
        public boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || getClass() != otherObject.getClass()) {
                return false;
            }

            ButtonPropertiesMapChangeListener<?> otherListener = (ButtonPropertiesMapChangeListener<?>) otherObject;

            T btn = btnWeakReference.get();
            ButtonBase otherBtn = otherListener.btnWeakReference.get();
            if (btn != null ? !btn.equals(otherBtn) : otherBtn != null) {
                return false;
            }
            return action.equals(otherListener.action);
        }

        @Override
        public int hashCode() {
            T btn = btnWeakReference.get();
            int result = btn != null ? btn.hashCode() : 0;
            result = 31 * result + action.hashCode();
            return result;
        }
    }
}
