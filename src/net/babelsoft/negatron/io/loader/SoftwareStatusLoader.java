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
import java.util.HashMap;
import java.util.Map;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.cache.StatusCache;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwareList;

/**
 *
 * @author capan
 */
public class SoftwareStatusLoader implements InitialisedCallable<Void> {
    
    private static final String OBS_ID = "softwareStatuses";

    private final StatusCache cache;
    private Map<String, SoftwareList> softwareLists;
    private LoadingObserver observer;

    public SoftwareStatusLoader(StatusCache cache) {
        this.cache = cache;
    }

    @Override
    public void initialise(LoadingObserver observer, Map<String, Machine> machines, Map<String, SoftwareList> softwareLists) {
        this.softwareLists = softwareLists;
        this.observer = observer;
    }
    
    private void updateUI(Map<Software, Status> batch) {
        batch.entrySet().forEach(
            entry -> entry.getKey().setStatus(entry.getValue())
        );
        observer.notify(OBS_ID, batch.size());
        batch.clear();
    }
    
    @Override
    public Void call() throws Exception {
        HashMap<String, HashMap<String, Status>> listStatuses = new HashMap<>();
        Map<Software, Status> batch = new HashMap<>();
        final int cutoffSize = 500;
        
        observer.begin(
            OBS_ID,
            softwareLists.values().stream().mapToInt(softList -> softList.size()).sum()
        );
        
        Process process = Mame.newProcess("-verifysoftware");
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
                String[] names = lineElements[1].split(":");
                
                Software software = softwareLists.get(names[0]).getSoftware(names[1]);
                HashMap<String, Status> statuses = listStatuses.get(names[0]);
                if (statuses == null) {
                    statuses = new HashMap<>();
                    listStatuses.put(names[0], statuses);
                }
                
                if (
                    lineElements[lineElements.length - 1].equals("good") ||
                    lineElements[lineElements.length - 2].equals("best") && lineElements[lineElements.length - 1].equals("available")
                ) { 
                    statuses.put(names[1], Status.GOOD);
                    if (software != null)
                        //software.setStatus(Status.GOOD);
                        batch.put(software, Status.GOOD);
                } else {
                    statuses.put(names[1], Status.BAD);
                    if (software != null)
                        //software.setStatus(Status.BAD);
                        batch.put(software, Status.BAD);
                }
                
                if (!Thread.currentThread().isInterrupted()) {
                    if (batch.size() > cutoffSize) {
                        updateUI(batch);
                    }
                } else
                    process.destroy();
            });
        }
        
        if (!Thread.interrupted()) {
            updateUI(batch);
            cache.saveSoftware(listStatuses);
        }
        
        observer.end(OBS_ID);
        
        return null;
    }
}
