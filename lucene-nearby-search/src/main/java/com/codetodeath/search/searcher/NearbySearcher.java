package com.codetodeath.search.searcher;

import com.google.common.collect.Lists;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.FSDirectory;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A nearby searcher
 * Created by dengguo on 14-5-21.
 */
public class NearbySearcher {
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private String indexPath;

    private SpatialContext spatialContext;
    private SpatialStrategy spatialStrategy;

    public NearbySearcher(String indexPath) throws IOException {
        this.indexPath = indexPath;
        this.spatialContext = SpatialContext.GEO;
        SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, 11);
        spatialStrategy = new RecursivePrefixTreeStrategy(grid, "geo");

        indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        indexSearcher = new IndexSearcher(indexReader);
    }

    public List<Document> search(Query query, int num, double lon, double lat, double km) throws IOException {
        return search(query, num, km, spatialContext.makePoint(lon, lat));
    }

    public List<Document> search(Query query, int num, double radius, Point center) throws IOException {
        // circle filter
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
                spatialContext.makeCircle(center.getX(), center.getY(), DistanceUtils.dist2Degrees(radius, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
        Filter filter = spatialStrategy.makeFilter(args);

        // sort by distance
        ValueSource valueSource = spatialStrategy.makeDistanceValueSource(center, DistanceUtils.DEG_TO_KM);
        Sort distSort = new Sort(valueSource.getSortField(false).rewrite(indexSearcher));

        // search with filter and sort
        TopDocs docs = indexSearcher.search(query, filter, num, distSort);

        ArrayList<Document> result = Lists.newArrayListWithExpectedSize(Math.min(num, docs.scoreDocs.length));
        for (int i = 0; i < docs.scoreDocs.length; ++i) {
            Document doc = indexSearcher.doc(docs.scoreDocs[i].doc);
            result.add(doc);
        }
        return result;
    }
}