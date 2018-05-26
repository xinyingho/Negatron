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

import com.sun.javafx.event.EventDispatchChainImpl;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventDispatchChain;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.babelsoft.negatron.io.Video;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.util.Strings;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.TrackInfo;
import uk.co.caprica.vlcj.player.TrackType;
import uk.co.caprica.vlcj.player.VideoTrackInfo;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

/**
 * Used Oracle's Overlay Media Player code for the layout, and vlcj to replace Oracle's GStreamer back-end.
 * Media playback in JavaFX is done using GStreamer. But on Windows, Oracle set it up to use the default embedded OS codecs, which aren't up to date.
 * So replacing GStreamer by VLC allows Negatron to play any videos on Windows and also solves the libav versioning issues on recent Linux distributions.
 * @author capan
 */
public class MediaViewPane extends Region implements Disposable {
    
    private class CanvasPlayerComponent extends DirectMediaPlayerComponent {
        
        private final WritablePixelFormat<ByteBuffer> pixelFormat;
        
        public CanvasPlayerComponent() {
            super(new CanvasBufferFormatCallback());
            pixelFormat = PixelFormat.getByteBgraPreInstance();
        }
        
        @Override
        public void display(DirectMediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            if (writableImage == null)
                return;
            
            Platform.runLater(() -> {/*
                try {
                    ByteBuffer[] byteBuffers = mediaPlayer.lock();
                    if (byteBuffers != null) {
                        ByteBuffer byteBuffer = byteBuffers[0];
                        pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                    }
                } finally {
                    mediaPlayer.unlock();
                }*/
                ByteBuffer byteBuffer = nativeBuffers[0];
                pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
            });
        }
        
        @Override
        public void finished(MediaPlayer mediaPlayer) {
            if (!mediaPlayer.getRepeat()) {
                _stop();
                hideMediaView();
            }
        }
    }
    
