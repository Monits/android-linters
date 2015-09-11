/*
	Copyright 2010-2015 Monits

	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
	file except in compliance with the License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software distributed under
	the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
	ANY KIND, either express or implied. See the License for the specific language governing
	permissions and limitations under the License.
 */
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
