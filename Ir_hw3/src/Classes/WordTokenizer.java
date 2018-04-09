package Classes;

/**
 * This is for INFSCI 2140 in 2018
 * <p>
 * TextTokenizer can split a sequence of text into individual word tokens.
 */

public class WordTokenizer {
    //you can add essential private methods or variables
    private String[] words;
    //words position
    private int pos = 0;

    // YOU MUST IMPLEMENT THIS METHOD
    public WordTokenizer(char[] texts) {
        // this constructor will tokenize the input texts (usually it is a char array for a whole document)
        String token = new String(texts);
        //use regular expression to get only a-z and A-z and \\pZ for space or \n
        token = token.replaceAll("[^a-zA-Z'\\pZ]", " ");
        //split String token into array by use many spaces and \n
        words = token.split("[\\s]+");
    }

    // YOU MUST IMPLEMENT THIS METHOD
    public char[] nextWord() {
        // read and return the next word of the document
        // or return null if it is the end of the document
        if (pos < words.length) {
            //return the word[pos].
            return words[pos++].toCharArray();
        } else return null;
    }

}
