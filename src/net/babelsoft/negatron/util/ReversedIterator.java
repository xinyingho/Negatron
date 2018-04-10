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
package net.babelsoft.negatron.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author capan
 */
public class ReversedIterator {

    public static <T> Iterator<T> reversedIterator(List<T> list) {
        ListIterator<T> iterator = list.listIterator(list.size());
        return new Iterator<T>(){

            @Override
            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            @Override
            public T next() {
                return iterator.previous();
            }
        };
    }

    public static <T> Stream<T> stream(List<T> list) {
        Iterator<T> sourceIterator = reversedIterator(list);
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
