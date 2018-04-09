package PreProcessData;

import Classes.Path;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is for INFSCI 2140 in 2018
 */
public class TrectextCollection implements DocumentCollection {
    //you can add essential private methods or variables
    //my file input stream
    private FileInputStream fis;
    //my buffer reader read from input stream
    private BufferedReader br;

    // YOU SHOULD IMPLEMENT THIS METHOD
    public TrectextCollection() throws IOException {
        // This constructor should open the file in Path.DataTextDir
        // and also should make preparation for function nextDocument()
        // you cannot load the whole corpus into memory here!!
        try {
            //get path
            fis = new FileInputStream(Path.DataTextDir);
            //get buffer reader
            br = new BufferedReader(new InputStreamReader(fis));
        } catch (IOException e1) {
            Logger.getLogger(TrectextCollection.class.getName()).log(Level.SEVERE, null, e1);
        }

    }

    public static void main(String[] main) throws IOException {
        TrectextCollection test = new TrectextCollection();
        Map<String, Object> map = test.nextDocument();
        for (String docno : map.keySet()) {
            System.out.println(docno);
            System.out.println(map.get(docno).toString());
        }
    }

    // YOU SHOULD IMPLEMENT THIS METHOD
    public Map<String, Object> nextDocument() throws IOException {
        // this method should load one document from the corpus, and return this document's number and content.
        // the returned document should never be returned again.
        // when no document left, return null
        // NTT: remember to close the file that you opened, when you do not use it any more
        Map<String, Object> document = new HashMap<>();
        String line = br.readLine();
        String content = "", key = "";
        if (line != null) {
            try {
                //start from doc
                while (line != null && !line.equals("<DOC>")) {
                    line = br.readLine();
                }
                //get key docno.
                while (line != null && !(line.startsWith("<DOCNO>") && line.endsWith("</DOCNO>"))) {
                    line = br.readLine();
                }
                //have read all file
                if (line == null) {
                    //close file input stream and buffer reader
                    br.close();
                    fis.close();
                    return null;
                } else {
                    //delete docno html tag, get key.
                    key = line.replaceAll("\\<.*?\\>", "").toString();
                    //get text
                    while (line != null && !line.equals("<TEXT>")) {
                        line = br.readLine();
                    }
                    while (line != null && !line.equals("</TEXT>")) {
                        content += line + " ";
                        line = br.readLine();
                    }
                    //put <key,value> key and content into Map<> document
                    document.put(key, content.toCharArray());

                }
            } catch (IOException e1) {
                //print error in the log
                Logger.getLogger(TrectextCollection.class.getName()).log(Level.SEVERE, null, e1);
            }
        }
        else {
            //close file input stream and buffer reader
            br.close();
            fis.close();
            return null;
        }
        return document;
    }
}
