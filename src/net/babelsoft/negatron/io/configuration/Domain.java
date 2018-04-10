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
public enum Domain {
    NONE,
    MAME_FOLDER_ONLY,
    MAME_FILE_FOLDER,
    EXTRAS_INFORMATION,
    EXTRAS_GENERIC,
    EXTRAS_MACHINE_ONLY,
    EXTRAS_SOFTWARE_ONLY,
    EXTRAS_MACHINE_SOFTWARE,
    MULTIMEDIA_MACHINE_ONLY,
    MULTIMEDIA_MACHINE_SOFTWARE
}