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
package net.babelsoft.negatron;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ErrorNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.babelsoft.negatron.controller.MainController;
import net.babelsoft.negatron.io.cache.MachineListCache;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.preloader.NegatronPreloader;
import net.babelsoft.negatron.preloader.NegatronPreloader.Notifier;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class NegatronApp extends Application implements Notifier {
    
    private Stage stage;
    private MainController controller;
    private boolean mustClose;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", NegatronPreloader.class.getCanonicalName());
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception { try {
        if (stage != null) {
            this.stage = stage;

            if (mustClose) {
                stage.close();
                return;
            }
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/control/fxml/MainView.fxml"), Language.Manager.getBundle());
        Parent root = fxmlLoader.load();

        controller = fxmlLoader.getController();
        if (stage != null) {
            controller.ProgressProperty().addListener(
                o -> notifyPreloader(new Preloader.ProgressNotification(controller.getProgress()))
            );
            controller.OnSucceededProperty().addListener(o -> {
                if (controller.hasSucceeded())
                    notifyPreloader(new StateChangeNotification(StateChangeNotification.Type.BEFORE_START));
            });
        }
        controller.setApplication(this);
        controller.initialiseData();
        
        if (stage != null) {
            String implVersion = null;
            try {
                // Java 8 version of the below block: implVersion = getClass().getPackage().getImplementationVersion();
                // Since Java 9 and the advent of modules, information from manifest aren't loaded anymore and so a workaround is needed
                String res = getClass().getResource(getClass().getSimpleName() + ".class").toString();
                URL url = new URL(res.substring(0, res.length() - (getClass().getName() + ".class").length()) + JarFile.MANIFEST_NAME);
                Manifest manifest = new Manifest(url.openStream());
                implVersion = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            } catch (IOException ex) {
                // swallow errors
            }
            stage.setTitle("Negatron v" + implVersion);
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron.png")));
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron@1.5x.png")));
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron@2x.png")));
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron@3x.png")));
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron@4x.png")));
            stage.getIcons().add(new Image(NegatronApp.class.getResourceAsStream("resource/icon/Negatron@16x.png")));
        } else
            controller.restart();
        
        this.stage.setScene(new Scene(root));
    } catch (Throwable ex) {
        Logger.getLogger(NegatronApp.class.getName()).log(Level.SEVERE, "Couldn't start Negatron", ex);
        throw ex;
    }}

    @Override
    public void onConfigurationSucceeded() {
        String locale = Configuration.Manager.getLanguage();
        if (Strings.isValid(locale))
            Locale.setDefault(Locale.forLanguageTag(locale));
        ResourceBundle language = Language.Manager.getBundle();
        
        try {
            Configuration.Manager.initialise();
        } catch (Exception ex) {
            Logger.getLogger(NegatronApp.class.getName()).log(Level.SEVERE, "Couldn't read .ini file", ex);
            mustClose = true;
            notifyPreloader(new ErrorNotification(
                language.getString("iniReading.error"), language.getString("iniReading.error.text"), ex
            ));
            return;
        }
        
        boolean mustSynchronize, needsConfirmation;
        MachineListCache cache;
        try {
            cache = new MachineListCache();
            mustSynchronize = !cache.checkVersion();
            needsConfirmation = cache.getVersion() != null;
        } catch (Exception ex) {
            Logger.getLogger(NegatronApp.class.getName()).log(Level.SEVERE, "Couldn't determine MAME version", ex);
            mustClose = true;
            notifyPreloader(new ErrorNotification(
                language.getString("mameVersion.error"), language.getString("mameVersion.error.text"), ex
            ));
            return;
        }
        
        if (mustSynchronize) {
            Optional<ButtonType> result;
            if (needsConfirmation) {
                // ask user if cache should be refreshed
                Alert alert = new Alert(
                    Alert.AlertType.WARNING, language.getString("mameUpdated.warning"),
                    ButtonType.YES, ButtonType.NO
                );
                alert.initOwner(stage);
                result = alert.showAndWait();
            } else
                // it's the first run of Negatron, do not popup prompt and directly proceed with cache generation
                result = Optional.of(ButtonType.YES);

            if (result.isPresent() && result.get() == ButtonType.YES) {
                // clear cache
                try {
                    cache.clear();
                } catch (IOException ex) {
                    Logger.getLogger(NegatronApp.class.getName()).log(Level.SEVERE, "Couldn't clear cache", ex);
                    mustClose = true;
                    notifyPreloader(new ErrorNotification(
                        language.getString("cacheClearing.error"), language.getString("cacheClearing.error.text"), ex
                    ));
                }
            }
        }
    }

    @Override
    public void onPreloadingSucceeded() {
        Platform.runLater(() -> {
            stage.show();
            controller.postInitialise(stage);
        });
    }
    
    @Override
    public void stop() {
        if (controller != null)
            controller.dispose();
        System.exit(0); // required to force the PDF Viewer to let go of its AWT event thread and 2 timer threads
    }
}
