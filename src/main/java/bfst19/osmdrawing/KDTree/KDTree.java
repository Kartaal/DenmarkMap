package bfst19.osmdrawing.KDTree;

import bfst19.osmdrawing.BoundingBox;
import bfst19.osmdrawing.BoundingBoxable;
import bfst19.osmdrawing.Drawable;

import java.util.*;

public class KDTree {
	KDNode root;
	private static xComparator xComp = new xComparator();
	private static yComparator yComp = new yComparator();
	private Comparator<BoundingBoxable> selectComp;
	//TODO static?
	private final int listSize = 500;

	public KDTree(){
		root = null;
	}

	//Method for creating a KDTree from a list of Drawable
	public void insertAll(List<Drawable> list) {
		//If tree is currently empty, do a lot of work
		if(root == null) {
			//Set xComp as the first comparator
			selectComp = KDTree.xComp;
			//Use a modified QuickSort to ensure the lower values are in the left half
			// and the higher values are in the right half
			sort(list, selectComp);
			//Find the middle index to find the root element
			int splitIndex = list.size() / 2;

			//Ensure that our Drawable list is not empty
			if(list.size() > 0) {
				//TODO figure out something about all these typecasts
				//Find the comparator correct value of the middle element (Root so X value)
				float splitValue = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
				root = new KDNode(null, splitValue, false);

				//Start recursively creating the left and right subtrees
				// of indexes 0 to splitIndex for left subtree and splitIndex+1 to list.size() for the right subtree
				root.nodeL = createTree(list, root, 0, splitIndex);
				root.nodeR = createTree(list, root, splitIndex + 1, list.size());
			} else {
				//Arbitrary values to fill root in case the list of Drawable is empty
				root = new KDNode(null, -1, false);
			}
		}
	}

	private KDNode createTree(List<Drawable> list, KDNode parentNode, int lo, int hi) {
		//Added to prevent errors when lo == hi (there was a WayType with 2 elements that caused this problem)
		//TODO ensure correctness (still?)
		if (hi <= lo) return null;

		//Change comparator
		//? is a shorthand of if-else. (expression) ? (if expression true) : (if expression false)
		selectComp = selectComp == KDTree.xComp ? KDTree.yComp : KDTree.xComp;
		//Might want an overloaded version that only sorts a sublist
		sort(list, selectComp);

		int splitIndex = lo + (hi-lo) / 2;
		boolean horizontal = !parentNode.horizontal;

		float splitVal;
		if(!horizontal) {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterX();
		} else {
			splitVal = ((BoundingBoxable) list.get(splitIndex)).getCenterY();
		}

		KDNode currNode = new KDNode(null, splitVal, horizontal);

		//If we have reached a leaf node (list has fewer than listSize elements)
		if(listSize >= hi-lo) {
			List<BoundingBoxable> valueList = new ArrayList<>();
			for(int i = lo ; i < hi ; i++) {
				//Ugly typecast
				valueList.add((BoundingBoxable) list.get(i));
			}
			currNode.values = valueList;
			return currNode;
		} else {
			//Left subtree
			parentNode.nodeL = createTree(list, currNode, lo, splitIndex);

			//Right subtree
			parentNode.nodeR = createTree(list, currNode, splitIndex+1, hi);
		}

		return currNode;
	}

	//Currently never used
	public void insert(BoundingBoxable value){
		if(root == null) {
			List<BoundingBoxable> list = new ArrayList<>();
			list.add(value);
			root = new KDNode(list, value.getCenterX(), false);
		} else {
			//recursive insert, third parameter is for splitting dimension
			// 0 means split is on x-axis, 1 means split is on y-axis
			root = insert(root, value, false);
		}
	}

