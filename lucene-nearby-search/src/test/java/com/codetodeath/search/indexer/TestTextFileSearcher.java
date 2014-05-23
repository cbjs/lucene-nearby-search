package com.codetodeath.search.indexer;

import com.codetodeath.search.searcher.TextFileSearcher;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Test TextfileSearcher
 * Created by dengguo on 14-5-21.
 */
public class TestTextFileSearcher {

    @Test
    public void testNearbySearch() throws IOException {
        TextFileSearcher searcher = new TextFileSearcher("/data/lucene-nearby-search/index");

        String queryString = "tag:美发 OR tag:美容";
        double lat = 40.03068;
        double lon = 116.33681;

        double km = 3.0;
        List<String> searchResults = searcher.search(queryString, lon, lat, km, 100);
        for (String str : searchResults) {
            System.out.println(str);
        }
    }
}
