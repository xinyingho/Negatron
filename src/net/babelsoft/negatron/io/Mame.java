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
package net.babelsoft.negatron.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.HddGeometry;
import net.babelsoft.negatron.util.Shell;
import net.babelsoft.negatron.util.Strings;

/**
 *
 * @author capan
 */
public class Mame {
    
    private Mame() { }
    
    private static void escapeEmptyArguments(List<String> arguments) {
        if (Shell.isWindows())
            return; // this workaround doesn't apply to Windows
        
        final ListIterator<String> li = arguments.listIterator();
        while (li.hasNext())
            if (li.next().equals("\"\""))
                li.set("");
    }
    
    public static InputStream newInputStream(String... arguments) throws IOException {
        return newProcess(arguments).getInputStream();
    }
    
    public static InputStream newInputStream(List<String> arguments) throws IOException {
        return newProcess(arguments).getInputStream();
    }
    
    public static Process newProcess(String... arguments) throws IOException {
        List<String> command = new ArrayList<>(arguments.length + 1);
        command.addAll(Arrays.asList(arguments));
        
        return newProcess(command);
    }
    
    public static Process newProcess(List<String> arguments) throws IOException {
        escapeEmptyArguments(arguments);
        arguments.add(0, Configuration.Manager.getMameExecutable());
        
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(new File(Configuration.Manager.getMamePath()));
        return processBuilder.redirectErrorStream(true).start();
    }
    
    public static void launch(List<String> arguments) throws IOException {
        try {
            launch(arguments, false);
        } catch (InterruptedException ex) {
            // launched asynchronously, so InterruptedExceptions should never happen
        }
    }
    
    public static void launch(List<String> arguments, boolean synchronise) throws IOException, InterruptedException {
        launch(arguments, Configuration.Manager.getMamePath(), synchronise);
    }
    
    public static void launch(List<String> arguments, String workingFolder, boolean synchronise) throws IOException, InterruptedException {
        if (arguments == null)
            arguments = new ArrayList<>();
        escapeEmptyArguments(arguments);
        arguments.add(0, Configuration.Manager.getMameExecutable());
        
        Path workingPath = Paths.get(workingFolder);
        if (!Files.exists(workingPath))
            Files.createDirectories(workingPath);
        
        ProcessBuilder pb = new ProcessBuilder(arguments);
        pb.directory(new File(workingFolder));

        File err = Files.createTempFile(Configuration.getRootFolder(), "tmp-", ".log").toFile();
        err.deleteOnExit();

        pb.redirectError(err);
        Process process = pb.start();
        
        if (synchronise)
            process.waitFor();
    }
    
    public static void createBlankHdd(HddGeometry hddGeometry) throws IOException, InterruptedException {
        List<String> arguments = new ArrayList<>();
        
        arguments.add(Configuration.Manager.getChdmanExecutable());
        arguments.add("createhd");
        arguments.add("--compression"); arguments.add("none");
        arguments.add("--sectorsize");
        arguments.add(Long.toString(hddGeometry.getSectorSize()));
        arguments.add("--chs");
        arguments.add(String.format("%d,%d,%d",
            hddGeometry.getCylinder(), hddGeometry.getHead(), hddGeometry.getSector()
        ));
        arguments.add("--output"); arguments.add(hddGeometry.getPath());
        
        new ProcessBuilder(arguments).start().waitFor();
        
        if (Strings.isValid(hddGeometry.getManufacturer())) {
            arguments.clear();
            
            arguments.add(Configuration.Manager.getChdmanExecutable());
            arguments.add("addmeta");
            arguments.add("--input"); arguments.add(hddGeometry.getPath());
            arguments.add("--tag"); arguments.add("IDNT");
            arguments.add("--valuetext");
            String format = "%-8S %-16S ";
            if (hddGeometry.getVersion() < 10.0)
                format += "%.2f";
            else // if (hddGeometry.getVersion() < 100.0)
                format += "%.1f";
            arguments.add(String.format(Locale.US, format,
                hddGeometry.getManufacturer(), hddGeometry.getModel(), hddGeometry.getVersion()
            ));
            
            new ProcessBuilder(arguments).start().waitFor();
        }
    }
    
    /**
     * If the current version of MAME is "0.279 (negamame0279-1)", this should return 279.
     * For any version of MAME before v0.211, returns 0 as the default version.
     * @return the current MAME version
     */
    public static int version() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream("-version")))) {
            String version = reader.readLine(); // should retrieve something like "0.279 (negamame0279-1)"
            if (Strings.isValid(version) && version.startsWith("0.")) {
                version = version.split(" ")[0].split("\\.")[1]; // just want the actual version number without the meaningless "0." header e.g. "279"
                return Integer.parseInt(version);
            }
            return 0; // for anything before MAME v0.211 i.e. a MAME version missing the -version option, go with a dummy default version
        } catch (IOException ex) {
            System.getLogger(Mame.class.getName()).log(System.Logger.Level.ERROR, "Couldn't retrieve the current MAME version", ex);
            return 0;
        }
    }
}
