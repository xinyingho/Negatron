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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import net.babelsoft.negatron.io.loader.InformationLoader;
import net.babelsoft.negatron.io.loader.LoadingObserver;
import net.babelsoft.negatron.view.control.adapter.LoadingData;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class NotifierPopupController implements Initializable, LoadingObserver {
    
    @FXML
    private GridPane grid;

    private Map<String, LoadingData> subjects;
    private int informationThreadCount;
    
    public NotifierPopupController() {
        subjects = new HashMap<>();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { }

    @Override
    public synchronized void begin(String id, int total) {
        if (InformationLoader.OBS_ID.equals(id)) {
            if (informationThreadCount == 0)
                subjects.put(id, new LoadingData(grid, id, subjects.size(), total));
            ++informationThreadCount;
        } else
            subjects.put(id, new LoadingData(grid, id, subjects.size(), total));
    }

    @Override
    public void notify(String id, int processed) {
        subjects.get(id).incrementProcessed(processed);
    }

    @Override
    public synchronized void end(String id) {
        if (InformationLoader.OBS_ID.equals(id)) {
            --informationThreadCount;
            if (informationThreadCount == 0)
                subjects.get(id).end();
        } else
            subjects.get(id).end();
    }
}
