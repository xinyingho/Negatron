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
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import net.babelsoft.negatron.model.HddGeometry;
import net.babelsoft.negatron.model.SizeUnit;
import net.babelsoft.negatron.theme.Language;
import net.babelsoft.negatron.util.function.Delegate;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author capan
 */
public class HddCreationDialog extends Dialog<HddGeometry> {
    
    private class HddModelDataHandler extends DefaultHandler {
        
        private String manufacturer;

        @Override
        public void startElement(String namespaceURI,
            String localName,
            String qName,
            Attributes atts
        ) throws SAXException {
            switch (qName) {
                case "manufacturer" -> manufacturer = atts.getValue("name");
                case "model" -> {
                    HddGeometry model = new HddGeometry(
                        manufacturer, atts.getValue("name"),
                        atts.getValue("cylinder"), atts.getValue("head"),
                        atts.getValue("sector"), atts.getValue("sectorSize")
                    );
                    hddModels.getItems().add(model);
                }
            }
        }
    }
    
    private static final ButtonType BTN_TYPE_CREATE = new ButtonType(
        Language.Manager.getString("create"), ButtonData.OK_DONE
    );
    
    private ComboBox<HddGeometry> hddModels;
    private TextField path;
    private SizeField cylinder;
    private SizeField head;
    private SizeField sector;
    private ComboBox<Long> sectorSize;
    private TextField manufacturer;
    private TextField model;
    private Spinner<Double> version;
    private Button createButton;
    
    private boolean modifyingComboBox;
    private boolean modifyingSpinners;
    private boolean isScsi;
    
