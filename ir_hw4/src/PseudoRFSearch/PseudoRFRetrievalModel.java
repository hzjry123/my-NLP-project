package PseudoRFSearch;

import Classes.Document;
import Classes.Query;
import IndexingLucene.MyIndexReader;
import SearchLucene.ExtractQuery;
import SearchLucene.QueryRetrievalModel;

import java.util.*;
//import Search.*;

public class PseudoRFRetrievalModel {

    static MyIndexReader ixreader;
    private static QueryRetrievalModel queryRetrievalModel;
    private static List<Document> feedback;
    private List result;
    private Queue doclist;
    private Map<Integer,Double> docScore;
    private Map<Integer,int[]> merList;
    //    private Map<Integer,Double> match;
    private ArrayList<double[]> docRank;

    public PseudoRFRetrievalModel(MyIndexReader ixreader) {
        this.ixreader = ixreader;
        queryRetrievalModel = new QueryRetrievalModel(ixreader);

    }


    /**
     * Search for the topic with pseudo relevance feedback in 2017 spring assignment 4.
     * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
     *
     * @param aQuery The query to be searched for.
     * @param TopN   The maximum number of returned document
     * @param TopK   The count of feedback documents
     * @param alpha  parameter of relevance feedback model
     * @return TopN most relevant document, in List structure
     */
    public List<Document> RetrieveQuery(Query aQuery, int TopN, int TopK, double alpha) throws Exception {
        // this method will return the retrieval result of the given Query, and this result is enhanced with pseudo relevance feedback
        // (1) you should first use the original retrieval model to get TopK documents, which will be regarded as feedback documents
        // (2) implement GetTokenRFScore to get each query token's P(token|feedback model) in feedback documents
        // (3) implement the relevance feedback model for each token: combine the each query token's original retrieval score P(token|document) with its score in feedback documents P(token|feedback model)
        // (4) for each document, use the query likelihood language model to get the whole query's new score, P(Q|document)=P(token_1|document')*P(token_2|document')*...*P(token_n|document')

        //get feedback with LuceneK
        feedback = queryRetrievalModel.retrieveQuery(aQuery, TopK);
        //get P(token|feedback documents)
        HashMap<String, Double> TokenRFScore = GetTokenRFScore(aQuery, TopK);

        double miu = 2000.0;
        merList = new HashMap<>();
        long cLength = ixreader.docnum();
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
         *  1			3                0               0
         *  5			7               1               0
         *  9			1               1               3
         *  13			9               0               0
         *
         */
        for (int i = 0; i < words.length; i++) {
            //get collection frequence
            double collectionFreq = ixreader.CollectionFreq(words[i]);
            if (collectionFreq != 0) {
                //get existing words in collection position.
                pos.add(i);
                int[][] postlist = ixreader.getPostingList(words[i]);
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
            double docLength = ixreader.docLength(id);
            for (int i : pos) {
                //smoothing
                //score = docFre/docLength;
                double collectionFreq = ixreader.CollectionFreq(words[i]);
                score *= (alpha * (freArray[i] + (miu * collectionFreq / cLength)) / (docLength + miu)+(1-alpha)*TokenRFScore.get(words[i]));
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
            Document document = new Document(String.valueOf(temp[0]), ixreader.getDocno((int) temp[0]), temp[1]);
            result.add(document);
        }
        return result;

        // sort all retrieved documents from most relevant to least, and return TopN
//        List<Document> results = new ArrayList<Document>();

//        return results;
    }

    public static HashMap<String, Double> GetTokenRFScore(Query aQuery, int TopK) throws Exception {
        // for each token in the query, you should calculate token's score in feedback documents: P(token|feedback documents)
        // use Dirichlet smoothing
        // save <token, score> in HashMap TokenRFScore, and return it
        HashMap<String, Double> TokenRFScore = new HashMap<String, Double>();
        double miu = 2000.0;
        long cLength = ixreader.docnum();
//        System.out.println(cLength);
        String[] words = aQuery.GetQueryContent().split(" ");
        for (String token : words) {
            double score = 0.0;
            double docFre = 0.0;
            double docLength = 0.0;
            double collectionFreq = ixreader.CollectionFreq(token);
            if (collectionFreq > 0) {
                int[][] postlist = ixreader.getPostingList(token);
                HashMap<Integer, Integer> postMap = new HashMap<Integer, Integer>();
                for (int i = 0; i < postlist.length; i++)
                    postMap.put(postlist[i][0], postlist[i][1]);
                //得到新的merge的document的freq
                for (Iterator<Document> it = feedback.iterator(); it.hasNext() ; ) {
                    Document feedback = it.next();
                    int docId = Integer.valueOf(feedback.docid());
                    if(postMap.containsKey(docId))
                        docFre += postMap.get(docId);
                    docLength += ixreader.docLength(docId);
                }
            }
            //smoothing
//            score = (docFre+ miu * collectionFreq/cLength)/(docLength+miu);
            score = (docFre + miu * collectionFreq / cLength)/(docLength + miu);

//            System.out.println(token +" "+ docFre+" " + score);
            TokenRFScore.put(token, score);
        }
        return TokenRFScore;
    }

    public static void main(String[] main) throws Exception {
        MyIndexReader ixreader = new MyIndexReader("trectext");
        PseudoRFRetrievalModel pseudoRFRetrievalModel = new PseudoRFRetrievalModel(ixreader);
        ExtractQuery queries = new ExtractQuery();
        Query query = queries.next();
        feedback = queryRetrievalModel.retrieveQuery(query, 20);
        GetTokenRFScore(query,2);
    }
}