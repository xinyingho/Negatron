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
package net.babelsoft.negatron.model.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import net.babelsoft.negatron.model.item.Software;
import net.babelsoft.negatron.model.item.SoftwarePart;

/**
 *
 * @author capan
 */
public class SoftwareStatistics implements Serializable {
    static final long serialVersionUID = 1L;
    
    private int parentCount;
    private int cloneCount;
    private ConcurrentMap<String, AtomicInteger> parentCountByType = new ConcurrentHashMap<>();
    private ConcurrentMap<String, AtomicInteger> cloneCountByType = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> parentCountByListByType =  new ConcurrentHashMap<>();
    private ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> cloneCountByListByType =  new ConcurrentHashMap<>();

    private static final Collector<AtomicInteger, AtomicInteger, Integer> sumCollector =
        new Collector<AtomicInteger, AtomicInteger, Integer>() {
            @Override
            public Supplier<AtomicInteger> supplier() {
                return AtomicInteger::new;
            }

            @Override
            public BiConsumer<AtomicInteger, AtomicInteger> accumulator() {
                return (a, b) -> {
                    a.set(a.get() + b.get());
                };
            }

            @Override
            public BinaryOperator<AtomicInteger> combiner() {
                return (a, b) -> {
                     AtomicInteger c = new AtomicInteger();
                     c.set(a.get() + b.get());
                     return c;
                };
            }

            @Override
            public Function<AtomicInteger, Integer> finisher() {
                return a -> a.get();
            }

            @Override
            public Set<Collector.Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }
    ;

    private int remove(
        String softwareList,
        final ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> softwareListTypeCount,
        final ConcurrentMap<String, AtomicInteger> softwareTypeCount
    ) {
        ConcurrentMap<String, AtomicInteger> stc = softwareListTypeCount.remove(softwareList);
        if (stc != null) {
            int total = stc.values().stream().collect(sumCollector);
            stc.forEach((interfaceFormat, count) -> {
                AtomicInteger i = softwareTypeCount.get(interfaceFormat);
                if (i != null) {
                    i.getAndAdd(-count.get());
                    if (i.get() <= 0)
                        softwareTypeCount.remove(interfaceFormat);
                }
            });
            return total;
        }
        return 0;
    }

    public void remove(String softwareList) {
        cloneCount -= remove(softwareList, cloneCountByListByType, cloneCountByType);
        parentCount -= remove(softwareList, parentCountByListByType, parentCountByType);
    }

    private void add(
        final SoftwarePart softwarePart, String softwareList,
        final ConcurrentMap<String, ConcurrentMap<String, AtomicInteger>> countByListByType,
        final ConcurrentMap<String, AtomicInteger> countByType
    ) {
        countByType.putIfAbsent(softwarePart.getInterfaceFormat(), new AtomicInteger(0));
        countByType.get(softwarePart.getInterfaceFormat()).getAndIncrement();

        countByListByType.putIfAbsent(softwareList, new ConcurrentHashMap<>());
        ConcurrentMap<String, AtomicInteger> cBT = countByListByType.get(softwareList);
        cBT.putIfAbsent(softwarePart.getInterfaceFormat(), new AtomicInteger(0));
        cBT.get(softwarePart.getInterfaceFormat()).getAndIncrement();
    }

    public void add(Software software) {
        software.getSoftwareParts().forEach(softwarePart -> {
            // a software with several interfaces, e.g. floppy + cd, will be counted that many times instead of only once
            if (software.hasParent()) {
                add(softwarePart, software.getGroup(), cloneCountByListByType, cloneCountByType);
                ++cloneCount;
            } else {
                add(softwarePart, software.getGroup(), parentCountByListByType, parentCountByType);
                ++parentCount;
            }
        });
    }
    
    public int getParentCount() {
        return parentCount;
    }
    
    public int getCloneCount() {
        return cloneCount;
    }
    
    public int getTotalCount() {
        return parentCount + cloneCount;
    }
    
    public ConcurrentMap<String, AtomicInteger> getParentCountByType() {
        return parentCountByType;
    }
    
    public ConcurrentMap<String, AtomicInteger> getCloneCountByType() {
        return cloneCountByType;
    }
}
