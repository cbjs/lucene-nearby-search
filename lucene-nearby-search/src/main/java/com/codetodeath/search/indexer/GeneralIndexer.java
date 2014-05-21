package com.codetodeath.search.indexer;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * A general indexer
 * Created by dengguo on 14-5-21.
 */
public class GeneralIndexer {
    private IndexWriterConfig writerConfig;
    private IndexWriter indexWriter;
    private Directory indexDir;
    private Analyzer analyzer;
    private static GeneralIndexer indexer;
    private String indexPath;

    public GeneralIndexer(String indexPath) throws IOException {
        this.indexPath = indexPath;
        indexDir = FSDirectory.open(new File(indexPath));
        analyzer = new MMSegAnalyzer();
        writerConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writerConfig.setRAMBufferSizeMB(256.0);
        indexWriter = new IndexWriter(indexDir, writerConfig);
    }

    public void close() throws IOException {
        indexWriter.close();
    }

    public void add(Document doc) throws IOException {
        if (null == doc) return;
        indexWriter.addDocument(doc);
    }

    public void update(Term term, Document doc) throws IOException {
        if (null == doc) return;
        indexWriter.updateDocument(term, doc);
    }
}
