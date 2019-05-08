package bfst19.Route_parsing;

import java.util.NoSuchElementException;

class IndexMinPQ<Key extends Comparable<Key>> {
	private int maxN;        // maximum number of elements on PQ
	private int n;           // number of elements on PQ
	private int[] pq;        // binary heap using 1-based indexing
	private int[] qp;        // inverse of pq - qp[pq[i]] = pq[qp[i]] = i
	private Key[] keys;      // keys[i] = priority of i

	@SuppressWarnings("unchecked")
	IndexMinPQ(int maxN) {
		if (maxN < 0) throw new IllegalArgumentException();
		this.maxN = maxN;
		n = 0;
		keys = (Key[]) new Comparable[maxN + 1];    // make this of length maxN??
		pq   = new int[maxN + 1];
		qp   = new int[maxN + 1];                   // make this of length maxN??
		for (int i = 0; i <= maxN; i++)
			qp[i] = -1;
	}

	boolean isEmpty() {
		return n == 0;
	}

	boolean contains(int i) {
		if (i < 0 || i >= maxN) throw new IllegalArgumentException();
		return qp[i] != -1;
	}

	public int size() {
		return n;
	}

	void insert(int i, Key key) {
		if (i < 0 || i >= maxN) throw new IllegalArgumentException();
		if (contains(i)) throw new IllegalArgumentException("index is already in the priority queue");
		n++;
		qp[i] = n;
		pq[n] = i;
		keys[i] = key;
		swim(n);
	}

	int delMin() {
		if (n == 0) throw new NoSuchElementException("Priority queue underflow");
		int min = pq[1];
		exch(1, n--);
		sink(1);
		assert min == pq[n+1];
		qp[min] = -1;        // delete
		keys[min] = null;    // to help with garbage collection
		pq[n+1] = -1;        // not needed
		return min;
	}

	private void changeKey(int i, Key key) {
		if (i < 0 || i >= maxN) throw new IllegalArgumentException();
		if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
		keys[i] = key;
		swim(qp[i]);
		sink(qp[i]);
	}

	@Deprecated
	public void change(int i, Key key) {
		changeKey(i, key);
	}

	void decreaseKey(int i, Key key) {
		if (i < 0 || i >= maxN) throw new IllegalArgumentException();
		if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
		if (keys[i].compareTo(key) <= 0)
			throw new IllegalArgumentException("Calling decreaseKey() with given argument would not strictly decrease the key");
		keys[i] = key;
		swim(qp[i]);
	}

	private boolean greater(int i, int j) {
		return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
	}

	private void exch(int i, int j) {
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
}