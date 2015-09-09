package com.monits.linters.parcelable.models;

import java.util.Queue;

import javax.annotation.Nonnull;

public class QueueManager {
	private final Queue<ParcelableField> writeFieldQueue;
	private final Queue<ParcelableField> readFieldQueue;
	private final Queue<Method> readMethodQueue;
	private final Queue<Method> writeMethodQueue;

	/**
	 *  Creates a new QueueManager instance.
	 *
	 *  @param writeFieldQueue Field writing queue
	 *  @param readFieldQueue Field reading queue
	 *  @param writeMethodQueue Method writing queue
	 *  @param readMethodQueue Method reading queue
	 */

	public QueueManager(@Nonnull final Queue<ParcelableField> writeFieldQueue,
			@Nonnull final Queue<ParcelableField> readFieldQueue,
			@Nonnull final Queue<Method> writeMethodQueue,
			@Nonnull final Queue<Method> readMethodQueue) {
		this.writeFieldQueue = writeFieldQueue;
		this.readFieldQueue = readFieldQueue;
		this.writeMethodQueue = writeMethodQueue;
		this.readMethodQueue = readMethodQueue;
	}

	@Nonnull
	public Queue<ParcelableField> getWriteFieldQueue() {
		return writeFieldQueue;
	}

	@Nonnull
	public Queue<ParcelableField> getReadFieldQueue() {
		return readFieldQueue;
	}

	@Nonnull
	public Queue<Method> getReadMethodQueue() {
		return readMethodQueue;
	}

	@Nonnull
	public Queue<Method> getWriteMethodQueue() {
		return writeMethodQueue;
	}
}
