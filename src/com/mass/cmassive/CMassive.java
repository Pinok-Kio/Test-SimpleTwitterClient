package com.mass.cmassive;

import java.util.List;

public class CMassive<T> {
	private T[] massive;
	private int fullSize;
	private int dataSize;
	private int availableSize;
	private int start;
	private int end;
	private final int resizeFactor = 2;
	private final int MIN_SIZE = 50;
	private static final Object lock = new Object();

	@SuppressWarnings("unchecked")
	public CMassive(List<? extends T> other) {
		fullSize = (other.size() < MIN_SIZE) ? (other.size() * resizeFactor + MIN_SIZE) : (other.size() * resizeFactor);
		massive = (T[]) new Object[fullSize];
		T[] otherAsArray = (T[]) new Object[other.size()];
		synchronized (lock) {
			otherAsArray = other.toArray(otherAsArray);
			System.arraycopy(otherAsArray, 0, massive, 0, otherAsArray.length);
			dataSize = otherAsArray.length;
			availableSize = fullSize - dataSize;
			end = otherAsArray.length - 1;
		}
	}

	@SuppressWarnings("unchecked")
	public T[] insertToEnd(List<? extends T> other) {
		if (other.size() + 1 > availableSize) {
			massive = resize(other.size());
		}
		T[] otherAsArray = (T[]) new Object[other.size()];
		synchronized (lock) {
			otherAsArray = other.toArray(otherAsArray);
			System.arraycopy(otherAsArray, 0, massive, end + 1, otherAsArray.length);
			dataSize += otherAsArray.length;
			end += otherAsArray.length;
			availableSize -= otherAsArray.length;
		}
		return massive;
	}

	@SuppressWarnings("unchecked")
	public T[] insertToStart(List<? extends T> other) {
		if (other.size() + 1 > availableSize) {
			massive = resize(other.size());
		}
		T[] otherAsArray = (T[]) new Object[other.size()];
		synchronized (lock) {
			otherAsArray = other.toArray(otherAsArray);
			start = (start > end) ? (start - otherAsArray.length) : (fullSize - otherAsArray.length);
			System.arraycopy(otherAsArray, 0, massive, start, otherAsArray.length);
			availableSize -= otherAsArray.length;
			dataSize += otherAsArray.length;
		}
		return massive;
	}

	@SuppressWarnings("unchecked")
	private T[] resize(int size) {
		final int biggerMassiveSize = fullSize * resizeFactor + size;
		T[] biggerMassive = (T[]) new Object[biggerMassiveSize];
		if (start > end) {
			synchronized (lock) {
				System.arraycopy(massive, start, biggerMassive, 0, (fullSize - start));
				System.arraycopy(massive, 0, biggerMassive, (fullSize - start), end + 1);
			}
		} else {
			synchronized (lock) {
				System.arraycopy(massive, 0, biggerMassive, 0, dataSize);
			}
		}
		synchronized (lock) {
			fullSize = biggerMassiveSize;
			start = 0;
			end = dataSize - 1;
			availableSize = fullSize - dataSize;
		}

		return biggerMassive;
	}

	public T[] removeItem(int position) {
		if (start == fullSize) {
			start = 0;
		}
		if (position > dataSize) {
			throw new IllegalArgumentException("position " + position + " is bigger than Massive size " + dataSize);
		}
		if (position == dataSize) {
			massive[end] = null;
			end--;
		} else if (start > end) {
			if ((massive.length - start) > position) {
				position += start;
				synchronized (lock) {
					System.arraycopy(massive, start, massive, start + 1, position - start);
					massive[start] = null;
				}
				start++;
			} else {
				position -= (massive.length - start);
				synchronized (lock) {
					System.arraycopy(massive, position + 1, massive, position, end - position - 1);
					massive[end] = null;
				}
				end--;
			}
		} else {
			synchronized (lock) {
				System.arraycopy(massive, position + 1, massive, position, end - position - 1);
				massive[end] = null;
			}
			end--;
		}
		availableSize++;
		dataSize--;
		return massive;
	}

	public T[] removeItem(T item) {
		int position = findPosition(item);
		if (position == -1) {
			throw new IllegalArgumentException("element not found " + item);
		}
		return removeItem(position);
	}

	private synchronized int findPosition(T item) {
		int position = 0;
		if (start < end) {
			for (int i = 0; i <= end; i++) {
				if (massive[i].equals(item)) {
					return position;
				}
				position++;
			}
		} else {
			for (int i = start; i < fullSize; i++) {
				if (massive[i].equals(item)) {
					return (i - start);
				}
			}
			for (int i = 0; i <= end; i++) {
				if (massive[i].equals(item)) {
					return (fullSize - start) + i;
				}
			}
		}
		return -1;
	}

	public synchronized T getItem(int position) {
		if (start == fullSize) {
			start = 0;
		}
		if (position > dataSize) {
			throw new IllegalArgumentException("position " + position + " is bigger than Massive size");
		}
		if (position == 0) {
			return massive[start];
		}
		if (position == dataSize - 1) {
			return massive[end];
		}
		if (start > end) {
			if ((fullSize - start) > position) {
				position += start;
				return massive[position];
			} else {
				position -= (fullSize - start);
				return massive[position];
			}
		} else {
			return massive[position];
		}
	}

//	public void show() {
//		System.out.println("SHOW START=" + start + " END=" + end + " FULLSIZE=" + fullSize + " DATASIZE=" + dataSize + " AVAILABLESIZE=" + availableSize);
//		if (start >= end) {
//			for (int i = start; i < massive.length; i++) {
//				System.out.print(massive[i] + " ");
//			}
//			for (int i = 0; i < end; i++) {
//				System.out.print(massive[i] + " ");
//			}
//		} else {
//			for (int i = 0; i < end; i++) {
//				System.out.print(massive[i] + " ");
//			}
//		}
//	}

	public int getDataSize() {
		return dataSize;
	}

	public void updateItem(T item, int position) {
		if (position > dataSize) {
			throw new IllegalArgumentException("position " + position + " is bigger than Massive size");
		}
		if (position == 0) {
			massive[start] = item;
		}
		if (position == dataSize) {
			massive[end] = item;
		}
		if (start > end) {
			if ((massive.length - start) > position) {
				position += start;
				massive[position] = item;
			} else {
				position -= (massive.length - start);
				massive[position] = item;
			}
		} else {
			massive[position] = item;
		}
	}
}
