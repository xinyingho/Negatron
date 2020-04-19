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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import net.babelsoft.negatron.controller.MainController;
import net.babelsoft.negatron.io.configuration.FavouriteTree;
import net.babelsoft.negatron.io.loader.FavouriteLoader;
import net.babelsoft.negatron.io.loader.InitialisedCallable;
import net.babelsoft.negatron.io.loader.MachineListLoader;
import net.babelsoft.negatron.io.loader.MachineListLoader.MachineListData;
import net.babelsoft.negatron.model.item.Machine;
import net.babelsoft.negatron.model.statistics.MachineStatistics;
import net.babelsoft.negatron.model.statistics.SoftwareStatistics;
import net.babelsoft.negatron.util.function.TetraConsumer;

/**
 *
 * @author capan
 */
public class CacheManager extends Task<Void> {
    
    private static class CacheThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CacheThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "cache-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(
                group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0
            );
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
    private final MainController controller;
    private final ExecutorService service;
    private final ExecutorCompletionService<Void> execService;
    private final TetraConsumer<Map<String, Machine>, List<Machine>, MachineStatistics, SoftwareStatistics> onMachineListLoaded;
    
    public CacheManager(
        MainController controller,
        TetraConsumer<Map<String, Machine>, List<Machine>, MachineStatistics, SoftwareStatistics> onMachineListLoaded
    ) {
        this.controller = controller;
        this.onMachineListLoaded = onMachineListLoaded;
        
        int nbProcessors = Runtime.getRuntime().availableProcessors();
        if (nbProcessors < 3)
            nbProcessors = 3; // at least 1 monitoring thread + 2 working threads
        
        CacheThreadFactory threadFactory = new CacheThreadFactory();
        service = Executors.newFixedThreadPool(nbProcessors, runnable -> {
            return threadFactory.newThread(runnable);
        });
        execService = new ExecutorCompletionService<>(service);
    }
    
    public void execute() {
        service.execute(this);
    }
    
    private void notify(boolean isRunning) {
        Platform.runLater(
            () -> controller.handleCacheNotification(isRunning)
        );
    }

    @Override
    protected Void call() throws Exception {
        List<Future<Void>> tasks = new ArrayList<>();
        
        try {
            notify(true);
            
            ///// Software lists
            
            // launch tasks
            SoftwareListCache softwareListCache = new SoftwareListCache();
            softwareListCache.threadedLoad().forEach(
                loader -> tasks.add(execService.submit(loader))
            );
            
            // wait for them to finish
            for (int i = 0, max = tasks.size(); i < max; ++i)
                tasks.remove(execService.take());
            
            controller.setSoftwareLists(softwareListCache.get());
            
            ///// Machine lists
            
            // launch tasks
            MachineListLoader machineListLoader = new MachineListLoader(
                softwareListCache.get(), controller.ProgressProperty()
            );
            Future<MachineListData> machineListFuture = service.submit(machineListLoader);
            
            // wait for it to finish
            MachineListData machines = machineListFuture.get();
            onMachineListLoaded.accept(
                machines.getMap(), machines.getList(), machines.getStatistics(), softwareListCache.getStatistics()
            );
            
            controller.setSucceeded(true);
            
            ///// Favourites
            
            if (!isCancelled()) {
                FavouriteTree favourites = new FavouriteLoader(controller, machines.getMap(), softwareListCache.get()).call();
                if (favourites != null)
                    controller.setFavouriteTree(favourites);
            }
            
            ///// Extras
            
            if (!isCancelled()) {
                
                // launch tasks
                Consumer<List<InitialisedCallable<Void>>> load = loaders -> {
                    loaders.forEach(loader -> {
                        loader.initialise(controller, machines.getMap(), softwareListCache.get());
                        tasks.add( execService.submit(loader) );
                    });
                };

                IconCacheSingleton iconCache = IconCacheSingleton.Instance;
                load.accept(iconCache.threadedLoad());

                InformationCache informationCache = new InformationCache();
                load.accept(informationCache.threadedLoad());

                StatusCache statusCache = new StatusCache(machines.getMap(), softwareListCache.get());
                load.accept(statusCache.threadedLoad());

                // wait for them to finish
                int nbTasks = tasks.size();
                do {
                    Future<Void> future = execService.poll(5, TimeUnit.SECONDS);
                    if (future != null) {
                        tasks.remove(future);
                        --nbTasks;
                    }
                    if (isCancelled()) {
                        tasks.forEach(task -> task.cancel(true));
                        nbTasks = 0;
                    }
                } while (nbTasks > 0);
            }
            
            return null;
        } catch (InterruptedException ex) {
            tasks.forEach(task -> task.cancel(true));
            return null;
        } catch (Exception ex) {
            Logger.getLogger(CacheManager.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> controller.alert(
                AlertType.ERROR, "Unexpected error while building cache: " + ex.getLocalizedMessage()
            ));
            throw ex;
        } finally {
            notify(false);
            service.shutdown();
        }
    }
}
