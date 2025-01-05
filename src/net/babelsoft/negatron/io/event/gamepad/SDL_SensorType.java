/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2024 BabelSoft S.A.S.U.
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
package net.babelsoft.negatron.io.event.gamepad;

/**
 *
 * @author Xiny
 */
public class SDL_SensorType {
    public static final int SDL_SENSOR_INVALID = -1;        /**< Returned for an invalid sensor */
    public static final int SDL_SENSOR_UNKNOWN = 0;         /**< Unknown sensor type */
    public static final int SDL_SENSOR_ACCEL = 1;           /**< Accelerometer */
    public static final int SDL_SENSOR_GYRO = 2;            /**< Gyroscope */
    public static final int SDL_SENSOR_ACCEL_L = 3;         /**< Accelerometer for left Joy-Con controller and Wii nunchuk */
    public static final int SDL_SENSOR_GYRO_L = 4;          /**< Gyroscope for left Joy-Con controller */
    public static final int SDL_SENSOR_ACCEL_R = 5;         /**< Accelerometer for right Joy-Con controller */
    public static final int SDL_SENSOR_GYRO_R = 6;          /**< Gyroscope for right Joy-Con controller */
}
