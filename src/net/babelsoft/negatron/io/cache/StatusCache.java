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
package net.babelsoft.negatron.io.cache;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.io.configuration.Property;
import net.babelsoft.negatron.io.loader.InitialisedCallable;
import net.babelsoft.negatron.io.loader.MachineStatusLoader;
import net.babelsoft.negatron.io.loader.SoftwareStatusLoader;
import net.babelsoft.negatron.io.loader.ThreadedCacheLoader;
import net.babelsoft.negatron.model.Status;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwareList;

/**
 *
 * @author capan
 */
public class StatusCache extends Cache<StatusCache.Data, StatusVersion> implements ThreadedCacheLoader<InitialisedCallable<Void>> {
    
    protected static class Data implements Serializable  {
        static final long serialVersionUID = 1L;
        
        protected HashMap<String, Status> machineStatuses;
        protected HashMap<String, HashMap<String, Status>> softwareStatuses;
    }
    
    private Data data;
    
    private final Map<String, Machine> machines;
    private final Map<String, SoftwareList> softwareLists;
    
    private int processingItemCount;
    private AtomicInteger processedItemCount;
    
    public StatusCache(
        Map<String, Machine> machines, Map<String, SoftwareList> softwareLists
    ) throws ClassNotFoundException, IOException {
        super("status");
        this.machines = machines;
        this.softwareLists = softwareLists;
    }
    
    private void checkVersion(
        List<String> paths,
        Supplier<Instant> getLastCreationTime, Consumer<Instant> setLastCreationTime,
        Supplier<Instant> getLastModifiedTime, Consumer<Instant> setLastModifiedTime
    ) {
        paths.stream().map(
            path -> Paths.get(path)
        ).filter(
            path -> Files.exists(path)
        ).forEach(path -> {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        Instant time = attrs.creationTime().toInstant();
                        if (getLastCreationTime.get().isBefore(time))
                            setLastCreationTime.accept(time);
                        
                        time = attrs.lastModifiedTime().toInstant();
                        if (getLastModifiedTime.get().isBefore(time))
                            setLastModifiedTime.accept(time);
                        
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(MachineStatusLoader.class.getName()).log(Level.WARNING, "Couldn't retrieve status versioning", ex);
            }
        });
    }
    
    private void updateMachineUI() {
        if (data.machineStatuses == null)
            return;
        
        data.machineStatuses.entrySet().forEach(entry -> {
            Machine machine = machines.get(entry.getKey());
            if (machine != null)
                machine.setStatus(entry.getValue());
        });
    }
    
    private void updateSoftwareUI() {
        if (data.softwareStatuses == null)
            return;
        
        data.softwareStatuses.entrySet().forEach(entry -> {
            SoftwareList softwareList = softwareLists.get(entry.getKey());
            if (softwareList != null) {
                entry.getValue().entrySet().forEach(subEntry -> {
                    Software software = softwareList.getSoftware(subEntry.getKey());
                    if (software != null)
                        software.setStatus(subEntry.getValue());
                });
            }
        });
    }
    
    @Override
    public List<InitialisedCallable<Void>> threadedLoad() throws ClassNotFoundException, IOException {
        // load data
        data = load();
        
        if (data == null)
            data = new Data();
        if (version == null)
            version = new StatusVersion();
        
        // check version
        String mameVersion = new MachineListCache().getVersion();
        if (!mameVersion.equals(version.getMameVersion()))
            version.setMameVersion(mameVersion);
        
        checkVersion(
            Configuration.Manager.getFolderPaths(Property.HASH),
            version::getSoftlistLastCreationTime, version::setSoftlistLastCreationTime,
            version::getSoftlistLastModifiedTime, version::setSoftlistLastModifiedTime
        );
        
        checkVersion(
            Configuration.Manager.getFolderPaths(Property.ROM),
            version::getRomLastCreationTime, version::setRomLastCreationTime,
            version::getRomLastModifiedTime, version::setRomLastModifiedTime
        );
        
        // create required loaders
        List<InitialisedCallable<Void>> loaders = new ArrayList<>();
        
        if (version.isMachineModified() || version.isRomModified()) {
            loaders.add(new MachineStatusLoader(this));
            processedItemCount = new AtomicInteger();
            ++processingItemCount;
        } else
            updateMachineUI();
        
        if (version.isSoftwareListModified() || version.isRomModified()) {
            loaders.add(new SoftwareStatusLoader(this));
            if (processedItemCount == null)
                processedItemCount = new AtomicInteger();
            ++processingItemCount;
        } else
            updateSoftwareUI();
        
        return loaders;
    }

    public void saveSoftware(HashMap<String, HashMap<String, Status>> listStatuses) throws IOException {
        data.softwareStatuses = listStatuses;
        save();
    }

    public void saveMachines(HashMap<String, Status> statuses) throws IOException {
        data.machineStatuses = statuses;
        save();
    }
    
    private void save() throws IOException {
        if (processedItemCount.incrementAndGet() == processingItemCount) try {
            save(data);
            saveVersion();
        } catch (Exception ex) {
            Logger.getLogger(StatusCache.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
}
