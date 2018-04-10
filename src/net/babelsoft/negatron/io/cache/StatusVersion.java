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

import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author capan
 */
class StatusVersion implements Serializable {
    static final long serialVersionUID = 1L;
    
    private String mameVersion;
    private Instant softlistLastCreationTime;
    private Instant softlistLastModifiedTime;
    private Instant romLastCreationTime;
    private Instant romLastModifiedTime;
    
    private transient boolean machineModified;
    private transient boolean softwareListModified;
    private transient boolean romModified;

    public StatusVersion() {
        softlistLastCreationTime = Instant.MIN;
        softlistLastModifiedTime = Instant.MIN;
        romLastCreationTime = Instant.MIN;
        romLastModifiedTime = Instant.MIN;
    }

    /**
     * @return the mameVersion
     */
    public String getMameVersion() {
        return mameVersion;
    }

    /**
     * @param mameVersion the mameVersion to set
     */
    public void setMameVersion(String mameVersion) {
        this.mameVersion = mameVersion;
        machineModified = true;
    }

    /**
     * @return the softlistLastCreationTime
     */
    public Instant getSoftlistLastCreationTime() {
        return softlistLastCreationTime;
    }

    /**
     * @param softlistLastCreationTime the softlistLastCreationTime to set
     */
    public void setSoftlistLastCreationTime(Instant softlistLastCreationTime) {
        this.softlistLastCreationTime = softlistLastCreationTime;
        softwareListModified = true;
    }

    /**
     * @return the softlistLastModifiedTime
     */
    public Instant getSoftlistLastModifiedTime() {
        return softlistLastModifiedTime;
    }

    /**
     * @param softlistLastModifiedTime the softlistLastModifiedTime to set
     */
    public void setSoftlistLastModifiedTime(Instant softlistLastModifiedTime) {
        this.softlistLastModifiedTime = softlistLastModifiedTime;
        softwareListModified = true;
    }

    /**
     * @return the romLastCreationTime
     */
    public Instant getRomLastCreationTime() {
        return romLastCreationTime;
    }

    /**
     * @param romLastCreationTime the romLastCreationTime to set
     */
    public void setRomLastCreationTime(Instant romLastCreationTime) {
        this.romLastCreationTime = romLastCreationTime;
        romModified = true;
    }

    /**
     * @return the romLastModifiedTime
     */
    public Instant getRomLastModifiedTime() {
        return romLastModifiedTime;
    }

    /**
     * @param romLastModifiedTime the romLastModifiedTime to set
     */
    public void setRomLastModifiedTime(Instant romLastModifiedTime) {
        this.romLastModifiedTime = romLastModifiedTime;
        romModified = true;
    }

    /**
     * @return the machineModified
     */
    public boolean isMachineModified() {
        return machineModified;
    }

    /**
     * @return the softwareListModified
     */
    public boolean isSoftwareListModified() {
        return softwareListModified;
    }

    /**
     * @return the romModified
     */
    public boolean isRomModified() {
        return romModified;
    }
}
