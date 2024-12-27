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
package net.babelsoft.negatron.io.loader;

import java.util.HashMap;
import java.util.function.BiConsumer;
import net.babelsoft.negatron.model.item.EmulatedItem;
import net.babelsoft.negatron.util.function.HexaConsumer;
import net.babelsoft.negatron.util.function.PentaConsumer;
import net.babelsoft.negatron.util.function.TetraConsumer;
import net.babelsoft.negatron.util.function.TriConsumer;
import net.babelsoft.negatron.util.function.TriFunction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author capan
 */
public abstract class EmulatedItemListDataHandler<T extends EmulatedItem<T>> extends DefaultHandler {

    private final HashMap<String, T> map;
    private final EmulatedItemFactory<T> factory;
    protected T currentItem;
    private StringBuilder text;

    public EmulatedItemListDataHandler(EmulatedItemFactory<T> factory) {
        this.factory = factory;
        map = new HashMap<>();
    }
    
    protected void buildCurrentItem(String name, String group, String cloneof) {
        currentItem = map.get(name);
        if (currentItem == null) {
            currentItem = factory.create(name, group);
            if (cloneof == null)
                map.put(name, currentItem);
        }

        if (cloneof != null) {
            T parent = map.get(cloneof);
            if (parent == null) {
                parent = factory.create(cloneof, group);
                map.put(cloneof, parent);
            }
            currentItem.setParent(parent);
        }
    }

    @Override
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts
    ) throws SAXException {
        switch (qName) {
            case "description", "year" -> startTextElement();
        }
    }
    
    protected void startTextElement() {
        if (currentItem != null)
            text = new StringBuilder();
    }
    
    protected <V> void startConsumeCurrentItem(BiConsumer<T, V> consumer, V value) {
        if (currentItem != null)
            consumer.accept(currentItem, value);
    }
    
    protected <U, V> void startConsumeCurrentItem(TriConsumer<T, U, V> consumer, U value1, V value2) {
        if (currentItem != null)
            consumer.accept(currentItem, value1, value2);
    }
    
    protected <U, V, W> void startConsumeCurrentItem(TetraConsumer<T, U, V, W> consumer, U value1, V value2, W value3) {
        if (currentItem != null)
            consumer.accept(currentItem, value1, value2, value3);
    }
    
    protected <U, V, W, X> void startConsumeCurrentItem(PentaConsumer<T, U, V, W, X> consumer, U value1, V value2, W value3, X value4) {
        if (currentItem != null)
            consumer.accept(currentItem, value1, value2, value3, value4);
    }
    
    protected <U, V, W, X, Y> void startConsumeCurrentItem(HexaConsumer<T, U, V, W, X, Y> consumer, U value1, V value2, W value3, X value4, Y value5) {
        if (currentItem != null)
            consumer.accept(currentItem, value1, value2, value3, value4, value5);
    }
    
    protected <U, V, R> R startApplyOnCurrentItem(TriFunction<T, U, V, R> function, U value1, V value2) {
        if (currentItem != null)
            return function.apply(currentItem, value1, value2);
        else
            return null;
    }
    
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        if (text != null)
            text.append(chars, start, length);
    }
    
    protected <S> void endConsumeCurrentItem(BiConsumer<S, T> consumer, S operand) {
        if (currentItem != null) {
            consumer.accept(operand, currentItem);
            currentItem = null;
        }
    }
    
    protected void endTextElement(BiConsumer<T, String> consumer) {
        if (text != null) {
            consumer.accept(currentItem, text.toString());
            text = null;
        }
    }
    
    protected <V> void endTextElement(TriConsumer<T, String, V> consumer, V value) {
        if (text != null) {
            consumer.accept(currentItem, text.toString(), value);
            text = null;
        }
    }

    @Override
    public void endElement(
        String namespaceURI,
        String localName,
        String qName
    ) throws SAXException {
        switch (qName) {
            case "description" -> endTextElement(T::setDescription);
            case "year" -> endTextElement(T::setYear);
        }
    }
}
