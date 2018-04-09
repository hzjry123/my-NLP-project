package Indexing;

import Classes.Path;

import java.io.*;
import java.util.*;

public class MyIndexWriter {
    // I suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...
    /*
    Map: Dict<term,info{fre,Post_ID}> ,
	Map: Post(term,{<Doc_ID,fre>,<Doc_ID,fre>}).
	*/
    private static FileWriter dict;
    private static FileWriter id_no;
    private static FileWriter post_merge;
    private static FileWriter post_block;
    //set result path
    private static String Result = "";
    //set map for post_block Post(term,{<Doc_ID,fre>,<Doc_ID,fre>})
    private static Map<String, ArrayList<long[]>> post_block_map;
    //set map for dictionary
    private static LinkedHashMap<String, Long> dict_map;
    //set ArrayList for blocks' buffer reader and file path
    ArrayList<FileInputStream> fises;
    ArrayList<BufferedReader> brs;
    ArrayList<File> blocks;
    //mark current doc ID and post ID
    private long cur_doc_ID = 0;
    private long cur_post_ID = 0;
    //mark block number
    private int block = 0;
    //set Map for doc id and no
    private Map<Long, String> doc_id_no = new HashMap<>();

    public MyIndexWriter(String type) throws IOException {
        // This constructor should initiate the FileWriter to output your index files
        // remember to close files if you finish writing the index

        //initiate map.
        doc_id_no = new LinkedHashMap<>();
        post_block_map = new LinkedHashMap<>();
        dict_map = new LinkedHashMap<>();
        //make dir for result
        Result = type.equals("trecweb") ? Path.IndexWebDir : Path.IndexTextDir;
        File file = new File(Result);
        file.mkdir();

        //initiate file writer for some result
        post_merge = new FileWriter(Result + "post_list_merge");
        id_no = new FileWriter(Result + "id_no");
        dict = new FileWriter(Result + "dictionary");
    }

    public void IndexADocument(String docno, char[] content) throws IOException {
        // you are strongly suggested to build the index by installments
        // you need to assign the new non-negative integer docId to each document, which will be used in MyIndexReader

        //if post_block_map size == 30000,
        //then read previous file and merge current post_block_map and write to file.
        //last clear the map.

        //assign doc_id doc_no pair
        long doc_id = cur_doc_ID++;
        //set dict of doc_id and doc_no
        doc_id_no.put(doc_id, docno);

        //deal with content
        index_content(content);
        if ((cur_doc_ID + 1) % 30000 == 0) {
            //initiate block path
            post_block = new FileWriter(Result + block);
            write_block();
            post_block_map.clear();
            post_block_map = new HashMap();
            post_block.close();
        }
    }

    /*
    Map: Dict<term,Dict_id,info{fre,Post_ID}> ,
    Map: Post(term,Post_id,Dict_ID,{<Doc_ID,fre>,<Doc_ID,fre>}).
    */
    private void index_content(char[] content) {
        //user StringTokenizer to get String
        StringTokenizer stringTokenizer = new StringTokenizer(new String(content));
        while (stringTokenizer.hasMoreTokens()) {
            String term = stringTokenizer.nextToken();
            //if dictionary doesn't contain term, put the term into it and set frequency as 0
            if (!dict_map.containsKey(term)) {
                dict_map.put(term, Long.valueOf("0"));
            }
            //if post doesn't contain current index, put it in and set frequency as 1.
            if (!post_block_map.containsKey(term)) {
                //
                ArrayList<long[]> post_info = new ArrayList<>();
                post_info.add(new long[]{cur_doc_ID, 1});
                post_block_map.put(term, post_info);
            }
            //else if post contain current index, if it is in same document:cur doc id = doc_id,add fre by 1.
            else {
                ArrayList<long[]> temp_list = post_block_map.get(term);
                //if current doc_id have been recorded into ArrayList, it must be in last of ArrayList
                long[] array = temp_list.get(temp_list.size() - 1);
                //if last is current doc, add the frequency by 1.if not just put it in.
                if (array[0] == cur_doc_ID) {
                    array[1] = array[1] + 1;
                } else {
                    temp_list.add(new long[]{cur_doc_ID, 1});
                }
            }
        }
    }

