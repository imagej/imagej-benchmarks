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

package net.imagej.ops.threshold.localMean;

import java.util.concurrent.TimeUnit;

import net.imagej.ops.AbstractOpBenchmark;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ByteType;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks {@link LocalMeanThreshold}.
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
@State(Scope.Benchmark)
public class LocalMeanThresholdBenchmark extends AbstractOpBenchmark {

	private Img<ByteType> in;
	private Img<BitType> out;

	@Param({ "1", "2" })
	private int spanSize;

	@Param("250")
	private int imageBorderLength;

	@Setup
	public void initImg() {
		in = generateByteArrayTestImg(true, imageBorderLength, imageBorderLength);
		try {
			out = in.factory().imgFactory(new BitType()).create(in, new BitType());
		}
		catch (IncompatibleTypeException exc) {
			logService.error(exc);
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations = 5)
	@Measurement(iterations = 10)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 4)
	public Img<BitType> testLocalMean() {
		ops.run(LocalMeanThreshold.class, out, in, new RectangleShape(spanSize,
			false), new OutOfBoundsMirrorFactory<ByteType, Img<ByteType>>(
				Boundary.SINGLE), 0.0);

		return out;
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(LocalMeanThresholdBenchmark.class
			.getSimpleName()).warmupIterations(5).measurementIterations(10).forks(1).threads(1)
			.build();

		new Runner(opt).run();
	}

}
