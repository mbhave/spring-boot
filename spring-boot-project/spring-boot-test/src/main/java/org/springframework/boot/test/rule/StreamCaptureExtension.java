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
public class StreamCaptureExtension implements BeforeEachCallback, AfterEachCallback, CharSequence {

	private CaptureOutputStream captureOut;

	private CaptureOutputStream captureErr;

	private ByteArrayOutputStream copy;

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

	}

	protected void captureOutput() {
		AnsiOutputControl.get().disableAnsiOutput();
		this.copy = new ByteArrayOutputStream();
		this.captureOut = new CaptureOutputStream(System.out, this.copy);
		this.captureErr = new CaptureOutputStream(System.err, this.copy);
		System.setOut(new PrintStream(this.captureOut));
		System.setErr(new PrintStream(this.captureErr));
	}

	protected void releaseOutput() {
		AnsiOutputControl.get().enabledAnsiOutput();
		System.setOut(this.captureOut.getOriginal());
		System.setErr(this.captureErr.getOriginal());
		this.copy = null;
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
		return 0;
	}

	@Override
	public char charAt(int index) {
		return 0;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return null;
	}

	@Override
	public String toString() {
		flush();
		return this.copy.toString();
	}

	/**
	 * Verify that the output is matched by the supplied {@code matcher}. Verification is
	 * performed after the test method has executed.
	 * @param matcher the matcher
	 */
	public void expect(Matcher<? super String> matcher) {
		this.matchers.add(matcher);
	}

	private static class CaptureOutputStream extends OutputStream {

		private final PrintStream original;

		private final OutputStream copy;

		CaptureOutputStream(PrintStream original, OutputStream copy) {
			this.original = original;
			this.copy = copy;
		}

		@Override
		public void write(int b) throws IOException {
			this.copy.write(b);
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
			this.original.write(b, off, len);
		}

		public PrintStream getOriginal() {
			return this.original;
		}

		@Override
		public void flush() throws IOException {
			this.copy.flush();
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