    //write block into disk
    private void write_block() {
        //write the block according the dictionary order,
        //so all blocks are in same order, it is easy to merge blocks
        for (String term : dict_map.keySet()) {
            try {
                //if current block has the term in dictionary, just write it's posting list into file
                //else just write no term into file
                if (post_block_map.containsKey(term)) {
                    int fre = 0;
                    ArrayList<long[]> temp_list = post_block_map.get(term);
                    for (long[] array : temp_list) {
                        //calculate term's frequency of this block
                        fre += array[1];
                        post_block.append(array[0] + ":" + array[1] + " ");
                    }
                    //renew frequency.
                    dict_map.put(term, dict_map.get(term) + fre);
                } else {
                    post_block.append("no term");
                }
                post_block.append("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        block++;
    }

    public void Close() throws IOException {
        // close the index writer, and you should output all the buffered content (if any).
        // if you write your index into several files, you need to fuse them here.

        //write rest block into file
        post_block = new FileWriter(Result + block);
        write_block();
        post_block_map.clear();
        post_block_map = new HashMap();
        post_block.close();

        //write doc_id_no
        for (long doc_id : doc_id_no.keySet()) {
            try {
                id_no.append(doc_id + " " + doc_id_no.get(doc_id) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        id_no.close();

        //write dict
        for (String term : dict_map.keySet()) {
            dict.append(term + " " + dict_map.get(term) + "\n");
        }
        dict.close();

        //merge all post
        //initiate all block paths
        fises = new ArrayList<>();
        brs = new ArrayList<>();
        blocks = new ArrayList<>();
        for (int i = 0; i < block; i++) {
            fises.add(new FileInputStream(Result + i));
            brs.add(new BufferedReader(new InputStreamReader(fises.get(i))));
            blocks.add(new File(Result + i));
        }

        //merge post
        for (String term : dict_map.keySet()) {
            //first all files are in same order.
            //some block may not have such term, just skip.
            //if one block has run out, remove it from array and delete that block.
            post_merge.append(term + " ");
            for (int i = 0; i < brs.size(); i++) {
                String line = brs.get(i).readLine();
                if (line == null) {
                    //if one block has run out, remove it from array.
                    delete(i);
                    //and keep i not change
                    i--;
                }
                //if one block has the term write it into post_merge.
                else if (!line.equals("no term")) {
                    post_merge.append(line + " ");
                }
            }
            post_merge.append("\n");
        }
        //delete rest block
        if (brs.size() == 1) {
            delete(0);
        }
        post_merge.close();
    }
    //remove block from list and delete it from disk.
    private void delete(int i) throws IOException {
        fises.get(i).close();
        fises.remove(i);
        brs.get(i).close();
        brs.remove(i);
        //delete the block from disk
        blocks.get(i).delete();
        blocks.remove(i);
    }

    public static void main(String[] args) throws IOException {
        String dataType = "trectext";
        PreProcessedCorpusReader corpus = new PreProcessedCorpusReader(dataType);

        // initiate the output object
        MyIndexWriter output = new MyIndexWriter(dataType);

        // initiate a doc object, which will hold document number and document content
        Map<String, Object> doc = null;

        int count = 0;
        // build index of corpus document by document
        while ((doc = corpus.NextDocument()) != null) {
            // load document number and content of the document
            String docno = doc.keySet().iterator().next();
            char[] content = (char[]) doc.get(docno);

            // index this document
            output.IndexADocument(docno, content);
            count++;
            if (count % 30000 == 0)
                System.out.println("finish " + count + " docs");
        }
        System.out.println("totaly document count:  " + count);
        output.Close();
    }
}
