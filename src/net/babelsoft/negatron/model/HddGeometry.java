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
package net.babelsoft.negatron.model;

import java.util.function.Function;

/**
 *
 * @author capan
 */
public class HddGeometry {
        
    private String manufacturer;
    private String model;
    private double version;
    private String path;
    private final long cylinder;
    private final long head;
    private final long sector;
    private final long sectorSize;

    private HddGeometry(String cylinder, String head, String sector, String sectorSize) {
        Function<String, Long> parse = text -> {
            return !text.isEmpty() ? Long.parseUnsignedLong(text) : 0L;
        };
        this.cylinder = parse.apply(cylinder);
        this.head = parse.apply(head);
        this.sector = parse.apply(sector);
        this.sectorSize = parse.apply(sectorSize);
    }
    
    public HddGeometry(
        String manufacturer, String model,
        String cylinder, String head, String sector, String sectorSize
    ) {
        this(cylinder, head, sector, sectorSize);
        this.manufacturer = manufacturer;
        this.model = model;
    }
    
    public HddGeometry(
        String path,
        String cylinder, String head, String sector, String sectorSize
    ) {
        this(cylinder, head, sector, sectorSize);
        this.path = path;
    }
    
    public HddGeometry(
        String path,
        String manufacturer, String model, double version,
        String cylinder, String head, String sector, String sectorSize
    ) {
        this(manufacturer, model, cylinder, head, sector, sectorSize);
        this.version = version;
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", getManufacturer(), getModel(), getTotalSize());
    }

    public String getTotalSize() {
        return SizeUnit.factorise(getCylinder() * getHead() * getSector() * getSectorSize());
    }

    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the version
     */
    public double getVersion() {
        return version;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the cylinder
     */
    public long getCylinder() {
        return cylinder;
    }

    /**
     * @return the head
     */
    public long getHead() {
        return head;
    }

    /**
     * @return the sector
     */
    public long getSector() {
        return sector;
    }

    /**
     * @return the sectorSize
     */
    public long getSectorSize() {
        return sectorSize;
    }
}
