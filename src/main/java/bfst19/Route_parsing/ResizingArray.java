package bfst19.Route_parsing;

import java.io.Serializable;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

public class ResizingArray<T> implements Serializable {
    private Object[] a;         // array of items
    private int n;            // number of elements on stack
    private boolean isSorted = false;

    public ResizingArray(){
        a = new Object[2];
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }


    // resize the underlying array holding the elements
    private void resize(int capacity) {
        assert capacity >= n;

        // textbook implementation
        Object[] temp =  new Object[capacity];
        for (int i = 0; i < n; i++) {
            temp[i] = a[i];
        }
        a = temp;

        // alternative implementation
        // a = java.util.Arrays.copyOf(a, capacity);
    }

    public T get(int index) {
        if (0 > index || index > n) throw new ArrayIndexOutOfBoundsException();
        //dont know why i have to cast to type T
        return (T)a[index];
    }

    public void add(Object item) {
        if (n == a.length) resize(2 * a.length);    // double size of array if necessary
        a[n++] = item;                            // add item
        isSorted = false;
    }

    public void set(int i, Object item) {
        a[i] = item;
    }

    public void sort(Comparator<? super T> c) {
        Arrays.sort((T[]) a, 0, n, c);
    }

    //only to be used no longer has to grow/shrink
    public void trim(){
        resize(n+1);
    }
}