    private class CanvasBufferFormatCallback implements BufferFormatCallback {
        
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            Platform.runLater(() -> initializeImageView(sourceWidth, sourceHeight));
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }
    }
    
    private class DragState {
        private final ReadOnlyObjectProperty<Bounds> boundsProperty;
        private boolean dragEntered;
        
        private DragState(ImageViewPane dropTarget) {
            boundsProperty = dropTarget.boundsInParentProperty();
        }
        
        private boolean isWithinBounds(DragEvent event) {
            return boundsProperty.get().contains(event.getX(), event.getY());
        }
    }

    private static final PseudoClass CSS_PLAYING = PseudoClass.getPseudoClass("playing");
    
    private final DirectMediaPlayerComponent mediaPlayerComponent;
    private final MediaPlayer mediaPlayer;
    private double displayAspectRatio;
    private WritableImage writableImage;
    private ImageView mediaView;
    private PixelWriter pixelWriter;
    
    private final ToolBar mediaButtonBar;
    private final HBox mediaBottomBar;
    private ParallelTransition mediaViewTransition;
    private FadeTransition mediaButtonBarTransition;
    
    private static final EventDispatchChain DUMMY_CHAIN = new EventDispatchChainImpl();
    private final List<ImageViewPane> dropTargets;
    private final Map<ImageViewPane, DragState> dragStates;
    private String dragCopyPath;
    private Delegate onDropCompleted;
    private ImageViewPane previousMouseTarget;
    private Delegate onMouseRightClick;
    
    private static final BooleanProperty loopEnabled;
    private final BooleanProperty playingProperty;
    private boolean isMouseEntered;
    private boolean disabled;
    
    private AnchorPane background;
    
    static {
        loopEnabled = new SimpleBooleanProperty();
        loopEnabled.set(Configuration.Manager.isLoopEnabled());
        loopEnabled.addListener((o, oV, newValue) -> { try {
            Configuration.Manager.updateLoopEnabled(newValue);
        } catch (IOException ex) {
            Logger.getLogger(MediaViewPane.class.getName()).log(Level.SEVERE, "Couldn't save loop/repeat status of media player", ex);
        }});
    }
    
    public MediaViewPane() {
        setStyle("-fx-background-color: rgba(0,0,0,0)");
        
        if (Video.isEnabled()) {
            mediaPlayerComponent = new CanvasPlayerComponent();
            mediaPlayer = mediaPlayerComponent.getMediaPlayer();
            displayAspectRatio = 3.0f / 4.0f;
            
            parentProperty().addListener((o, oV, newParent) -> {
                if (newParent != null && background == null) {
                    ObservableList<Node> nodes = ((StackPane) newParent).getChildren();
                    background = new AnchorPane();
                    background.setStyle("-fx-background-color: -fx-background");
                    background.setOpacity(0);
                    nodes.add(nodes.indexOf(this), background);
                }
            });
            
            // build the video player controls
            isMouseEntered = false;
            
            mediaButtonBar = new ToolBar();
            mediaButtonBar.getStyleClass().add("media-button-bar");
            mediaButtonBar.setStyle("-fx-background-color: rgba(0,0,0,0)");
            
            mediaBottomBar = new HBox();
            mediaBottomBar.setSpacing(0);
            mediaBottomBar.setAlignment(Pos.CENTER);

            Button backButton = new Button("Back");
            backButton.getStyleClass().add("back-button");
            backButton.setOnAction(e -> {
                if (!disabled && mediaView != null) {
                    showMediaView();
                    mediaPlayer.setTime(0);
                    _play();
                }
            });

            Button stopButton = new Button("Stop");
            stopButton.getStyleClass().add("stop-button");
            stopButton.setOnAction(e -> {
                if (!disabled) {
                    _stop();
                    if (isMediaViewShown())
                        hideMediaView();
                }
            });

            Button playButton = new Button("Play");
            playButton.getStyleClass().add("play-button");
            playButton.setOnAction(e -> {
                if (!disabled) {
                    _play();
                    // showMediaView will be triggered by initializeImageView()
                }
            });

            Button pauseButton = new Button("Pause");
            pauseButton.getStyleClass().add("pause-button");
            pauseButton.setOnAction(e -> {
                if (!disabled && mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            });

            Button forwardButton = new Button("Forward");
            forwardButton.getStyleClass().add("forward-button");
            forwardButton.setOnAction(e -> {
                if (!disabled && mediaView != null) {
                    long currentTime = mediaPlayer.getTime();
                    mediaPlayer.setTime(currentTime + 5000); // going forward 5 seconds
                }
            });
            
            ToggleButton loopButton = new ToggleButton("Loop");
            loopButton.getStyleClass().add("loop-button");
            loopButton.setOnAction(e -> {
                if (!disabled) {
                    if (loopButton.isSelected())
                        mediaPlayer.setRepeat(true);
                    else
                        mediaPlayer.setRepeat(false);
                    loopEnabled.set(loopButton.isSelected());
                }
            });

            mediaBottomBar.getChildren().addAll(backButton, stopButton, playButton, pauseButton, forwardButton, loopButton);
            mediaButtonBar.getItems().add(mediaBottomBar);
            mediaButtonBar.setOpacity(0.0);
            
            
            if (loopEnabled.get())
                loopButton.fire();
            loopEnabled.addListener((o, oV, newValue) -> {
                if (newValue != loopButton.isSelected())
                    loopButton.fire();
            });

            // show/hide animation handling of the video player controls
            setOnMouseEntered(event -> {
                if (!disabled && !isMouseEntered)
                    showMediaButtonBar();
            });
            addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
                if (!disabled)
                    hideMediaButtonBar();
            });
            setOnTouchPressed(event -> {
                if (!disabled && mediaView != null) {
                    if (isMouseEntered)
                        hideMediaButtonBar();
                    else
                        showMediaButtonBar();
                }
            });
        } else {
            mediaPlayerComponent = null;
            mediaPlayer = null;
            mediaButtonBar = null;
            mediaBottomBar = null;
        }
        
        playingProperty = new SimpleBooleanProperty();
        playingProperty.addListener((o, oV, newValue) -> pseudoClassStateChanged(CSS_PLAYING, newValue));

        // In a StackPane, only the topmost child (the video player here) gets the mouse event notifications,
        // so we hack around to convey them to the other children (mostly different kinds of screenshots) when necessary
        
        dropTargets = new ArrayList<>();
        
        setOnMouseMoved(event -> {
            if (mediaView == null || mediaView.getOpacity() == 0)
                // re-dispatch events for tooltips
                dropTargets.stream().filter(
                    dropTarget -> dropTarget.getBoundsInParent().contains(event.getX(), event.getY())
                ).findAny().ifPresent(
                    dropTarget -> {
                        if (previousMouseTarget != null && previousMouseTarget != dropTarget) {
                            MouseEvent evt = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_EXITED);
                            previousMouseTarget.getEventDispatcher().dispatchEvent(evt, DUMMY_CHAIN);
                        }
                        dropTarget.getEventDispatcher().dispatchEvent(event, DUMMY_CHAIN);
                        previousMouseTarget = dropTarget;
                    }
                );
        });
        addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            // re-dispatch events for tooltips
            if (previousMouseTarget != null) {
                previousMouseTarget.getEventDispatcher().dispatchEvent(event, DUMMY_CHAIN);
                previousMouseTarget = null;
            }
        });
        setOnMousePressed(event -> {
            // re-dispatch events for tooltips
            dropTargets.stream().filter(
                dropTarget -> dropTarget.getBoundsInParent().contains(event.getX(), event.getY())
            ).findAny().ifPresent(
                dropTarget -> dropTarget.getEventDispatcher().dispatchEvent(event, DUMMY_CHAIN)
            );
        });
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY)
                // re-dispatch events for image zooming
                dropTargets.stream().filter(
                    dropTarget -> dropTarget.getBoundsInParent().contains(event.getX(), event.getY())
                ).findAny().ifPresent(
                    dropTarget -> dropTarget.getEventDispatcher().dispatchEvent(event, DUMMY_CHAIN)
                );
            else if (onMouseRightClick != null && event.getButton() == MouseButton.SECONDARY)
                onMouseRightClick.fire();
        });
        
        // In a StackPane, only the topmost child (the video player here) gets the drag event notifications,
        // so we hack around to convey them to the other children (mostly different kinds of screenshots) when necessary
        
        dragStates = new HashMap<>();
        
        final DropShadow ds = new DropShadow(10, Color.RED);
        final Function<Dragboard, Boolean> isAcceptable =
            dragBoard -> Strings.isValid(dragCopyPath) && dragBoard.hasUrl() && dragBoard.getUrl().matches("[\\s\\S]+\\.(mp4|flv)")
        ;
        setOnDragEntered(event -> {
            dragStates.clear();

            if (isAcceptable.apply(event.getDragboard()))
                setEffect(ds);
            else dropTargets.forEach(
                dropTarget -> {
                    DragState dragState = new DragState(dropTarget);
                    dragStates.put(dropTarget, dragState);

                    if (dragState.isWithinBounds(event)) {
                        dropTarget.onDragEnteredProperty().get().handle(event);
                        dragState.dragEntered = true;
                    }
                }
            );

            event.consume();
        });
        setOnDragOver(event -> {
            if (isAcceptable.apply(event.getDragboard()))
                event.acceptTransferModes(TransferMode.COPY);
            else dropTargets.forEach(dropTarget -> {
                DragState dragState = dragStates.get(dropTarget);
                if (dragState.isWithinBounds(event)) {
                    dropTarget.onDragOverProperty().get().handle(event);
                    if (!dragState.dragEntered) {
                        dropTarget.onDragEnteredProperty().get().handle(event);
                        dragState.dragEntered = true;
                    }
                } else {
                    if (dragState.dragEntered) {
                        dropTarget.onDragExitedProperty().get().handle(event);
                        dragState.dragEntered = false;
                    }
                }
            });

            event.consume();
        });
        setOnDragExited(event -> {
            if (isAcceptable.apply(event.getDragboard()))
                setEffect(null);
            dropTargets.forEach(
                dropTarget -> dropTarget.onDragExitedProperty().get().handle(event)
            );
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

                if (mediaPlayer.isPlaying())
                    _stop();

                Path tmpPath;
                if (extension.endsWith("mp4"))
                    tmpPath = Paths.get(dragCopyPath + ".flv");
                else
                    tmpPath = Paths.get(dragCopyPath + ".mp4");
                Files.deleteIfExists(tmpPath);

                Files.copy(Paths.get(new URL(url.replace(" ", "%20")).toURI()), targetPath, StandardCopyOption.REPLACE_EXISTING);
                if (onDropCompleted != null)
                    onDropCompleted.fire();
                success = true;
            } catch (IOException | URISyntaxException ex) {
                Logger.getLogger(ImageViewPane.class.getName()).log(Level.SEVERE, null, ex);
            } else dropTargets.stream().filter(
                dropTarget -> dragStates.get(dropTarget).isWithinBounds(event)
            ).forEach(
                dropTarget -> dropTarget.onDragDroppedProperty().get().handle(event)
            );

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @Override
    public void dispose() {
        if (mediaPlayerComponent != null)
            mediaPlayerComponent.release(true);
    }

    private void initializeImageView(int width, int height) {
        if (mediaView != null)
            getChildren().remove(mediaView);
        getChildren().remove(mediaButtonBar);

        writableImage = new WritableImage(width, height);
        pixelWriter = writableImage.getPixelWriter();
        mediaView = new ImageView(writableImage);
        getChildren().addAll(mediaView, mediaButtonBar);

        List<TrackInfo> tracksInfo = mediaPlayer.getTrackInfo(TrackType.VIDEO);
        if (tracksInfo.size() > 0) {
            VideoTrackInfo trackInfo = (VideoTrackInfo) tracksInfo.get(0);

            // when SAR is set to an invalid 0:0 ratio, force it to default to 1:1
            double sampleAspectRatio = Math.max(trackInfo.sampleAspectRatio(), 1);
            double sampleAspectRatioBase = Math.max(trackInfo.sampleAspectRatioBase(), 1);
            displayAspectRatio = (sampleAspectRatioBase * height) / (sampleAspectRatio * width); // DAR = PAR * SAR
        }
        
        if (!isMediaViewShown())
            showMediaView();
    }
    
    private boolean isMediaViewShown() {
        return mediaViewTransition != null && ((FadeTransition) mediaViewTransition.getChildren().get(0)).getToValue() > 0.0;
    }
    
    private boolean isMediaButtonBarShown() {
        return mediaButtonBarTransition != null && mediaButtonBarTransition.getToValue() > 0.0;
    }
    
    private void _play() {
        mediaPlayer.play();
        playingProperty.set(true);
    }
    
    private void _stop() {
        mediaPlayer.stop();
        playingProperty.set(false);
    }
    
    public void play() {
        if (mediaPlayer != null)
            _play();
    }
    
    public void stop() {
        disabled = true;
        
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying() || mediaPlayer.getPosition() > 0.0)
                _stop();
            if (isMediaViewShown())
                hideMediaView();
            if (isMediaButtonBarShown())
                hideMediaButtonBar();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }
    
    public BooleanProperty playingProperty() {
        return playingProperty;
    }
    
    public void setMedia(Path mediaPath) {
        disabled = false;
        
        if (mediaPlayer != null)
            if (mediaPath != null) {
                mediaPlayer.prepareMedia(mediaPath.toString());
                if (mediaView != null && !isMediaViewShown())
                    showMediaView();
            } else {
                _stop();
                if (mediaView != null)
                    hideMediaView();
                disabled = true;
            }
    }

    public void setDragCopyPath(String dragCopyPath) {
        this.dragCopyPath = dragCopyPath;
    }
    
    public void setOnDropCompleted(Delegate delegate) {
        onDropCompleted = delegate;
    }
    
    public void setOnMouseRightClick(Delegate delegate) {
        onMouseRightClick = delegate;
    }

    public void setVolume(int volume) {
        if (mediaPlayer != null)
            mediaPlayer.setVolume(volume);
    }

    public void addDropTarget(ImageViewPane eventTargets) {
        dropTargets.add(eventTargets);
    }
    
    public void addAllDropTargets(ImageViewPane... eventTargets) {
        Collections.addAll(dropTargets, eventTargets);
    }
    
    private void showMediaView() {
        if (mediaView != null) {
            if (mediaViewTransition != null) {
                mediaViewTransition.stop();
            }

            FadeTransition viewFade = new FadeTransition();
            viewFade.setNode(mediaView);
            viewFade.setToValue(1.0);

            FadeTransition bgFade = new FadeTransition();
            bgFade.setNode(background);
            bgFade.setToValue(.75);

            mediaViewTransition = new ParallelTransition(
                viewFade, bgFade
            );
            mediaViewTransition.setInterpolator(Interpolator.EASE_OUT);
            mediaViewTransition.setDelay(Duration.millis(200));
            mediaViewTransition.play();
        }
    }
    
    private void hideMediaView() {
        if (mediaViewTransition != null) {
            mediaViewTransition.stop();
        }
        
        FadeTransition viewFade = new FadeTransition();
        viewFade.setNode(mediaView);
        viewFade.setToValue(0.0);
        
        FadeTransition bgFade = new FadeTransition();
        bgFade.setNode(background);
        bgFade.setToValue(0.0);
        
        mediaViewTransition = new ParallelTransition(
            viewFade, bgFade
        );
        mediaViewTransition.setInterpolator(Interpolator.EASE_OUT);
        mediaViewTransition.setDelay(Duration.millis(200));
        mediaViewTransition.setOnFinished(e -> mediaView = null);
        mediaViewTransition.play();
    }
    
    private void showMediaButtonBar() {
        if (mediaButtonBarTransition != null) {
            mediaButtonBarTransition.stop();
        }
        mediaButtonBarTransition = new FadeTransition(Duration.millis(200), mediaButtonBar);
        mediaButtonBarTransition.setToValue(1.0);
        mediaButtonBarTransition.setInterpolator(Interpolator.EASE_OUT);
        mediaButtonBarTransition.play();

        mediaButtonBar.setMouseTransparent(false);
        isMouseEntered = true;
    }
    
    private void hideMediaButtonBar() {
        if (isMouseEntered) {
            if (mediaButtonBarTransition != null) {
                mediaButtonBarTransition.stop();
            }
            mediaButtonBarTransition = new FadeTransition(Duration.millis(800), mediaButtonBar);
            mediaButtonBarTransition.setToValue(0.0);
            mediaButtonBarTransition.setInterpolator(Interpolator.EASE_OUT);
            mediaButtonBarTransition.play();

            mediaButtonBar.setMouseTransparent(true);
            isMouseEntered = false;
        }
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        
        if (mediaView != null) {
            double fitHeight = displayAspectRatio * width;
            if (fitHeight > height) {
                mediaView.setFitHeight(height);
                double fitWidth = height / displayAspectRatio;
                mediaView.setFitWidth(fitWidth);
            } else {
                mediaView.setFitWidth(width);
                mediaView.setFitHeight(fitHeight);
            }
            layoutInArea(mediaView, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
        }
        if (mediaButtonBar != null) mediaButtonBar.resizeRelocate(
            (width - mediaButtonBar.getWidth()) / 2,
            height - mediaButtonBar.getHeight(),
            width, mediaButtonBar.getHeight()
        );
        
        super.layoutChildren();
    }
}
