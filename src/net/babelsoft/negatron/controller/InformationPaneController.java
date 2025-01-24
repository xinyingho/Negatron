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
package net.babelsoft.negatron.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Shear;
import javafx.util.Duration;
import net.babelsoft.negatron.io.cache.InformationCache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.extras.Extras;
import net.babelsoft.negatron.io.extras.Icons;
import net.babelsoft.negatron.io.extras.Images;
import net.babelsoft.negatron.model.ScreenOrientation;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.ImageViewPane;
import net.babelsoft.negatron.view.control.MediaViewPane;
import net.babelsoft.negatron.view.control.ScrollTextFlow;
import net.babelsoft.negatron.view.control.TabPane;
import net.babelsoft.negatron.view.control.Text;
import org.jpedal.examples.viewer.PdfViewer;

/**
 *
 * @author capan
 */
public abstract class InformationPaneController<T extends EmulatedItem<T>> implements Initializable, Disposable {
    @FXML
    private TabPane tabPane;
    @FXML
    private Button favouritesButton;
    
    @FXML
    private Tab externalTab;
    @FXML
    private Tab ingameTab;
    @FXML
    private Tab informationTab;
    @FXML
    private Tab manualTab;
    
    @FXML
    private ImageViewPane externalsZoomImagePane;
    @FXML
    private ImageViewPane ingameZoomImagePane;
    
    @FXML
    protected GridPane ingameGrid;
    @FXML
    protected ImageViewPane titleImagePane;
    @FXML
    protected ImageViewPane snapshotImagePane;
    @FXML
    protected MediaViewPane mediaViewPane;
    
    @FXML
    private AnchorPane pdfPane;
    
    private PdfViewer pdfViewer;
    
    private InformationCache cache;
    protected T currentEmulatedItem;
    protected String systemName;
    
    private Application application;
    protected Image noiseImage;
    protected Image colourTestImage;
    
    private final Timeline timeline;
    private final List<KeyValue> keyValues1;
    private final List<KeyValue> keyValues2;
    
    private final Delegate onDropCompleted;
    private MouseEvent favouritesEvent;
    
    private boolean soundEnabled;
    private boolean videoEnabled;
    private boolean zoomEnabled;
    
    protected InformationPaneController() {
        timeline = new Timeline();
        keyValues1 = new ArrayList<>();
        keyValues2 = new ArrayList<>();
        onDropCompleted = () -> updateTabContent(true);
        zoomEnabled = true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noiseImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/VideoNoise.png"));
        colourTestImage = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/TvColourTestCard.png"));
    
        favouritesEvent = new MouseEvent(favouritesButton, favouritesButton,
            MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.MIDDLE, 1,
            false, false, false, false, false, false, false, false, false, false, null
        );
        
        keyValues1.add(new KeyValue(tabPane.scaleXProperty(), 1.0));
        keyValues1.add(new KeyValue(tabPane.opacityProperty(), 1.0));
        keyValues2.add(new KeyValue(tabPane.scaleXProperty(), 0.0));
        keyValues2.add(new KeyValue(tabPane.opacityProperty(), 0.0));
        
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (o, oV, newValue) -> updateTabContent(newValue)
        );
        
        pdfViewer = new PdfViewer(pdfPane, null);
        pdfViewer.setupViewer();
        AnchorPane.setBottomAnchor(pdfViewer.getRoot(), 0.0);
        AnchorPane.setLeftAnchor(pdfViewer.getRoot(), 0.0);
        AnchorPane.setRightAnchor(pdfViewer.getRoot(), 0.0);
        AnchorPane.setTopAnchor(pdfViewer.getRoot(), 0.0);
        
        mediaViewPane.addAllDropTargets(titleImagePane, snapshotImagePane);
        mediaViewPane.playingProperty().addListener((o, oV, newValue) -> zoomEnabled = !newValue);
        soundEnabled = Configuration.Manager.isSoundEnabled();
        
        Consumer<ImageViewPane> unzoom = zoomingPane -> {
            zoomingPane.setMouseTransparent(true);
            FadeTransition fade = new FadeTransition(Duration.millis(200), zoomingPane);
            fade.setToValue(0.0);
            fade.setInterpolator(Interpolator.EASE_OUT);
            fade.play();
        };
        externalsZoomImagePane.setOnMouseClicked(event -> unzoom.accept(externalsZoomImagePane));
        ingameZoomImagePane.setOnMouseClicked(event -> unzoom.accept(ingameZoomImagePane));
        favouritesButton.setDisable(true);
        
