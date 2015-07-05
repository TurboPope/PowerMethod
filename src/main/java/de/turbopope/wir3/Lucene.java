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

import java.io.*;

public class Lucene {
    private static int limit = -1;

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        //Directory directory = FSDirectory.open("/tmp/testindex");

        System.out.println("Building index...");
        long ping = System.currentTimeMillis();
        BufferedReader articleReader = new BufferedReader(new FileReader(new File("ARTICLES.txt")));
        String line;
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        while((line = articleReader.readLine()) != null) {
            String[] splitted = line.split("\\t");

            try {
                String title = splitted[0];
                String body = splitted[1];

                Document document = new Document();
                document.add(new Field("body", body, TextField.TYPE_STORED));
                document.add(new Field("title", title, TextField.TYPE_STORED));
                indexWriter.addDocument(document);
                if (limit != -1 &&-- limit == 0) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // TODO: Why are so many articles broken?
            }
        }
        indexWriter.close();
        System.out.printf("%nBuilt index in %sms%n%n", System.currentTimeMillis() - ping);

        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser queryParser = new QueryParser("body", analyzer);
        BufferedReader stdReader = new BufferedReader(new InputStreamReader(System.in));

        String in;
        System.out.print("Query: ");
        while (!(in = stdReader.readLine()).equals("")) {
            Query query = queryParser.parse(in);

            ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;

            int c = 1;
            for (ScoreDoc hit : hits) {
                Document hitDoc = indexSearcher.doc(hit.doc);
                System.out.printf("%s: %s%n", c++, hitDoc.get("title"));
                if (c > 10) {
                    break;
                }
            }
            System.out.println(hits.length + " results.");
            System.out.println();
            System.out.print("Query: ");
        }

        directoryReader.close();
        directory.close();
    }
}
