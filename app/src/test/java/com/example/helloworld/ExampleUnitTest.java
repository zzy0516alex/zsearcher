package com.example.helloworld;

import com.example.helloworld.Utils.StringUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        //assertEquals(4, 2 + 2);
        double a=StringUtils.compareStrings("三国演义","三国演义(精修版)");
        System.out.println("a = " + a);
    }
}