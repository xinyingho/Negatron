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
package net.babelsoft.negatron.io.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.babelsoft.negatron.io.Mame;
import net.babelsoft.negatron.io.cache.UIConfigurationCache;
import net.babelsoft.negatron.io.cache.UIConfigurationData;
import net.babelsoft.negatron.util.Shell;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public enum Configuration {
    Manager;
    
    private static final String TRUE = "1";
    private static final String FALSE = "0";
    
    public static Path getRootFolder() {
        Path root = Paths.get(".");
        if (Files.isWritable(root))
            return root;
        else {
            if (Shell.isWindows())
                return Paths.get(System.getenv("AppData"), "Negatron");
            else if (Shell.isMacOs())
                return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Negatron");
            else // Linux
                return Paths.get(System.getProperty("user.home"), ".Negatron");
        }
    }
    
    private final String MAME = "mame";
    private final String MESS = "mess";
    private final String CHDMAN = "chdman";
    
    private final String MAME_ENTRY = MAME;
    private final String MESS_ENTRY = MESS;
    private final String MAME_INI_ENTRY = "mameini";
    private final String CHDMAN_ENTRY = CHDMAN;
    private final String EXTRAS_ENTRY = "extras";
    private final String MULTIMEDIA_ENTRY = "multimedia";
    private final String VLC_ENTRY = "vlc";
    private final String SKIN_ENTRY = "skin";
    private final String LANGUAGE_ENTRY = "language";
    private final String CHEAT_MENU_ENTRY = "cheatmenu";
    private final String VSYNC_ENTRY = "vsync";
    private final String FONT_FAMILY_ENTRY = "fontFamily";
    private final String FONT_SIZE_ENTRY = "fontSize";
    private final String SOUND_ENTRY = "sound";
    private final String VIDEO_ENTRY = "video";
    private final String VIEW3D_ENTRY = "view3d";
    
    private final String MAME_CHEAT_MENU_ENTRY = "cheat";
    
    private final Path NEGATRON_INI = Paths.get(getRootFolder().toString(), "Negatron.ini");
    private final String MAME_INI = "mame.ini";
    private final String MESS_INI = "mess.ini";
    
    private String mamePath;
    private String mameExec;
    private String mameIni;
    private String chdmanExec;
    private String extrasPath;
    private String multimediaPath;
    private String vlcPath;
    private String skin;
    private String language;
    private Boolean cheatMenuEnabled;
    private VsyncMethod vsync;
    private String fontFamily;
    private double fontSize;
    private Boolean soundEnabled;
    private Boolean videoEnabled;
    private Boolean view3dEnabled;
    private final Map<Property, List<String>> folders;
    private final Map<Property, List<PathCharset>> files;
    private final Map<Property, String> primaryMachineFolders;
    private final Map<Property, String> primarySoftwareFolders;
    private final Map<String, String> globalConfiguration;
    private UIConfigurationCache uiConfigurationCache;
    
    private boolean disableWrite;
    private boolean masterConfigPointSet;
    private boolean isMess;
    private boolean isAsyncExecutionMode;
    private boolean isXmlMediaOptionAvailable;
    
    Configuration() {
        folders = new LinkedHashMap<>();
        files = new LinkedHashMap<>();
        primaryMachineFolders = new LinkedHashMap<>();
        primarySoftwareFolders = new LinkedHashMap<>();
        globalConfiguration = new HashMap<>();
        
        try {
            uiConfigurationCache = new UIConfigurationCache();
        } catch (Exception ex) { }
    }
    
    public void initialise() throws IOException, InterruptedException {
        // negatron.ini check
        try (BufferedReader reader = Files.newBufferedReader(NEGATRON_INI)) {
            reader.lines().filter(
                line -> !line.trim().startsWith("#")
            ).map(
                line -> splitLine(line)
            ).forEach(content -> {
                // Quick hack to easily handle the renaming of the "screenshot" entry to the more commonly used "snapshot"
                if ("screenshotpath".equals(content[0]))
                    content[0] = Property.SNAPSHOT.name;
                
                switch (content[0]) {
                    case MAME_ENTRY:
                        setMamePath(tidyStringPath(content[1]));
                        break;
                    case MESS_ENTRY:
                        setMamePath(tidyStringPath(content[1]));
                        isMess = true;
                        break;
                    case MAME_INI_ENTRY:
                        String iniPath = tidyStringPath(content[1]);
                        if (!iniPath.isEmpty())
                            mameIni = iniPath;
                        break;
                    case CHDMAN_ENTRY:
                        chdmanExec = tidyStringPath(content[1]);
                        break;
                    case EXTRAS_ENTRY:
                        extrasPath = tidyStringPath(content[1]);
                        break;
                    case MULTIMEDIA_ENTRY:
                        multimediaPath = tidyStringPath(content[1]);
                        break;
                    case VLC_ENTRY:
                        vlcPath = tidyStringPath(content[1]);
                        break;
                    case SKIN_ENTRY:
                        skin = tidyStringPath(content[1]);
                        break;
                    case LANGUAGE_ENTRY:
                        language = tidyStringPath(content[1]);
                        break;
                    case CHEAT_MENU_ENTRY:
                        cheatMenuEnabled = digitToBoolean(content[1]);
                        break;
                    case VSYNC_ENTRY:
                        vsync = Arrays.stream(VsyncMethod.values()).filter(
                            vsyncMethod -> vsyncMethod.equals(content[1])
                        ).findAny().orElse(null);
                        break;
                    case FONT_FAMILY_ENTRY:
                        fontFamily = content[1].trim();
                        break;
                    case FONT_SIZE_ENTRY:
                        fontSize = Double.parseDouble(content[1]);
                        break;
                    case SOUND_ENTRY:
                        soundEnabled = digitToBoolean(content[1]);
                        break;
                    case VIDEO_ENTRY:
                        videoEnabled = digitToBoolean(content[1]);
                        break;
                    case VIEW3D_ENTRY:
                        view3dEnabled = digitToBoolean(content[1]);
                        break;
                    default:
                        Arrays.stream(Property.values()).filter(
                            property -> property.name.equals(content[0])
                        ).findAny().ifPresent(
                            property -> {
                                if (property.domain == Domain.EXTRAS_INFORMATION)
                                    files.put(property, pathStringToFileArray(content[1]));
                                else
                                    folders.put(property, pathStringToFolderArray(property, content[1]));
                            }
                        );
                        break;
                }
            });
        }
        
        boolean mustWriteDefaultConfiguration = false;
        if (Strings.isValid(extrasPath)) {
            Optional<Property> result = Arrays.stream(Property.values()).filter(
                property -> !property.isMamePath && folders.get(property) != null
            ).findAny();
            if (!result.isPresent())
                mustWriteDefaultConfiguration = true;
        }
        
        // mame.ini / mess.ini check
        if (Strings.isValid(mamePath)) {
            if (mameIni == null) {
                final String INI = isMess ? MESS_INI : MAME_INI;
                if (Shell.isLinux()) {
                    // MAME on Linux can be packaged (system-wide) or compiled from source code.
                    List<String> mameIniPaths = Shell.find(INI, mamePath);
                    mameIniPaths.addAll(Shell.find(INI, Shell.expandPath("$HOME/." + (isMess ? MESS : MAME))));
                    mameIniPaths.addAll(Shell.find(INI, "/etc/"));
                    mameIniPaths.addAll(Shell.find(INI, "/usr/"));
                    Optional<String> mameIniPath = mameIniPaths.stream().filter(path -> {
                        File file = Paths.get(path).toFile();
                        return file.canRead() && file.canWrite();
                    }).findAny();

                    if (mameIniPath.isPresent())
                        // found an editable mame.ini, so use it
                        mameIni = mameIniPath.get();
                    else {
                        // couldn't find any editable mame.ini, so search for any read-only system-wide mame.ini
                        mameIniPath = mameIniPaths.stream().filter(
                            path -> Paths.get(path).toFile().canRead()
                        ).findAny();

                        if (mameIniPath.isPresent()) try (
                            BufferedReader reader = Files.newBufferedReader(Paths.get(mameIniPath.get()))
                        ) {
                            // read the system-wide mame.ini to know where to create a corresponding user-wide mame.ini
                            reader.lines().filter(
                                line -> line.startsWith("inipath")
                            ).findAny().ifPresent(
                                iniPathLine -> {
                                    List<String> iniPaths = pathStringToArray(
                                        splitLine(iniPathLine)[1]
                                    );
                                    // the very first found path should be "$HOME/.mame/ini" or the sort
                                    String mameIniFolder = iniPaths.get(0);
                                    if (mameIniFolder.startsWith("$"))
                                        mameIniFolder = Shell.expandPath(mameIniFolder);
                                    if (mameIniFolder != null)
                                        mameIni = Paths.get(mameIniFolder, INI).toString();
                                }
                            );
                        }
                    }

                    if (mameIni == null)
                        // if still didn't find anything, just go for the default SDLMAME location
                        mameIni = Paths.get(Shell.expandPath("$HOME/." + (isMess ? MESS : MAME)), INI).toString();
                } else // Windows or Mac OS X
                    mameIni = Paths.get(mamePath, INI).toString();
            }
            
            // mame.ini update: check if required. If yes, then perform sync between negatron.ini and mame.ini
            boolean mameIniCreatedJustNow = false;
            Path iniPath = Paths.get(mameIni);
            if (Files.notExists(iniPath))
                mameIniCreatedJustNow = true;
            // Create ini file or update it so that its content get updated to the latest format
            Mame.launch(stringToArray("-cc"), iniPath.getParent().toString(), true);
            
            boolean shouldUpdateMameConfiguration = mustWriteDefaultConfiguration || extrasPath != null && mameIniCreatedJustNow;
            
            Path mameRoot = Paths.get(mamePath);
            boolean mustUpdateMameConfiguration;
            try (BufferedReader reader = Files.newBufferedReader(iniPath)) {
                mustUpdateMameConfiguration = reader.lines().filter(
                    line -> !line.trim().startsWith("#") && Strings.isValid(line.trim())
                ).map(
                    line -> splitLine(line)
                ).map(content -> {
                    boolean[] processedLine = { false }; // workaround: transform the boolean as a table so that it remains editable from lambda expressions...
                    
                    // for each MAME paths editable in the global configuration pane, read their values from mame.ini
                    Arrays.stream(Property.values()).filter(
                        property -> property.isMamePath && content[0].equals(property.name)
                    ).forEach(property -> {
                        String defaultFolders = null;
                        if (shouldUpdateMameConfiguration && property.defaultFolders.size() > 0) {
                            defaultFolders = property.defaultFolders.stream().map(
                                defaultFolder -> Paths.get(extrasPath, defaultFolder).normalize().toString()
                            ).filter(
                                defaultFolder -> {
                                    String relativePath = null;
                                    try {
                                        relativePath = mameRoot.relativize(
                                            Paths.get(defaultFolder)
                                        ).toString();
                                    } catch (IllegalArgumentException ex) {
                                        // the path cannot be relativised,
                                        // thus swallow the exception to simply use the current absolute path
                                    }

                                    return
                                        !content[1].contains(defaultFolder) &&
                                        (relativePath == null || !content[1].contains(relativePath))
                                    ;
                                }
                            ).collect(Collectors.joining(";"));
                        }

                        folders.put(property, contentToPathArray(
                            content[1] + (defaultFolders != null ? ";" + defaultFolders : "")
                        ));
                        
                        processedLine[0] = true;
                    });
                    
                    boolean mustUpdate = false;
                    
                    // if mame.ini and negatron.ini values doesn't agree, force sync to the value in negatron.ini
                    if (content[0].equals(MAME_CHEAT_MENU_ENTRY)) {
                        boolean mameCheatMenuEnabled = digitToBoolean(content[1]);
                        if (cheatMenuEnabled == null) {
                            cheatMenuEnabled = mameCheatMenuEnabled;
                            mustUpdate = true;
                        } else if (cheatMenuEnabled != mameCheatMenuEnabled)
                            mustUpdate = true;
                        processedLine[0] = true;
                    } else if (content[0].equals(VsyncMethod.DOUBLE_BUFFERING.name)) {
                        boolean doubleBufferingEnabled = digitToBoolean(content[1]);
                        if (vsync == null && doubleBufferingEnabled) {
                            vsync = VsyncMethod.DOUBLE_BUFFERING;
                            mustUpdate = true;
                        } else if (vsync == VsyncMethod.DOUBLE_BUFFERING && !doubleBufferingEnabled)
                            mustUpdate = true;
                        processedLine[0] = true;
                    } else if (content[0].equals(VsyncMethod.TRIPLE_BUFFERING.name)) {
                        boolean tripleBufferingEnabled = digitToBoolean(content[1]);
                        if (vsync == null && tripleBufferingEnabled) {
                            vsync = VsyncMethod.TRIPLE_BUFFERING;
                            mustUpdate = true;
                        } else if (vsync == VsyncMethod.TRIPLE_BUFFERING && !tripleBufferingEnabled)
                            mustUpdate = true;
                        // processedLine[0] = true; // Triple Buffer is a Windows native only MAME option
                        // So to have a way to detect if triple buffering should be enabled and
                        // by assuming that SDL MAME doesn't add this option in -cc generated ini files,
                        // we let the process register the triple buffer line into the global conf dic
                    }
                    
                    // memorise not yet processed mame configuration lines into the Global Configuration dictionary
                    if (!processedLine[0])
                        globalConfiguration.put(content[0], content.length > 1 ? content[1].trim() : "");
                    
                    return mustUpdate;
                }).reduce(false, (a, b) -> a || b);
            }
            
            if (shouldUpdateMameConfiguration || mustUpdateMameConfiguration)
                writeMameInitialisationFile();
        }
        
        if (chdmanExec == null) {
            if (Shell.isWindows()) {
                Path path = Paths.get(mamePath, CHDMAN + ".exe");
                if (path.toFile().exists())
                    chdmanExec = path.toString();
            } else { // Linux or Mac OS X
                chdmanExec = Shell.which(CHDMAN); // packaged MAME on Linux
                if (chdmanExec == null) {
                    List<String> chdmanPaths = Shell.find(CHDMAN, mamePath);
                    if (chdmanPaths.size() > 0)
                        chdmanExec = chdmanPaths.get(0);
                }
            }
            if (chdmanExec == null)
                chdmanExec = "";
        }
        
        // set default values if required
        if (vlcPath == null)
            vlcPath = "";
        if (skin == null)
            skin = "";
        if (language == null)
            language = "";
        if (vsync == null)
            vsync = VsyncMethod.NONE;
        if (soundEnabled == null)
            soundEnabled = true;
        if (videoEnabled == null)
            videoEnabled = true;
        if (view3dEnabled == null)
            view3dEnabled = false;
        
        // negatron.ini update: if nothing is defined yet, use the default configuration
        if (mustWriteDefaultConfiguration) {
            for (Property property : Property.values()) {
                if (property.isMamePath)
                    continue;

                if (property.domain == Domain.EXTRAS_INFORMATION) {
                    property.defaultFiles.keySet().stream().forEach(
                        file -> {
                            Path path = Paths.get(extrasPath, file);
                            if (files.get(property) == null)
                                files.put(property, new ArrayList<>());

                            String pathString = path.toString();
                            String charset = property.defaultFiles.get(file);

                            files.get(property).add(new PathCharset(pathString, charset));
                        }
                    );
                } else {
                    Consumer<String> initProperty = (rootPath) -> {
                        property.defaultFolders.stream().map(
                            folder -> Paths.get(rootPath, folder).toString()
                        ).forEach(
                            path -> {
                                if (folders.get(property) == null)
                                    folders.put(property, new ArrayList<>());
                                folders.get(property).add(path);

                                if (property.hasSoftwarePrimaryPath && path.endsWith(property.defaultPrimarySoftwareFolder))
                                    primarySoftwareFolders.put(property, path);
                                if (property.hasMachinePrimaryPath && path.endsWith(property.defaultPrimaryMachineFolder))
                                    primaryMachineFolders.put(property, path);
                            }
                        );
                    };
                    
                    if (property.isExtrasPath)
                        initProperty.accept(extrasPath);
                    else if (Strings.isValid(multimediaPath))
                        initProperty.accept(multimediaPath);
                }
            }
            
            writeNegatronInitialisationFile();
        }
    }
    
    private boolean digitToBoolean(String content) {
        return TRUE.equals(content.trim());
    }
    
    private String tidyStringPath(String path) {
        path = path.replace("\"", "").trim();
        
        if(Shell.isPosix())
            return Shell.expandPath(path);
        else
            return path;
    }
    
    private String[] splitLine(String line) {
        return line.split("\\s", 2);
    }
    
    private List<String> stringToArray(String string) {
        List<String> array = new ArrayList<>();
        array.add(string);
        return array;
    }
    
    private List<String> pathStringToArray(String paths) {
        paths = paths.trim();
        
        if (paths.isEmpty())
            return new ArrayList<>();
        else
            return Arrays.asList(paths.trim().replace("\"", "").replaceAll(";{2,}", ";").split(";"));
    }
    
    private List<String> pathStringToFolderArray(Property property, String paths) {
        List<String> array = pathStringToArray(paths);
        
        return array.stream().map(
            element -> {
                String[] folderPrimary = element.split("\\|");
                if (folderPrimary.length > 1) {
                    if (folderPrimary[1].contains("M"))
                        primaryMachineFolders.put(property, folderPrimary[0]);
                    if (folderPrimary[1].contains("S"))
                        primarySoftwareFolders.put(property, folderPrimary[0]);
                }
                return folderPrimary[0];
            }
        ).collect(Collectors.toList());
    }

    private List<PathCharset> pathStringToFileArray(String paths) {
        List<String> array = pathStringToArray(paths);
        List<PathCharset> pathCharsets = new ArrayList<>();
        
        array.stream().forEachOrdered(element -> {
                String[] pathCharset = element.split("\\|");
                pathCharsets.add(new PathCharset(pathCharset[0], pathCharset[1]));
            }
        );
        
        return pathCharsets;
    }
    
    private List<String> contentToPathArray(String content) {
        List<String> array = pathStringToArray(content);
        Path mameRoot = Paths.get(mamePath);

        return array.stream().map(
            path -> {
                if (Shell.isPosix())
                    path = Shell.expandPath(path);
                return mameRoot.resolve(path).normalize().toString();
            }
        ).collect(Collectors.toList());
    }

    private void writeConfigurationSectionHeader(BufferedWriter writer, String header) throws IOException {
        writer.write("#");
        writer.newLine();
        writer.write("# ");
        writer.write(header);
        writer.newLine();
        writer.write("#");
        writer.newLine();
    }
    
    private void writeConfigurationLineHeader(BufferedWriter writer, String header) throws IOException {
        writer.write(header);
        
        int blankSize = 26 - header.length();
        for (int i = 0; i < blankSize; ++i)
            writer.write(" ");
    }
    
    private void writeConfigurationLine(BufferedWriter writer, String header, String content) throws IOException {
        writeConfigurationLine(writer, header, Collections.singletonList(content));
    }
    
    private void writeConfigurationLine(BufferedWriter writer, String header, double content) throws IOException {
        writeConfigurationLine(writer, header, Collections.singletonList(String.valueOf(content)));
    }
    
    private void writeConfigurationLine(BufferedWriter writer, String header, List<String> content) throws IOException {
        writeConfigurationLineHeader(writer, header);

        Iterator<String> it = content.iterator();
        if (it.hasNext()) {
            writer.write(it.next());
            while (it.hasNext()) {
                String part = it.next();
                writer.write(";");
                writer.write(part);
            }
        }
        writer.newLine();
    }
    
    private void writeConfigurationLine(BufferedWriter writer, String header, boolean content) throws IOException {
        writeConfigurationLineHeader(writer, header);
        if (content)
            writer.write(TRUE);
        else
            writer.write(FALSE);
        writer.newLine();
    }
    
    private void writeInitialisationFile(Property property) throws IOException, InterruptedException {
        if (!disableWrite) {
            if (property.isMamePath)
                writeMameInitialisationFile();
            else
                writeNegatronInitialisationFile();
        }
    }
    
    public void writeMameInitialisationFile() throws IOException, InterruptedException {
        Path iniPath = Paths.get(mameIni);
        
        if (Strings.isEmpty(mamePath) || mameIni == null || !Files.isWritable(iniPath))
            return;
        
        Path mameRoot = Paths.get(mamePath);
        Path tempPath = Files.createTempFile("negatron", "mameini");

        try (
            BufferedReader reader = Files.newBufferedReader(iniPath);
            BufferedWriter writer = Files.newBufferedWriter(tempPath);
        ) {
            for (String line : reader.lines().toArray(String[]::new)) {
                boolean lineUpdated = false;
                
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] splitLine = splitLine(line);
                    
                    for (Property property : Property.values()) {
                        if (property.isMamePath && splitLine[0].equals(property.name)) {
                            List<String> content = new ArrayList<>();

                            folders.get(property).stream().forEachOrdered(
                                folder -> {
                                    if (Shell.isLinux()) {
                                        // get absolute paths having user paths masked with environment variables
                                        folder = Shell.maskPath(folder);
                                    } else try { // Windows or Mac OS X
                                        Path res = mameRoot.relativize(Paths.get(folder));
                                        folder = res.toString();
                                    } catch (IllegalArgumentException ex) {
                                        // the path cannot be relativised,
                                        // thus swallow the exception to simply use the current absolute path
                                    }
                                    content.add(folder);
                                }
                            );

                            writeConfigurationLine(writer, property.name, content);
                            lineUpdated = true;
                            break;
                        }
                    }
                    
                    if (splitLine[0].equals(MAME_CHEAT_MENU_ENTRY)) {
                        writeConfigurationLine(writer, MAME_CHEAT_MENU_ENTRY, cheatMenuEnabled);
                        lineUpdated = true;
                    } else if (splitLine[0].equals(VsyncMethod.DOUBLE_BUFFERING.name)) {
                        writeConfigurationLine(writer, VsyncMethod.DOUBLE_BUFFERING.name, vsync == VsyncMethod.DOUBLE_BUFFERING);
                        lineUpdated = true;
                    } else if (splitLine[0].equals(VsyncMethod.TRIPLE_BUFFERING.name)) {
                        writeConfigurationLine(writer, VsyncMethod.TRIPLE_BUFFERING.name, vsync == VsyncMethod.TRIPLE_BUFFERING);
                        lineUpdated = true;
                    }
                    
                    if (
                        !lineUpdated && splitLine.length > 1 &&
                        globalConfiguration.containsKey(splitLine[0]) && !globalConfiguration.get(splitLine[0]).equals(splitLine[1])
                    ) {
                        writeConfigurationLine(writer, splitLine[0], globalConfiguration.get(splitLine[0]));
                        lineUpdated = true;
                    }
                }
                
                if (!lineUpdated) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        
        Files.move(tempPath, iniPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public void writeNegatronInitialisationFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(NEGATRON_INI)) {
            writeConfigurationSectionHeader(writer, "MAME PATHS");
            if (isMess)
                writeConfigurationLine(writer, MESS_ENTRY, mameExec);
            else
                writeConfigurationLine(writer, MAME_ENTRY, mameExec);
            writeConfigurationLine(writer, MAME_INI_ENTRY, mameIni);
            writeConfigurationLine(writer, CHDMAN_ENTRY, chdmanExec);
            writeConfigurationLine(writer, EXTRAS_ENTRY, extrasPath);
            writeConfigurationLine(writer, MULTIMEDIA_ENTRY, multimediaPath);
            writer.newLine();
            writeConfigurationSectionHeader(writer, "EXTRAS / MULTIMEDIA PATHS");
            
            for (Property property : Property.values()) {
                if (property.isMamePath)
                    continue;

                if (property.domain == Domain.EXTRAS_INFORMATION) {
                    List<String> paths = new ArrayList<>();

                    if (files.get(property) != null) {
                        files.get(property).stream().forEach(
                            pathCharset -> paths.add(pathCharset.getPath().toString() + "|" + pathCharset.getCharSet())
                        );

                        writeConfigurationLine(writer, property.name, paths);
                    }
                } else {
                    List<String> paths = new ArrayList<>();

                    if (folders.get(property) != null) {
                        folders.get(property).stream().forEach(
                            path -> {
                                String primary = "";
                                if (property.hasSoftwarePrimaryPath && path.equals(primarySoftwareFolders.get(property)))
                                    primary += "S";
                                if (property.hasMachinePrimaryPath && path.equals(primaryMachineFolders.get(property)))
                                    primary += "M";
                                if (!primary.isEmpty())
                                    path += "|" + primary;

                                paths.add(path);
                            }
                        );

                        writeConfigurationLine(writer, property.name, paths);
                    }
                }
            }
            
            writer.newLine();
            writeConfigurationSectionHeader(writer, "MISC");
            writeConfigurationLine(writer, VLC_ENTRY, vlcPath);
            writeConfigurationLine(writer, SKIN_ENTRY, skin);
            writeConfigurationLine(writer, LANGUAGE_ENTRY, language);
            writeConfigurationLine(writer, CHEAT_MENU_ENTRY, cheatMenuEnabled);
            writeConfigurationLine(writer, VSYNC_ENTRY, vsync.name);
            writeConfigurationLine(writer, FONT_FAMILY_ENTRY, fontFamily);
            writeConfigurationLine(writer, FONT_SIZE_ENTRY, fontSize);
            writeConfigurationLine(writer, SOUND_ENTRY, soundEnabled);
            writeConfigurationLine(writer, VIDEO_ENTRY, videoEnabled);
            writeConfigurationLine(writer, VIEW3D_ENTRY, view3dEnabled);
        }
    }
    
    public void determineExecutionMode(String mameVersion) {
        // The default value is sync execution mode (since MAME v0.186)
        // --> machine configurations must be entirely deferred from the content of the initial call to -listxml
        // But as of MAME v0.198, media options are still not available for third-party front-ends...
        isAsyncExecutionMode = false;
        isXmlMediaOptionAvailable = false;
        
        String[] versionTab = mameVersion.split(" ");
        if (versionTab.length > 1 && versionTab[1].startsWith("v0.")) {
            try {
                int minorVersion = Integer.parseInt(versionTab[1].substring(3));
                if (minorVersion < 186) {
                    // For MAME v0.185 and older, async mode --> machine configuration updates must be done
                    // through MAME callbacks each time to ensure getting all available options, including media options
                    isAsyncExecutionMode = true;
                } else if (mameVersion.matches("MAME.*\\(negamame.*\\)")) {
                    // For MAME v0.186+, sync mode --> machine configuration updates must be done
                    // only by using information available in the machine cache, generated by the initial call to -listxml:
                    // 1/ this works well to determine default machine configurations
                    // 2/ when a user changes the value of a slot, Negatron can determine whether some slots must disappear or be created
                    // 3/ but there's not enough information to determine the same for devices
                    // -listmedia cannot mitigate the issue in 3/ as it's missing some important information.
                    // But the MAME derivative, NegaMAME, mitigates this by adding the new command line -listmediaxml,
                    // which do provide all the required information for devices to third-party front-ends.
                    isXmlMediaOptionAvailable = true;
                }
            } catch (NumberFormatException ex) {
                // swallow exception
            }
        }
    }
    
    public boolean isAsyncExecutionMode() {
        return isAsyncExecutionMode;
    }
    
    public boolean isSyncExecutionMode() {
        return !isAsyncExecutionMode;
    }
    
    public boolean isXmlMediaOptionAvailable() {
        return isXmlMediaOptionAvailable;
    }
    
    public boolean isMess() {
        return isMess;
    }
    
    public String getMamePath() {
        return mamePath;
    }
    
    public String getMameExecutable() {
        return mameExec;
    }
    
    public String getMameIni() {
        return mameIni;
    }
    
    public String getChdmanExecutable() {
        return chdmanExec;
    }
    
    public String getExtrasPath() {
        return extrasPath;
    }
    
    public String getMultimediaPath() {
        return multimediaPath;
    }
    
    public String getVlcPath() {
        return vlcPath;
    }
    
    public String getSkin() {
        return skin;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public boolean isCheatMenuEnabled() {
        return cheatMenuEnabled;
    }

    public VsyncMethod getVsyncMethod() {
        return vsync;
    }
    
    public String getFontFamily() {
        return fontFamily;
    }
    
    public double getFontSize() {
        return fontSize;
    }
    
    /**
     * @return the soundEnabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public boolean isVideoEnabled() {
        return videoEnabled;
    }
    
    public boolean isView3dEnabled() {
        return view3dEnabled;
    }
    
    public List<String> getFolderPaths(Property property) {
        if (property.domain != Domain.EXTRAS_INFORMATION && folders.get(property) != null)
            return folders.get(property);
        else
            return Collections.emptyList();
    }
    
    public List<PathCharset> getFilePaths(Property property) {
        if (property.domain == Domain.EXTRAS_INFORMATION && files.get(property) != null)
            return files.get(property);
        else
            return Collections.emptyList();
    }
    
    public String getPrimaryMachineFolder(Property property) {
        return primaryMachineFolders.get(property);
    }
    
    public String getPrimarySoftwareFolder(Property property) {
        return primarySoftwareFolders.get(property);
    }

    public List<PathPrimary> getDefaultFolderPaths(Property property, String rootFolder) {
        List<PathPrimary> pathPrimaries = new ArrayList<>();
        
        if (property.domain != Domain.EXTRAS_INFORMATION) {
            property.defaultFolders.stream().map(
                folder -> rootFolder + File.separator + folder
            ).forEach(
                path -> {
                    boolean softwarePrimary = false, machinePrimary = false;
                    
                    if (property.hasSoftwarePrimaryPath && path.endsWith(property.defaultPrimarySoftwareFolder))
                        softwarePrimary = true;
                    if (property.hasMachinePrimaryPath && path.endsWith(property.defaultPrimaryMachineFolder))
                        machinePrimary = true;
                    
                    pathPrimaries.add(new PathPrimary(path, machinePrimary, softwarePrimary));
                }
            );
        }
        
        return pathPrimaries;
    }

    public List<PathCharset> getDefaultFilePaths(Property property, String rootFolder) {
        List<PathCharset> pathCharsets = new ArrayList<>();
        
        if (property.domain == Domain.EXTRAS_INFORMATION) {
            property.defaultFiles.keySet().stream().forEach(
                file -> {
                    String path = rootFolder + File.separator + file;
                    String charset = property.defaultFiles.get(file);

                    pathCharsets.add(new PathCharset(path, charset));
                }
            );
        }
        
        return pathCharsets;
    }
    
    public String getGlobalConfiguration(String key) {
        return globalConfiguration.get(key);
    }
    
    public boolean isGlobalConfiguration(String key) {
        return digitToBoolean(globalConfiguration.get(key));
    }

    public Map<String, TreeTableColumnConfiguration> getTreeTableColumnsConfiguration(String id) {
        if (uiConfigurationCache != null) {
            Map<String, TreeTableColumnConfiguration> res = uiConfigurationCache.loadTreeTableColumnsConfiguration(id);
            if (res != null)
                return res;
        }
        
        return Collections.emptyMap();
    }
    
    public boolean getTreeTableFlattenConfiguration(String id) {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadTreeTableFlattenConfiguration(id);
        return false;
    }

    public Map<String, TableColumnConfiguration> getTableColumnsConfiguration(String id) {
        if (uiConfigurationCache != null) {
            Map<String, TableColumnConfiguration> res = uiConfigurationCache.loadTableColumnsConfiguration(id);
            if (res != null)
                return res;
        }
        
        return Collections.emptyMap();
    }
    
    public double getMainDividerPosition() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadMainDividerPosition();
        return -1;
    }

    public int getMachineInformationTabIndex() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadMachineInformationTabIndex();
        return 0;
    }

    public int getSoftwareInformationTabIndex() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadSoftwareInformationTabIndex();
        return 0;
    }

    public int getGlobalConfigurationTabIndex() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadGlobalConfigurationTabIndex();
        return 0;
    }
    
    public UIConfigurationData getFilterConfiguration() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadFilterConfiguration();
        return new UIConfigurationData();
    }

    public UIConfigurationData getFilterConfiguration(String key) {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadFilterConfiguration(key);
        return new UIConfigurationData();
    }
    
    public boolean isLoopEnabled() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadLoopEnabled();
        return false;
    }

    public int getWindowWidth() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadWindowWidth();
        return UIConfigurationCache.DEFAULT_WIDTH;
    }

    public int getWindowHeight() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadWindowHeight();
        return UIConfigurationCache.DEFAULT_HEIGHT;
    }
    
    public boolean isWindowMaximised() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadWindowMaximised();
        return false;
    }
    
    public boolean isWindowFullscreen() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadWindowFullscreen();
        return false;
    }
    
    public String getSelectedMachineFolderView() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadSelectedMachineFolderView();
        return null;
    }
    
    public Map<String, Void> getMachineFoldersRemovedFromView() {
        if (uiConfigurationCache != null)
            return uiConfigurationCache.loadMachineFoldersRemovedFromView();
        return new HashMap<>();
    }

    public void setMasterConfigPoint() {
        beginMasterConfigTransaction();
        masterConfigPointSet = true;
    }

    public void beginMasterConfigTransaction() {
        disableWrite = true;
    }
    
    public void rollbackMasterConfigTransaction() {
        disableWrite = false;
    }
    
    /**
     * 
     * @param path
     * @param domain
     * @return if a current transaction ended
     * @throws IOException
     * @throws InterruptedException 
     */
    public boolean updateMasterConfigPath(Domain domain, String path) throws IOException, InterruptedException {
        boolean changed = false;
        
        if (domain == Domain.EXTRAS_MACHINE_SOFTWARE && !path.equals(extrasPath) ||
            domain == Domain.MULTIMEDIA_MACHINE_SOFTWARE && !path.equals(multimediaPath)
        ) {
            if (domain == Domain.EXTRAS_MACHINE_SOFTWARE)
                extrasPath = path;
            else // domain == Domain.MULTIMEDIA_MACHINE_SOFTWARE
                multimediaPath = path;
            changed = true;
        }
        
        if (masterConfigPointSet || !disableWrite) {
            if (changed) {
                if (domain == Domain.EXTRAS_MACHINE_SOFTWARE)
                    writeMameInitialisationFile();
                writeNegatronInitialisationFile();
            }
            
            masterConfigPointSet = false;
            disableWrite = false;
            return true;
        }
        
        return false;
    }

    public void commitMasterConfigTransaction(Domain domain) throws IOException, InterruptedException {
        disableWrite = false;
        if (domain == Domain.EXTRAS_MACHINE_SOFTWARE)
            writeMameInitialisationFile();
        writeNegatronInitialisationFile();
    }
    
    private boolean setMamePath(String path) {
        boolean changed = false;
        
        if (Strings.isValid(path)) {
            Path _path = Paths.get(path);

            String _mameExec = _path.toString();
            if (!_mameExec.equals(mameExec)) {
                mameExec = _path.toString();
                mamePath = _path.getParent().toString();
                changed = true;
            }
        }
        
        return changed;
    }
    
    public void updateMamePath(String path) throws IOException {
        if (setMamePath(path))
            writeNegatronInitialisationFile();
    }
    
    public void updateMameIni(String path) throws IOException {
        if (!path.equals(mameIni)) {
            mameIni = path;
            writeNegatronInitialisationFile();
        }
    }
    
    public void updateChdmanPath(String path) throws IOException {
        if (!path.equals(chdmanExec)) {
            chdmanExec = path;
            writeNegatronInitialisationFile();
        }
    }
    
    public void updateVlcPath(String path) throws IOException {
        if (!path.equals(vlcPath)) {
            vlcPath = path;
            writeNegatronInitialisationFile();
        }
    }
    
    public void updateSkin(String name) throws IOException {
        if (!skin.equals(name)) {
            skin = name;
            writeNegatronInitialisationFile();
        }
    }
    
    public void updateLanguage(String language) throws IOException {
        if (!this.language.equals(language)) {
            this.language = language;
            writeNegatronInitialisationFile();
        }
    }
    
    public void updateCheatMenuEnabled(boolean cheatMenuEnabled) throws IOException, InterruptedException {
        this.cheatMenuEnabled = cheatMenuEnabled;
        writeNegatronInitialisationFile();
        writeMameInitialisationFile();
    }
    
    public void updateVsyncMethod(VsyncMethod vsync) throws IOException, InterruptedException {
        this.vsync = vsync;
        writeNegatronInitialisationFile();
        writeMameInitialisationFile();
    }
    
    public void updateFont(String family, double size) throws IOException, InterruptedException {
        fontFamily = family;
        fontSize = size;
        writeNegatronInitialisationFile();
    }

    /**
     * @param soundEnabled the soundEnabled to set
     * @throws java.io.IOException
     */
    public void updateSoundEnabled(boolean soundEnabled) throws IOException {
        this.soundEnabled = soundEnabled;
        writeNegatronInitialisationFile();
    }
    
    public void updateVideoEnabled(boolean videoEnabled) throws IOException {
        this.videoEnabled = videoEnabled;
        writeNegatronInitialisationFile();
    }

    public void updateView3dEnabled(boolean view3dEnabled) throws IOException {
        this.view3dEnabled = view3dEnabled;
        writeNegatronInitialisationFile();
    }
    
    public void updateFolderPath(Property property, int index, String path) throws IOException, InterruptedException {
        if (index != -1 && property.domain != Domain.EXTRAS_INFORMATION) {
            List<String> f = folders.get(property);
            
            if (f == null) {
                f = new ArrayList<>();
                folders.put(property, f);
            }
            
            while (index >= f.size())
                f.add("");
            if (!path.equals(f.get(index)))
                f.set(index, path);
            writeInitialisationFile(property);
        }
    }

    public void updateFilePath(Property property, int index, String path, String charSet) throws IOException, InterruptedException {
        if (index != -1 && property.domain == Domain.EXTRAS_INFORMATION) {
            List<PathCharset> f = files.get(property);
            
            if (f == null) {
                f = new ArrayList<>();
                files.put(property, f);
            }
            
            if (f.size() <= index) {
                f.add(new PathCharset(path, charSet));
                writeInitialisationFile(property);
            } else if (
                !path.equals(f.get(index).getPathString()) ||
                !charSet.equals(f.get(index).getCharSet())
            ) {
                f.get(index).setPath(path);
                f.get(index).setCharSet(charSet);
                writeInitialisationFile(property);
            }
        }
    }

    public void updateMachinePrimaryPath(Property property, String text) throws IOException, InterruptedException {
        if (!text.equals(primaryMachineFolders.get(property))) {
            primaryMachineFolders.put(property, text);
            writeInitialisationFile(property);
        }
    }

    public void updateSoftwarePrimaryPath(Property property, String text) throws IOException, InterruptedException {
        if (!text.equals(primarySoftwareFolders.get(property))) {
            primarySoftwareFolders.put(property, text);
            writeInitialisationFile(property);
        }
    }
    
    public void updateGlobalConfigurationSetting(String key, String value) throws IOException, InterruptedException {
        globalConfiguration.put(key, value);
        writeMameInitialisationFile();
    }
    
    public void updateGlobalConfigurationSetting(String key, boolean value) throws IOException, InterruptedException {
        globalConfiguration.put(key, value ? TRUE : FALSE);
        writeMameInitialisationFile();
    }

    public void updateTreeTableColumnsConfiguration(String id, Map<String, TreeTableColumnConfiguration> conf) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveTreeTableColumnsConfiguration(id, conf);
    }
    
    public void updateTreeTableFlattenConfiguration(String id, boolean flatten) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveTreeTableFlattenConfiguration(id, flatten);
    }
    
    public void updateTableColumnsConfiguration(String id, Map<String, TableColumnConfiguration> conf) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveTableColumnsConfiguration(id, conf);
    }
    
    public void updateMainDividerPosition(double position) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveMainDividerPosition(position);
    }

    public void updateMachineInformationTabIndex(int index) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveMachineInformationTabIndex(index);
    }

    public void updateSoftwareInformationTabIndex(int index) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveSoftwareInformationTabIndex(index);
    }
    
    public void updateGlobalConfigurationTabIndex(int index) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveGlobalConfigurationTabIndex(index);
    }
    
    public void updateFilterConfiguration(UIConfigurationData data) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveFilterConfiguration(data);
    }

    public void updateFilterConfiguration(String key, UIConfigurationData data) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveFilterConfiguration(key, data);
    }
    
    public void updateLoopEnabled(boolean value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveLoopEnabled(value);
    }
    
    public void updateWindowWidth(int value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveWindowWidth(value);
    }
    
    public void updateWindowHeight(int value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveWindowHeight(value);
    }
    
    public void updateWindowMaximised(boolean value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveWindowMaximised(value);
    }
    
    public void updateWindowFullscreen(boolean value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveWindowFullscreen(value);
    }
    
    public void updateSelectedMachineFolderView(String value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveSelectedMachineFolderView(value);
    }
    
    public void updateMachineFolderRemovedFromView(String value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveMachineFolderRemovedFromView(value);
    }
    
    public void updateMachineFolderAddedIntoView(String value) throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.saveMachineFolderAddedIntoView(value);
    }
    
    public void beginUIConfigurationTransaction() {
        if (uiConfigurationCache != null)
            uiConfigurationCache.beginTransaction();
    }
    
    public void endUIConfigurationTransaction() throws IOException {
        if (uiConfigurationCache != null)
            uiConfigurationCache.endTransaction();
    }
    
    public void addPath(Property property, String path, String charSet) {
        if (property.domain == Domain.EXTRAS_INFORMATION)
            files.get(property).add(new PathCharset(path, charSet));
        else
            folders.get(property).add(path);
    }
    
    public void removePath(Property property, int index) throws IOException, InterruptedException {
        if (property.domain == Domain.EXTRAS_INFORMATION) {
            if (index < files.get(property).size())
                files.get(property).remove(index);
        } else
            if (index < folders.get(property).size())
                folders.get(property).remove(index);
        writeInitialisationFile(property);
    }
}
