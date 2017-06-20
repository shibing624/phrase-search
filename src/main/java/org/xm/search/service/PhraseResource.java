package org.xm.search.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xm.search.Search;
import org.xm.search.domain.Document;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据辅助类
 *
 * @author XuMing
 */
public class PhraseResource {
    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();

    private static final Map<Integer, Document> DOCUMENT_MAP = new HashMap<>();

    static {
        String path = Search.Config.PharseTextPath;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
            int id = 1;
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (StringUtils.isBlank(line) || line.startsWith("#")) {
                        continue;
                    }
                    Document document = new Document();
                    document.setId(id);
                    document.setValue(line);
                    DOCUMENT_MAP.put(id, document);
                    id++;
                } catch (Exception e) {
                    logger.error("错误数据：" + line, e);
                }
            }
        } catch (Exception e) {
            logger.error("数据解析出错，文件路径: " + path, e);
        }
    }

    public static Map<Integer, Document> loadPhraseText() {
        return DOCUMENT_MAP;
    }

}
