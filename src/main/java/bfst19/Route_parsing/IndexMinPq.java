package bfst19.Route_parsing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexMinPQ<Key extends Comparable<Key>> {
    private int maxN;        // maximum number of elements on PQ
    private int n;           // number of elements on PQ
    private int[] pq;        // binary heap using 1-based indexing
    private int[] qp;        // inverse of pq - qp[pq[i]] = pq[qp[i]] = i
    private Key[] keys;    // keys[i] = priority of i
    private HashMap<Long,Integer> idToIndex;
    private HashMap<Integer,Long> indexToId;

    public IndexMinPQ(int maxN) {
        if (maxN < 0) throw new IllegalArgumentException();
        idToIndex = new HashMap<>();
        indexToId = new HashMap<>();
        this.maxN = maxN;
        n = 0;
        keys = (Key[]) new Comparable[maxN + 1];     // make this of length maxN??
        pq   = new int[maxN + 1];
        qp   = new int[maxN + 1];                   // make this of length maxN??
        for (int i = 0; i <= maxN; i++)
            qp[i] = -1;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public boolean contains(long i) {
        int indexI = idToIndex.get(i);
        if (i < 0 || i >= maxN) throw new IllegalArgumentException();
        return qp[indexI] != -1;
    }

    public int size() {
        return n;
    }

    public void insert(long i, Key key) {
        int indexOfI = idToIndex.get(i);
        if (indexOfI < 0 || indexOfI >= maxN) throw new IllegalArgumentException();
        if (contains(indexOfI)) throw new IllegalArgumentException("index is already in the priority queue");
        idToIndex.put(i,n);
        indexToId.put(n,i);
        n++;
        //why n?
        //qp[currentIndex] = n;
        pq[n] = indexOfI;
        keys[indexOfI] = key;
        swim(n);
    }

    public long minIndex() {
        if (n == 0) throw new NoSuchElementException("Priority queue underflow");
        return indexToId.get(pq[1]);
    }

    public Key minKey() {
        if (n == 0) throw new NoSuchElementException("Priority queue underflow");
        return keys[pq[1]];
    }

    public long delMin() {
        if (n == 0) throw new NoSuchElementException("Priority queue underflow");
        int min = pq[1];
        exch(1, n--);
        sink(1);
        assert min == pq[n+1];
        qp[min] = -1;        // delete
        keys[min] = null;    // to help with garbage collection
        pq[n+1] = -1;        // not needed
        return indexToId.get(min);
    }

    public Key keyOf(long i) {
        int indexI = idToIndex.get(i);
        if (indexI < 0 || indexI >= maxN) throw new IllegalArgumentException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        else return keys[indexI];
    }

    public void changeKey(long i, Key key) {
        int indexI = idToIndex.get(i);
        if (indexI < 0 || indexI >= maxN) throw new IllegalArgumentException();
        if (!contains(indexI)) throw new NoSuchElementException("index is not in the priority queue");
        keys[indexI] = key;
        swim(qp[indexI]);
        sink(qp[indexI]);
    }

    @Deprecated
    public void change(int i, Key key) {
        changeKey(i, key);
    }

    public void decreaseKey(long i, Key key) {
        int indexI = idToIndex.get(i);
        if (indexI < 0 || indexI >= maxN) throw new IllegalArgumentException();
        if (!contains(indexI)) throw new NoSuchElementException("index is not in the priority queue");
        if (keys[indexI].compareTo(key) <= 0)
            throw new IllegalArgumentException("Calling decreaseKey() with given argument would not strictly decrease the key");
        keys[indexI] = key;
        swim(qp[indexI]);
    }

    public void increaseKey(long i, Key key) {
        int indexI = idToIndex.get(i);
        if (indexI < 0 || indexI >= maxN) throw new IllegalArgumentException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        if (keys[indexI].compareTo(key) >= 0)
            throw new IllegalArgumentException("Calling increaseKey() with given argument would not strictly increase the key");
        keys[indexI] = key;
        sink(qp[indexI]);
    }

    public void delete(long i) {
        int indexI = idToIndex.get(i);
        if (indexI < 0 || indexI >= maxN) throw new IllegalArgumentException();
        if (!contains(indexI)) throw new NoSuchElementException("index is not in the priority queue");
        int index = qp[indexI];
        exch(indexI, n--);
        swim(indexI);
        sink(indexI);
        keys[indexI] = null;
        qp[indexI] = -1;
    }

    private boolean greater(int i, int j) {
        return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
    }

    private void exch(int i, int j) {
        //to be able to acess nodes based on their index and their id's we must swap the values in the maps
        long idOfI = indexToId.get(i);
        long idOfJ = indexToId.get(j);
        idToIndex.put(idOfI,j);
        idToIndex.put(idOfJ,i);
        indexToId.put(i,idOfJ);
        indexToId.put(j,idOfI);
        //normal exch
        int swap = pq[i];
        pq[i] = pq[j];
        pq[j] = swap;
        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }

    private void swim(int k) {
        while (k > 1 && greater(k/2, k)) {
            exch(k, k/2);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= n) {
            int j = 2*k;
            if (j < n && greater(j, j+1)) j++;
            if (!greater(k, j)) break;
            exch(k, j);
            k = j;
        }
    }

    //public Iterator<Integer> iterator() { return new HeapIterator(); }

    /*private class HeapIterator implements Iterator<Integer> {
        // create a new pq
        private IndexMinPQ<Key> copy;

        // add all elements to copy of heap
        // takes linear time since already in heap order so no keys move
        public HeapIterator() {
            copy = new IndexMinPQ<Key>(pq.length - 1);
            for (int i = 1; i <= n; i++)
                copy.insert(pq[i], keys[pq[i]]);
        }

        public boolean hasNext()  { return !copy.isEmpty();                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Integer next() {
            if (!hasNext()) throw new NoSuchElementException();
            return copy.delMin();
        }
    }*/

    /*public static void main(String[] args) {
        // insert a bunch of strings
        String[] strings = { "it", "was", "the", "best", "of", "times", "it", "was", "the", "worst" };

        IndexMinPQ<String> pq = new IndexMinPQ<String>(strings.length);
        for (int i = 0; i < strings.length; i++) {
            pq.insert(i, strings[i]);
        }

        // delete and print each key
        while (!pq.isEmpty()) {
            int i = pq.delMin();
            System.out.println(i + " " + strings[i]);
        }
        System.out.println();

        // reinsert the same strings
        for (int i = 0; i < strings.length; i++) {
            pq.insert(i, strings[i]);
        }

        // print each key using the iterator
        for (int i : pq) {
            System.out.println(i + " " + strings[i]);
        }
        while (!pq.isEmpty()) {
            pq.delMin();
        }

    }*/
}




