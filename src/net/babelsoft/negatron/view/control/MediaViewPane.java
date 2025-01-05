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
import java.net.URI;
import java.net.URISyntaxException;
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
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
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
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.behavior.EventDispatchChainImpl;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 * Used Oracle's Overlay Media Player code for the layout, and vlcj to replace Oracle's GStreamer back-end.
 * Media playback in JavaFX is done using GStreamer. But on Windows, Oracle set it up to use the default embedded OS codecs, which aren't up to date.
 * So replacing GStreamer by VLC allows Negatron to play any videos on Windows and also solves the libav versioning issues that can happen on some Linux distributions.
 * @author capan
 */
public class MediaViewPane extends Region implements Disposable {
    
    private class CanvasCallbackVideoSurface extends CallbackVideoSurface {
        CanvasCallbackVideoSurface() {
            super(new CanvasBufferFormatCallback(), new CanvasRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }
    }
    
    private class CanvasMediaPlayerEventAdapter extends MediaPlayerEventAdapter {
        @Override
        public void finished(MediaPlayer mediaPlayer) {
            Platform.runLater(() -> {
                if (!mediaPlayer.controls().getRepeat()) {
                    _stop();
                    hideMediaView();
                }
            });
        }
    }
    
    private class CanvasBufferFormatCallback implements BufferFormatCallback {
        private int sourceWidth;
        private int sourceHeight;
        
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            assert buffers[0].capacity() == sourceWidth * sourceHeight * 4;
            Platform.runLater(() -> {
                initializeImageView(buffers[0], sourceWidth, sourceHeight);
            });
        }
    }
    
    private class CanvasRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            Platform.runLater(() -> {
                videoPixelBuffer.updateBuffer(pb -> null);
            });
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
    
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;
    private ImageView mediaView;
    private WritableImage videoImage;
    private PixelBuffer<ByteBuffer> videoPixelBuffer;
    private double displayAspectRatio;
    
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
            mediaPlayerFactory = new MediaPlayerFactory();
            mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
            mediaPlayer.videoSurface().set(new CanvasCallbackVideoSurface());
            mediaPlayer.events().addMediaPlayerEventListener(new CanvasMediaPlayerEventAdapter());
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
                    mediaPlayer.controls().setTime(0);
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
                if (!disabled && mediaPlayer.status().isPlaying())
                    mediaPlayer.controls().pause();
            });

            Button forwardButton = new Button("Forward");
            forwardButton.getStyleClass().add("forward-button");
            forwardButton.setOnAction(e -> {
                if (!disabled && mediaView != null) {
                    long currentTime = mediaPlayer.status().time();
                    mediaPlayer.controls().setTime(currentTime + 5000); // going forward 5 seconds
                }
            });
            
            ToggleButton loopButton = new ToggleButton("Loop");
            loopButton.getStyleClass().add("loop-button");
            loopButton.setOnAction(e -> {
                if (!disabled) {
                    if (loopButton.isSelected())
                        mediaPlayer.controls().setRepeat(true);
                    else
                        mediaPlayer.controls().setRepeat(false);
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
            mediaPlayerFactory = null;
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

                if (mediaPlayer.status().isPlaying())
                    _stop();

                Path tmpPath;
                if (extension.endsWith("mp4"))
                    tmpPath = Paths.get(dragCopyPath + ".flv");
                else
                    tmpPath = Paths.get(dragCopyPath + ".mp4");
                Files.deleteIfExists(tmpPath);

                Files.copy(Paths.get(new URI(url.replace(" ", "%20"))), targetPath, StandardCopyOption.REPLACE_EXISTING);
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
        if (mediaPlayerFactory != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
            mediaPlayerFactory.release();
        }
    }

    private void initializeImageView(ByteBuffer buffer, int width, int height) {
        if (mediaView != null)
            getChildren().remove(mediaView);
        getChildren().remove(mediaButtonBar);

        PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
        videoPixelBuffer = new PixelBuffer<>(width, height, buffer, pixelFormat);
        videoImage = new WritableImage(videoPixelBuffer);
        mediaView = new ImageView(videoImage);
        getChildren().addAll(mediaView, mediaButtonBar);

        List<VideoTrackInfo> tracksInfo = mediaPlayer.media().info().videoTracks();
        if (!tracksInfo.isEmpty()) {
            VideoTrackInfo trackInfo = tracksInfo.get(0);

            // when SAR is set to an invalid 0:0 ratio, force it to default to 1:1
            double sampleAspectRatioX = Math.max(trackInfo.sampleAspectRatio(), 1);
            double sampleAspectRatioY = Math.max(trackInfo.sampleAspectRatioBase(), 1);
            double sampleAspectRatio = sampleAspectRatioX / sampleAspectRatioY;
            displayAspectRatio = (double) width / (double) height * sampleAspectRatio; // DAR = PAR * SAR
            
            if (sampleAspectRatio != 1.0 || Math.abs(displayAspectRatio - 16.0/9.0) > 0.001 && Math.abs(displayAspectRatio - 9.0/16.0) > 0.001) {
                // it's not about a 16/9 or 9/16 DAR as expected from LCD/LED/OLED/QLED games
                // so, it's about an old game that was always displayed on a classic 4/3 or 3/4 CRT
                if (displayAspectRatio >= 1.0)
                    displayAspectRatio = 4.0/3.0;
                else
                    displayAspectRatio = 3.0/4.0;
            }
            
            // macOS fix: when a system-wide VLC is still opened in the background,
            // the update layout event can get fired before the new video file has been loaded in VLC,
            // leading to a default display of 1:1 in the top-left corner
            // instead of the expected centered display enlarged to the whole pane.
            // So, Negatron needs to force a layout update after the correct DAR has been computed.
            layoutChildren();
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
        mediaPlayer.controls().play();
        playingProperty.set(true);
    }
    
    private void _stop() {
        mediaPlayer.controls().stop();
        playingProperty.set(false);
    }
    
    public void play() {
        if (mediaPlayer != null)
            _play();
    }
    
    public void stop() {
        disabled = true;
        
        if (mediaPlayer != null) {
            if (mediaPlayer.status().isPlaying() || mediaPlayer.status().position() > 0.0)
                _stop();
            if (isMediaViewShown())
                hideMediaView();
            if (isMediaButtonBarShown())
                hideMediaButtonBar();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.status().isPlaying())
            mediaPlayer.controls().pause();
    }
    
    public BooleanProperty playingProperty() {
        return playingProperty;
    }
    
    public void setMedia(Path mediaPath) {
        disabled = false;
        
        if (mediaPlayer != null)
            if (mediaPath != null) {
                mediaPlayer.media().prepare(mediaPath.toString());
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
            mediaPlayer.audio().setVolume(volume);
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
            double fitHeight = width / displayAspectRatio;
            if (fitHeight > height) {
                mediaView.setFitHeight(height);
                double fitWidth = displayAspectRatio * height;
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
