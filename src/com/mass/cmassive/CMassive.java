package com.mass.cmassive;

public class CMassive<T> {
	private T[] massive;
	private int fullSize;
	private int dataSize;
	private int availableSize;
	private int start;
	private int end;
	private final int resizeFactor = 2;
	private final int MIN_SIZE = 20;

	@SuppressWarnings("unchecked")
	public CMassive(int initSize) {
		fullSize = initSize;
		massive = (T[]) new Object[fullSize];
		availableSize = initSize;
	}

	@SuppressWarnings("unchecked")
	public CMassive(T[] other) {
		fullSize = (other.length < MIN_SIZE) ? (other.length * resizeFactor + MIN_SIZE) : (other.length * resizeFactor);

		massive = (T[]) new Object[fullSize];
		System.arraycopy(other, 0, massive, 0, other.length);
		dataSize = other.length;
		availableSize = fullSize - dataSize;
		end = other.length - 1;
	}

	public T[] insertToEnd(T[] other) {
		if (other.length + 1 > availableSize) {
			massive = resize(other.length);
		}
		System.arraycopy(other, 0, massive, end + 1, other.length);
		dataSize += other.length;
		end += other.length;
		availableSize -= other.length;
		return massive;
	}

	public T[] insertToStart(T[] other) {
		if (other.length + 1 > availableSize) {
			massive = resize(other.length);
		}

		start = (start > end) ? (start - 1) : (fullSize - other.length);

		System.arraycopy(other, 0, massive, start, other.length);
		availableSize -= other.length;
		dataSize += other.length;
		return massive;
	}

	@SuppressWarnings("unchecked")
	private T[] resize(int size) {
		fullSize *= resizeFactor;
		fullSize += size;
		T[] biggerMassive = (T[]) new Object[fullSize];
		if (start > end) {
			System.arraycopy(massive, start, biggerMassive, 0, (fullSize - start));
			System.arraycopy(massive, 0, biggerMassive, (fullSize - start), end + 1);
		} else {
			System.arraycopy(massive, 0, biggerMassive, 0, dataSize);
		}
		start = 0;
		end = dataSize - 1;
		availableSize = fullSize - dataSize;

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
				System.arraycopy(massive, start, massive, start + 1, position - start);
				massive[start] = null;
				start++;
			} else {
				position -= (massive.length - start);
				System.arraycopy(massive, position + 1, massive, position, end - position - 1);
				massive[end] = null;
				end--;
			}
		} else {
			System.arraycopy(massive, position + 1, massive, position, end - position - 1);
			massive[end] = null;
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

	private int findPosition(T item) {
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

	public T getItem(int position) {
		if (start == fullSize) {
			start = 0;
		}
		if (position > dataSize) {
			throw new IllegalArgumentException("position " + position + " is bigger than Massive size");
		}
		if (position == 0) {
			return massive[start];
		}
		if (position == dataSize) {
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

	public void show() {
		System.out.println("SHOW START=" + start + " END=" + end + " FULLSIZE=" + fullSize + " DATASIZE=" + dataSize + " AVAILABLESIZE=" + availableSize);
		if (start >= end) {
			for (int i = start; i < massive.length; i++) {
				System.out.print(massive[i] + " ");
			}
			for (int i = 0; i < end; i++) {
				System.out.print(massive[i] + " ");
			}
		} else {
			for (int i = 0; i < end; i++) {
				System.out.print(massive[i] + " ");
			}
		}
	}

	public void showReal() {
		for (T t : massive) {
			if (t == null) {
				System.out.print(" n ");
			} else {
				System.out.print(t + " ");
			}
		}
	}

	public int getFullSize() {
		return fullSize;
	}

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