	private KDNode insert(KDNode x, BoundingBoxable value, boolean horizontal) {
		//If an empty leaf has been reached, create a new KDNode and return
		if(x == null) {
			//Ensure the new node has the correct axis split value
			float splitValue;
			if(horizontal) {
				splitValue = value.getCenterY();
			} else {
				splitValue = value.getCenterX();
			}
			List<BoundingBoxable> list = new ArrayList<>();
			list.add(value);

			return new KDNode(list, splitValue, horizontal);
		}

		//maybe to-do improve on this, KDNode.getSplit gets the correct dimensional split value
		//Split on x
		if(!horizontal) {
			//if current BoundingBoxable has a centerX less than current KDNode
			// recursive insert the BoundingBoxable to left child
			//Otherwise, insert BoundingBoxable to right child
			if(value.getCenterX() <= x.getSplit()) {
				x.nodeL = insert(x.nodeL, value, false);
			} else {
				x.nodeR = insert(x.nodeR, value, false);
			}
		} else {
			if(value.getCenterY() <= x.getSplit()) {
				x.nodeL = insert(x.nodeL, value, true);
			} else {
				x.nodeR = insert(x.nodeR, value, true);
			}
		}

		return x;
	}

	//Method for finding elements in the KDTree that intersects a BoundingBox
	public Iterable<Drawable> rangeQuery(BoundingBox bbox) {
		List<Drawable> returnElements = new ArrayList<>();
		rangeQuery(bbox, root, returnElements);
		return returnElements;
	}

	//Recursive checks down through the KDTree
	private List<Drawable> rangeQuery(BoundingBox queryBB, KDNode node, List<Drawable> returnElements) {
		//Return null if current node is null to stop endless recursion
		if(node == null) return null;

		//Ugly casting to Drawable...
		//if we have values, check for each if its BoundingBox intersects our query BoundingBox
		// if true, report it
		if(node.values != null) {
			for (BoundingBoxable value : node.values) {
				if (queryBB.intersects(value.getBB())) {
					returnElements.add((Drawable) value);
				}
			}
		}

		//Make temporary list to keep elements, so null returns don't cause problems
		//Check the left subtree for elements intersecting BoundingBox
		List<Drawable> tempList = rangeQuery(queryBB, node.nodeL, returnElements);
		if(tempList != null) {
			returnElements.addAll(tempList);
		}

		//Check the right subtree for elements intersecting BoundingBox
		tempList = rangeQuery(queryBB, node.nodeR, returnElements);
		if(tempList != null) {
			returnElements.addAll(tempList);
		}

		return returnElements;
	}


	public class KDNode {
		public List<BoundingBoxable> values = new ArrayList<>();
		public float split;
		public boolean horizontal; //if false, splits on x
		public KDNode nodeL; //child
		public KDNode nodeR; //child
		public BoundingBox bb;

		public KDNode(List<BoundingBoxable> value, float split, boolean horizontal) {
			if(value != null) {
				values.addAll(value);
			}
			this.split = split;
			this.horizontal = horizontal;
			nodeL = nodeR = null;

			//Create BoundingBox for the KDNode
			//Duplicate code from MultiPolyline
			//Arbitrary values that should exceed the coords on Denmark
			double minX = 100, maxX = 0, minY = 100, maxY = 0;
			for(BoundingBoxable valueBB : values) {
				BoundingBox lineBB = valueBB.getBB();
				if (lineBB.getMinX() < minX) {
					minX = lineBB.getMinX();
				}
				if (lineBB.getMaxX() > maxX) {
					maxX = lineBB.getMaxX();
				}
				if (lineBB.getMinY() < minY) {
					minY = lineBB.getMinY();
				}
				if (lineBB.getMaxY() > maxY) {
					maxY = lineBB.getMaxY();
				}
			}

			bb = new BoundingBox(minX, minY, maxX-minX, maxY-minY);
		}

		public float getSplit() {
			return split;
		}

		//Returns a BoundingBox object representing the bounding box of all the elements in the node
		public BoundingBox getBB() {
			return bb;
		}
	}

	//Innerclass comparator for X dimension
	public static class xComparator implements Comparator<BoundingBoxable> {
		//This calculation is made to convert from float to int because Comparator interface requires it
		// Returns a negative integer if a's centerX value is smaller than b's centerX value
		// Returns 0 if a's centerX value is equal to b's centerX value
		// Returns a positive integer if a's centerX value is larger than b's centerX value
		public int compare(BoundingBoxable a, BoundingBoxable b) {
			return (int) (a.getCenterX() - b.getCenterX())*1000000;
		}
	}

