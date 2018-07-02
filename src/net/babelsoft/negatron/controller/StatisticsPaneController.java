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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import net.babelsoft.negatron.model.statistics.MachineStatistics;
import net.babelsoft.negatron.model.statistics.SoftwareStatistics;
import net.babelsoft.negatron.view.control.TitledWindowPane;

/**
 * FXML Controller class
 *
 * @author capan
 */
public class StatisticsPaneController implements Initializable {
    
    private static final PseudoClass ODD_CLASS = PseudoClass.getPseudoClass("odd");
    
    @FXML
    private TitledWindowPane root;
    @FXML
    private PieChart pieChart1;
    @FXML
    private PieChart pieChart2;
    @FXML
    private PieChart pieChart3;
    @FXML
    private PieChart pieChart4;
    
    private final List<String> redList;
    private final List<String> yellowList;
    private final List<String> blueList;
    
    private MachineStatistics machineStats;
    private SoftwareStatistics softwareStats;
    
    private Map<String, Integer> machineCategoryCount;
    private Map<String, Integer> softwareCategoryCount;
    
    public StatisticsPaneController() {
        redList = new ArrayList<>();
        redList.add("#c94141");
        redList.add("#b03939");
        redList.add("#973131");
        redList.add("#7e2929");
        redList.add("#652121");
        redList.add("#4b1818");
        redList.add("#321010");
        redList.add("#190808");
        
        yellowList = new ArrayList<>();
        yellowList.add("#ffc300");
        yellowList.add("#dfab00");
        yellowList.add("#bf9200");
        yellowList.add("#9f7a00");
        yellowList.add("#806200");
        yellowList.add("#604900");
        yellowList.add("#403100");
        yellowList.add("#201800");
        
        blueList = new ArrayList<>();
        blueList.add("#41a9c9");
        blueList.add("#3994b0");
        blueList.add("#317f97");
        blueList.add("#296a7e");
        blueList.add("#215565");
        blueList.add("#183f4b");
        blueList.add("#102a32");
        blueList.add("#081519");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        root.setOnceOnAnimationEnded(() -> reset(true, true, true));
    }
    
    private int getTotalCount() {
        return machineStats.getDeviceCount() + machineStats.getTotalCount() + softwareStats.getTotalCount();
    }
    
    private String softwareInterfaceToCategory(String interfaceFormat) {
        // categorisation up to date with MAME v0.199
        if (interfaceFormat.startsWith("floppy_") || interfaceFormat.contains("_flop") || interfaceFormat.equals("_disk"))
            return "Floppy";
        if (interfaceFormat.endsWith("_cart") || interfaceFormat.endsWith("_card") || interfaceFormat.endsWith("pack") ||
            interfaceFormat.startsWith("snspell") || interfaceFormat.startsWith("snread") || interfaceFormat.equals("tntell") ||
            interfaceFormat.equals("fidel_scc") || interfaceFormat.equals("k28m2") || interfaceFormat.equals("ereader") ||
            interfaceFormat.equals("sm_memc")
        )
            return "Cartridge";
        if (interfaceFormat.contains("cdrom") || interfaceFormat.contains("_cd_") || interfaceFormat.equals("vsmile_vdisk"))
            return "CD-ROM";
        if (interfaceFormat.endsWith("cass"))
            return "Cassette";
        return "Others";
    }

    public void setStatistics(MachineStatistics machineStats, SoftwareStatistics softwareStats) {
        this.machineStats = machineStats;
        
        machineCategoryCount = new TreeMap<>();
        machineCategoryCount.put("Clone - Gambling / Pinball", machineStats.getGamblingPinballCloneCount());
        machineCategoryCount.put("Clone - Arcade Game", machineStats.getArcadeGameCloneCount());
        machineCategoryCount.put("Clone - Calculator / Computer", machineStats.getCalculatorComputerCloneCount());
        machineCategoryCount.put("Clone - Console", machineStats.getConsoleCloneCount());
        machineCategoryCount.put("Parent - Gambling / Pinball", machineStats.getGamblingPinballParentCount());
        machineCategoryCount.put("Parent - Arcade Game", machineStats.getArcadeGameParentCount());
        machineCategoryCount.put("Parent - Calculator / Computer", machineStats.getCalculatorComputerParentCount());
        machineCategoryCount.put("Parent - Console", machineStats.getConsoleParentCount());

        this.softwareStats = softwareStats;
        
        softwareCategoryCount = new TreeMap<>();
        softwareStats.getCloneCountByType().forEach((key, value) -> {
            String cat = "Clone - " + softwareInterfaceToCategory(key);
            softwareCategoryCount.put(cat, softwareCategoryCount.getOrDefault(cat, 0) + value.intValue());
        });
        softwareStats.getParentCountByType().forEach((key, value) -> {
            String cat = "Parent - " + softwareInterfaceToCategory(key);
            softwareCategoryCount.put(cat, softwareCategoryCount.getOrDefault(cat, 0) + value.intValue());
        });
    }
    
    private double pct(double value, double total) {
        return value / total * 100.0;
    }
    
