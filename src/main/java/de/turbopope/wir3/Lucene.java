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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.*;
import java.util.*;

public class Lucene {
    private static int ARTICLE_LIMIT = -1; // Set to -1 fo no limit
    private static int RESULTS_LIMIT = 10;

    public static void main(String[] args) throws IOException, ParseException {
        // Build the index
        System.out.println("Building index...");
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();

        long ping = System.currentTimeMillis();
        BufferedReader articleReader = new BufferedReader(new FileReader(new File("ARTICLES.txt")));
        String articleLine;
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        while((articleLine = articleReader.readLine()) != null) {
            String[] splitted = articleLine.split("\\t");

            try {
                String title = splitted[0];
                String body = splitted[1];

                Document document = new Document();
                document.add(new Field("body", body, TextField.TYPE_STORED));
                document.add(new Field("title", title, TextField.TYPE_STORED));
                indexWriter.addDocument(document);
                if (ARTICLE_LIMIT != -1 &&--ARTICLE_LIMIT == 0) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // TODO: Why are so many articles broken?
            }
        }
        indexWriter.close();
        System.out.printf("%nBuilt index in %sms%n%n", System.currentTimeMillis() - ping);


        // Get the page ranks
        System.out.println("Reading page ranks...");
        ping = System.currentTimeMillis();
        BufferedReader ranksReader = new BufferedReader(new FileReader(new File("RANKING")));
        String rankLine;
        ArrayList<String> pageRanks = new ArrayList<String>();
        while((rankLine = ranksReader.readLine()) != null) {
            pageRanks.add(rankLine);
        }
        System.out.printf("%nRead page ranks in %sms%n%n", System.currentTimeMillis() - ping);


        // Prepare querying
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser queryParser = new QueryParser("body", analyzer);
        BufferedReader stdReader = new BufferedReader(new InputStreamReader(System.in));


        // Let the user query
        String in;
        System.out.print("Query: ");
        while (!(in = stdReader.readLine()).equals("")) {
            Query query = queryParser.parse(in);

            TopDocs topDocs = indexSearcher.search(query, RESULTS_LIMIT);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//            for (ScoreDoc scoreDoc : scoreDocs) {
//                System.out.println(indexSearcher.doc(scoreDoc.doc));
//            }

            TreeSet<QueryResult> result = new TreeSet<QueryResult>();
            for (ScoreDoc scoreDoc : scoreDocs) {
                result.add(new QueryResult(scoreDoc.score, indexSearcher.doc(scoreDoc.doc).get("title").replaceAll("\\s", "_")));
            }

            HashMap<String, Integer> localPageRanks = getLocalPageRanks(result, pageRanks);
            for (QueryResult queryResult : result) {
                try {
                    queryResult.score *= localPageRanks.get(queryResult.title);
                } catch (NullPointerException e) {
                    System.err.println(queryResult.title + " was not found in page ranks. Assuming rank 1.");
                }
            }

            for (QueryResult queryResult : sort(result)) {
                System.out.println(queryResult);
            }

            System.out.println();
            System.out.print("Query: ");
        }

        directoryReader.close();
        directory.close();
    }

    private static HashMap<String, Integer> getLocalPageRanks(TreeSet<QueryResult> queryResults, ArrayList<String> pageRanks) {
        HashMap<String, Integer> localPageRanks = new HashMap<String, Integer>();
        Collections.reverse(pageRanks);

        ArrayList<String> titles = new ArrayList<String>();
        for (QueryResult queryResult : queryResults) {
            titles.add(queryResult.title);
        }

        int i = titles.size();
        for (String pageRank : pageRanks) {
            if (titles.contains(pageRank)) {
                localPageRanks.put(pageRank, i--);
            }
        }

        return localPageRanks;
    }

    private static class QueryResult implements Comparable<QueryResult> {
        public float score;
        public String title;

        public QueryResult(float score, String title) {
            this.score = score;
            this.title = title;
        }

        public int compareTo(QueryResult other) {
            if (this.score > other.score) {
                return -1;
            } else if (this.score == other.score) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public String toString() {
            return score + ": " + title;
        }
    }

    public static TreeSet<QueryResult> sort(TreeSet<QueryResult> toStort) {
        TreeSet<QueryResult> sorted = new TreeSet<QueryResult>();
        for (QueryResult queryResult : toStort) {
            sorted.add(queryResult);
        }
        return sorted;
    }
}
