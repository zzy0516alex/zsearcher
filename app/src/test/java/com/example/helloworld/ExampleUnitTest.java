package com.example.helloworld;

import com.example.helloworld.Utils.StringUtils;
import com.example.helloworld.Utils.TimeUtil;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
        try {
            long d= TimeUtil.getDifference(new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss").parse("2020-12-12 13:51:00"),TimeUtil.getCurrentTimeInDate(),1);
            System.out.println("d = " + d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}