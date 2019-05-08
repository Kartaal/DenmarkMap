package bfst19;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ResizingArray<T> implements Serializable, Iterable<T> {
	private Object[] a;         // array of items
	private int n;            // number of elements on stack

	public ResizingArray() {
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

		Object[] temp = new Object[capacity];
		for (int i = 0; i < n; i++) {
			temp[i] = a[i];
		}
		a = temp;

	}

	@SuppressWarnings("unchecked")
	public T get(int index) {
		if (0 > index || index > n)
			throw new ArrayIndexOutOfBoundsException("tried to access index " + index + " out of an array with length " + a.length);

		return (T) a[index];
	}

	public void add(Object item) {
		if (n == a.length) resize(2 * a.length);    // double size of array if necessary
		a[n++] = item;                            // add item
	}

	public void set(int i, Object item) {
		a[i] = item;
	}

	public void trim() {
		resize(n);
	}

	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator();
	}

	class ArrayIterator implements Iterator<T> {
		int current = 0;

		public boolean hasNext() {
			return current < ResizingArray.this.n;
		}

		@SuppressWarnings("unchecked")
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return (T) a[current++];
		}
	}
}
