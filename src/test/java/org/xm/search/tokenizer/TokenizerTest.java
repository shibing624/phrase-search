package org.xm.search.tokenizer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author XuMing
 */
public class TokenizerTest {
    @Test
    public void segment() throws Exception {
        System.out.println(Tokenizer.segment("我是一只小小鸟，我要自由的飞。"));
        assertEquals("[我, 是, 一, 只, 小小鸟, ，, 我, 要, 自由, 的, 飞, 。]", StringUtils.join(Tokenizer.segment("我是一只小小鸟，我要自由的飞。")));
    }

    @Test
    public void getPinyin() throws Exception {
        assertEquals("de guo", Tokenizer.getPinyin("德国", " ", false));
        assertEquals("deguo", Tokenizer.getPinyin("德国", "", false));
        assertEquals("dg", Tokenizer.getPinyin("德国", " ", true));
        assertEquals("wshyzhxxn", Tokenizer.getPinyin("我是一只小小鸟", " ", true));
    }

}