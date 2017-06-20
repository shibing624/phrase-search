package org.xm.search.util;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author XuMing
 */
public class TextUtilTest {
    @Test
    public void isAllNonChinese() throws Exception {
        Assert.assertTrue(TextUtil.isAllNonChinese("123english good 321"));
    }

    @Test
    public void isAllChinese() throws Exception {
        Assert.assertTrue(TextUtil.isAllChinese("我爱我家"));
        Assert.assertEquals(false, TextUtil.isAllChinese("我爱我家 得分"));
    }

    @Test
    public void isChinese() throws Exception {
        Assert.assertTrue(TextUtil.isChinese('我'));
        Assert.assertEquals(false, TextUtil.isChinese('0'));
        Assert.assertEquals(false, TextUtil.isChinese('a'));
    }

    @Test
    public void isEnglish() throws Exception {
        Assert.assertEquals(true, TextUtil.isEnglish('a'));
        Assert.assertEquals(true, TextUtil.isEnglish('A'));
        Assert.assertEquals(true, TextUtil.isEnglish('Ａ'));
        Assert.assertEquals(false, TextUtil.isEnglish('3'));
    }

    @Test
    public void isAllEnglish() throws Exception {
        Assert.assertEquals(true, TextUtil.isAllEnglish("dsf"));
        Assert.assertEquals(false, TextUtil.isAllEnglish("dsf s"));
        Assert.assertEquals(false, TextUtil.isAllEnglish("dsf爱s"));
    }

    @Test
    public void isNumber() throws Exception {
        Assert.assertEquals(true, TextUtil.isNumber('1'));
        Assert.assertEquals(false, TextUtil.isNumber('p'));
    }

    @Test
    public void normalize() throws Exception {
        Assert.assertEquals("爱上1个坏蛋心很累说ni不说", TextUtil.normalize("爱上1个 坏蛋，心很累?说ni不说"));
    }

    @Test
    public void extractChinese() throws Exception {
        Assert.assertEquals("爱上个坏蛋心很累说不说", TextUtil.extractChinese("爱上1个 坏蛋，心很累?说ni不说"));
    }

}