/*
 * Copyright 2012-2018 the original author or authors.
 *
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
 */
package org.springframework.boot.test.rule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.boot.ansi.AnsiOutput;

import static org.hamcrest.Matchers.allOf;

/**
 * @author Madhura Bhave
 */
public class StreamCaptureExtension
		implements BeforeEachCallback, AfterEachCallback, CharSequence {

	private CaptureOutputStream captureOut;

	private CaptureOutputStream captureErr;

	private ByteArrayOutputStream sysoutCopy;

	private ByteArrayOutputStream syserrCopy;

	private ByteArrayOutputStream mergedCopy;

	private List<Matcher<? super String>> matchers = new ArrayList<>();

	@Override
	public void afterEach(ExtensionContext context) {
		try {
			if (!this.matchers.isEmpty()) {
				String output = this.toString();
				Assert.assertThat(output, allOf(this.matchers));
			}
		}
		finally {
			releaseOutput();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		captureOutput();
	}

	public void reset() {
		this.sysoutCopy.reset();
		this.syserrCopy.reset();
		this.mergedCopy.reset();
	}

	protected void captureOutput() {
		AnsiOutputControl.get().disableAnsiOutput();
		initialize();
		this.captureOut = new CaptureOutputStream(System.out, this.sysoutCopy,
				this.mergedCopy);
		this.captureErr = new CaptureOutputStream(System.err, this.syserrCopy,
				this.mergedCopy);
		System.setOut(new PrintStream(this.captureOut));
		System.setErr(new PrintStream(this.captureErr));
	}

	private void initialize() {
		this.sysoutCopy = new ByteArrayOutputStream();
		this.syserrCopy = new ByteArrayOutputStream();
		this.mergedCopy = new ByteArrayOutputStream();
	}

	protected void releaseOutput() {
		AnsiOutputControl.get().enabledAnsiOutput();
		System.setOut(this.captureOut.getOriginal());
		System.setErr(this.captureErr.getOriginal());
		this.sysoutCopy = null;
	}

	public void flush() {
		try {
			this.captureOut.flush();
			this.captureErr.flush();
		}
		catch (IOException ex) {
			// ignore
		}
	}

	@Override
	public int length() {
		return (this.mergedCopy == null ? 0 : this.mergedCopy.toString().length());
	}

	@Override
	public char charAt(int index) {
		if (this.mergedCopy == null) {
			throw new IndexOutOfBoundsException();
		}
		return this.mergedCopy.toString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (this.mergedCopy == null) {
			throw new IndexOutOfBoundsException();
		}
		return this.mergedCopy.toString().subSequence(start, end);
	}

	@Override
	public String toString() {
		flush();
		return this.mergedCopy.toString();
	}

	public String justStdError() {
		flush();
		return this.syserrCopy.toString();
	}

	/**
	*
	 * @return the output of System.out
	 */
	public String justStdOut() {
		flush();
		return this.sysoutCopy.toString();
	}

	private static class CaptureOutputStream extends OutputStream {

		private final PrintStream original;

		private final OutputStream copy;

		private final OutputStream additional;

		CaptureOutputStream(PrintStream original, OutputStream copy,
				OutputStream merged) {
			this.original = original;
			this.copy = copy;
			this.additional = merged;
		}

		@Override
		public void write(int b) throws IOException {
			this.copy.write(b);
			this.additional.write(b);
			this.original.write(b);
			this.original.flush();
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.copy.write(b, off, len);
			this.additional.write(b, off, len);
			this.original.write(b, off, len);
		}

		public PrintStream getOriginal() {
			return this.original;
		}

		@Override
		public void flush() throws IOException {
			this.copy.flush();
			this.additional.flush();
			this.original.flush();
		}

	}

	/**
	 * Allow AnsiOutput to not be on the test classpath.
	 */
	private static class AnsiOutputControl {

		public void disableAnsiOutput() {
		}

		public void enabledAnsiOutput() {
		}

		public static AnsiOutputControl get() {
			try {
				Class.forName("org.springframework.boot.ansi.AnsiOutput");
				return new AnsiPresentOutputControl();
			}
			catch (ClassNotFoundException ex) {
				return new AnsiOutputControl();
			}
		}

	}

	private static class AnsiPresentOutputControl extends AnsiOutputControl {

		@Override
		public void disableAnsiOutput() {
			AnsiOutput.setEnabled(AnsiOutput.Enabled.NEVER);
		}

		@Override
		public void enabledAnsiOutput() {
			AnsiOutput.setEnabled(AnsiOutput.Enabled.DETECT);
		}

	}

}