	//Innerclass comparator for Y dimension
	public static class yComparator implements Comparator<BoundingBoxable> {
		//This calculation is made to convert from float to int because Comparator interface requires it
		// Returns a negative integer if a's centerY value is smaller than b's centerY value
		// Returns 0 if a's centerY value is equal to b's centerY value
		// Returns a positive integer if a's centerY value is larger than b's centerY value
		public int compare(BoundingBoxable a, BoundingBoxable b) {
			return (int) (a.getCenterY() - b.getCenterY())*1000000;
		}
	}



	//TODO CLEAN THIS SHIT UP
	//Not in use currently
	public BoundingBoxable select(List<Drawable> a, int k, Comparator<BoundingBoxable> comp)
	{
		StdRandom rand = new StdRandom();
		rand.shuffle(a);
		int lo = 0, hi = a.size() - 1;
		while (hi > lo)
		{
			int j = partition(a, lo, hi, comp);
			if (j == k) return (BoundingBoxable) a.get(k);
			else if (j > k) hi = j - 1;
			else if (j < k) lo = j + 1;
		}
		return (BoundingBoxable) a.get(k);
	}

	//From Algs4 book, modified
	public void sort(List<Drawable> a, Comparator<BoundingBoxable> comp) {
		StdRandom rand = new StdRandom();
		rand.shuffle(a);
		sort(a, 0, a.size() - 1, comp);
	}

	// quicksort the subarray from a[lo] to a[hi]
	private void sort(List<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp) {
		if (hi <= lo) return;
		int j = partition(a, lo, hi, comp);
		sort(a, lo, j-1, comp);
		sort(a, j+1, hi, comp);
	}

	//From Algs4 book
	private int partition(List<Drawable> a, int lo, int hi, Comparator<BoundingBoxable> comp)
	{ // Partition into a[lo..i-1], a[i], a[i+1..hi].
		int i = lo, j = hi+1; // left and right scan indices
		Drawable v = a.get(lo); // partitioning item
		while (true)
		{ // Scan right, scan left, check for scan complete, and exchange.
			while (comp.compare((BoundingBoxable) a.get(++i), (BoundingBoxable) v) > 0) if (i == hi) break;
			while (comp.compare((BoundingBoxable) v, (BoundingBoxable)  a.get(--j)) < 0) if (j == lo) break;
			if (i >= j) break;
			exch(a, i, j);
		}
		exch(a, lo, j); // Put v = a[j] into position
		return j; // with a[lo..j-1] <= a[j] <= a[j+1..hi].
	}

	private void exch(List<Drawable> a, int i, int j) {
		Drawable t = a.get(i);
		a.set(i, a.get(j));
		a.set(j, t);
	}



	/**
	 *  The {@code StdRandom} class provides static methods for generating
	 *  random number from various discrete and continuous distributions,
	 *  including uniform, Bernoulli, geometric, Gaussian, exponential, Pareto,
	 *  Poisson, and Cauchy. It also provides method for shuffling an
	 *  array or subarray and generating random permutations.
	 *  <p>
	 *  By convention, all intervals are half open. For example,
	 *  <code>uniform(-1.0, 1.0)</code> returns a random number between
	 *  <code>-1.0</code> (inclusive) and <code>1.0</code> (exclusive).
	 *  Similarly, <code>shuffle(a, lo, hi)</code> shuffles the <code>hi - lo</code>
	 *  elements in the array <code>a[]</code>, starting at index <code>lo</code>
	 *  (inclusive) and ending at index <code>hi</code> (exclusive).
	 *  <p>
	 *  For additional documentation,
	 *  see <a href="https://introcs.cs.princeton.edu/22library">Section 2.2</a> of
	 *  <i>Computer Science: An Interdisciplinary Approach</i>
	 *  by Robert Sedgewick and Kevin Wayne.
	 *
	 *  @author Robert Sedgewick
	 *  @author Kevin Wayne
	 */
	public final class StdRandom {

		private Random random;    // pseudo-random number generator
		private long seed;        // pseudo-random number generator seed


		// don't instantiate
		private StdRandom() {
			// static initializer
			{
				// this is how the seed was set in Java 1.4
				seed = System.currentTimeMillis();
				random = new Random(seed);
			}
		}

		/**
		 * Sets the seed of the pseudo-random number generator.
		 * This method enables you to produce the same sequence of "random"
		 * number for each execution of the program.
		 * Ordinarily, you should call this method at most once per program.
		 *
		 * @param s the seed
		 */
		public void setSeed(long s) {
			seed = s;
			random = new Random(seed);
		}

