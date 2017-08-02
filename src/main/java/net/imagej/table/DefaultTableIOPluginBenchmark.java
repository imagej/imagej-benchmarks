/*
 * #%L
 * ImageJ server for RESTful access to ImageJ.
 * %%
 * Copyright (C) 2013 - 2016 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.scijava.io.DataHandle;
import org.scijava.io.DataHandleService;
import org.scijava.io.FileLocation;
import org.scijava.io.IOPlugin;
import org.scijava.io.IOService;
import org.scijava.io.Location;
import org.scijava.plugin.Parameter;
import org.scijava.util.MersenneTwisterFast;

/**
 * @author Leon Yang
 */
@State(Scope.Benchmark)
public class DefaultTableIOPluginBenchmark extends AbstractBenchmark {

	@Parameter
	private DataHandleService dataHandleService;

	@Parameter
	private IOService ioService;

	/** Reference to file for cleaning up after the benchmark. */
	private Location location;

	@Override
	protected Context createContext() {
		return new Context(IOService.class, DataHandleService.class);
	}

	@Setup
	public void prepare() {
		final StringBuilder sb = new StringBuilder(10 * 1024 * 1024);
		for (int i = 0; i < 1023; i++) {
			sb.append(String.format("%09d,", i));
		}
		sb.append(String.format("%08d\r\n", 1023));
		final MersenneTwisterFast r = new MersenneTwisterFast();
		for (int i = 0; i < 1023; i++) {
			for (int j = 0; j < 1023; j++) {
				sb.append(String.format("%.7f,", r.nextFloat()));
			}
			sb.append(String.format("%.6f\r\n", r.nextFloat()));
		}

		location = new FileLocation("large.csv");
		final DataHandle<? extends Location> handle = dataHandleService.create(location);
		try {
			handle.write(sb.toString().getBytes());
		}
		catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	@TearDown
	public synchronized void cleanUp() {
		new File(location.getURI()).delete();

		super.cleanUp();
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@Warmup(iterations = 10)
	@Measurement(iterations = 20)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Fork(value = 2)
	public void openLarge() {
		final IOPlugin<GenericTable> tableIO = ioService.getInstance(
			DefaultTableIOPlugin.class);
		try {
			tableIO.open("large.csv");
		}
		catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(
			DefaultTableIOPluginBenchmark.class.getSimpleName()).warmupIterations(10)
			.measurementIterations(20).forks(2).threads(1).build();

		new Runner(opt).run();
	}

}
