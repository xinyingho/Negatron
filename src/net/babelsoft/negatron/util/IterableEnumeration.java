package net.babelsoft.negatron.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * http://www.javaspecialists.eu/archive/Issue107.html
 * @author Dr. Heinz M. Kabutz
 */
public class IterableEnumeration<T> implements Iterable<T> {
    
    private final Enumeration<T> en;
    
    public IterableEnumeration(Enumeration<T> en) {
        this.en = en;
    }
    
    // return an adaptor for the Enumeration
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            @Override
            public boolean hasNext() {
                return en.hasMoreElements();
            }
            
            @Override
            public T next() {
                return en.nextElement();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public static <T> Iterable<T> make(Enumeration<T> en) {
        return new IterableEnumeration<>(en);
    }
}
