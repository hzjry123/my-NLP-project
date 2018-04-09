package PseudoRFSearch;

import Classes.Document;
import Classes.Query;
import IndexingLucene.MyIndexReader;

import java.io.IOException;
import java.util.*;

public class ref {
    protected MyIndexReader indexReader;
    private Queue doclist;
    private List result;
    private Map<Integer,Double> docScore;
    private Map<Integer,int[]> merList;
    //    private Map<Integer,Double> match;
    private ArrayList<double[]> docRank;

    int cLength = 0;
    public List<Document> retrieveQuery(Query aQuery, int TopN) throws IOException {
        // NT: you will find our IndexingLucene.Myindexreader provides method: docLength()
        // implement your retrieval model here, and for each input query, return the topN retrieved documents
        // sort the docs based on their relevance score, from high to low
        double miu = 2000.0;
        merList = new HashMap<>();
        long cLength = indexReader.docnum();
        doclist = new PriorityQueue();
        docScore = new HashMap<Integer, Double>();
        docRank = new ArrayList<>();
        result = new LinkedList();
        String[] words = aQuery.GetQueryContent().split(" ");
        //get words in collection position.
        ArrayList<Integer> pos = new ArrayList<>();
        //merge all the postlists
        /**
         * [docid]		[word1 freq] [word2 freq] [word3 freq]
         *  1			3               0               0
         *  5			7               1               0
         *  9			1               1               3
         *  13			9               0               0
         *
         */
        for (int i = 0; i < words.length; i++) {
            //get collection frequence
            double collectionFreq = indexReader.CollectionFreq(words[i]);
            if (collectionFreq != 0) {
                //get existing words in collection position.
                pos.add(i);
                int[][] postlist = indexReader.getPostingList(words[i]);
                for (int[] info : postlist) {
                    int id = info[0];
                    int fre = info[1];
                    //put new post list into merList, if already have id, just change the list.
                    int[] temp = merList.containsKey(id) ? merList.get(id) : new int[words.length];
                    temp[i] = fre;
                    merList.put(id, temp);
                }
            }
        }
        for (int id : merList.keySet()) {
            int[] freArray = merList.get(id);
            double score = 1;
            double docLength = indexReader.docLength(id);
            for (int i : pos) {
                //smoothing
                //score = docFre/docLength;
                double collectionFreq = indexReader.CollectionFreq(words[i]);
                score *= (freArray[i] + (miu * collectionFreq / cLength)) / (docLength + miu);
            }
            docRank.add(new double[]{id, score});
        }
        //comparator
        Comparator<double[]> valueComparator = new Comparator<double[]>() {
            public int compare(double[] o1,
                               double[] o2) {
                return o1[1] - o2[1] < 0 ? 1 : -1;
            }
        };
        //sort result
        Collections.sort(docRank, valueComparator);
        for (int i = 0; i < docRank.size() && i < TopN; i++) {
            double[] temp = docRank.get(i);
            Document document = new Document(String.valueOf(temp[0]), indexReader.getDocno((int) temp[0]), temp[1]);
            result.add(document);
        }
        return result;
    }

}
