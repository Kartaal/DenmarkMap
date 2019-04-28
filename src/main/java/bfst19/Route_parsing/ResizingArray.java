package bfst19.Route_parsing;

import java.io.Serializable;

public class ResizingArray implements Serializable {
    private long[] a;         // array of items
    private int n;            // number of elements on stack

    public ResizingArray(){
        a = new long[2];
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
        long[] temp =  new long[capacity];
        for (int i = 0; i < n; i++) {
            temp[i] = a[i];
        }
        a = temp;

        // alternative implementation
        // a = java.util.Arrays.copyOf(a, capacity);
    }

    public long get(int index) {
        if (0 > index || index > n) throw new ArrayIndexOutOfBoundsException();
        return a[index];
    }

    public void add(long item) {
        if (n == a.length) resize(2 * a.length);    // double size of array if necessary
        a[n++] = item;                            // add item
    }

}