    private void reset(boolean device, boolean machine, boolean software) {
        // data
        
        int _total = 0;
        if (device)
            _total += machineStats.getDeviceCount();
        if (machine)
            _total += machineStats.getTotalCount();
        if (software)
            _total += softwareStats.getTotalCount();
        final double total = _total; // workaround to have a final variable for lambda expression
        
        ObservableList<PieChart.Data> pieChartData1 = FXCollections.observableArrayList();
        if (device)
            pieChartData1.add(new PieChart.Data(
                String.format("Device (%,d - %.1f%%)", machineStats.getDeviceCount(), pct(machineStats.getDeviceCount(), total)),
                machineStats.getDeviceCount()
            ));
        if (machine)    
            machineCategoryCount.forEach((key, value) -> {
                pieChartData1.add(new PieChart.Data(String.format("%s (%,d - %.1f%%)", key, value, pct(value, total)), value));
            });
        if (software)
            softwareCategoryCount.forEach((key, value) -> {
                pieChartData1.add(new PieChart.Data(String.format("%s (%,d - %.1f%%)", key, value, pct(value, total)), value));
            });
        pieChart1.setData(pieChartData1);
        
        ObservableList<PieChart.Data> pieChartData2 = FXCollections.observableArrayList();
        if (device)
            pieChartData2.add(new PieChart.Data("Device",machineStats.getDeviceCount()));
        if (machine)
            pieChartData2.addAll(
                new PieChart.Data(
                    String.format("Machine - Clone (%,d - %.1f%%)", machineStats.getCloneCount(), pct(machineStats.getCloneCount(), total)),
                    machineStats.getCloneCount()
                ),
                new PieChart.Data(
                    String.format("Machine - Parent (%,d - %.1f%%)", machineStats.getParentCount(), pct(machineStats.getParentCount(), total)),
                    machineStats.getParentCount()
                )
            );
        if (software)
            pieChartData2.addAll(
                new PieChart.Data(
                    String.format("Software - Clone (%,d - %.1f%%)", softwareStats.getCloneCount(), pct(softwareStats.getCloneCount(), total)),
                    softwareStats.getCloneCount()
                ),
                new PieChart.Data(
                    String.format("Software - Parent (%,d - %.1f%%)", softwareStats.getParentCount(), pct(softwareStats.getParentCount(), total)),
                    softwareStats.getParentCount()
                )
            );
        pieChart2.setData(pieChartData2);
        
        ObservableList<PieChart.Data> pieChartData3 = FXCollections.observableArrayList();
        if (device)
            pieChartData3.add(new PieChart.Data("Device",machineStats.getDeviceCount()));
        if (machine)
            pieChartData3.add(new PieChart.Data(
                String.format("Machine (%,d - %.1f%%)", machineStats.getTotalCount(), pct(machineStats.getTotalCount(), total)),
                machineStats.getTotalCount()
            ));
        if (software)
            pieChartData3.add(new PieChart.Data(
                String.format("Software (%,d - %.1f%%)", softwareStats.getTotalCount(), pct(softwareStats.getTotalCount(), total)),
                softwareStats.getTotalCount()
            ));
        pieChart3.setData(pieChartData3);
        
        ObservableList<PieChart.Data> pieChartData4 = FXCollections.observableArrayList();
        if (device && machine && software)
            pieChartData4.add(new PieChart.Data(
                String.format("Total (%,d - %.1f%%)", getTotalCount(), pct(getTotalCount(), total)),
                getTotalCount()
            ));
        pieChart4.setData(pieChartData4);
        
        // styling
        
        if (pieChartData3.size() == 1) {
            pieChart2.pseudoClassStateChanged(ODD_CLASS, true);
            pieChart3.pseudoClassStateChanged(ODD_CLASS, true);
        } else {
            pieChart2.pseudoClassStateChanged(ODD_CLASS, false);
            pieChart3.pseudoClassStateChanged(ODD_CLASS, false);
        }
        
        // colours
        
        int deviceLimit = 0;
        if (device) {
            pieChartData1.get(0).getNode().setStyle("-fx-pie-color: " + redList.get(2));
            deviceLimit = 1;
        }
        
        int i;
        int greenLimit = deviceLimit + (machine ? machineCategoryCount.size() : 0);
        for (i = deviceLimit; i < greenLimit; ++i)
            pieChartData1.get(i).getNode().setStyle(
                "-fx-pie-color: " + yellowList.get((i - deviceLimit) % 8)
            );
        
        int blueLimit = greenLimit + (software ? softwareCategoryCount.size() : 0);
        for (i = greenLimit; i < blueLimit; ++i)
            pieChartData1.get(i).getNode().setStyle(
                "-fx-pie-color: " + blueList.get((i - greenLimit) % 8)
            );
        
        i = 0;
        if (device)
            pieChartData2.get(i++).getNode().setStyle("-fx-pie-color: " + redList.get(1));
        if (machine) {
            pieChartData2.get(i++).getNode().setStyle("-fx-pie-color: " + yellowList.get(0));
            pieChartData2.get(i++).getNode().setStyle("-fx-pie-color: " + yellowList.get(3));
        }
        if (software) {
            pieChartData2.get(i++).getNode().setStyle("-fx-pie-color: " + blueList.get(0));
            pieChartData2.get(i).getNode().setStyle("-fx-pie-color: " + blueList.get(3));
        }
        
        i = 0;
        if (device)
            pieChartData3.get(i++).getNode().setStyle("-fx-pie-color: " + redList.get(0));
        if (machine)
            pieChartData3.get(i++).getNode().setStyle("-fx-pie-color: " + yellowList.get(0));
        if (software)
            pieChartData3.get(i++).getNode().setStyle("-fx-pie-color: " + blueList.get(0));
        
        if (device && machine && software)
            pieChartData4.get(0).getNode().setStyle("-fx-pie-color: silver");
    }
    
    @FXML
    private void handleAllMode(ActionEvent event) {
        reset(true, true, true);
    }
    
    @FXML
    private void handleMachineOnlyMode(ActionEvent event) {
        reset(false, true, false);
    }
    
    @FXML
    private void handleSoftwareOnlyMode(ActionEvent event) {
        reset(false, false, true);
    }
}
