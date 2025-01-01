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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.Pair;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class MachineElementList extends ArrayList<MachineElement<?>> {
    private static final long serialVersionUID = 1L;
    
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
        Stream<MachineElement<?>> deviceStream = stream().filter(elt -> elt instanceof Device);
        // if currently processing a slot that has just changed its value
        if (origin != null && stream().filter(elt -> elt instanceof Slot).anyMatch(
            slot -> slot.getName().equals(origin)
        ))
            // filter out the related subdevices that are now all invalid
            deviceStream = deviceStream.filter(
                elt -> !((Device) elt).getTag().startsWith(origin + ":")
            );
        List<MachineElement<?>> devices = deviceStream.toList();
        
        Stream<MachineElement<?>> slotStream = stream().filter(elt -> elt instanceof Slot).filter(
            // remove any empty slots that are actually used through an equivalent device e.g. by inputting a game cartridge
            elt -> elt.getValue() != null && ((SlotOption) elt.getValue()).getDevice() != null
            || devices.stream().noneMatch(
                d -> ((Device) d).getTag().equals(elt.getName())
            )
        );
        
        Function<Stream<MachineElement<?>>, List<String>> flatten = stream -> stream.flatMap(
            value -> value.parameters().stream()
        ).collect(
            Collectors.toList()
        );
  
        List<String> params = flatten.apply(stream().filter(
            elt -> !(elt instanceof Device)
        ).filter(
            elt -> !(elt instanceof Slot)
        ));
        params.addAll(flatten.apply(slotStream));
        params.addAll(flatten.apply(devices.stream()));
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
