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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.babelsoft.negatron.util.Shell;

/**
 *
 * @author capan
 */
public enum Property {
    ARTWORK         ("artpath",          Domain.MAME_FOLDER_ONLY,            "artwork"),
    ARTWORK_PREVIEW ("artpreviewpath",   Domain.EXTRAS_MACHINE_ONLY,         "artpreview|M"), // new since 0.172
    BOSS            ("bosspath",         Domain.EXTRAS_MACHINE_ONLY,         "bosses|M"), // new since 0.172
    BOX_ART         ("boxartpath",       Domain.EXTRAS_SOFTWARE_ONLY,        "boxart|S"),
    CABINET         ("cabinetpath",      Domain.EXTRAS_MACHINE_ONLY,         "cabinets|M"),
    CHEAT           ("cheatpath",        Domain.MAME_FILE_FOLDER,            "cheat"),
    CONTROL_PANEL   ("cpanelpath",       Domain.EXTRAS_MACHINE_ONLY,         "cpanel|M"),
    CONTROLLER      ("ctrlrpath",        Domain.MAME_FOLDER_ONLY,            "ctrlr"),
    COVER           ("coverpath",        Domain.EXTRAS_SOFTWARE_ONLY,        "covers_SL|S"), // new since 0.172
    DEVICE          ("devicepath",       Domain.EXTRAS_MACHINE_ONLY,         "devices|M"),
    END             ("endpath",          Domain.EXTRAS_MACHINE_ONLY,         "ends|M"), // new since 0.172
    FLYER           ("flyerpath",        Domain.EXTRAS_MACHINE_ONLY,         "flyers|M"),
    FOLDER_VIEW     ("folderviewpath",   Domain.EXTRAS_GENERIC,              "folders"), // new since 0.175
    GAME_OVER       ("gameoverpath",     Domain.EXTRAS_MACHINE_ONLY,         "gameover|M"), // new since 0.172
    HASH            ("hashpath",         Domain.MAME_FOLDER_ONLY),
    HOW_TO          ("howtopath",        Domain.EXTRAS_MACHINE_ONLY,         "howto|M"), // new since 0.172
    INFORMATION     ("informationpath",  Domain.EXTRAS_INFORMATION,          "dats/history.dat|UTF-8", "dats/sysinfo.dat|ISO-8859-1", "dats/mameinfo.dat|ISO-8859-1", "dats/messinfo.dat|UTF-8", "dats/gameinit.dat|UTF-8", "dats/story.dat|UTF-8"),
    ICON            ("iconpath",         Domain.EXTRAS_MACHINE_SOFTWARE,     "icons|SM"),
    LOGO            ("logopath",         Domain.EXTRAS_MACHINE_ONLY,         "logo|M"), // new since 0.172
    MANUAL          ("manualpath",       Domain.EXTRAS_MACHINE_SOFTWARE,     "manuals|M", "manuals_SL|S"),
    MARQUEE         ("marqueepath",      Domain.EXTRAS_MACHINE_ONLY,         "marquees|M"),
    MEDIA           ("mediapath",        Domain.EXTRAS_SOFTWARE_ONLY,        "media|S"),
    PCB             ("pcbpath",          Domain.EXTRAS_MACHINE_ONLY,         "pcb|M"),
    ROM             ("rompath",          Domain.MAME_FOLDER_ONLY),
    SAMPLE          ("samplepath",       Domain.MAME_FOLDER_ONLY,            "samples"),
    SCORE           ("scorepath",        Domain.EXTRAS_MACHINE_ONLY,         "scores|M"), // new since 0.172
    SELECT          ("selectpath",       Domain.EXTRAS_MACHINE_ONLY,         "select|M"), // new since 0.172
    SNAPSHOT        ("snapshotpath",     Domain.EXTRAS_MACHINE_SOFTWARE,     "snap|M", "snap_SL|S"),
    SOUNDTRACK      ("soundtrackpath",   Domain.MULTIMEDIA_MACHINE_ONLY,     "soundtrack|M"), // new since 0.189
    TITLE           ("titlepath",        Domain.EXTRAS_MACHINE_SOFTWARE,     "titles|M", "titles_SL|S"),
    VERSUS          ("versuspath",       Domain.EXTRAS_MACHINE_ONLY,         "versus|M"), // new since 0.172
    VIDEO_PREVIEW   ("videopreviewpath", Domain.MULTIMEDIA_MACHINE_SOFTWARE, "videosnaps|M", "videosnaps_SL|S");

    final String name;
    final boolean isMamePath;
    final boolean isExtrasPath;
    final boolean isMultimediaPath;
    final List<String> defaultFolders;
    final Map<String, String> defaultFiles;
    final Domain domain;

    String defaultPrimaryMachineFolder;
    String defaultPrimarySoftwareFolder;
    boolean hasMachinePrimaryPath;
    boolean hasSoftwarePrimaryPath;

    Property(String name, Domain domain, String... defaultFoldersFiles) {
        this.name = name;
        this.domain = domain;

        defaultPrimaryMachineFolder = null;
        defaultPrimarySoftwareFolder = null;
        hasMachinePrimaryPath = false;
        hasSoftwarePrimaryPath = false;

        if (domain == Domain.EXTRAS_INFORMATION) {
            defaultFiles = new LinkedHashMap<>();
            defaultFolders = null;

            Arrays.stream(defaultFoldersFiles).forEachOrdered(
                defaultFile -> {
                    String[] fileCharset = defaultFile.split("\\|");
                    if (Shell.isWindows())
                        fileCharset[0] = fileCharset[0].replace("/", "\\");
                    defaultFiles.put(fileCharset[0], fileCharset[1]);
                }
            );
        } else {
            defaultFiles = null;
            defaultFolders = new ArrayList<>();

            Arrays.stream(defaultFoldersFiles).forEachOrdered(
                defaultFolder -> {
                    String[] folderPrimary = defaultFolder.split("\\|");
                    defaultFolders.add(folderPrimary[0]);

                    if (folderPrimary.length > 1) {
                        if (folderPrimary[1].contains("M")) {
                            defaultPrimaryMachineFolder = folderPrimary[0];
                            hasMachinePrimaryPath = true;
                        }
                        if (folderPrimary[1].contains("S")) {
                            defaultPrimarySoftwareFolder = folderPrimary[0];
                            hasSoftwarePrimaryPath = true;
                        }
                    }
                }
            );
        }

        isMamePath = domain == Domain.MAME_FOLDER_ONLY || domain == Domain.MAME_FILE_FOLDER;
        isMultimediaPath = domain == Domain.MULTIMEDIA_MACHINE_ONLY || domain == Domain.MULTIMEDIA_MACHINE_SOFTWARE;
        isExtrasPath = !isMamePath && !isMultimediaPath;
    }

    public Domain getDomain() {
        return domain;
    }

    public boolean hasMachinePrimaryPath() {
        return hasMachinePrimaryPath;
    }

    public boolean hasSoftwarePrimaryPath() {
        return hasSoftwarePrimaryPath;
    }
    
    public boolean isMamePath() {
        return isMamePath;
    }
    
    public boolean isExtrasPath() {
        return isExtrasPath;
    }
    
    public boolean isMultimediaPath() {
        return isMultimediaPath;
    }
}