		/**
		 * Returns the seed of the pseudo-random number generator.
		 *
		 * @return the seed
		 */
		public long getSeed() {
			return seed;
		}

		/**
		 * Returns a random real number uniformly in [0, 1).
		 *
		 * @return a random real number uniformly in [0, 1)
		 */
		public double uniform() {
			return random.nextDouble();
		}

		/**
		 * Returns a random integer uniformly in [0, n).
		 *
		 * @param n number of possible integers
		 * @return a random integer uniformly between 0 (inclusive) and {@code n} (exclusive)
		 * @throws IllegalArgumentException if {@code n <= 0}
		 */
		public int uniform(int n) {
			if (n <= 0) throw new IllegalArgumentException("argument must be positive: " + n);
			return random.nextInt(n);
		}


		/**
		 * Returns a random long integer uniformly in [0, n).
		 *
		 * @param n number of possible {@code long} integers
		 * @return a random long integer uniformly between 0 (inclusive) and {@code n} (exclusive)
		 * @throws IllegalArgumentException if {@code n <= 0}
		 */
		public long uniform(long n) {
			if (n <= 0L) throw new IllegalArgumentException("argument must be positive: " + n);

			// https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#longs-long-long-long-
			long r = random.nextLong();
			long m = n - 1;

			// power of two
			if ((n & m) == 0L) {
				return r & m;
			}

			// reject over-represented candidates
			long u = r >>> 1;
			while (u + m - (r = u % n) < 0L) {
				u = random.nextLong() >>> 1;
			}
			return r;
		}

		///////////////////////////////////////////////////////////////////////////
		//  STATIC METHODS BELOW RELY ON JAVA.UTIL.RANDOM ONLY INDIRECTLY VIA
		//  THE STATIC METHODS ABOVE.
		///////////////////////////////////////////////////////////////////////////

		/**
		 * Returns a random real number uniformly in [0, 1).
		 *
		 * @return a random real number uniformly in [0, 1)
		 * @deprecated Replaced by {@link #uniform()}.
		 */
		@Deprecated
		public double random() {
			return uniform();
		}

		/**
		 * Returns a random integer uniformly in [a, b).
		 *
		 * @param a the left endpoint
		 * @param b the right endpoint
		 * @return a random integer uniformly in [a, b)
		 * @throws IllegalArgumentException if {@code b <= a}
		 * @throws IllegalArgumentException if {@code b - a >= Integer.MAX_VALUE}
		 */
		public int uniform(int a, int b) {
			if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
				throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
			}
			return a + uniform(b - a);
		}

		/**
		 * Returns a random real number uniformly in [a, b).
		 *
		 * @param a the left endpoint
		 * @param b the right endpoint
		 * @return a random real number uniformly in [a, b)
		 * @throws IllegalArgumentException unless {@code a < b}
		 */
		public double uniform(double a, double b) {
			if (!(a < b)) {
				throw new IllegalArgumentException("invalid range: [" + a + ", " + b + ")");
			}
			return a + uniform() * (b - a);
		}

		/**
		 * Returns a random boolean from a Bernoulli distribution with success
		 * probability <em>p</em>.
		 *
		 * @param p the probability of returning {@code true}
		 * @return {@code true} with probability {@code p} and
		 * {@code false} with probability {@code 1 - p}
		 * @throws IllegalArgumentException unless {@code 0} &le; {@code p} &le; {@code 1.0}
		 */
		public boolean bernoulli(double p) {
			if (!(p >= 0.0 && p <= 1.0))
				throw new IllegalArgumentException("probability p must be between 0.0 and 1.0: " + p);
			return uniform() < p;
		}

		/**
		 * Returns a random boolean from a Bernoulli distribution with success
		 * probability 1/2.
		 *
		 * @return {@code true} with probability 1/2 and
		 * {@code false} with probability 1/2
		 */
		public boolean bernoulli() {
			return bernoulli(0.5);
		}

