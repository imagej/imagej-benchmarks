/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2017 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops;

import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Intervals;

import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

public abstract class AbstractOpBenchmark {

	@Parameter
	protected Context context;

	@Parameter
	protected OpService ops;

	@Parameter
	protected LogService logService;

	private int seed;

	private int pseudoRandom() {
		return seed = 3170425 * seed + 132102;
	}

	/** Subclasses can override to create a context with different services. */
	protected Context createContext() {
		return new Context(OpService.class, LogService.class);
	}

	/**
	 * Sets up a SciJava context with {@link OpService} and {@link LogService}.
	 */
	@Setup
	public void setUp() {
		createContext().inject(this);
	}

	/**
	 * Disposes of the {@link OpService} and {@link LogService} that were
	 * initialized in {@link #setUp()}.
	 */
	@TearDown
	public synchronized void cleanUp() {
		if (context != null) {
			context.dispose();
			context = null;
			ops = null;
			logService = null;
		}
	}

	public ArrayImg<ByteType, ByteArray> generateByteArrayTestImg(
		final boolean fill, final long... dims)
	{
		final byte[] array = new byte[(int) Intervals.numElements(new FinalInterval(
			dims))];

		if (fill) {
			seed = 17;
			for (int i = 0; i < array.length; i++) {
				array[i] = (byte) pseudoRandom();
			}
		}

		return ArrayImgs.bytes(array, dims);
	}

}
