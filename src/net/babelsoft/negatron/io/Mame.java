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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.babelsoft.negatron.io.configuration.Configuration;
import net.babelsoft.negatron.model.HddGeometry;

/**
 *
 * @author capan
 */
public class Mame {
    
    private Mame() { }
    
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
    }
}