		/**
		 * Returns a random real number from a standard Gaussian distribution.
		 *
		 * @return a random real number from a standard Gaussian distribution
		 * (mean 0 and standard deviation 1).
		 */
		public double gaussian() {
			// use the polar form of the Box-Muller transform
			double r, x, y;
			do {
				x = uniform(-1.0, 1.0);
				y = uniform(-1.0, 1.0);
				r = x * x + y * y;
			} while (r >= 1 || r == 0);
			return x * Math.sqrt(-2 * Math.log(r) / r);

			// Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
			// is an independent random gaussian
		}

		/**
		 * Returns a random real number from a Gaussian distribution with mean &mu;
		 * and standard deviation &sigma;.
		 *
		 * @param mu    the mean
		 * @param sigma the standard deviation
		 * @return a real number distributed according to the Gaussian distribution
		 * with mean {@code mu} and standard deviation {@code sigma}
		 */
		public double gaussian(double mu, double sigma) {
			return mu + sigma * gaussian();
		}

		/**
		 * Returns a random integer from a geometric distribution with success
		 * probability <em>p</em>.
		 * The integer represents the number of independent trials
		 * before the first success.
		 *
		 * @param p the parameter of the geometric distribution
		 * @return a random integer from a geometric distribution with success
		 * probability {@code p}; or {@code Integer.MAX_VALUE} if
		 * {@code p} is (nearly) equal to {@code 1.0}.
		 * @throws IllegalArgumentException unless {@code p >= 0.0} and {@code p <= 1.0}
		 */
		public int geometric(double p) {
			if (!(p >= 0)) {
				throw new IllegalArgumentException("probability p must be greater than 0: " + p);
			}
			if (!(p <= 1.0)) {
				throw new IllegalArgumentException("probability p must not be larger than 1: " + p);
			}
			// using algorithm given by Knuth
			return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
		}

		/**
		 * Returns a random integer from a Poisson distribution with mean &lambda;.
		 *
		 * @param lambda the mean of the Poisson distribution
		 * @return a random integer from a Poisson distribution with mean {@code lambda}
		 * @throws IllegalArgumentException unless {@code lambda > 0.0} and not infinite
		 */
		public int poisson(double lambda) {
			if (!(lambda > 0.0))
				throw new IllegalArgumentException("lambda must be positive: " + lambda);
			if (Double.isInfinite(lambda))
				throw new IllegalArgumentException("lambda must not be infinite: " + lambda);
			// using algorithm given by Knuth
			// see http://en.wikipedia.org/wiki/Poisson_distribution
			int k = 0;
			double p = 1.0;
			double expLambda = Math.exp(-lambda);
			do {
				k++;
				p *= uniform();
			} while (p >= expLambda);
			return k - 1;
		}

		/**
		 * Returns a random real number from the standard Pareto distribution.
		 *
		 * @return a random real number from the standard Pareto distribution
		 */
		public double pareto() {
			return pareto(1.0);
		}

		/**
		 * Returns a random real number from a Pareto distribution with
		 * shape parameter &alpha;.
		 *
		 * @param alpha shape parameter
		 * @return a random real number from a Pareto distribution with shape
		 * parameter {@code alpha}
		 * @throws IllegalArgumentException unless {@code alpha > 0.0}
		 */
		public double pareto(double alpha) {
			if (!(alpha > 0.0))
				throw new IllegalArgumentException("alpha must be positive: " + alpha);
			return Math.pow(1 - uniform(), -1.0 / alpha) - 1.0;
		}

		/**
		 * Returns a random real number from the Cauchy distribution.
		 *
		 * @return a random real number from the Cauchy distribution.
		 */
		public double cauchy() {
			return Math.tan(Math.PI * (uniform() - 0.5));
		}

		/**
		 * Returns a random integer from the specified discrete distribution.
		 *
		 * @param probabilities the probability of occurrence of each integer
		 * @return a random integer from a discrete distribution:
		 * {@code i} with probability {@code probabilities[i]}
		 * @throws IllegalArgumentException if {@code probabilities} is {@code null}
		 * @throws IllegalArgumentException if sum of array entries is not (very nearly) equal to {@code 1.0}
		 * @throws IllegalArgumentException unless {@code probabilities[i] >= 0.0} for each index {@code i}
		 */
		public int discrete(double[] probabilities) {
			if (probabilities == null) throw new IllegalArgumentException("argument array is null");
			double EPSILON = 1.0E-14;
			double sum = 0.0;
			for (int i = 0; i < probabilities.length; i++) {
				if (!(probabilities[i] >= 0.0))
					throw new IllegalArgumentException("array entry " + i + " must be nonnegative: " + probabilities[i]);
				sum += probabilities[i];
			}
			if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)
				throw new IllegalArgumentException("sum of array entries does not approximately equal 1.0: " + sum);

