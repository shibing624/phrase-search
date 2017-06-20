package org.xm.search.util;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * @author XuMing
 */
public class TimeUtilTest {
    @Test
    public void getTimeDes() throws Exception {
        System.out.println("time des:" + TimeUtil.getTimeDes(1000 * 60 * 60 + 2000 + 3L));
        assertEquals("1小时,2秒,3毫秒", TimeUtil.getTimeDes(1000 * 60 * 60 + 2000 + 3L));
    }

    @Test
    public void toString0() throws Exception {
        System.out.println(TimeUtil.toString(LocalDateTime.now(), "yyyy-MM"));
    }

    @Test
    public void toString1() throws Exception {
        System.out.println(TimeUtil.toString(LocalDateTime.now()));
    }

    @Test
    public void toString2() throws Exception {
        // 去掉微秒
        System.out.println(TimeUtil.toString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"));
    }

    @Test
    public void toString3() throws Exception {
        System.out.println(TimeUtil.toString(System.currentTimeMillis()));
    }

}