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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import net.babelsoft.negatron.io.Audio;
import net.babelsoft.negatron.io.Audio.Sound;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.Video;
import net.babelsoft.negatron.io.cache.CacheManager;
import net.babelsoft.negatron.io.cache.MachineListCache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.FavouriteTree;
import net.babelsoft.negatron.io.loader.LoadingObserver;
import net.babelsoft.negatron.io.loader.MachineLoader;
import net.babelsoft.negatron.io.loader.MachineLoader.Mode;
import net.babelsoft.negatron.model.SoftwareListFilter;
import net.babelsoft.negatron.model.comparing.Difference;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.favourites.MachineConfiguration;
import net.babelsoft.negatron.model.favourites.SoftwareConfiguration;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwareList;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.DirectoryWatchService;
import net.babelsoft.negatron.util.Disposable;
import net.babelsoft.negatron.util.SimpleDirectoryWatchService;
import net.babelsoft.negatron.util.Strings;
import net.babelsoft.negatron.util.function.Delegate;
import net.babelsoft.negatron.view.control.AdvancedParametrisationDialog;
import net.babelsoft.negatron.view.control.EmulatedItemTreePane;
import net.babelsoft.negatron.view.control.FavouriteTreePane;
import net.babelsoft.negatron.view.control.GlobalConfigurationPane;
import net.babelsoft.negatron.view.control.MachineConfigurationPane;
import net.babelsoft.negatron.view.control.MachineFilterPane;
import net.babelsoft.negatron.view.control.MachineFolderViewPane;
import net.babelsoft.negatron.view.control.MachineInformationPane;
import net.babelsoft.negatron.view.control.NotifierPopup;
import net.babelsoft.negatron.view.control.SoftwareConfigurationPane;
import net.babelsoft.negatron.view.control.SoftwareFilterPane;
import net.babelsoft.negatron.view.control.SoftwareInformationPane;
import net.babelsoft.negatron.view.control.StatisticsPane;
import net.babelsoft.negatron.view.control.TitledWindowPane;
import net.babelsoft.negatron.view.control.TitledWindowPane.DisplayMode;
import net.babelsoft.negatron.view.control.adapter.SelectionData;
import net.babelsoft.negatron.view.control.form.Control;
import net.babelsoft.negatron.view.control.form.DeviceControl;

/**
 *
 * @author capan
 */
public class MainController implements Initializable, AlertController, EditController, LoadingObserver, Disposable {
    
    @FXML
    private SplitPane mainSplitPane;
    
    @FXML
    private TitledPane machineTreeWindow;
    @FXML
    private EmulatedItemTreePane<Machine> machineTreePane;
    @FXML
    private MachineFilterPane machineFilterWindow;
    @FXML
    private MachineInformationPane machineInformationPane;
    @FXML
    private TitledWindowPane machineConfigurationWindow;
    @FXML
    private MachineConfigurationPane machineConfigurationPane;

    @FXML
    private TitledWindowPane softwareTreeWindow;
    @FXML
    private EmulatedItemTreePane<Software> softwareTreePane;
    @FXML
    private SoftwareFilterPane softwareFilterWindow;
    @FXML
    private SoftwareInformationPane softwareInformationWindow;
    @FXML
    private TitledWindowPane softwareConfigurationWindow;
    @FXML
    private SoftwareConfigurationPane softwareConfigurationTable;
    
    @FXML
    private TitledWindowPane favouriteMachineTreeWindow;
    @FXML
    private TitledWindowPane favouriteSoftwareTreeWindow;
    @FXML
    private TitledWindowPane favouriteSoftwareConfigurationWindow;
    @FXML
    private TitledWindowPane favouriteTreeWindow;
    @FXML
    private FavouriteTreePane favouriteTreePane;
    
    @FXML
    private MachineFolderViewPane machineFolderViewWindow;
    
    @FXML
    private GlobalConfigurationPane globalConfigurationWindow;
    @FXML
    private StatisticsPane statisticsWindow;
    
    @FXML
    private TitledWindowPane loggingWindow;
    @FXML
    private ScrollPane loggingPane;
    @FXML
    private TextFlow loggingTextFlow;
    
    @FXML
    private ToolBar buttonBar;
    @FXML
    private Label statusLabel;
    @FXML
    private SplitMenuButton launchButton;
    @FXML
    private ToggleButton machineConfigurationButton;
    @FXML
    private ToggleButton softwareConfigurationButton;
    @FXML
    private Button advancedParametrisationButton;
    @FXML
    private ToggleButton favouriteViewButton;
    
    @FXML
    private ToggleButton statisticsButton;
    
    @FXML
    private HBox notificationArea;
    @FXML
    private ToggleButton soundButton;
    @FXML
    private ToggleButton videoButton;
    @FXML
    private ToggleButton view3dButton;
    @FXML
    private ToggleButton globalConfigurationButton;
    @FXML
    private ProgressIndicator notifier;
    
    private Application application;
    private CacheManager cache;
    private NotifierPopup notifierPopup;
    
    private final SimpleBooleanProperty onSucceededProperty;
    private final SimpleDoubleProperty progressProperty;
    
    private final MachineLoader machineLoader;
    private final AtomicInteger machineLoadingCount;
    private final AtomicBoolean machineLoadingSucceeded;
    private boolean isLoading;
    private boolean isDoingMachineTreeWiseOperation;
    private boolean isDoingSoftwareTreeWiseOperation;
    private String softwareTreeWiseOperationDeviceValue;
    private boolean isMameFatalErrorMode;
    private boolean mustTriggerLaunchAction;
    private boolean isDataReloadFromSoftwareListEnabled;
    private final Timeline launchActionTimeline;
    
    private DeviceController currentDeviceController;
    private Machine previousMachine;
    private Machine currentMachine;
    private Map<String, SoftwareList> softwareLists;
    
    private boolean isFocusOnMame;
    private final Timeline emulationErrorWatcherTimeline;
    
    private final Timeline layoutTimeline;
    
    private boolean isHidingConfigurationPane;
    private SoftwareConfiguration displayingSoftwareConfiguration;
    private SoftwareConfiguration editingSoftwareConfiguration;
    private ConfigurationChangeListener onMachineLoaded;
    
    private double mousePosX, mousePosY;