    public HddCreationDialog(List<String> interfaceFormats, Window owner) {
        super();
        initOwner(owner);
        
        ResourceBundle language = Language.Manager.getBundle();
        setTitle(language.getString("hddCreation"));
        setGraphic(new ImageView(new Image(HddCreationDialog.class.getResourceAsStream(
            "/net/babelsoft/negatron/resource/icon/device/drive-harddisk.png"
        ))));
        setHeaderText(language.getString("hddCreation.text"));
        
        // Set up main area
        hddModels = new ComboBox<>();
        path = new TextField();
        Button browseButton = new Button(language.getString("browse..."));
        browseButton.onActionProperty().set(event -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(language.getString("chdFiles"), "*.chd"),
                new FileChooser.ExtensionFilter(language.getString("allFiles"), "*.*")
            );
            File f = fc.showSaveDialog(getOwner());

            if (f != null)
                path.setText(f.getAbsolutePath());
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(5.0);
        grid.setVgap(5.0);
        grid.setAlignment(Pos.CENTER);
        grid.add(new Label(language.getString("hddModel")), 0, 0);
        grid.add(hddModels, 1, 0);
        grid.add(new Label(language.getString("outputPath")), 0, 1);
        grid.add(path, 1, 1);
        grid.add(browseButton, 2, 1);
        getDialogPane().setContent(grid);
        
        getDialogPane().getButtonTypes().addAll(BTN_TYPE_CREATE, ButtonType.CANCEL);
        createButton = (Button) getDialogPane().lookupButton(BTN_TYPE_CREATE);
        createButton.setDisable(true);
        
        // Fill HDD models
        isScsi = interfaceFormats.stream().anyMatch(
            interfaceFormat -> interfaceFormat.equals("scsi_hdd")
        );
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(false);
        try {
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(new HddModelDataHandler());
            xmlReader.parse(new InputSource(HddCreationDialog.class.getResourceAsStream(
                isScsi ?
                "/net/babelsoft/negatron/resource/data/SCSI-HDDModels.xml" :
                "/net/babelsoft/negatron/resource/data/HDDModels.xml"
            )));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(HddCreationDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Set up details area
        cylinder = new SizeField();
        head = new SizeField();
        sector = new SizeField();
        sectorSize = new ComboBox<>(FXCollections.observableArrayList(
            // there have also been hard disks with 520 bytes per sector for non-IBM compatible machines
            128L, 256L, 512L, 520L, 1024L, 2048L, 4096L, 8192L, 16384L, 32768L, 65536L
        ));
        TextField totalSize = new TextField(SizeUnit.factorise(0));
        totalSize.setDisable(true);
        if (isScsi) {
            BiFunction<Change, Integer, Change> formatChange = (c, maxLength) -> {
                if (c.isContentChange()) {
                    int newLength = c.getControlNewText().length();
                    if (newLength > maxLength) {
                        // replace the input text with the last length chars
                        String tail = c.getControlNewText().substring(newLength - maxLength, newLength);
                        c.setText(tail);
                        // replace the range to complete text
                        // valid coordinates for range is in terms of old text
                        int oldLength = c.getControlText().length();
                        c.setRange(0, oldLength);
                    }
                }
                return c;
            };
            
            manufacturer = new TextField();
            manufacturer.setTextFormatter(new TextFormatter<>(c -> formatChange.apply(c, 8)));
            model = new TextField();
            manufacturer.setTextFormatter(new TextFormatter<>(c -> formatChange.apply(c, 16)));
            version = new Spinner<>(0.0, 99.9, 1.0, 0.1);
            version.setEditable(true);
            version.getEditor().focusedProperty().addListener((o, oV, newValue) -> {
                if (!newValue)
                    version.getValueFactory().setValue(Double.valueOf(version.getEditor().getText()));
            });
        }
        
        HBox hbox1 = new HBox(
            new Label(language.getString("cylinder")), cylinder,
            new Label(language.getString("head")), head,
            new Label(language.getString("sector")), sector
        );
        hbox1.setAlignment(Pos.CENTER);
        hbox1.setSpacing(5.0);
        HBox hbox2 = new HBox(
            new Label(language.getString("sectorSize")), sectorSize,
            new Label(language.getString("totalSize")), totalSize
        );
        hbox2.setAlignment(Pos.CENTER);
        hbox2.setSpacing(5.0);
        VBox vbox = new VBox(5.0, hbox1, hbox2);
        if (isScsi) {
            HBox hbox3 = new HBox(
                new Label(language.getString("manufacturer")), manufacturer,
                new Label(language.getString("hddModel")), model,
                new Label(language.getString("version")), version
            );
            hbox3.setAlignment(Pos.CENTER);
            hbox3.setSpacing(5.0);
            vbox.getChildren().add(hbox3);
        }
        
        getDialogPane().setExpandableContent(vbox);
        
        // Tie choicebox and details area together
        modifyingComboBox = false;
        modifyingSpinners = false;
        
        hddModels.getSelectionModel().selectedItemProperty().addListener((o, oV, newValue) -> {
            if (modifyingSpinners)
                return;
            
            modifyingComboBox = true;
            cylinder.setText(Long.toString(newValue.getCylinder()));
            head.setText(Long.toString(newValue.getHead()));
            sector.setText(Long.toString(newValue.getSector()));
            sectorSize.getSelectionModel().select(newValue.getSectorSize());
            totalSize.setText(newValue.getTotalSize());
            if (isScsi) {
                manufacturer.setText(newValue.getManufacturer());
                model.setText(newValue.getModel());
                version.getValueFactory().setValue(1.0);
            }
            modifyingComboBox = false;
        });
        
        path.textProperty().addListener((o, oV, nV) -> validateCreateButtonState());
        
        Delegate resetComboBox = () -> {
            validateCreateButtonState();
            
            if (modifyingComboBox)
                return;
            
            modifyingSpinners = true;
            hddModels.getSelectionModel().clearSelection();
            totalSize.setText(new HddGeometry("",
                cylinder.getText(), head.getText(), sector.getText(),
                sectorSize.getSelectionModel().getSelectedItem().toString()
            ).getTotalSize());
            modifyingSpinners = false;
        };
        cylinder.textProperty().addListener((o, oV, nV) -> resetComboBox.fire());
        head.textProperty().addListener((o, oV, nV) -> resetComboBox.fire());
        sector.textProperty().addListener((o, oV, nV) -> resetComboBox.fire());
        sectorSize.getSelectionModel().selectedIndexProperty().addListener((o, oV, nV) -> resetComboBox.fire());
        if (isScsi) {
            manufacturer.textProperty().addListener((o, oV, nV) -> resetComboBox.fire());
            model.textProperty().addListener((o, oV, nV) -> resetComboBox.fire());
            version.valueProperty().addListener((o, oV, nV) -> resetComboBox.fire());
        }
        
        // Set up final requirements
        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if (data == ButtonData.OK_DONE)
                if (isScsi)
                    return new HddGeometry(
                        path.getText(),
                        manufacturer.getText(), model.getText(), version.getValue(),
                        cylinder.getText(), head.getText(), sector.getText(),
                        sectorSize.getSelectionModel().getSelectedItem().toString()
                    );
                else
                    return new HddGeometry(
                        path.getText(),
                        cylinder.getText(), head.getText(), sector.getText(),
                        sectorSize.getSelectionModel().getSelectedItem().toString()
                    );
            else
                return null;
        });
    }
    
    private void validateCreateButtonState() {
        if (
            path.getText().isEmpty() ||
            cylinder.getText().isEmpty() || head.getText().isEmpty() || sector.getText().isEmpty() ||
            sectorSize.getSelectionModel().isEmpty() || isScsi && manufacturer.getText().isEmpty() ||
            isScsi && model.getText().isEmpty() || isScsi && version.getEditor().getText().isEmpty()
        )   createButton.setDisable(true);
        else
            createButton.setDisable(false);
    }
}
