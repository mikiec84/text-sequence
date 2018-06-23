package org.ice1000.textseq.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineSpanTest {
	@Test
	public void simple() {
		String initial = "# Text Sequence\n" +
				"\n" +
				"[Read this paper][paper0]\n" +
				"\n" +
				"Windows|Linux\n" +
				":---:|:---:\n" +
				"[![AV][w-l]][w-i]|[![CircleCI][l-l]][l-i]\n";
		LineSpan lineSpan = new LineSpan(initial);
		assertEquals(initial, lineSpan.toString());
		assertEquals(initial.length(), lineSpan.length());
		for (int i = 0; i < initial.length(); i++) {
			assertEquals(initial.charAt(i), lineSpan.charAt(i));
		}
	}
}