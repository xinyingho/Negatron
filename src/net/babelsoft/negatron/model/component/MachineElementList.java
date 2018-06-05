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
package net.babelsoft.negatron.model.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class MachineElementList extends ArrayList<MachineElement<?>> {
    
    private final String machine;
    
    public MachineElementList(Machine machine) {
        super();
        this.machine = machine.getName();
    }
    
    protected MachineElementList(MachineElementList ref) {
        List<Pair<MachineElement<?>, MachineElement<?>>> oldNewList = new ArrayList<>();
        
        ref.forEach(element -> {
            MachineElement<?> copied = element.copy();
            this.add(copied);
            oldNewList.add(new Pair<>(element, copied));
        });
        oldNewList.forEach(pair -> {
            MachineElement<?> oldElement = pair.getKey();
            MachineElement<?> newElement = pair.getValue();
            newElement.copyDependencies(oldElement.getDependencies(), this);
        });
        
        machine = ref.machine;
    }
    
    public String getMachineName() {
        return machine;
    }
    
    public MachineElementList copy() {
        return new MachineElementList(this);
    }
    
    public List<String> toParameters() {
        return toParameters(null);
    }
    
    public List<String> toParameters(String origin) {
        List<String> params = stream().filter(
            elt -> !(elt instanceof Device)
        ).flatMap(
            value -> value.parameters().stream()
        ).collect(
            Collectors.toList()
        );
        
        Stream<MachineElement<?>> stream = null;
        if (origin != null) {
            // if currently processing a slot that has just changed its value
            if (stream().filter(elt -> elt instanceof Slot).filter(
                slot -> slot.getName().equals(origin)
            ).findAny().isPresent())
                // filter out the related subdevices that are now all invalid
                stream = stream().filter(elt -> {
                    if (elt instanceof Device)
                        return !((Device) elt).getTag().startsWith(origin + ":");
                    else
                        return false;
                });
        }
        if (stream == null)
            stream = stream().filter(elt -> elt instanceof Device);
        
        params.addAll(stream.flatMap(
            value -> value.parameters().stream()
        ).collect(
            Collectors.toList()
        ));
        
        params.add(0, machine);
        return params;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        toParameters().stream().forEachOrdered(
            string -> sb.append(string).append(" ")
        );
        return sb.toString().trim();
    }
}
