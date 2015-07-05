package de.turbopope.wir3;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class Main {
    private static final String[] lines = new String[]{
            "Hello my name is Jonas",
            "This is a document",
            "Apple Apple Apple Banana",
            "Apple Banana",
            "Test document, pleas don't upvote",
            "DAE index their documents with lucene?"
    };

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        //Directory directory = FSDirectory.open("/tmp/testindex");
        IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));

        for (String line : lines) {
            Document document = new Document();
            document.add(new Field("text", line, TextField.TYPE_STORED));
            iwriter.addDocument(document);
        }
        iwriter.close();

        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("text", analyzer);
        Query query = parser.parse("Apple");

        ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;

        for (ScoreDoc hit : hits) {
            Document hitDoc = isearcher.doc(hit.doc);
            System.out.println("hitDoc = " + hitDoc);
        }

        ireader.close();
        directory.close();
    }


}
