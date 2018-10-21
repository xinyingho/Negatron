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

/**
 *
 * @author capan
 */
public enum SampleRate {
    _1000("1000"),
    _8000("8000"),
    _11025("11025"),
    _16000("16000"),
    _22050("22050"),
    _32000("32000"),
    _44100("44100"),
    _48000("48000"),
    _88200("88200"),
    _96000("96000"),
    _176400("176400"),
    _192000("192000");
    
    final String name;
    
    SampleRate(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
