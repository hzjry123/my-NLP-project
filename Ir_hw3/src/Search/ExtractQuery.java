package Search;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import Classes.*;

public class ExtractQuery {
    //new query list
    private static Queue<Query> queryList;
    //new file input steam
    private static FileInputStream fileInputStream;
    private static BufferedReader bufferedReader;
    private static StopWordRemover stopWordRemover;
	public ExtractQuery()throws IOException {
		//you should extract the 4 queries from the Path.TopicDir = data//topics.txt
		//NT: the query content of each topic should be 1) tokenized, 2) to lowercase, 3) remove stop words, 4) stemming
		//NT: you can simply pick up title only for query, or you can also use title + description + narrative for the query content.
		queryList = new LinkedList<>();
		Query query = new Query();
        stopWordRemover = new StopWordRemover();
        Stemmer s = new Stemmer();
        fileInputStream = new FileInputStream(Path.TopicDir);
        bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String readLine = bufferedReader.readLine();
        StringTokenizer stringTokenizer;
        String content="";
        while(readLine != null){
            if(readLine.startsWith("<num>")){
                query = new Query();
                query.SetTopicId(readLine.split(" ")[2]);
                content="";
            }
            if(readLine.startsWith("<title>")){
                //get char array of title
                //1) tokenized
                WordTokenizer tokenizer = new WordTokenizer(readLine.replaceAll("\\<.*?\\>"," ").toCharArray());
                char[] word;
                while ((word=tokenizer.nextWord())!=null){
                    //2) to lowercase
                    lowercase(word);
                    s.add(word, word.length);
                    s.stem();
                    //3) remove stop words
                    if(!stopWordRemover.isStopword(word)){
                        //4) stem
                        content += String.valueOf(s) + " ";
                    }
                }
                query.SetQueryContent(content);
                queryList.add(query);
            }
            readLine = bufferedReader.readLine();
        }
        bufferedReader.close();
        fileInputStream.close();
    }

	public boolean hasNext()
	{
        if(!queryList.isEmpty()){
            return true;
        }
	    return false;
	}
	
	public Query next()
	{
	    // poll query
        if(!queryList.isEmpty()){
            return queryList.poll();
        }
	    return null;
	}
    public char[] lowercase(char[] chars) {
        //transform the uppercase characters in the word to lowercase
        for (int i = 0; i < chars.length; i++)
            //if it is a uppercase
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                //change it to a lowercase
                chars[i] = (char) (chars[i] - 'A' + 'a');
            }
        return chars;
    }
}
