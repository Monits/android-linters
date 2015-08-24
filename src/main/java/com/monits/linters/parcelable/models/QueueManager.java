package com.monits.linters.parcelable.models;

import java.util.Queue;

import javax.annotation.Nonnull;

public class QueueManager<T> {
	private final Queue<T> writeQueue;
	private final Queue<T> readQueue;

	/**
	 * Creates a new QueueManager instance
	 *
	 * @param writeQueue The queue used for writing
	 * @param readQueue The queue used for reading
	 */

	public QueueManager(@Nonnull final Queue<T> writeQueue,
		@Nonnull final Queue<T> readQueue) {
		this.writeQueue = writeQueue;
		this.readQueue = readQueue;
	}

	@Nonnull
	public Queue<T> getWriteQueue() {
		return writeQueue;
	}

	@Nonnull
	public Queue<T> getReadQueue() {
		return readQueue;
	}
}
