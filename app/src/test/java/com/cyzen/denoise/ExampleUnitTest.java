package com.cyzen.denoise;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        byte i = -55;
        System.out.println((byte) (-(int) i & 0xff));
        System.out.println((byte) -(((int) i) & 0xff));
        System.out.println((byte) ((int) -i & 0xff));
    }
}