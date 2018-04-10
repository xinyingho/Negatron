/*
The MIT License (MIT)

Copyright (c) 2015, Hindol Adhya

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package net.babelsoft.negatron.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple class which can monitor files and notify interested parties
 * (i.e. listeners) of file changes.
 *
 * This class is kept lean by only keeping methods that are actually being
 * called.
 */
public class SimpleDirectoryWatchService implements DirectoryWatchService, Runnable {

    private final WatchService mWatchService;
    private final AtomicBoolean mIsRunning;
    private final ConcurrentMap<WatchKey, Path> mWatchKeyToDirPathMap;
    private final ConcurrentMap<Path, Set<OnFileChangeListener>> mDirPathToListenersMap;
    private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> mListenerToFilePatternsMap;

    /**
     * A simple no argument constructor for creating a <code>SimpleDirectoryWatchService</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    private SimpleDirectoryWatchService() throws IOException {
        mWatchService = FileSystems.getDefault().newWatchService();
        mIsRunning = new AtomicBoolean(false);
        mWatchKeyToDirPathMap = newConcurrentMap();
        mDirPathToListenersMap = newConcurrentMap();
        mListenerToFilePatternsMap = newConcurrentMap();
    }

    public static SimpleDirectoryWatchService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    private static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(newConcurrentMap());
    }

    public static PathMatcher matcherForGlobExpression(String globPattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
    }

    public static boolean matches(Path input, PathMatcher pattern) {
        return pattern.matches(input);
    }

    public static boolean matchesAny(Path input, Set<PathMatcher> patterns) {
        if (patterns.stream().anyMatch(pattern -> matches(input, pattern)))
            return true;

        return false;
    }

    private Path getDirPath(WatchKey key) {
        return mWatchKeyToDirPathMap.get(key);
    }

    private Set<OnFileChangeListener> getListeners(Path dir) {
        return mDirPathToListenersMap.get(dir);
    }

    private Set<PathMatcher> getPatterns(OnFileChangeListener listener) {
        return mListenerToFilePatternsMap.get(listener);
    }

    private Set<OnFileChangeListener> matchedListeners(Path dir, Path file) {
        return getListeners(dir).stream().filter(
            listener -> matchesAny(file, getPatterns(listener))
        ).collect(Collectors.toSet());
    }

    private void notifyListeners(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind eventKind = event.kind();

            // Overflow occurs when the watch event queue is overflown
            // with events.
            if (eventKind.equals(OVERFLOW))
                // TODO: Notify all listeners.
                return;

            WatchEvent<Path> pathEvent = cast(event);
            Path file = pathEvent.context();
            
            if (eventKind.equals(ENTRY_CREATE)) {
                matchedListeners(getDirPath(key), file).forEach(
                    listener -> listener.onFileCreate(file)
                );
            } else if (eventKind.equals(ENTRY_MODIFY)) {
                matchedListeners(getDirPath(key), file).forEach(
                    listener -> listener.onFileModify(file)
                );
            } else if (eventKind.equals(ENTRY_DELETE)) {
                matchedListeners(getDirPath(key), file).forEach(
                    listener -> listener.onFileDelete(file)
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(OnFileChangeListener listener, Path dirPath, String... globPatterns) throws IOException {
        String dir = dirPath.toString();
        
        if (!Files.isDirectory(dirPath))
            throw new IllegalArgumentException(dir + " is not a directory.");

        if (!mDirPathToListenersMap.containsKey(dirPath)) {
            // May throw
            WatchKey key = dirPath.register(
                mWatchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
            );

            mWatchKeyToDirPathMap.put(key, dirPath);
            mDirPathToListenersMap.put(dirPath, newConcurrentSet());
        }

        getListeners(dirPath).add(listener);

        Set<PathMatcher> patterns = newConcurrentSet();

        for (String globPattern : globPatterns) {
            patterns.add(matcherForGlobExpression(globPattern));
        }

        if (patterns.isEmpty()) {
            patterns.add(matcherForGlobExpression("*")); // Match everything if no filter is found
        }

        mListenerToFilePatternsMap.put(listener, patterns);
        
        Logger.getLogger(SimpleDirectoryWatchService.class.getName()).log(
            Level.INFO, "Watching files matching {0} under {1} for changes.", new Object[] {Arrays.toString(globPatterns), dir}
        );
    }

    /**
     * Start this <code>SimpleDirectoryWatchService</code> instance by spawning a new thread.
     *
     * @see #stop()
     */
    public void start() {
        if (mIsRunning.compareAndSet(false, true)) {
            Thread runnerThread = new Thread(this, DirectoryWatchService.class.getSimpleName());
            runnerThread.setDaemon(true);
            runnerThread.start();
        }
    }

    /**
     * Stop this <code>SimpleDirectoryWatchService</code> thread.
     * The killing happens lazily, giving the running thread an opportunity
     * to finish the work at hand.
     *
     * @see #start()
     */
    public void stop() {
        // Kill thread lazily
        mIsRunning.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Logger.getLogger(SimpleDirectoryWatchService.class.getName()).log(Level.INFO, "Starting file watcher service.");

        while (mIsRunning.get()) {
            WatchKey key;
            try {
                key = mWatchService.take();
            } catch (InterruptedException e) {
                Logger.getLogger(SimpleDirectoryWatchService.class.getName()).log(
                    Level.INFO, "{0} service interrupted.", DirectoryWatchService.class.getSimpleName()
                );
                break;
            }

            if (null == getDirPath(key)) {
                Logger.getLogger(SimpleDirectoryWatchService.class.getName()).log(Level.SEVERE, "Watch key not recognized.");
                continue;
            }

            notifyListeners(key);

            // Reset key to allow further events for this key to be processed.
            boolean valid = key.reset();
            if (!valid) {
                mWatchKeyToDirPathMap.remove(key);
                if (mWatchKeyToDirPathMap.isEmpty())
                    break;
            }
        }

        mIsRunning.set(false);
        Logger.getLogger(SimpleDirectoryWatchService.class.getName()).log(Level.INFO, "Stopping file watcher service.");
    }

    private static class SingletonHolder {
        /** The singleton instance. */
        private static final SimpleDirectoryWatchService INSTANCE;
        static {
            try {
                INSTANCE = new SimpleDirectoryWatchService();
            } catch (IOException|UnsupportedOperationException e) {
                throw new ExceptionInInitializerError(
                    "Unable to start " + DirectoryWatchService.class.getSimpleName() + " instance."
                );
            }
        }
    }
}