    public MainController() {
        onSucceededProperty = new SimpleBooleanProperty(false);
        progressProperty = new SimpleDoubleProperty(0.0);
        machineLoader = new MachineLoader();
        machineLoadingCount = new AtomicInteger(1);
        machineLoadingSucceeded = new AtomicBoolean();
        launchActionTimeline = new Timeline(new KeyFrame(Duration.millis(5000)));
        isDataReloadFromSoftwareListEnabled = true;
        
        emulationErrorWatcherTimeline = new Timeline(new KeyFrame(Duration.millis(1000)));
        layoutTimeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            try {
                Configuration.Manager.updateMainDividerPosition(mainSplitPane.getDividers().get(0).getPosition());
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Main divider layout configuration failed", ex);
            }
        }));
    }
    
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private void log(String s, Object... args) {
        loggingTextFlow.getChildren().add(
                new Text(
                        String.format("%s - %s\n\n",
                                ZonedDateTime.now().format(dateTimeFormatter),
                                String.format(s, args)
                        )
                )
        );
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DeviceController.setAlertController(this);
        DeviceController.setSoftwareConfigurationPane(softwareConfigurationTable);
        
        double dividerPosition = Configuration.Manager.getMainDividerPosition();
        if (dividerPosition >= 0)
            mainSplitPane.setDividerPosition(0, dividerPosition);
        mainSplitPane.getDividers().get(0).positionProperty().addListener((o, oV, nV) -> {
            layoutTimeline.playFromStart();
            // when the divider is around the window centre at more or less 10 pixels, force it to snap to the centre for a perfect alignment
            if (
                mainSplitPane.getWidth() / 2 - 10 < nV.doubleValue() * mainSplitPane.getWidth() &&
                nV.doubleValue() * mainSplitPane.getWidth() < mainSplitPane.getWidth() / 2 + 10
            ) mainSplitPane.setDividerPosition(0, 0.5);
        });
        
        Delegate onWindowClose = () -> {
            reset();
            if (!isDataReloadFromSoftwareListEnabled)
                disableAction(false);
            machineConfigurationButton.setSelected(false);
            notificationArea.getChildren().remove(view3dButton);
        };
        
        buttonBar.getItems().removeAll(machineConfigurationButton, softwareConfigurationButton);
        notificationArea.getChildren().remove(view3dButton);
        machineFilterWindow.setOnClose(() -> machineTreePane.setFilterButtonSelected(false));
        machineFolderViewWindow.setOnClose(() -> machineTreePane.setViewButtonSelected(false));
        softwareFilterWindow.setOnClose(() -> softwareTreePane.setFilterButtonSelected(false));
        favouriteTreeWindow.setOnClose(() -> {
            favouriteViewButton.setSelected(false);
            machineConfigurationWindow.hide();
        });
        statisticsWindow.setOnClose(() -> statisticsButton.setSelected(false));
        globalConfigurationWindow.setOnClose(() -> globalConfigurationButton.setSelected(false));
        globalConfigurationWindow.setOnRestart(() -> { try {
            dispose();
            application.start(null);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't restart Negatron to change UI language", ex);
        }});
        
        ///// MESS adornment
        if (Configuration.Manager.isMess()) {
            Tooltip tooltip = launchButton.getTooltip();
            tooltip.setText(tooltip.getText().replace("MAME", "MESS"));
            
            launchButton.getStyleClass().removeIf(styleClass -> styleClass.equals("launch-mame-button"));
            launchButton.getStyleClass().add("launch-mess-button");
            
            tooltip = advancedParametrisationButton.getTooltip();
            tooltip.setText(tooltip.getText().replace("MAME", "MESS"));
        }
        
        ///// Initialisation of machine panes
        
        machineTreePane.setOnAction(() -> handleLaunchAction(new ActionEvent()));
        machineTreePane.setOnSpaceKeyPressed(() -> handleMachineConfigurationAction(null));
        machineTreePane.setOnTreeMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                handleMachineConfigurationAction(null);
                machineConfigurationPane.setEditable(true);
            }
        });
        machineTreePane.setOnMoreViews(selected -> {
            if (selected)
                machineFolderViewWindow.showMaximised();
            else
                machineFolderViewWindow.close();
            if (machineFilterWindow.isDisplayed())
                machineFilterWindow.close();
        });
        machineTreePane.setFilterPane(machineFilterWindow);
        machineTreePane.setOnMoreFilters(selected -> {
            if (selected)
                machineFilterWindow.showMaximised();
            else
                machineFilterWindow.close();
            if (machineFolderViewWindow.isDisplayed())
                machineFolderViewWindow.close();
        });
        machineTreePane.setOnTreeWiseOperation(beginOp -> isDoingMachineTreeWiseOperation = beginOp);
        machineTreePane.currentItemProperty().addListener((o, oV, newValue) -> {
            if (isDoingMachineTreeWiseOperation)
                return;
            
            if (newValue != null) {
                Machine machine = newValue.getValue();
                if (machine.isFolder())
                    return;
                
                machineInformationPane.hideTab(() -> machineConfigurationPane.clearControls());
                
                currentMachine = machine;
                if (currentMachine != previousMachine) {
                    previousMachine = currentMachine;
                    // fill machine configuration pane
                    load();
                }
            } else {
                if (machineLoader.isRunning())
                    machineLoader.cancel();
                
                currentMachine = null;
                previousMachine = null;
                reset();
                
                if (machineConfigurationButton.getParent() != null)
                    buttonBar.getItems().remove(machineConfigurationButton);
                machineConfigurationWindow.close();
                machineInformationPane.hideTab(
                    event -> loadMachineInformation() // reset the information pane to its default state as well
                );
            }
        });
        machineTreePane.setEditableControl(machineConfigurationPane);
        
        machineConfigurationPane.setOnDataUpdated(origin -> {
            if (displayingSoftwareConfiguration == null && !isDoingMachineTreeWiseOperation && !isDoingSoftwareTreeWiseOperation)
                reload(origin);
        });
        machineConfigurationWindow.setWindowSlaves(softwareTreeWindow, softwareInformationWindow, softwareConfigurationWindow);
        machineConfigurationWindow.setOnClose(onWindowClose);
        machineInformationPane.setOnVideoShortcut(() -> videoButton.fire());
        machineFolderViewWindow.setOnFolderViewTypeChanged(machineTreePane::setFolderViewType);
        machineFolderViewWindow.setOnCheckAction(machineTreePane::setFolderVisible);
        
        machineLoader.setOnReady(e -> {
            int count = machineLoadingCount.incrementAndGet();
            if (count == 1) {
                machineInformationPane.setFavouriteEnabled(false);
                softwareInformationWindow.setFavouriteEnabled(false);
            }
        });
        Delegate decrementMachineLoadingCount = () -> {
            int count = machineLoadingCount.decrementAndGet();
            if (count == 0 && favouriteTreeWindow.isHidden()) {
                machineInformationPane.setFavouriteEnabled(true);
                softwareInformationWindow.setFavouriteEnabled(true);
            }
        };
        machineLoader.setOnSucceeded(e -> {
            machineLoadingSucceeded.set(true);
            handleMachineLoaded();
            decrementMachineLoadingCount.fire();
        });
        machineLoader.setOnCancelled(e -> {
            if (!machineLoadingSucceeded.compareAndSet(true, false))
                decrementMachineLoadingCount.fire();
        });
        machineLoader.setOnFailed(event -> {
            Logger.getLogger(MachineLoader.class.getName()).log(Level.SEVERE, "System panic", event.getSource().getException());
            
            isLoading = false;
            mustTriggerLaunchAction = false;
            
            alert(AlertType.ERROR, Language.Manager.getString("machineConfLoading.error.text"));
        });
        
        ///// Initialisation of software panes
        
        softwareTreePane.setOnAction(() -> handleLaunchAction(new ActionEvent()));
        softwareTreePane.setOnSpaceKeyPressed(() -> handleSoftwareConfigurationAction(null));
        softwareTreePane.setOnTreeMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY)
                handleSoftwareConfigurationAction(null);
        });
        softwareTreePane.setFilterPane(softwareFilterWindow);
        softwareTreePane.setOnMoreFilters(selected -> {
            if (selected)
                softwareFilterWindow.showMaximised();
            else
                softwareFilterWindow.close();
        });
        softwareTreePane.setOnAllowAction(() -> disableAction(false));
        softwareTreePane.setOnForbidAction(() -> disableAction(true));
        softwareTreePane.setOnEscapeNode(softwareTreeWindow);
        softwareTreePane.setOnTreeWiseOperation(beginOp -> {
            if (!beginOp && currentDeviceController != null) {
                currentDeviceController.setText(softwareTreeWiseOperationDeviceValue);
                softwareTreeWiseOperationDeviceValue = null;
            }
            
            isDoingSoftwareTreeWiseOperation = beginOp;
            
            if (beginOp && currentDeviceController != null)
                softwareTreeWiseOperationDeviceValue = currentDeviceController.getText();
        });
        softwareTreePane.currentItemProperty().addListener((o, oV, newValue) -> {
            // Don't need the below safe guard as software lists usually have below a thousand items, so easily manageable
            /*if (isDoingSoftwareTreeWiseOperation)
                return;*/
            
            Software software = newValue != null ? newValue.getValue() : null;
            
            if (mustSetSoftware(software)) {
                if (!currentDeviceController.setSoftware(software))
                    fireOnMachineLoaded();
                machineConfigurationPane.satisfyRequirement(software.getRequirement(), softwareLists);
                softwareConfigurationTable.setSoftware(software);
                softwareInformationWindow.setEmulatedItem(software, favouriteTreeWindow.isHidden());
                if (softwareInformationWindow.isHidden()) {
                    softwareInformationWindow.setSyncWindow(machineConfigurationWindow);
                    if (machineConfigurationWindow.getDisplayMode() != DisplayMode.MAXIMISED)
                        softwareInformationWindow.show();
                }
                if (software.getSoftwareParts().size() > 1) {
                    if (softwareConfigurationButton.getParent() == null) {
                        buttonBar.getItems().add(2, softwareConfigurationButton);
                        highlight(softwareConfigurationButton);
                    }
                    if (displayingSoftwareConfiguration != null) {
                        if (displayingSoftwareConfiguration.getSoftwarePart() != null) {
                            softwareConfigurationTable.setCurrentItem(displayingSoftwareConfiguration.getSoftwarePart(), displayingSoftwareConfiguration.getDevice());
                            if (softwareConfigurationWindow.isHidden())
                                handleSoftwareConfigurationAction(null);
                        }
                        displayingSoftwareConfiguration = null;
                    }
                } else if (softwareConfigurationButton.getParent() != null)
                    buttonBar.getItems().remove(softwareConfigurationButton);
            } else {
                softwareConfigurationTable.setSoftware(null);
                softwareInformationWindow.setEmulatedItem(null, favouriteTreeWindow.isHidden());
                if (softwareConfigurationButton.getParent() != null)
                    buttonBar.getItems().remove(softwareConfigurationButton);
            }
            
            if (view3dButton.getParent() == null) {
                notificationArea.getChildren().add(2, view3dButton);
                highlight(view3dButton);
            }
        });
        softwareTreeWindow.setWindowSlaves(machineConfigurationWindow, softwareInformationWindow, softwareConfigurationWindow);
        softwareInformationWindow.setWindowSlaves(machineConfigurationWindow, softwareTreeWindow, softwareConfigurationWindow);
        softwareInformationWindow.setOnVideoShortcut(() -> videoButton.fire());
        softwareInformationWindow.setOnView3dShortcut(() -> view3dButton.fire());
        softwareTreeWindow.setOnClose(() -> {
            softwareFilterWindow.close();
            onWindowClose.fire();
        });
        softwareInformationWindow.setOnClose(onWindowClose);
        softwareConfigurationWindow.setOnClose(() -> softwareConfigurationButton.setSelected(false));
        softwareConfigurationTable.setMachineConfigurationPane(machineConfigurationPane);
        
        ///// Initialisation of the favourite pane
        
        favouriteTreeWindow.setOnClose(() -> {
            if (favouriteMachineTreeWindow.isDisplayed()) {
                favouriteMachineTreeWindow.setContent(null);
                machineTreeWindow.setContent(machineTreePane);
            }
            if (favouriteSoftwareTreeWindow.isDisplayed()) {
                favouriteSoftwareTreeWindow.setContent(null);
                softwareTreeWindow.setContent(softwareTreePane);
            }
            if (favouriteSoftwareConfigurationWindow.isDisplayed()) {
                favouriteSoftwareConfigurationWindow.setContent(null);
                softwareConfigurationWindow.setContent(softwareConfigurationTable);
            }
            favouriteTreePane.cancelEdit();
            if (machineConfigurationWindow.isDisplayed())
                machineConfigurationWindow.close();
            if (favouriteViewButton.isSelected())
                favouriteViewButton.setSelected(false);
            displayingSoftwareConfiguration = null;
            
            machineTreePane.requestTreeFocus();
            
            machineInformationPane.setFavouriteEnabled(true);
            softwareInformationWindow.setFavouriteEnabled(true);
        });
        favouriteTreePane.setEditController(this);
        favouriteTreePane.setEditableControl(machineConfigurationPane);
        favouriteTreePane.setOnCommitted(favourite -> {
            SoftwareConfiguration softwareConfiguration = favourite.getSoftwareConfiguration();
            if (softwareConfiguration != null) {
                if (!machineConfigurationButton.isSelected())
                    machineConfigurationButton.fire();
                if (softwareTreeWindow.isHidden()) {
                    displayingSoftwareConfiguration = softwareConfiguration;
                    machineConfigurationPane.showList(softwareConfiguration.getDevice());
                } else if (softwareTreePane.getCurrentItem() != softwareConfiguration.getSoftware()) {
                    softwareTreePane.setCurrentItem(softwareConfiguration.getSoftware());
                } else if (softwareInformationWindow.isHidden()) {
                    softwareInformationWindow.show();
                }
            }
        });
        favouriteTreePane.setOnAction(() -> handleLaunchAction(new ActionEvent()));
        
        favouriteMachineTreeWindow.setOnClose(() -> {
            favouriteMachineTreeWindow.setOnceOnAnimationEnded(() -> {
                favouriteMachineTreeWindow.setContent(null);
                machineTreeWindow.setContent(machineTreePane);
            });
            if (!favouriteTreePane.isCommitting())
                favouriteTreePane.cancelEdit();
        });
        favouriteSoftwareTreeWindow.setOnClose(() -> {
            favouriteSoftwareTreeWindow.setOnceOnAnimationEnded(() -> {
                favouriteSoftwareTreeWindow.setContent(null);
                softwareTreeWindow.setContent(softwareTreePane);
            });
            
            if (softwareInformationWindow.isDisplayed() && favouriteTreePane.isEditingMachine())
                favouriteSoftwareConfigurationWindow.close();
            if (favouriteSoftwareConfigurationWindow.isDisplayed())
                favouriteSoftwareConfigurationWindow.close();
            
            if (favouriteTreePane.isEditingSoftware() && !favouriteTreePane.isCommitting())
                favouriteTreePane.cancelEdit();
            else if (favouriteTreePane.isEditingConfiguration())
                favouriteTreePane.hideListBackground();
            if (!favouriteTreePane.isCommitting() && currentDeviceController != null)
                currentDeviceController.hideList();
        });
        favouriteSoftwareConfigurationWindow.setOnClose(() -> {
            favouriteSoftwareConfigurationWindow.setOnceOnAnimationEnded(() -> {
                favouriteSoftwareConfigurationWindow.setContent(null);
                softwareConfigurationWindow.setContent(softwareConfigurationTable);
            });
            if (softwareConfigurationWindow.isDisplayed())
                handleSoftwareConfigurationAction(null);
        });
        
        final Consumer<Boolean> addToFavourites = isShiftDown -> {
            SelectionData data = new SelectionData(currentMachine, machineLoadingCount, softwareTreePane.getCurrentItem(), currentDeviceController);
            if (data.hasSelection()) {
                favouriteTreePane.insert(data);
                favouriteTreeWindow.setOnceOnAnimationEnded(() -> {
                    if (isShiftDown)
                        favouriteTreeWindow.close();
                    else {
                        machineInformationPane.setFavouriteEnabled(false);
                        softwareInformationWindow.setFavouriteEnabled(false);
                    }
                });
                favouriteViewButton.fire();
            }
        };
        // TODO find what prevents event propagations to mainSplitPane.setOnMouseClicked()
        mainSplitPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.MIDDLE && favouriteTreeWindow.isHidden()) {
                addToFavourites.accept(event.isShiftDown());
                event.consume();
            }
        });
        mainSplitPane.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.INSERT) {
                addToFavourites.accept(event.isShiftDown());
                event.consume();
            }
        });
                
        ///// Initialisation of the toolbar buttons
        
        soundButton.selectedProperty().addListener((o, oV, newValue) -> {
            machineInformationPane.setSoundEnabled(newValue);
            softwareInformationWindow.setSoundEnabled(newValue);
        });
        soundButton.setSelected(Configuration.Manager.isSoundEnabled());
        
        videoButton.selectedProperty().addListener((o, oV, newValue) -> {
            machineInformationPane.setVideoEnabled(newValue);
            softwareInformationWindow.setVideoEnabled(newValue);
            soundButton.setDisable(!newValue);
        });
        videoButton.setSelected(Configuration.Manager.isVideoEnabled());
        if (!Configuration.Manager.isVideoEnabled())
            soundButton.setDisable(true);
        
        view3dButton.selectedProperty().addListener((o, oV, newValue) -> {
            softwareInformationWindow.setView3dEnabled(newValue);
        });
        view3dButton.setSelected(Configuration.Manager.isView3dEnabled());
        
        ///// Set up the watcher monitoring for MAME errors
        // Processes launching MAME instances will write errors to temporary files.
        // Those files are captured by the watcher for on-screen display through alerts.
        
        try {
            Path root = Configuration.getRootFolder();
            EmulationErrorWatcher watcher = new EmulationErrorWatcher(root);
            emulationErrorWatcherTimeline.setOnFinished((evt) -> watcher.displayAlert());
            SimpleDirectoryWatchService.getInstance().register(
                watcher,
                root.toAbsolutePath(), // Directory to watch
                "tmp-*.log"
            );
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Unable to register file change listener for *.log", ex);
        }
        SimpleDirectoryWatchService.getInstance().start();
        
        loggingPane.vvalueProperty().bind(loggingTextFlow.heightProperty()); // make the logs pane to always scroll automatically to the bottom
        log("Negatron started and ready");
    }
    
    public void initialiseData() {
        notifierPopup = new NotifierPopup();
        
        cache = new CacheManager(this, (machineList, machineStats, softwareStats, selection) -> Platform.runLater(() -> {
            machineTreePane.setItems(machineList);
            machineFolderViewWindow.initialiseData();
            machineFilterWindow.bind(machineTreePane);
            softwareFilterWindow.bind(softwareTreePane);
            statisticsWindow.setStatistics(machineStats, softwareStats);
            
            try {
                MachineListCache machineListCache = new MachineListCache();
                statusLabel.setText(String.format(Language.Manager.getString("statistics.status"),
                    machineListCache.getVersion().split(" - ")[0].trim(),
                    machineStats.getTotalCount(), machineStats.getParentCount(), machineStats.getCloneCount()
                ));
            } catch (ClassNotFoundException | IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't display statistics", ex);
            }
            
            favouriteTreePane.setOnInitialised(() -> {
                boolean favouriteSelection = false;
                if (Configuration.Manager.isFavouritesDisplayed()) {
                    favouriteViewButton.setSelected(true);
                    handleFavouriteViewAction(null);
                    favouriteSelection = favouriteTreePane.select(Configuration.Manager.getSelectedFavouriteId());
                    Platform.runLater(() -> favouriteTreePane.requestTreeFocus());
                }

                if (!favouriteSelection && selection.hasSelection()) {
                    show(
                            selection.getMachine(),
                            selection.getSoftwareConfiguration(),
                            selection.getMachineConfiguration(),
                            false
                    );
                    machineTreePane.scrollToSelection();
                }
            });
        }));
        
        cache.execute();
    }
    
    public void postInitialise(Stage stage) {
        if (!Video.isEnabled()) {
            soundButton.setSelected(false);
            soundButton.setDisable(true);
            videoButton.setSelected(false);
            videoButton.setDisable(true);
        }
        globalConfigurationWindow.resetVlcPath(); // video automatic detection may have updated the path to VLC
        
        buttonBar.getScene().getWindow().focusedProperty().addListener((o, oV, newValue) -> {
            if (soundButton.isSelected() && newValue && isFocusOnMame) {
                Audio.play(Sound.BACK_FROM_MAME);
                isFocusOnMame = false;
            }
        });
        
        stage.setHeight(Configuration.Manager.getWindowHeight());
        stage.setWidth(Configuration.Manager.getWindowWidth());
        // macOS workaround: if the UI requests to get maximised on the go, some graphical bugs occurs
        // this gets fixed by waiting for all graphical components to be initialised
        if (Configuration.Manager.isWindowMaximised())
            new Timeline(new KeyFrame(Duration.seconds(1),
                evt -> stage.setMaximized(Configuration.Manager.isWindowMaximised())
            )).play();
        stage.setFullScreen(Configuration.Manager.isWindowFullscreen());
        stage.centerOnScreen();
        
        stage.heightProperty().addListener((o, oV, newValue) -> {
            if (newValue.intValue() != Configuration.Manager.getWindowHeight()) try {
                Configuration.Manager.updateWindowHeight(newValue.intValue());
            } catch (IOException ex) { }
        });
        stage.widthProperty().addListener((o, oV, newValue) -> {
            if (newValue.intValue() != Configuration.Manager.getWindowWidth()) try {
                Configuration.Manager.updateWindowWidth(newValue.intValue());
            } catch (IOException ex) { }
        });
        stage.maximizedProperty().addListener((o, oV, newValue) -> {
            if (newValue != Configuration.Manager.isWindowMaximised()) try {
                Configuration.Manager.updateWindowMaximised(newValue);
            } catch (IOException ex) { }
        });
        stage.fullScreenProperty().addListener((o, oV, newValue) -> {
            if (newValue != Configuration.Manager.isWindowFullscreen()) try {
                Configuration.Manager.updateWindowFullscreen(newValue);
            } catch (IOException ex) { }
        });
        
        machineTreePane.requestTreeFocus();
    }
    
    /**
     * Called by the main application when the user asked to change the UI language
     */
    public void restart() {
        Platform.runLater(() -> {
            globalConfigurationButton.fire();
            globalConfigurationWindow.selectOptionsTab();
        });
    }

    @Override
    public void dispose() {
        SelectionData data = new SelectionData(
                currentMachine, machineLoadingCount, softwareTreePane.getCurrentItem(), currentDeviceController
        );
        try {
            if (data.hasSelection())
                Configuration.Manager.updateSelection(
                        data.getMachine().getName(), data.getMachineConfiguration(), data.getSoftwareConfiguration(),
                        favouriteTreeWindow.isDisplayed(), favouriteTreePane.getSelectionId()
                );
            else
                Configuration.Manager.updateSelection(
                        null, null, null,
                        favouriteTreeWindow.isDisplayed(), favouriteTreePane.getSelectionId()
                );
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't save the last selection before Negatron exits", ex);
        }
        
        machineInformationPane.dispose();
        if (cache != null) {
            cache.cancel();
            cache = null;
        }
    }

    @Override
    public void begin(String id, int total) {
        notifierPopup.begin(id, total);
    }

    @Override
    public void notify(String id, int processed) {
        notifierPopup.notify(id, processed);
    }

    @Override
    public void end(String id) {
        notifierPopup.end(id);
    }

    private class SoftwareTreeTableDataFiller implements EventHandler<ActionEvent> {

        private final DeviceController controller;

        public SoftwareTreeTableDataFiller(DeviceController controller) {
            this.controller = controller;
        }

        @Override
        public void handle(ActionEvent evt) {
            reset();
            
            if (controller.isListButtonSelected()) {
                if (softwareConfigurationWindow.isDisplayed())
                    handleSoftwareConfigurationAction(null);
                
                List<String> interfaceFormats = controller.getMachineComponent().getInterfaceFormats();
                Machine machine = machineTreePane.getCurrentItem();

                // Link software tree to software filter pane
                String configurationId = "";
                for (String interfaceFormat : interfaceFormats) {
                    if (!configurationId.equals(""))
                        configurationId += ".";
                    configurationId += interfaceFormat;
                }
                configurationId += "-";
                for (SoftwareListFilter softwareListFilter : machine.getSoftwareLists()) {
                    if (!configurationId.endsWith("-"))
                        configurationId += ".";
                    configurationId += softwareListFilter.getSoftwareList();
                }
                softwareFilterWindow.setConfigurationId(configurationId);

                // Fill software tree
                List<Software> softwares = machine.getSoftwareLists().stream().flatMap(
                    softwareListFilter -> {
                        SoftwareList softwareList = softwareLists.get(softwareListFilter.getSoftwareList());
                        if (softwareList != null)
                            return softwareList.getSoftwares(interfaceFormats, softwareListFilter.getFilter()).stream();
                        
                        alert(AlertType.WARNING, String.format(
                                "Negatron couldn't find the software list %s for the machine %s within MAME's internal database.\n\n" +
                                "Please, contact the MAME team at https://www.mamedev.org/ or report this issue to http://www.babelsoft.net/forum/index.php",
                                softwareListFilter.getSoftwareList(), machine.getName()
                        ));
                        return Stream.ofNullable(null);
                    }
                ).collect(Collectors.toList());
                softwareTreePane.setItems(softwares);

                // Show software tree
                softwareTreeWindow.showMaximised();
                if (
                    favouriteTreePane.isEditing() && !favouriteTreePane.isEditingSoftware() &&
                    favouriteSoftwareTreeWindow.isHidden()
                )   // in the case of software editing, the softlist is already invoked by the cell
                    favouriteTreePane.showSoftwareList();

                // Link software tree to current device
                currentDeviceController = controller;

                if (displayingSoftwareConfiguration != null) {
                    softwareTreePane.setCurrentItem(displayingSoftwareConfiguration.getSoftware());
                    if (softwareInformationWindow.isHidden())
                        softwareInformationWindow.show();
                    if (displayingSoftwareConfiguration != null && displayingSoftwareConfiguration.getSoftwarePart() == null)
                        displayingSoftwareConfiguration = null;
                }
            } else {
                softwareTreeWindow.hide();
                if (softwareConfigurationWindow.isDisplayed())
                    handleSoftwareConfigurationAction(null);
                if (!isHidingConfigurationPane)
                    softwareInformationWindow.hide();
                else // When hiding the config pane, the soft info pane is already hidden through another trigger. Doing it again here would result into a graphical bug.
                    isHidingConfigurationPane = false;
                
                if (favouriteTreePane.isEditing())
                    favouriteTreePane.hideSoftwareList();
            }
        }
    }
    
    private class EmulationErrorWatcher implements DirectoryWatchService.OnFileChangeListener {

        private final Path root;
        private Path filePath;
        private long readLines;
        
        public EmulationErrorWatcher(Path root) {
            this.root = root;
        }
        
        @Override
        public void onFileModify(Path filePath) {
            if (!filePath.equals(this.filePath)) {
                this.filePath = filePath;
                readLines = 0;
            }
            // instead of directly displaying file content,
            // do it after a delay in order to avoid having several message boxes
            // because of very fast successive file update notifications
            emulationErrorWatcherTimeline.playFromStart();
        }
        
        public void displayAlert() {
            try {
                StringBuilder sb = new StringBuilder();
                Files.newBufferedReader(root.resolve(filePath)).lines().skip(readLines).forEach(line -> {
                    if (sb.length() > 0)
                        sb.append(System.lineSeparator());
                    sb.append(line);
                    ++readLines;
                });
                String msg = sb.toString();
                
                if (Strings.isValid(msg)) Platform.runLater(() -> {
                    log(msg);

                    if (loggingWindow.isHidden()) {
                        String s = msg.toLowerCase();
                        if (s.contains("error:")) // only bother users when actual errors have been raised by MAME
                            loggingWindow.show();
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setApplication(Application application) {
        this.application = application;
        machineInformationPane.setApplication(application);
        softwareInformationWindow.setApplication(application);
    }

    public void setSoftwareLists(Map<String, SoftwareList> softwareLists) {
        this.softwareLists = softwareLists;
        machineLoader.setSoftwareLists(softwareLists);
    }
    
    public void setFavouriteTree(FavouriteTree favourites) {
        favouriteTreePane.setFavouriteTree(favourites);
    }
    
    public ReadOnlyBooleanProperty OnSucceededProperty() {
        return onSucceededProperty;
    }
    
    public boolean hasSucceeded() {
        return onSucceededProperty.get();
    }
    
    public void setSucceeded(boolean succeeded) {
        onSucceededProperty.set(succeeded);
    }
    
    public SimpleDoubleProperty ProgressProperty() {
        return progressProperty;
    }
    
    public double getProgress() {
        return progressProperty.get();
    }
    
    @Override
    public void alert(AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.initOwner(launchButton.getScene().getWindow());
        alert.setResizable(true);
        alert.show();
        if (soundButton.isSelected())
            Audio.play(Sound.ERROR);
    }
    
    private int _highlightCount;
    private void highlight(ToggleButton button) {
        final PseudoClass CSS_FOCUSED = PseudoClass.getPseudoClass("focused");
        final PseudoClass CSS_HOVER = PseudoClass.getPseudoClass("hover");
        final Duration FREQUENCY = Duration.millis(200.0);
        final int MAX_ROUND = 25;
        
        _highlightCount = 0;
        
        PauseTransition highlighted = new PauseTransition(FREQUENCY);
        PauseTransition dimmed = new PauseTransition(FREQUENCY);
        
        highlighted.setOnFinished(e -> { if (_highlightCount < MAX_ROUND) {
            button.pseudoClassStateChanged(CSS_FOCUSED, true);
            button.pseudoClassStateChanged(CSS_HOVER, true);
            dimmed.playFromStart();
            ++_highlightCount;
        }});
        dimmed.setOnFinished(e -> {
            button.pseudoClassStateChanged(CSS_FOCUSED, false);
            button.pseudoClassStateChanged(CSS_HOVER, false);
            if (_highlightCount < MAX_ROUND) {
                highlighted.playFromStart();
                ++_highlightCount;
            }
        });
        
        highlighted.play();
    }
    
    private boolean mustSetSoftware(Software software) {
        return isDataReloadFromSoftwareListEnabled && currentDeviceController != null && software != null;
    }
    
    private List<String> commandLineToParameters(String commandLine) {
        List<String> parameters = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
        Matcher regexMatcher = regex.matcher(commandLine.trim());
        
        while (regexMatcher.find())
            parameters.add(regexMatcher.group());
        Logger.getLogger(MainController.class.getName()).log(Level.INFO, parameters.toString());
        
        return parameters;
    }
    
    private void load() {
        reload(null);
        softwareInformationWindow.setMachine(currentMachine);
    }
    
    private void reload(String origin) {
        if (currentMachine != null) {
            machineLoader.setInitialisationData(currentMachine, origin, origin != null ? Mode.UPDATE : Mode.CREATE);
            machineLoader.restart();
            isLoading = true;
        } /* else {
            the code can arrive here only when, in the favourite pane, the user selected the favourite root
            after having selected a favourite machine with a software part (e.g. multi-part CD) as a value to one of its device (e.g. CD drive)
        } */
    }
    
    private void reset() {
        if (currentDeviceController != null) {
            currentDeviceController.setListButtonSelected(false);
            currentDeviceController = null;
        }
        softwareTreePane.reset();
    }
    
    private void reset(List<Control<?>> controls) {
        if (machineLoader.getMode() == Mode.UPDATE && controls != null && currentDeviceController != null) {
            Optional<Control<?>> candidateControl = controls.stream().filter(control -> {
                if (control.getStatus() != Difference.DELETED && control instanceof DeviceControl) {
                    Device candidateDevice = ((DeviceControl) control).getController().getMachineComponent();
                    Device currentDevice = currentDeviceController.getMachineComponent();
                    
                    return candidateDevice.canReplace(currentDevice);
                } else
                    return false;
            }).findAny();
            //re-wire software tree to replacing device
            if (candidateControl.isPresent()) {
                currentDeviceController = (DeviceController) candidateControl.get().getController();
                if (softwareTreeWindow.isDisplayed())
                    currentDeviceController.setListButtonSelected(true);
                return;
            }
        }
        
        if (controls != null && controls.size() > 0) {
            if (machineConfigurationButton.getParent() == null) {
                buttonBar.getItems().add(1, machineConfigurationButton);
                highlight(machineConfigurationButton);
            }
        } else if (machineConfigurationButton.getParent() != null)
                buttonBar.getItems().remove(machineConfigurationButton);
        
        reset();
    }
    
    private void loadMachineInformation() {
        machineInformationPane.setEmulatedItem(currentMachine, favouriteTreeWindow.isHidden());
        machineInformationPane.showTab();
    }
    
    private void disableAction(boolean value) {
        machineTreePane.setDisable(value);
        launchButton.setDisable(value);
        advancedParametrisationButton.setDisable(value);
        isDataReloadFromSoftwareListEnabled = !value;
    }
    
    private void closeSoftwareFilterWindow() {
        if (softwareFilterWindow.isDisplayed())
            softwareFilterWindow.close();
    }
    
    private void closeGlobalConfigurationWindow() {
        if (globalConfigurationWindow.isDisplayed())
            globalConfigurationWindow.close();
    }
    
    private void closeStatisticsWindow() {
        if (statisticsWindow.isDisplayed())
            statisticsWindow.close();
    }
    
    private void launchMame(List<String> parameters) throws IOException {
        if (soundButton.isSelected()) {
            Audio.play(Sound.LAUNCH);
            isFocusOnMame = true;
        }
        machineInformationPane.pauseVideo();
        softwareInformationWindow.pauseVideo();
        Mame.launch(parameters);
    }
    
    @Override
    public void requestMachineList(ConfigurationChangeListener listener, SoftwareConfiguration softwareConfiguration) {
        onMachineLoaded = listener;
        editingSoftwareConfiguration = softwareConfiguration;
        
        machineTreeWindow.setContent(null);
        favouriteMachineTreeWindow.setContent(machineTreePane);
        favouriteMachineTreeWindow.show();
        if (machineConfigurationWindow.isDisplayed())
            machineConfigurationWindow.close();
    }
    
    @Override
    public void dismissMachineList(ConfigurationChangeListener listener) {
        dismissConfigurationPane(listener);
        
        if (favouriteMachineTreeWindow.isDisplayed())
            favouriteMachineTreeWindow.close();
    }
    
    @Override
    public void requestSoftwareList(ConfigurationChangeListener listener) {
        onMachineLoaded = listener;
        
        softwareTreeWindow.setContent(null);
        favouriteSoftwareTreeWindow.setContent(softwareTreePane);
        favouriteSoftwareTreeWindow.show();
    }
    
    @Override
    public void dismissSoftwareList(ConfigurationChangeListener listener) {
        if (favouriteSoftwareTreeWindow.isDisplayed())
            favouriteSoftwareTreeWindow.close();
        if (!favouriteTreePane.isEditingMachine())
            onMachineLoaded = null;
    }
    
    @Override
    public void requestConfigurationPane(ConfigurationChangeListener listener, SoftwareConfiguration softwareConfiguration) {
        onMachineLoaded = listener;
        editingSoftwareConfiguration = softwareConfiguration;
        
        if (machineConfigurationWindow.isDisplayed())
            machineConfigurationWindow.setOnceOnAnimationEnded(() -> handleMachineConfigurationAction(null));
        handleMachineConfigurationAction(null);
    }
    
    @Override
    public void dismissConfigurationPane(ConfigurationChangeListener listener) {
        isHidingConfigurationPane = true;
        favouriteTreePane.hideSoftwareList();
        onMachineLoaded = null;
        editingSoftwareConfiguration = null;
        
        if (machineConfigurationWindow.isDisplayed())
            handleMachineConfigurationAction(null);
    }

    @Override
    public void show(Machine machine, SoftwareConfiguration software, MachineConfiguration configuration, boolean mustMigrate) {
        displayingSoftwareConfiguration = software;
        
        if (softwareConfigurationWindow.isDisplayed())
            handleSoftwareConfigurationAction(null);
        if (software == null && softwareInformationWindow.isDisplayed())
            softwareInformationWindow.close();

        if (machine != null) {
            if (!mustMigrate)
                // force sync between favourite conf and current machine conf
                machine.forceParameters(configuration.getParameters());
            if (machineTreePane.getCurrentItem() == machine)
                load();
            else
                machineTreePane.setCurrentItem(machine);
        } else
            machineTreePane.setCurrentItem(null);
    }
    
    private void fireOnMachineLoaded() {
        if (onMachineLoaded != null) {
            Software software = softwareTreePane.getCurrentItem();
            if (software != null) {
                if (currentDeviceController.getMachineComponent().getValue() != null)
                    onMachineLoaded.changed(currentMachine, new SoftwareConfiguration(
                        currentMachine, currentDeviceController.getMachineComponent(), software
                    ));
                else
                    onMachineLoaded.changed(currentMachine, null);
            } else if (editingSoftwareConfiguration != null && currentMachine.getName().equals(editingSoftwareConfiguration.getMachine()))
                onMachineLoaded.changed(currentMachine, editingSoftwareConfiguration);
            else
                onMachineLoaded.changed(currentMachine, null);
        }
    }
    
    /*
    private void handleTestAction(ActionEvent event) {
        Debug.dump(machineTree);
    }
    */

    public void handleCacheNotification(boolean isRunning) {
        if (isRunning) {
            if (notifier.getParent() == null)
                notificationArea.getChildren().add(notifier);
        } else {
            cache = null;
            notificationArea.getChildren().remove(notifier);
        }
        machineFilterWindow.disableStatusCriteria(isRunning);
        softwareFilterWindow.disableStatusCriteria(isRunning);
        globalConfigurationWindow.disableLanguageOption(isRunning);
    }
    
    private void handleMachineLoaded() {
        List<Control<?>> controls = machineLoader.getValue();

        if (controls == MachineLoader.MAME_FATAL_ERROR) {
            isMameFatalErrorMode = true;
            if (mustTriggerLaunchAction)
                mustTriggerLaunchAction = false;

            disableAction(true);
            alert(AlertType.WARNING, Language.Manager.getString("machineConfValidated.error.text"));
        } else {
            if (isMameFatalErrorMode) {
                disableAction(false);
                isMameFatalErrorMode = false;
            }

            if (controls != null) {
                reset(controls);
                
                machineConfigurationPane.setControls(currentMachine, controls);
                Software software = softwareTreePane.getCurrentItem();
                if (mustSetSoftware(software))
                    softwareConfigurationTable.setSoftware(software);

                controls.stream().filter(
                    control -> control instanceof DeviceControl
                ).map(
                    control -> (DeviceController) control.getController()
                ).forEach(
                    controller -> controller.addSoftwareListActionHandler(new SoftwareTreeTableDataFiller(controller))
                );

                loadMachineInformation();
                
                if (displayingSoftwareConfiguration != null) {
                    if (!favouriteTreePane.isEditing() || favouriteTreePane.isEditingSoftware()) {
                        if (!machineConfigurationButton.isSelected())
                            machineConfigurationButton.fire();
                        machineConfigurationPane.showList(displayingSoftwareConfiguration.getDevice());
                    } else
                        displayingSoftwareConfiguration = null;
                }
            } else
                machineConfigurationPane.clearBadges();
            fireOnMachineLoaded();
        }

        isLoading = false;

        if (mustTriggerLaunchAction) {
            mustTriggerLaunchAction = false;
            handleLaunchAction(null);
        } else if (soundButton.isSelected() && machineLoader.getMode() == Mode.CREATE && controls.size() > 0)
            Audio.play(Sound.MACHINE_SETTINGS);
    }

    @FXML
    private void handleLaunchAction(ActionEvent event) {
        // workaround to avoid having 2 instances of MAME being launch simultaneously, e.g. because of quadruple mouse clicking
        if (event != null) {
            if (launchActionTimeline.getStatus() == Animation.Status.RUNNING)
                return;
            else
                launchActionTimeline.play();
        } // else, as method call isn't resulting from a user action, it's safe to go on
        // end of workaround
        if (!isLoading) try {
            if (currentMachine == null) {
                log("Launching MAME");
                launchMame(new ArrayList<>());
            } else if (currentMachine.isReady()) {
                log("Launching MAME with those parameters: %s", currentMachine.toCommandLine());
                launchMame(currentMachine.parameters());
            } else {
                alert(AlertType.WARNING, Language.Manager.getString("machineConfMandatory.error.text"));
                if (machineConfigurationWindow.isHidden())
                    handleMachineConfigurationAction(null);
            }
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't launch MAME", ex);
            alert(AlertType.ERROR, String.format(Language.Manager.getString("mameLaunching.error.text"), ex));
        } else
            mustTriggerLaunchAction = true;
    }
    
    @FXML
    private void handleShowLogs(ActionEvent event) {
        if (loggingWindow.isHidden())
            loggingWindow.show();
    }

    @FXML
    private void handleMachineConfigurationAction(ActionEvent event) {
        closeGlobalConfigurationWindow();
        closeSoftwareFilterWindow();
        
        switch (machineConfigurationWindow.getDisplayMode()) {
            case INTERMEDIATE:
            case MAXIMISED:
                machineConfigurationWindow.close();
                machineConfigurationButton.setSelected(false);
                break;
            default:
                machineTreePane.closeFilterPane();
                machineTreePane.closeViewPane();
                machineConfigurationWindow.show();
                machineConfigurationButton.setSelected(true);
                break;
        }
    }
    
    @FXML
    private void handleSoftwareConfigurationAction(ActionEvent event) {
        closeGlobalConfigurationWindow();
        
        switch (softwareConfigurationWindow.getDisplayMode()) {
            case INTERMEDIATE:
            case MAXIMISED:
                softwareConfigurationWindow.close();
                softwareConfigurationButton.setSelected(false);
                if (favouriteSoftwareConfigurationWindow.isDisplayed())
                    favouriteSoftwareConfigurationWindow.close();
                break;
            default:
                softwareConfigurationWindow.show();
                softwareConfigurationButton.setSelected(true);
                if (favouriteTreePane.isEditing()) {
                    softwareConfigurationWindow.setContent(null);
                    favouriteSoftwareConfigurationWindow.setContent(softwareConfigurationTable);
                    favouriteSoftwareConfigurationWindow.show();
                }
                break;
        }
    }

    @FXML
    private void handleAdvancedParametrisationAction(ActionEvent event) {
        if (!isLoading) {
            String commandLine = null;
            if (currentMachine != null)
                commandLine = currentMachine.toCommandLine();
            String name;
            if (currentDeviceController != null)
                name = currentDeviceController.getText();
            else if (currentMachine != null)
                name = currentMachine.getName();
            else
                name = "test";
            
            Dialog<String> dialog = new AdvancedParametrisationDialog(
                advancedParametrisationButton.getScene().getWindow(), name, commandLine,
                new ButtonType(Language.Manager.getString("launch"), ButtonData.OK_DONE)
            );
            
            dialog.showAndWait().ifPresent(command -> {
                try {
                    List<String> parameters = commandLineToParameters(command);
                    launchMame(parameters);
                } catch (IOException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't launch MAME", ex);
                    alert(AlertType.ERROR, String.format(Language.Manager.getString("mameLaunching.error.text"), ex));
                }
            });
        }
    }
    
    @FXML
    private void handleFavouriteViewAction(ActionEvent event) {
        if (favouriteViewButton.isSelected()) {
            closeGlobalConfigurationWindow();
            closeStatisticsWindow();
            
            favouriteTreeWindow.showMaximised();
            machineTreePane.clearSelection();
            favouriteTreePane.clearSelection();
            
            machineInformationPane.setFavouriteEnabled(false);
            softwareInformationWindow.setFavouriteEnabled(false);
        } else {
            favouriteTreeWindow.close();
            favouriteTreePane.clearSelection();
            
            machineInformationPane.setFavouriteEnabled(true);
            softwareInformationWindow.setFavouriteEnabled(true);
        }
    }
    
    @FXML
    private void handleStatisticsAction(ActionEvent event) {
        if (statisticsButton.isSelected()) {
            closeGlobalConfigurationWindow();
            statisticsWindow.showMaximised();
        } else
            statisticsWindow.close();
    }
    
    @FXML
    private void handleSoundAction(ActionEvent event) throws IOException {
        if (soundButton.isSelected())
            Audio.play(Sound.SOUND_ON);
        try {
            Configuration.Manager.updateSoundEnabled(soundButton.isSelected());
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't update Negatron.ini", ex);
            alert(AlertType.ERROR, String.format(Language.Manager.getString("confUpdating.error.text"), ex));
        }
    }
    
    @FXML
    private void handleVideoAction(ActionEvent event) throws IOException {
        try {
            Configuration.Manager.updateVideoEnabled(videoButton.isSelected());
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't update Negatron.ini", ex);
            alert(AlertType.ERROR, String.format(Language.Manager.getString("confUpdating.error.text"), ex));
        }
    }
    
    @FXML
    private void handleView3dAction(ActionEvent event) throws IOException {
        try {
            Configuration.Manager.updateView3dEnabled(view3dButton.isSelected());
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Couldn't update Negatron.ini", ex);
            alert(AlertType.ERROR, String.format(Language.Manager.getString("confUpdating.error.text"), ex));
        }
    }
    
    @FXML
    private void handleGlobalConfigurationAction(ActionEvent event) {
        if (globalConfigurationWindow.isHidden()) {
            closeStatisticsWindow();
            globalConfigurationWindow.showMaximised();
        } else
            globalConfigurationWindow.close();
    }

    @FXML
    private void handleHelpAction(ActionEvent event) {
        closeGlobalConfigurationWindow();
        machineTreePane.closeFilterPane();
        machineTreePane.clearSelection();
        machineInformationPane.selectInformationTab();
    }
    
    @FXML
    private void handleNotifierMouseEntered(MouseEvent event) {
        notifierPopup.show(notifier);
    }
    
    @FXML
    private void handleWindowMaximized(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Stage stage = (Stage) buttonBar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }
    
    @FXML
    private void handleWindowInitiateDragging(MouseEvent event) {
        mousePosX = event.getScreenX();
        mousePosY = event.getScreenY();
    }
    
    @FXML
    private void handleWindowDragged(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
                double mouseOldX = mousePosX;
                double mouseOldY = mousePosY;
                mousePosX = event.getScreenX();
                mousePosY = event.getScreenY();
                double mouseDeltaX = mousePosX - mouseOldX;
                double mouseDeltaY = mousePosY - mouseOldY;
                
                Window window = buttonBar.getScene().getWindow();
                window.setX(window.getX() + mouseDeltaX);
                window.setY(window.getY() + mouseDeltaY);
        }
    }
}
