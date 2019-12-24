/*******************************************************************************
 * Copyright (c) 2014 EM-SOFTWARE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christoph Keimel <c.keimel@emsw.de> - initial API and implementation
 *******************************************************************************/
package net.babelsoft.negatron.util; //e(fx)clipse 2.0

import java.lang.reflect.Field;

/**
 * Utility Functions using the Java Reflection API
 */
public class ReflectionUtil {

    /**
     * Searches for a field in the given class and all of its super classes.
     * @param clazz Class to start the search for the field
     * @param name Name of the field
     * @return The field that was found
     * @throws NoSuchFieldException
     */
    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> searchClass = clazz;
        Field field = null;
        while (field == null && searchClass != null) {
            try {
                field = searchClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                searchClass = searchClass.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException(clazz.getSimpleName() + "." + name); //$NON-NLS-1$
        }
        return field;
    }

    /**
     * Utility method to set a field to a value. If the field is not accessible, it will be set to be accessible.
     * @param object Instance in which the value should be set
     * @param name Name of the field who's value should be set
     * @param value The value to be set
     */
    public static void setFieldValue(Object object, String name, Object value) {
        try {
            Field field = getField(object.getClass(), name);
            if (!field.canAccess(object)) {
                field.setAccessible(true);
            }
            field.set(object, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not set field value: " + object.getClass().getSimpleName() + "." + name, e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Utility method to read a field value. If the field is not accessible, it will be set to be accessible.
     * @param object Instance in which the value should be read
     * @param name Name of the field who's value should be read
     * @return The value of the field
     */
    public static Object getFieldValue(Object object, String name) {
        try {
            Field field = getField(object.getClass(), name);
            if (!field.canAccess(object)) {
                field.setAccessible(true);
            }
            return field.get(object);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not read field value: " + object.getClass().getSimpleName() + "." + name, e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}