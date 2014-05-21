package com.codetodeath.search.indexer;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by dengguo on 14-5-21.
 */
public class TestTextFileIndexer {

    @Test
    public void testBuildIndex() throws IOException {
        TextFileIndexer indexer = new TextFileIndexer("/data/lucene-nearby-search/index");
        String filePath = "/home/dengguo/dev/yp-search/yp.data";

        indexer.createIndex(filePath);
    }
}