        try {
            cache = new InformationCache();
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(InformationPaneController.class.getName()).log(Level.SEVERE, "Couldn't init cache", ex);
        }
    }
    
    protected void initialise(ImageViewPane pane) {
        pane.setOnDropCompleted(onDropCompleted);
    }
    
    protected void initialiseExternalsZooming(ImageViewPane... panes) {
        initialiseZooming(externalsZoomImagePane, panes);
    }
    
    protected void initialiseIngameZooming(ImageViewPane... panes) {
        initialiseZooming(ingameZoomImagePane, titleImagePane, snapshotImagePane);
        initialiseZooming(ingameZoomImagePane, panes);
    }
    
    private void initialiseZooming(ImageViewPane zoomingPane, ImageViewPane... panes) {
        Arrays.stream(panes).forEach(pane -> pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && (tabPane.getSelectionModel().getSelectedItem() != ingameTab || zoomEnabled)) {
                zoomingPane.setImage(pane.getImageView().getImage());
                zoomingPane.setOrientation(pane.getOrientation());
                zoomingPane.setMouseTransparent(false);
                FadeTransition fade = new FadeTransition(Duration.millis(200), zoomingPane);
                fade.setToValue(1.0);
                fade.setInterpolator(Interpolator.EASE_OUT);
                fade.play();
            }
        }));
    }
    
    protected void postInitialise(ImageViewPane[] panes) {
        postInitialise(null, panes);
    }
    
    protected void postInitialise(Tab[] tabs, ImageViewPane[] panes) {
        Object dummyMachine = new Object();
        externalTab.setUserData(dummyMachine);
        ingameTab.setUserData(dummyMachine);
        informationTab.setUserData(dummyMachine);
        manualTab.setUserData(dummyMachine);
        if (tabs != null)
            Arrays.stream(tabs).forEach(tab -> tab.setUserData(dummyMachine));
        
        setEmulatedItem(null, true);
        
        initialise(titleImagePane);
        initialise(snapshotImagePane);
        mediaViewPane.setOnDropCompleted(onDropCompleted);
        if (panes != null)
            Arrays.stream(panes).forEach(pane -> initialise(pane));
    }
    
    protected void setOrientation(ScreenOrientation orientation) {
        titleImagePane.setOrientation(orientation);
        snapshotImagePane.setOrientation(orientation);
    }

    @Override
    public void dispose() {
        mediaViewPane.dispose();
        // Since september 2015, IDR has been changing OpenViewerFX to be less and less plug'n play (to encourage people to move to their professional solutions?).
        // So now, the below workaround is required to force all viewer threads to close, but it shouldn't be mandatory...
        pdfViewer.close();
    }
    
    public void setFavouriteEnabled(boolean favouriteEnabled) {
        if (favouriteEnabled && currentEmulatedItem != null)
            favouritesButton.setDisable(false);
        else
            favouritesButton.setDisable(true);
    }

    public void setSoundEnabled(boolean soundEnabled) {
        if (soundEnabled)
            mediaViewPane.setVolume(100);
        else
            mediaViewPane.setVolume(0);
        this.soundEnabled = soundEnabled;
    }
    
    public void setVideoEnabled(boolean videoEnabled) {
        this.videoEnabled = videoEnabled;
        
        if (videoEnabled)
            playVideo(getCurrentName());
        else
            mediaViewPane.stop();
    }
    
    public void setOnVideoShortcut(Delegate delegate) {
        mediaViewPane.setOnMouseRightClick(delegate);
    }
    
    public TabPane getTabPane() {
        return tabPane;
    }
    
    protected abstract void setText(String text);
    protected abstract void setGraphic(ImageView graphic);

    public void setApplication(Application application) {
        this.application = application;
    }
    
    public void setEmulatedItem(T emulatedItem, boolean keepFavouritesButtonEnabled) {
        currentEmulatedItem = emulatedItem;
        
        try {
            // header
            if (currentEmulatedItem != null) {
                setText(emulatedItem.getDescription());
                Image image = Icons.newImage(emulatedItem.getName());
                if (image == null)
                    image = new Image(getClass().getResourceAsStream("/net/babelsoft/negatron/resource/icon/Negatron@2x.png"));
                setGraphic(new ImageView(image));
                if (keepFavouritesButtonEnabled)
                    favouritesButton.setDisable(false);
            } else {
                setText("Negatron");
                setGraphic(new ImageView(new Image(
                    getClass().getResourceAsStream("/net/babelsoft/negatron/resource/icon/Negatron@2x.png")
                )));
                favouritesButton.setDisable(true);
            }
            // tabs
            updateTabContent();
        } catch (IOException ex) {
            Logger.getLogger(InformationPaneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getCurrentName() {
        return currentEmulatedItem != null ? currentEmulatedItem.getName() : null;
    }
    
    private void playVideo(String currentName) {
        if (currentName != null && videoEnabled) {
            Path videoSource = Extras.toPath(systemName, currentName, Property.VIDEO_PREVIEW, ".mp4", ".flv");
            mediaViewPane.setMedia(videoSource);
            if (!soundEnabled)
                mediaViewPane.setVolume(0);
            mediaViewPane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.VIDEO_PREVIEW));
            
            if (videoSource != null && tabPane.getSelectionModel().getSelectedItem() == ingameTab)
                mediaViewPane.play();
        } else
            mediaViewPane.setMedia(null);
    }
    
    public void pauseVideo() {
        mediaViewPane.pause();
    }
    
    public void showTab() {
        if (tabPane.getOpacity() < 1.0) {
            List<KeyValue> keyValuesA = new ArrayList<>();
            List<KeyValue> keyValuesB = new ArrayList<>();
            Shear shear = new Shear();
            
            tabPane.getTransforms().add(shear);
            keyValuesA.add(new KeyValue(tabPane.scaleXProperty(), 0.0));
            keyValuesA.add(new KeyValue(tabPane.opacityProperty(), 0.0));
            keyValuesA.add(new KeyValue(shear.xProperty(), 0.7));
            keyValuesB.add(new KeyValue(tabPane.scaleXProperty(), 1.0));
            keyValuesB.add(new KeyValue(tabPane.opacityProperty(), 1.0));
            keyValuesB.add(new KeyValue(shear.xProperty(), 0.0));
            
            Timeline _timeline = new Timeline();
            _timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, null, null, keyValuesA));
            _timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(300), null, evt -> tabPane.getTransforms().remove(shear), keyValuesB)
            );
            _timeline.play();
        }
    }
    
    public void hideTab(Delegate delegate, boolean onStarted) {
        if (Configuration.Manager.isSyncExecutionMode()) {
            // With MAME 0.186+, as no information are required to be fetched from MAME,
            // processing from internal data cache can be so fast that showTab() is sometimes
            // called practically at the same time as hideTab() instead of some time after,
            // possibly resulting into the hide/show animation being stuck instead of being completed.
            // 
            // To avoid this, hideTab() is thus reduce to the strict minimum:
            // no animation, and directly go to the hiding animation's final state
            keyValues2.forEach(keyValue -> 
                ((DoubleProperty) keyValue.getTarget()).setValue((Double) keyValue.getEndValue())
            );
            delegate.fire();
        } else if (
            timeline.getStatus() == Animation.Status.STOPPED &&
            tabPane.getOpacity() > 0.0
        ) {
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().add(new KeyFrame(
                Duration.ZERO, null, null, keyValues1
            ));
            timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(300), null, onStarted ? null : (event -> delegate.fire()), keyValues2
            ));
            timeline.play();

            if (onStarted)
                delegate.fire();
        }
    }
    
    public void hideTab(EventHandler<ActionEvent> onFinished) {
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.ZERO, new KeyValue(tabPane.opacityProperty(), 1.0))
        );
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(300), onFinished, new KeyValue(tabPane.opacityProperty(), 0.0))
        );
        timeline.play();
    }
    
    public void selectInformationTab() {
        tabPane.getSelectionModel().select(informationTab);
    }
    
    private void updateTabContent() {
        updateTabContent(false);
    }
    
    protected void updateTabContent(boolean forceUpdate) {
        updateTabContent(tabPane.getSelectionModel().getSelectedItem(), forceUpdate);
    }
    
    private boolean updateTabContent(Tab selectedTab) {
        return updateTabContent(selectedTab, false);
    }
    
    protected boolean updateTabContent(Tab selectedTab, boolean forceUpdate) {
        String currentName = getCurrentName();
        
        try {
            if (selectedTab != ingameTab)
                pauseVideo();
            
            if (selectedTab == externalTab) {
                // external representation
                if (forceUpdate || currentEmulatedItem != externalTab.getUserData()) {
                    updateExternalTabContent(currentName);
                    externalTab.setUserData(currentEmulatedItem);
                }
                return true;
            } else if (selectedTab == ingameTab) {
                // in-game content
                if (forceUpdate || currentEmulatedItem != ingameTab.getUserData()) {
                    updateIngameTabContent(currentName);
                    ingameTab.setUserData(currentEmulatedItem);
                }
                return true;
            } else if (selectedTab == informationTab) {
                if (forceUpdate || currentEmulatedItem != informationTab.getUserData()) {
                    String parentName = null;
                    if (currentEmulatedItem != null && currentEmulatedItem.getParent() != null)
                        parentName = currentEmulatedItem.getParent().getName();
                    updateInformationTabContent(currentName, parentName);
                    informationTab.setUserData(currentEmulatedItem);
                }
                return true;
            } else if (selectedTab == manualTab) {
                // manual
                if (forceUpdate || currentEmulatedItem != manualTab.getUserData()) {
                    Path path = Extras.toPdfPath(systemName, currentName, Property.MANUAL);
                    if (path != null)
                        pdfViewer.openDefaultFile(path.toString());
                    else
                        pdfViewer.openDefaultFile(Extras.getDefaultPdfPath());
                    manualTab.setUserData(currentEmulatedItem);
                }
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(InformationPaneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    protected abstract void updateExternalTabContent(String currentName) throws IOException;

    protected void updateIngameTabContent(String currentName) throws IOException {
        Image image = Images.newImage(systemName, currentName, Property.TITLE);
        if (image == null)
            image = colourTestImage;
        titleImagePane.setImage(image);
        titleImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.TITLE));
        
        image = Images.newImage(systemName, currentName, Property.SNAPSHOT);
        if (image == null)
            image = noiseImage;
        snapshotImagePane.setImage(image);
        snapshotImagePane.setDragCopyPath(Extras.toPrimaryPath(systemName, currentName, Property.SNAPSHOT));
        
        playVideo(currentName);
    }

    private void updateInformationTabContent(String currentName, String parentName) {
        informationTab.setContent(null);
        
        TabPane informationPane = new TabPane();
        informationPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        informationTab.setContent(informationPane);
        
        BiConsumer<String, String> addTab = (tabName, content) -> {
            if (content != null) {
                ScrollTextFlow textFlow = new ScrollTextFlow();
                informationPane.getTabs().add(new Tab(tabName, textFlow));
                
                content = content.replaceAll("\\n\\s{2}\\*\\s", System.lineSeparator() + "• ");
                content = content.replaceAll("\\n\\s{4}\\*\\s", System.lineSeparator() + "\t• ");
                content = content.replaceAll("\\n\\*\\s", System.lineSeparator() + "• ");
                
                if (tabName.equals("command.dat")) {
                    // Reformat content to align move names and move commands properly
                    // replace " " by \t while ignoring sections containing the command indication "_`"
                    //textFlow.setStyle("-fx-font-family: 'monospaced'");
                    
                    // Replace command indications by glyphs
                    // SNK style
                    content = content.replaceAll("_A", "Ⓐ"); // light punch
                    content = content.replaceAll("_B", "Ⓑ"); // light kick
                    content = content.replaceAll("_C", "Ⓒ"); // strong punch
                    content = content.replaceAll("_D", "Ⓓ"); // strong kick
                    content = content.replaceAll("@E-button", "Ⓔ"); // leader super move in KoF XI
                    content = content.replaceAll("\\^s", "Ⓢ"); // slash in the Samurai Shodown series
                    // Capcom style
                    content = content.replaceAll("\\^E", "_BⓅ"); // light punch
                    content = content.replaceAll("\\^F", "_YⓅ"); // medium punch
                    content = content.replaceAll("\\^G", "_RⓅ"); // strong punch
                    content = content.replaceAll("\\^H", "_BⓀ"); // light kick
                    content = content.replaceAll("\\^I", "_YⓀ"); // medium kick
                    content = content.replaceAll("\\^J", "_RⓀ"); // strong kick
                    content = content.replaceAll("\\^U", "_BⓅ+_YⓅ+_RⓅ"); // 3 punch buttons
                    content = content.replaceAll("\\^T", "_BⓀ+_YⓀ+_RⓀ"); // 3 kick buttons
                    content = content.replaceAll("\\^W", "Ⓟ+Ⓟ"); // 2 punch buttons
                    content = content.replaceAll("\\^V", "Ⓚ+Ⓚ"); // 2 kick buttons
                    content = content.replaceAll("\\^M", "MAX"); // SF Zero: MAX super move
                    content = content.replaceAll("_x", "⭮"); // 360° on D-pad
                    content = content.replaceAll("@M-button", "Ⓜ"); // Star Gladiator: Movement
                    content = content.replaceAll("@L-button", "Ⓛ"); // Tenchi wo Kurau II: Sekiheki no Tatakai
                    content = content.replaceAll("@X-button", "Ⓧ"); // Tenchi wo Kurau II: Sekiheki no Tatakai
                    content = content.replaceAll("@R-button", "Ⓡ"); // Tenchi wo Kurau II: Sekiheki no Tatakai
                    content = content.replaceAll("@Y-button", "Ⓨ"); // Tenchi wo Kurau II: Sekiheki no Tatakai
                    // Sega (Virtua Fighter)
                    content = content.replaceAll("_G", "Ⓖ"); // guard / block
                    // Midway / NetherRealm style
                    content = content.replaceAll("@R-button", "Ⓡ"); // run
                    content = content.replaceAll("_c", "③"); // War Gods: 3-D button
                    // Tecmo (Dead or Alive) style
                    content = content.replaceAll("_H", "Ⓗ"); // hold
                    // Atlus (Groove on Fight)
                    content = content.replaceAll("@O-button", "Ⓞ"); // overhead attack
                    // Data East (Karate Champ)
                    content = content.replaceAll("@left", "⮜"); // Rear Kick ⬅
                    content = content.replaceAll("@down", "⮟"); // Low Kick ⬇
                    content = content.replaceAll("@right", "⮞"); // Front Kick ➡
                    content = content.replaceAll("@up", "⮝"); // Roundhouse Kick ⬆
                    // generic style
                    content = content.replaceAll("_`", "• ");
                    content = content.replaceAll("_P", "Ⓟ"); // punch
                    content = content.replaceAll("_K", "Ⓚ"); // kick
                    content = content.replaceAll("_S", "🅢"); // start button
                    content = content.replaceAll("\\^S", "🆂"); // select button
                    content = content.replaceAll("_\\+", "+");
                    content = content.replaceAll("_N", "Ⓝ"); // neutral direction
                    content = content.replaceAll("_\\?", "↔"); // any direction
                    content = content.replaceAll("_1", "↙");
                    content = content.replaceAll("\\^1", "⇙•"); // hold
                    content = content.replaceAll("_2", "↓");
                    content = content.replaceAll("\\^2", "⇓•"); // hold down
                    content = content.replaceAll("_3", "↘");
                    content = content.replaceAll("\\^3", "⇘•"); // hold
                    content = content.replaceAll("_4", "←");
                    content = content.replaceAll("\\^4", "⇐•"); // hold back
                    content = content.replaceAll("_6", "→");
                    content = content.replaceAll("\\^6", "⇒•"); // hold forward
                    content = content.replaceAll("_7", "↖");
                    content = content.replaceAll("\\^7", "⇖•"); // hold
                    content = content.replaceAll("_8", "↑");
                    content = content.replaceAll("\\^8", "⇑•"); // hold up
                    content = content.replaceAll("_9", "↗");
                    content = content.replaceAll("\\^9", "⇗•"); // hold
                    content = content.replaceAll("_\\^", "🚀"); // in the air
                    content = content.replaceAll("\\^\\*", "⮤⮧"); // repeatedly push button 🔁
                    content = content.replaceAll("_X\\)", "tap)"); // tap on button
                    content = content.replaceAll("_X", "tap "); // tap on button
                    content = content.replaceAll("_O", "⭳"); // hold button
                    content = content.replaceAll("_\\(", "🤼"); // normal throw
                    content = content.replaceAll("_\\)", "🏃"); // command move
                    content = content.replaceAll("_@", "🚵"); // special move
                    content = content.replaceAll("_\\*", "🏍"); // super move
                    content = content.replaceAll("_&", "★"); // desperation move
                    content = content.replaceAll("_>", "☆"); // hidden desperation move
                    content = content.replaceAll("_#", "⤮"); // alpha counter / counter attack / striker move
                    content = content.replaceAll("\\^!", "↳"); // chaining special move
                    content = content.replaceAll("_!", "⇢"); // chaining combo move
                    // non-fighting games
                    content = content.replaceAll("@F-button", "Ⓕ"); // fire
                    content = content.replaceAll("@J-button", "Ⓙ"); // jump
                    content = content.replaceAll("@W-button", "Ⓦ"); // weapon
                    content = content.replaceAll("_a", "①");
                    content = content.replaceAll("_b", "②");
                    content = content.replaceAll("_c", "③");
                    content = content.replaceAll("_d", "④");
                    content = content.replaceAll("_e", "⑤");
                    content = content.replaceAll("_f", "⑥");
                }

                int from = 0;
                if (content.startsWith("\n")) {
                    from = content.indexOf("\n", 1);
                    String title = content.substring(1, from);
                    if (!title.endsWith(":")) {
                        Text text = new Text(content.substring(1, from));
                        text.getStyleClass().add("h1");
                        textFlow.addNode(text);
                    } else
                        from = 1;
                }
                
                Pattern pattern = Pattern.compile(
                    "https?:\\/\\/\\S+[\\w/]|^- ([\\wα-ω\\(\\)\\.\\-/'\"!?&]+ )+-|={6} (\\S+ )+={6}|={5} (\\S+ )+={5}|={2} (\\S+ )+={2}|^─{10,}|_(B|Y|R)(Ⓟ|Ⓚ)",
                    Pattern.MULTILINE
                );
                Matcher matcher = pattern.matcher(content);
                Pattern capitaliser = Pattern.compile("\\b(.)(.*?)\\b");
                while (matcher.find()) {
                    int to = matcher.start();
                    if (from != to)
                        textFlow.addNode(new Text(content.substring(from, to)));

                    String match = matcher.group();
                    if (match.startsWith("_")) {
                        Text text = new Text(match.substring(2));
                        switch (match.substring(1, 2)) {
                            case "B" -> text.getStyleClass().add("blue");
                            case "Y" -> text.getStyleClass().add("green");
                            case "R" -> text.getStyleClass().add("red");
                        }
                        textFlow.addNode(text);
                    } else if (match.startsWith("h")) {
                        String label = match;
                        if (label.startsWith("http://www.arcade-history.com"))
                            label = currentName + " at http://www.arcade-history.com";
                        
                        Hyperlink hyperlink = new Hyperlink(label);
                        hyperlink.setOnAction(event -> application.getHostServices().showDocument(match));
                        textFlow.addNode(hyperlink);
                    } else if (match.startsWith("-")) {
                        Text text = new Text(capitaliser.matcher(
                                match.substring(2, match.length() - 2)
                        ).replaceAll(
                                str -> str.group(1).toUpperCase() + str.group(2).toLowerCase()
                        ));
                        text.getStyleClass().add("h2");
                        textFlow.addNode(text);
                    } else if (match.startsWith("─")) {
                        // swallow the line
                    } else if (match.startsWith("======")) {
                        Text text = new Text(match.substring(7, match.length() - 7));
                        text.getStyleClass().add("h1");
                        textFlow.addNode(text);
                    } else if (match.startsWith("=====")) {
                        Text text = new Text(match.substring(6, match.length() - 6));
                        text.getStyleClass().add("h2");
                        textFlow.addNode(text);
                    } else /*if (match.startsWith("=="))*/ {
                        Text text = new Text(match.substring(3, match.length() - 3));
                        text.getStyleClass().add("h3");
                        textFlow.addNode(text);
                    }

                    from = matcher.end();
                }

                if (from != content.length() - 1)
                    textFlow.addNode(new Text(content.substring(from)));
            }
        };
        
        if (currentName == null) {
            String content = "An unexpected error occurred while retrieving content.";
            try {
                content = Extras.getDefaultInformationContent();
            } catch (IOException ex) {
                Logger.getLogger(MachineInformationPaneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            addTab.accept("readme.txt", content);
        } else {
            String _systemName = systemName != null ? systemName : "info";
            
            Delegate displayInformation = () -> {
                Map<String, String> data = cache.get(_systemName, currentName, parentName);
                data.keySet().stream().forEachOrdered(tabName -> {
                    String content = data.get(tabName);
                    addTab.accept(tabName, content);
                });
            };
            
            if (cache.isReady()) {
                displayInformation.fire();
            } else
                Platform.runLater(displayInformation::fire); // the cache should hopefully be ready by the next pulse
        }
    }
    
    @FXML
    private void handleOnAddToFavourites(ActionEvent event) {
        favouritesButton.fireEvent(favouritesEvent);
    }
}
