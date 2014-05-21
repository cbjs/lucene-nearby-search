package com.codetodeath.search.searcher;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spatial4j.core.distance.DistanceUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.List;

/**
 * A test searcher
 * Created by dengguo on 14-5-21.
 */
public class TextFileSearcher {
    private NearbySearcher searcher;
    private String indexPath;

    public TextFileSearcher(String indexPath) throws IOException {
        this.searcher = new NearbySearcher(indexPath);
        this.indexPath = indexPath;
    }

    public List<String> search(String queryString, double lon, double lat, double km, int num) throws IOException {
        List<String> result = Lists.newLinkedList();
        Analyzer analyzer = new MMSegAnalyzer();
        QueryParser parser = new QueryParser(Version.LUCENE_48, "name", analyzer);

        Query query = null;
        try {
            query = parser.parse(queryString);
        } catch (Exception e) {
            query = new MatchAllDocsQuery();
        }
        List<Document> docs = searcher.search(query, num, lon, lat, km);
        for (Document doc : docs) {
            result.add(documentToString(doc, lon, lat));
        }
        return result;
    }

    public String documentToString(Document doc, double x, double y) {
        StringBuffer sb = new StringBuffer();
        sb.append(doc.get("name"));
        sb.append(", ");
        sb.append(doc.get("address"));
        sb.append(", ");
        sb.append(Joiner.on("|").skipNulls().join(doc.getValues("tag")));
        sb.append(", (");
        double lon = doc.getField("geo.lon").numericValue().doubleValue();
        sb.append(lon);
        sb.append(",");
        double lat = doc.getField("geo.lat").numericValue().doubleValue();
        sb.append(lat);
        sb.append(")");
        sb.insert(0, " ");
        sb.insert(0, calDistance(x, y, lon, lat));
        return sb.toString();
    }

    public double calDistance(double x, double y, double lon, double lat) {
        return DistanceUtils.degrees2Dist(DistanceUtils.distHaversineRAD(x,y,lon,lat), DistanceUtils.EARTH_MEAN_RADIUS_KM);
    }
}