			// the for loop may not return a value when both r is (nearly) 1.0 and when the
			// cumulative sum is less than 1.0 (as a result of floating-point roundoff error)
			while (true) {
				double r = uniform();
				sum = 0.0;
				for (int i = 0; i < probabilities.length; i++) {
					sum = sum + probabilities[i];
					if (sum > r) return i;
				}
			}
		}

		/**
		 * Returns a random integer from the specified discrete distribution.
		 *
		 * @param frequencies the frequency of occurrence of each integer
		 * @return a random integer from a discrete distribution:
		 * {@code i} with probability proportional to {@code frequencies[i]}
		 * @throws IllegalArgumentException if {@code frequencies} is {@code null}
		 * @throws IllegalArgumentException if all array entries are {@code 0}
		 * @throws IllegalArgumentException if {@code frequencies[i]} is negative for any index {@code i}
		 * @throws IllegalArgumentException if sum of frequencies exceeds {@code Integer.MAX_VALUE} (2<sup>31</sup> - 1)
		 */
		public int discrete(int[] frequencies) {
			if (frequencies == null) throw new IllegalArgumentException("argument array is null");
			long sum = 0;
			for (int i = 0; i < frequencies.length; i++) {
				if (frequencies[i] < 0)
					throw new IllegalArgumentException("array entry " + i + " must be nonnegative: " + frequencies[i]);
				sum += frequencies[i];
			}
			if (sum == 0)
				throw new IllegalArgumentException("at least one array entry must be positive");
			if (sum >= Integer.MAX_VALUE)
				throw new IllegalArgumentException("sum of frequencies overflows an int");

			// pick index i with probabilitity proportional to frequency
			double r = uniform((int) sum);
			sum = 0;
			for (int i = 0; i < frequencies.length; i++) {
				sum += frequencies[i];
				if (sum > r) return i;
			}

			// can't reach here
			assert false;
			return -1;
		}

		/**
		 * Returns a random real number from an exponential distribution
		 * with rate &lambda;.
		 *
		 * @param lambda the rate of the exponential distribution
		 * @return a random real number from an exponential distribution with
		 * rate {@code lambda}
		 * @throws IllegalArgumentException unless {@code lambda > 0.0}
		 */
		public double exp(double lambda) {
			if (!(lambda > 0.0))
				throw new IllegalArgumentException("lambda must be positive: " + lambda);
			return -Math.log(1 - uniform()) / lambda;
		}

		/**
		 * Rearranges the elements of the specified array in uniformly random order.
		 *
		 * @param a the array to shuffle
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 */
		public void shuffle(Object[] a) {
			validateNotNull(a);
			int n = a.length;
			for (int i = 0; i < n; i++) {
				int r = i + uniform(n - i);     // between i and n-1
				Object temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}


		public void shuffle(List<Drawable> a) {
			int n = a.size();
			for (int i = 0; i < n; i++) {
				int r = i + uniform(n - i);     // between i and n-1
				Drawable temp = a.get(i);
				a.set(i, a.get(r));
				a.set(r, temp);
			}
		}

		/**
		 * Rearranges the elements of the specified array in uniformly random order.
		 *
		 * @param a the array to shuffle
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 */
		public void shuffle(double[] a) {
			validateNotNull(a);
			int n = a.length;
			for (int i = 0; i < n; i++) {
				int r = i + uniform(n - i);     // between i and n-1
				double temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Rearranges the elements of the specified array in uniformly random order.
		 *
		 * @param a the array to shuffle
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 */
		public void shuffle(int[] a) {
			validateNotNull(a);
			int n = a.length;
			for (int i = 0; i < n; i++) {
				int r = i + uniform(n - i);     // between i and n-1
				int temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Rearranges the elements of the specified array in uniformly random order.
		 *
		 * @param a the array to shuffle
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 */
		public void shuffle(char[] a) {
			validateNotNull(a);
			int n = a.length;
			for (int i = 0; i < n; i++) {
				int r = i + uniform(n - i);     // between i and n-1
				char temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Rearranges the elements of the specified subarray in uniformly random order.
		 *
		 * @param a  the array to shuffle
		 * @param lo the left endpoint (inclusive)
		 * @param hi the right endpoint (exclusive)
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 * @throws IllegalArgumentException unless {@code (0 <= lo) && (lo < hi) && (hi <= a.length)}
		 */
		public void shuffle(Object[] a, int lo, int hi) {
			validateNotNull(a);
			validateSubarrayIndices(lo, hi, a.length);

			for (int i = lo; i < hi; i++) {
				int r = i + uniform(hi - i);     // between i and hi-1
				Object temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Rearranges the elements of the specified subarray in uniformly random order.
		 *
		 * @param a  the array to shuffle
		 * @param lo the left endpoint (inclusive)
		 * @param hi the right endpoint (exclusive)
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 * @throws IllegalArgumentException unless {@code (0 <= lo) && (lo < hi) && (hi <= a.length)}
		 */
		public void shuffle(double[] a, int lo, int hi) {
			validateNotNull(a);
			validateSubarrayIndices(lo, hi, a.length);

			for (int i = lo; i < hi; i++) {
				int r = i + uniform(hi - i);     // between i and hi-1
				double temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Rearranges the elements of the specified subarray in uniformly random order.
		 *
		 * @param a  the array to shuffle
		 * @param lo the left endpoint (inclusive)
		 * @param hi the right endpoint (exclusive)
		 * @throws IllegalArgumentException if {@code a} is {@code null}
		 * @throws IllegalArgumentException unless {@code (0 <= lo) && (lo < hi) && (hi <= a.length)}
		 */
		public void shuffle(int[] a, int lo, int hi) {
			validateNotNull(a);
			validateSubarrayIndices(lo, hi, a.length);

			for (int i = lo; i < hi; i++) {
				int r = i + uniform(hi - i);     // between i and hi-1
				int temp = a[i];
				a[i] = a[r];
				a[r] = temp;
			}
		}

		/**
		 * Returns a uniformly random permutation of <em>n</em> elements.
		 *
		 * @param n number of elements
		 * @return an array of length {@code n} that is a uniformly random permutation
		 * of {@code 0}, {@code 1}, ..., {@code n-1}
		 * @throws IllegalArgumentException if {@code n} is negative
		 */
		public int[] permutation(int n) {
			if (n < 0) throw new IllegalArgumentException("argument is negative");
			int[] perm = new int[n];
			for (int i = 0; i < n; i++)
				perm[i] = i;
			shuffle(perm);
			return perm;
		}

		/**
		 * Returns a uniformly random permutation of <em>k</em> of <em>n</em> elements.
		 *
		 * @param n number of elements
		 * @param k number of elements to select
		 * @return an array of length {@code k} that is a uniformly random permutation
		 * of {@code k} of the elements from {@code 0}, {@code 1}, ..., {@code n-1}
		 * @throws IllegalArgumentException if {@code n} is negative
		 * @throws IllegalArgumentException unless {@code 0 <= k <= n}
		 */
		public int[] permutation(int n, int k) {
			if (n < 0) throw new IllegalArgumentException("argument is negative");
			if (k < 0 || k > n) throw new IllegalArgumentException("k must be between 0 and n");
			int[] perm = new int[k];
			for (int i = 0; i < k; i++) {
				int r = uniform(i + 1);    // between 0 and i
				perm[i] = perm[r];
				perm[r] = i;
			}
			for (int i = k; i < n; i++) {
				int r = uniform(i + 1);    // between 0 and i
				if (r < k) perm[r] = i;
			}
			return perm;
		}

		// throw an IllegalArgumentException if x is null
		// (x can be of type Object[], double[], int[], ...)
		private void validateNotNull(Object x) {
			if (x == null) {
				throw new IllegalArgumentException("argument is null");
			}
		}

		// throw an exception unless 0 <= lo <= hi <= length
		private void validateSubarrayIndices(int lo, int hi, int length) {
			if (lo < 0 || hi > length || lo > hi) {
				throw new IllegalArgumentException("subarray indices out of bounds: [" + lo + ", " + hi + ")");
			}
		}

	}
}
