package org.xm.search.tokenizer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author XuMing
 */
public class TokenizerTest {
    @Test
    public void segment() throws Exception {
        System.out.println(Tokenizer.segment("我是一只小小鸟，我要自由的飞。"));
        assertEquals("", Tokenizer.segment("我是一只小小鸟，我要自由的飞。"));
    }

    @Test
    public void getPinyin() throws Exception {
        String result = Tokenizer.getPinyin("阿里巴巴", " ", false);

    }

}