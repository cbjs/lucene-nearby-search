package com.codetodeath.search.indexer;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A test indexer class
 * Created by dengguo on 14-5-21.
 */
public class TextFileIndexer {
    private String indexPath;
    private GeneralIndexer indexer;
    private Splitter tabSplitter;
    private Splitter commaSplitter;

    private SpatialContext spatialContext;
    private SpatialStrategy spatialStrategy;

    public TextFileIndexer(String indexPath) throws IOException {
        this.indexPath = indexPath;
        indexer = new GeneralIndexer(indexPath);
        tabSplitter = Splitter.on("\t").trimResults();
        commaSplitter = Splitter.on(",").trimResults().omitEmptyStrings();

        spatialContext = SpatialContext.GEO;
        SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, 11);
        spatialStrategy = new RecursivePrefixTreeStrategy(grid, "geo");
    }

    public Document stringToDocument(String line) {
        ArrayList<String> fields = Lists.newArrayList(tabSplitter.split(line));
        if (fields.size() != 6) {
            System.out.println("wrong format:" + line);
            return null;
        }

        Document doc = new Document();

        doc.add(new TextField("name", fields.get(0), Field.Store.YES));

        doc.add(new TextField("address", fields.get(2), Field.Store.YES));

        ArrayList<String> tags = Lists.newArrayList(commaSplitter.split(fields.get(3)));
        for (String tag : tags) {
            doc.add(new StringField("tag", tag, Field.Store.YES));
        }

        double lon = Double.valueOf(fields.get(4));
        double lat = Double.valueOf(fields.get(5));
        if (Math.abs(lat) > 90.0) {
            double tmp = lon;
            lon = lat;
            lat = tmp;
        }
        doc.add(new StoredField("geo.lon", lon));
        doc.add(new StoredField("geo.lat", lat));
        Shape shape = spatialContext.makePoint(lon, lat);
        Field[] fieldsList = spatialStrategy.createIndexableFields(shape);
        for (IndexableField f : fieldsList) {
            doc.add(f);
        }

        return doc;
    }

    public void createIndex(String file) throws IOException {
        File indexDirectory = new File(this.indexPath);
        indexDirectory.deleteOnExit();

        appendIndex(file);
    }

    public void appendIndex(String file) throws IOException {
        BufferedReader br = Files.asCharSource(new File(file), Charsets.UTF_8).openBufferedStream();
        String line = null;
        while ((line = br.readLine()) != null) {
            indexer.add(stringToDocument(line));
        }
        br.close();
        indexer.close();
    }
}
