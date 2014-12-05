package no.marintek.mylyn.wikitext.ooxml;

import static org.junit.Assert.*;

import org.junit.Test;

public class ColourSchemeTest {

	@Test
	public void testColourRange() {
		ChartFactory chartFac = new ChartFactory();
		byte[] num1 = chartFac.getColourTest(1);
		assertTrue(num1[0] <= 255);
		assertTrue(num1[1] <= 255);
		assertTrue(num1[2] <= 255);
		assertTrue(num1[0] >= -255);
		assertTrue(num1[1] >= -255);
		assertTrue(num1[2] >= -255);
		
		byte[] num2 = chartFac.getColourTest(2);
		assertTrue(num2[0] <= 255);
		assertTrue(num2[1] <= 255);
		assertTrue(num2[2] <= 255);
		assertTrue(num2[0] >= -255);
		assertTrue(num2[1] >= -255);
		assertTrue(num2[2] >= -255);
		
		assertTrue(num1[0] != num2[0]);
		
		byte[] num10 = chartFac.getColourTest(10);
		assertTrue(num10[0] <= 255);
		assertTrue(num10[1] <= 255);
		assertTrue(num10[2] <= 255);
		assertTrue(num10[0] >= -255);
		assertTrue(num10[1] >= -255);
		assertTrue(num10[2] >= -255);
		
		byte[] num20 = chartFac.getColourTest(20);
		assertTrue(num20[0] <= 255);
		assertTrue(num20[1] <= 255);
		assertTrue(num20[2] <= 255);
		assertTrue(num20[0] >= -255);
		assertTrue(num20[1] >= -255);
		assertTrue(num20[2] >= -255);
		
		assertTrue(num10[0] != num20[0]);
		
		byte[] num100 = chartFac.getColourTest(100);
		assertTrue(num100[0] <= 255);
		assertTrue(num100[1] <= 255);
		assertTrue(num100[2] <= 255);
		assertTrue(num100[0] >= -255);
		assertTrue(num100[1] >= -255);
		assertTrue(num100[2] >= -255);
		
		byte[] num200 = chartFac.getColourTest(200);
		assertTrue(num200[0] <= 255);
		assertTrue(num200[1] <= 255);
		assertTrue(num200[2] <= 255);
		assertTrue(num200[0] >= -255);
		assertTrue(num200[1] >= -255);
		assertTrue(num200[2] >= -255);
		
		assertTrue(num100[0] != num200[0]);
	}
	
	private final static byte[][] COLOUR_SCHEME = new byte[][] 
			{ { 1, 2, 2 }, // black
			{ (byte) 235, 49, 55 }, // red
			{ 18, (byte) 139, 75 }, // green
			{ 29, 92, (byte) 167 }, // blue
			{ (byte) 252, 125, 50 }, // orange
			{ 101, 48, (byte) 143 }, // purple
			{ (byte) 160, 31, 38 }, // burgundy
			{ (byte) 178, 60, (byte) 147 } // pink
	};
	
}
