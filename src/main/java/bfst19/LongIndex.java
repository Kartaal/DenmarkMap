package bfst19;

import java.util.Arrays;

public class LongIndex {
	private long[] a;
	private int n;
	private boolean sorted = false;

	LongIndex() {
		a = new long[2];
	}

	public void add(long id) {
		if (n == a.length) resize(2 * a.length);    // double size of array if necessary
		a[n++] = id;                            // add item
		sorted = false;
	}

	private void resize(int capacity) {
		assert capacity >= n;

		long[] temp = new long[capacity];
		for (int i = 0; i < n; i++) {
			temp[i] = a[i];
		}
		a = temp;
	}

	public int get(long ref) {
		if (!sorted) {
			trim();
			Arrays.sort(a);
			sorted = true;
		}

		int res = Arrays.binarySearch(a, ref);

		if (res >= 0) {
			return res;
		} else {
			return -1;
		}
	}

	private void trim() {
		resize(n);
	}
}