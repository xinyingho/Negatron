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
package net.babelsoft.negatron.io.loader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.cache.StatusCache;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.SoftwareList;

/**
 *
 * @author capan
 */
public class MachineStatusLoader implements InitialisedCallable<Void> {
    
    private static final String OBS_ID = "machineStatuses";

    private final StatusCache cache;
    private Map<String, Machine> machines;
    private LoadingObserver observer;

    public MachineStatusLoader(StatusCache cache) {
        this.cache = cache;
    }

    @Override
    public void initialise(LoadingObserver observer, Map<String, Machine> machines, Map<String, SoftwareList> softwareLists) {
        this.machines = machines;
        this.observer = observer;
    }
    
    private void updateUI(List<String> batch, HashMap<String, Status> statuses) {
        batch.forEach(name -> {
            Machine machine = machines.get(name);
            if (machine != null)
                machine.setStatus(statuses.get(name));
        });
        observer.notify(OBS_ID, batch.size());
        batch.clear();
    }
    
    @Override
    public Void call() throws Exception {
        HashMap<String, Status> statuses = new HashMap<>();
        List<String> batch = new ArrayList<>();
        final int cutoffSize = 500;
        
        observer.begin(OBS_ID, machines.size());
        
        Process process = Mame.newProcess("-verifyroms");
        try (
            InputStream input = process.getInputStream();
            InputStreamReader stream = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(stream);
        ) {
            reader.lines().map(
                line -> line.split("\\s")
            ).filter(
                lineElements -> lineElements.length > 2 && lineElements[0].equals("romset")
            ).forEach(lineElements -> {
                String name = lineElements[1];
                //Machine machine = machines.get(name);
                if (
                    lineElements[lineElements.length - 1].equals("good") ||
                    lineElements[lineElements.length - 2].equals("best") && lineElements[lineElements.length - 1].equals("available")
                ) {
                    statuses.put(name, Status.GOOD);
                    // Java 8u66 isn't reliable enough to bear heavy continuous GUI updates through grid bindings without graphical update freeze
                    // so disable below realtime update and do it by batches
                    /*if (machine != null)
                        machine.setStatus(Status.GOOD);*/
                } else {
                    statuses.put(name, Status.BAD);
                    /*if (machine != null)
                        machine.setStatus(Status.BAD);*/
                }
                
                if (!Thread.currentThread().isInterrupted()) {
                    batch.add(name);
                    if (batch.size() > cutoffSize)
                        updateUI(batch, statuses);
                } else
                    process.destroy();
            });
        }
        
        if (!Thread.interrupted()) {    
            updateUI(batch, statuses);
            cache.saveMachines(statuses);
        }
        
        observer.end(OBS_ID);
        
        return null;
    }
}
