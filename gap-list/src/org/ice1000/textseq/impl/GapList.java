package org.ice1000.textseq.impl;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Standalone implementation as a {@link java.util.List}.
 *
 * @author ice1000
 * @since v0.1
 */
@SuppressWarnings("WeakerAccess")
public class GapList<T> extends AbstractList<T> {
	private Object[] buffer;
	private int gapBegin, gapEnd;

	public GapList(T... buffer) {
		this.buffer = buffer;
		this.gapBegin = 0;
		this.gapEnd = buffer.length;
	}

	@SuppressWarnings("unchecked")
	public GapList(int initialCapacity) {
		this((T[]) new Object[initialCapacity]);
	}

	@SuppressWarnings("unchecked")
	public GapList() {
		this(32);
	}

	@Override
	public int size() {
		return buffer.length - gapLength();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		rangeCheck(index);
		if (index < gapBegin)
			return (T) buffer[index];
		else return (T) buffer[index + gapLength()];
	}

	private int gapLength() {
		return gapEnd - gapBegin;
	}

	@Override
	public void add(int index, T c) {
		rangeCheckForAdd(index);
		ensureLength(size() + 1);
		if (index == gapBegin) buffer[gapBegin++] = c;
		else {
			moveGap(index - gapBegin);
			assert index == gapBegin;
			buffer[gapBegin++] = c;
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> sequence) {
		rangeCheckForAdd(index);
		int length = sequence.size();
		ensureLength(size() + length);
		if (index == gapBegin) {
			int i = 0;
			for (Iterator<? extends T> iterator = sequence.iterator(); iterator.hasNext(); i++)
				buffer[gapBegin + i] = iterator.next();
		} else {
			moveGap(index - gapBegin);
			assert index == gapBegin;
			int i = 0;
			for (Iterator<? extends T> iterator = sequence.iterator(); iterator.hasNext(); i++)
				buffer[gapBegin + i] = iterator.next();
		}
		gapBegin += length;
		return true;
	}

	@Override
	public T set(int index, T element) {
		rangeCheck(index);
		int actualIndex;
		if (index <= gapBegin)
			actualIndex = index;
		else actualIndex = index + gapLength();
		Object old = buffer[actualIndex];
		buffer[actualIndex] = element;
		//noinspection unchecked
		return (T) old;
	}

	@Override
	public T remove(int index) {
		rangeCheck(index);
		Object result;
		int size = size();
		if (index == gapBegin) {
			result = buffer[gapBegin];
			if (index == size) gapBegin--;
			else gapEnd++;
		} else if (index == gapBegin - 1) {
			result = buffer[gapBegin--];
		} else {
			moveGap(index - gapBegin);
			assert index == gapBegin;
			result = buffer[gapBegin];
			if (index == size) gapBegin--;
			else gapEnd++;
		}
		//noinspection unchecked
		return (T) result;
	}

	private void moveGap(int shift) {
		if (shift == 0) return;
		int afterBegin = gapBegin + shift;
		int afterEnd = gapEnd + shift;
		if (shift > 0) System.arraycopy(buffer, gapBegin, buffer, gapEnd, shift);
		else System.arraycopy(buffer, afterBegin, buffer, afterEnd, -shift);
		gapBegin = afterBegin;
		gapEnd = afterEnd;
	}

	private void ensureLength(int length) {
		int bufferLength = buffer.length;
		if (bufferLength >= length) return;
		int newLength = Math.max(bufferLength * 2, length);
		Object[] newBuffer = new Object[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, gapBegin);
		int newGapEnd = newLength - size() + gapBegin;
		System.arraycopy(buffer, gapEnd, newBuffer, newGapEnd, bufferLength - gapEnd);
		gapEnd = newGapEnd;
		buffer = newBuffer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < gapBegin; i++) builder.append(buffer[i]);
		for (int i = gapEnd; i < buffer.length; i++) builder.append(buffer[i]);
		return builder.toString();
	}


	/**
	 * Checks if the given index is in range.  If not, throws an appropriate
	 * runtime exception.  This method does *not* check if the index is
	 * negative: It is always used immediately prior to an array access,
	 * which throws an ArrayIndexOutOfBoundsException if index is negative.
	 */
	private void rangeCheck(int index) {
		if (index >= size())
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	/**
	 * A version of rangeCheck used by add and addAll.
	 */
	private void rangeCheckForAdd(int index) {
		if (index > size() || index < 0)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	/**
	 * Constructs an IndexOutOfBoundsException detail message.
	 * Of the many possible refactorings of the error handling code,
	 * this "outlining" performs best with both server and client VMs.
	 */
	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size();
	}

	@Override
	public boolean contains(Object o) {
		for (int i = 0; i < gapBegin; i++) if (Objects.equals(o, buffer[i])) return true;
		for (int i = gapEnd; i < buffer.length; i++) if (Objects.equals(o, buffer[i])) return true;
		return false;
	}
}