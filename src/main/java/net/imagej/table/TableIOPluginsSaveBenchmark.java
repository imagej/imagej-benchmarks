/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package net.imagej.table;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.imagej.AbstractBenchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.scijava.Context;
import org.scijava.io.DataHandleService;
import org.scijava.io.IOPlugin;
import org.scijava.io.IOService;
import org.scijava.io.Location;
import org.scijava.plugin.Parameter;
import org.scijava.util.MersenneTwisterFast;

/**
 * @author Leon Yang
 */
@State(Scope.Benchmark)
public class TableIOPluginsSaveBenchmark extends AbstractBenchmark {

	@Parameter
	private IOService ioService;

	private GenericTable table;
	
	@Override
	protected Context createContext() {
		return new Context(IOService.class, DataHandleService.class);
	}

	@Setup
	public void prepare() {
		table = new DefaultGenericTable();
		
		final MersenneTwisterFast r = new MersenneTwisterFast();
		for (int i = 0; i < 1024; i++) {
			FloatColumn column = new FloatColumn(Integer.toString(i));

			for (int j = 0; j < 1024; j++) {
				column.add(r.nextFloat());
			}

			table.add(column);
		}
	}

	@Override
	@TearDown
	public synchronized void cleanUp() {
		new File("large.csv").delete();

		super.cleanUp();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations = 10)
	@Measurement(iterations = 10)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	public void writeLargeWithDefaultTableIOPlugin() {
		final IOPlugin<GenericTable> tableIO = ioService.getInstance(
			DefaultTableIOPlugin.class);
		try {
			tableIO.save(table, "large.csv");
		}
		catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations = 10)
	@Measurement(iterations = 10)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 1)
	public void writeLargeWithCommonsCSVTableIOPlugin() {
		@SuppressWarnings("rawtypes")
		final IOPlugin<Table> tableIO = ioService.getInstance(
			CommonsCSVTableIOPlugin.class);
		try {
			tableIO.save(table, "large.csv");
		}
		catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(
			TableIOPluginsSaveBenchmark.class.getSimpleName()).warmupIterations(5)
			.measurementIterations(10).forks(2).threads(1).build();

		new Runner(opt).run();
	}

}
