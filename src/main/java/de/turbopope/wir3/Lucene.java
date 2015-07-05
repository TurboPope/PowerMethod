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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lucene {
    private static final String[] lines = new String[]{
            "Hello my name is Jonas",
            "This is a document",
            "Apple Apple Apple Banana",
            "Apple Banana",
            "Test document, pleas don't upvote",
            "DAE index their documents with lucene?"
    };
    private static int limit = 4;

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        //Directory directory = FSDirectory.open("/tmp/testindex");

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        for (String line : lines) {
            Document document = new Document();
            document.add(new Field("text", line, TextField.TYPE_STORED));
            indexWriter.addDocument(document);
            if (--limit == 0) {
                break;
            }
        }
        indexWriter.close();

        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser queryParser = new QueryParser("text", analyzer);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String in;
        System.out.print("Query: ");
        while ((in = br.readLine()) != null) {
            Query query = queryParser.parse(in);

            ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;

            for (ScoreDoc hit : hits) {
                Document hitDoc = indexSearcher.doc(hit.doc);
                System.out.println("hitDoc = " + hitDoc);
            }
            System.out.println();
            System.out.print("Query: ");
        }

        directoryReader.close();
        directory.close();
    }

    private static void readArticles(int limit) throws IOException {

    }
}
