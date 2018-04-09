package Indexing;

import Classes.Path;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PreProcessedCorpusReader {

    private static FileInputStream fis;
    private static BufferedReader br;

    public PreProcessedCorpusReader(String type) throws IOException {
        // This constructor opens the pre-processed corpus file, Path.ResultHM1 + type
        // You can use your own version, or download from http://crystal.exp.sis.pitt.edu:8080/iris/resource.jsp
        // Close the file when you do not use it any more
        try {
            fis = new FileInputStream(Path.ResultHM1 + type);
            br = new BufferedReader(new InputStreamReader(fis));//input should be a reader
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    public Map<String, Object> NextDocument() throws IOException {
        // read a line for docNo and a line for content, put into the map with <docNo, content>
        Map<String, Object> next_document = null;
        String docNO = br.readLine();
        if (docNO != null) {
            next_document = new HashMap<>();
            String content = br.readLine();
            if (content != null) next_document.put(docNO, content.toCharArray());
        }
        return next_document;
    }

    public static void main(String[] args) {
        try {
            PreProcessedCorpusReader test = new PreProcessedCorpusReader("trecweb");

            for (int i = 0; i < 5; i++) {
                Map<String, Object> doc = test.NextDocument();
                String docNo = doc.keySet().iterator().next();
                System.out.println(docNo + " " + doc.get(docNo));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
