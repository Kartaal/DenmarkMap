package bfst19;

import bfst19.Route_parsing.ResizingArray;
import javafx.beans.binding.ObjectExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.LongSupplier;

//this is for storing LongSuppliers, if unsorted the array sorts itself so get() can utilize binary search
public class LongIndex<Long>{
	private long[] a;
	private int n;
	private boolean sorted = false;

	public LongIndex(){
		a = new long[2];
	}

	public void add(long id) {
		if (n == a.length) resize(2 * a.length);    // double size of array if necessary
		a[n++] = id;                            // add item
		sorted = false;
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

	public int get(long ref) {
		if (!sorted) {
			Arrays.sort(a);
			sorted = true;
		}
		int lo = 0;
		int hi = n;
		while (hi - lo > 1) {
			int mi = lo + (hi - lo) / 2;
			if (ref < a[mi]) {
				hi = mi;
			} else {
				lo = mi;
			}
		}
		long elm = a[lo];
		if (elm == ref) {
			return lo;
		} else {
			return -1;
		}
	}
}