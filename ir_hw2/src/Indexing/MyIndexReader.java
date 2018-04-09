package Indexing;

import Classes.Path;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class MyIndexReader {
    //you are suggested to write very efficient code here, otherwise, your memory cannot hold our corpus...

    //initiate path
    private static FileInputStream dictionary_fis;
    private static FileInputStream id_no_fis;
    private static FileInputStream post_list_fis;
    private static BufferedReader dictionary_br;
    private static BufferedReader id_no_br;
    private static BufferedReader post_list_br;

    //initiate LinkedHashMap to ensure the order is same as file.
    private static LinkedHashMap<String, Long> dictionary_map;
    //easy to get docno according to docID
    private static LinkedHashMap<Integer, String> id_no_map;
    //easy to get docID according to docno
    private static LinkedHashMap<String, Integer> no_id_map;

    private static String cur_path;
    public MyIndexReader(String type) throws IOException {
        //read the index files you generated in task 1
        //remember to close them when you finish using them
        //use appropriate structure to store your index

        //initiate map
        dictionary_map = new LinkedHashMap<>();
        id_no_map = new LinkedHashMap<>();
        no_id_map = new LinkedHashMap<>();

        //get path
        cur_path = type.equals("trecweb") ? Path.IndexWebDir : Path.IndexTextDir;
        dictionary_fis = new FileInputStream(cur_path + "dictionary");
        id_no_fis = new FileInputStream(cur_path + "id_no");
        post_list_fis = new FileInputStream(cur_path + "post_list_merge");
        dictionary_br = new BufferedReader(new InputStreamReader(dictionary_fis));
        id_no_br = new BufferedReader(new InputStreamReader(id_no_fis));
        post_list_br = new BufferedReader(new InputStreamReader(post_list_fis));

        //input doc id and no store in no_id_map<no,id>
        //input doc no and id store in id_no_map<id,no>
        String line = id_no_br.readLine();
        while (line != null) {
            String[] inputs = line.split(" ");
            no_id_map.put(inputs[1], Integer.valueOf(inputs[0]));
            id_no_map.put(Integer.valueOf(inputs[0]), inputs[1]);
            line = id_no_br.readLine();
        }

        //input dictionary in dictionary_map
        line = dictionary_br.readLine();
        while (line != null) {
            String[] inputs = line.split(" ");
            dictionary_map.put(inputs[0], Long.valueOf(inputs[1]));
            line = dictionary_br.readLine();
        }

    }

    //get the non-negative integer dociId for the requested docNo
    //If the requested docno does not exist in the index, return -1
    public static int GetDocid(String docno) {
        //get id from no_id_map <String no:int id>
        if (no_id_map.containsKey(docno)) {
            return no_id_map.get(docno);
        } else return -1;
    }

    // Retrieve the docno for the integer docid
    public static String GetDocno(int docid) {
        //get id from id_no_map <int id : String no>
        if (id_no_map.containsKey(docid)) {
            return id_no_map.get(docid);
        } else return null;
    }



    // Return the total number of times the token appears in the collection.
    public static long GetCollectionFreq(String token) throws IOException {
        //collection frequency from dictionary <term,frequency>
        if (dictionary_map.containsKey(token)) {
            return dictionary_map.get(token);
        }
        return 0;
    }

    /**
     * Get the posting list for the requested token.
     * <p>
     * The posting list records the documents' docids the token appears and corresponding frequencies of the term, such as:
     * <p>
     * [docid]		[freq]
     * 1			3
     * 5			7
     * 9			1
     * 13			9
     * <p>
     * ...
     * <p>
     * In the returned 2-dimension array, the first dimension is for each document, and the second dimension records the docid and frequency.
     * <p>
     * For example:
     * array[0][0] records the docid of the first document the token appears.
     * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
     * ...
     * <p>
     * NOTE that the returned posting list array should be ranked by docid from the smallest to the largest.
     *
     * @param token
     * @return
     */

    public static int[][] GetPostingList(String token) throws IOException {
        if (!dictionary_map.containsKey(token)) return null;
        //set temporary FileInputStream and buffer reader
        FileInputStream fis = new FileInputStream(cur_path + "post_list_merge");
        BufferedReader temp = new BufferedReader(new InputStreamReader(fis));
        //get the index of token in dictionary map
        int index = new ArrayList<String>(dictionary_map.keySet()).indexOf(token);
        //get the post whose index is same as dictionary index(put into file in same order)
        int i = 0;
        String line = temp.readLine();
        while (i < index && line != null) {
            line = temp.readLine();
            i++;
        }
        //split the post, term 1:2 2:2 3:3
        String[] fres = line.split("\\s+");
        int[][] result = new int[fres.length - 1][2];
        for (int j = 1; j < fres.length; j++) {
            //term 1:2 2:2 3:3 docId:frequency
            result[j - 1][0] = Integer.valueOf(fres[j].split(":")[0]);
            result[j - 1][1] = Integer.valueOf(fres[j].split(":")[1]);
        }
        temp.close();
        fis.close();
        return result;
    }

    // Return the number of documents that contains the token.
    public static int GetDocFreq(String token) throws IOException {
        //set temporary FileInputStream and buffer reader
        FileInputStream fis = new FileInputStream(cur_path + "post_list_merge");
        BufferedReader temp = new BufferedReader(new InputStreamReader(fis));
        if(!dictionary_map.containsKey(token))return 0;
        //get the index of token in dictionary map
        int index = new ArrayList<String>(dictionary_map.keySet()).indexOf(token);
        //get the post whose index is same as dictionary index(put into file in same order)
        int i = 0;
        String line = temp.readLine();
        while (i < index && line != null) {
            line = temp.readLine();
            i++;
        }
        //split the post, term 1:2 2:2 3:3
        String[] fres = line.split("\\s+");
        temp.close();
        fis.close();
        //except the term, rest string number is docs' number
        return fres.length - 1;
    }
    //close all buffer reader and input stream
    public void Close() throws IOException {
        dictionary_br.close();
        dictionary_fis.close();
        id_no_br.close();
        id_no_fis.close();
        post_list_br.close();
        post_list_fis.close();
    }

    public static void main(String[] arg) throws IOException {
        MyIndexReader ixreader=new MyIndexReader("trectext");

        // conduct retrieval
        String token ="yhoo";
        int df = ixreader.GetDocFreq(token);
        long ctf = ixreader.GetCollectionFreq(token);
        System.out.println(" >> the token \""+token+"\" appeared in "+df+" documents and "+ctf+" times in total");
        if(df>0){
            int[][] posting = ixreader.GetPostingList(token);
            for(int ix=0;ix<posting.length;ix++){
                int docid = posting[ix][0];
                int freq = posting[ix][1];
                String docno = ixreader.GetDocno(docid);
                System.out.printf("    %20s    %6d    %6d\n", docno, docid, freq);
            }
        }
        ixreader.Close();
    }
}