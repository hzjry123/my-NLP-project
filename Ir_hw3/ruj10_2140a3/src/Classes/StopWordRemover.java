package Classes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StopWordRemover {
    //you can add essential private methods or variables.
    //use hashSet to save stopword
    private HashSet<String> stop_Word;
    //file reader
    private BufferedReader br;
    private FileInputStream fis;

    public StopWordRemover() throws IOException {
        // load and store the stop words from the fileinputstream with appropriate data structure
        // that you believe is suitable for matching stop words.
        // address of stopword.txt should be Path.StopwordDir
        stop_Word = new HashSet<>();
        // line is everyone stop word in stopword.txt.
        String line;
        try {
            //get path
            fis = new FileInputStream(Path.StopwordDir);
            //get buffer
            br = new BufferedReader(new InputStreamReader(fis));
            line = br.readLine();
            while (line!=null) {
                //add stop word
                stop_Word.add(line);
                line = br.readLine();
            }
        }catch (IOException e) {
            System.out.println(e);
        }finally {
            br.close();
            fis.close();
        }
    }

    // YOU MUST IMPLEMENT THIS METHOD
    public boolean isStopword(char[] word) {
        // return true if the input word is a stopword, or false if not
        if(stop_Word.contains(String.valueOf(word))) return true;
        return false;
    }
}
