/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Beckett Porter
 */
public class TextCompressor
{
    // 254 most common words (255 is escapeChar)
    private static final String[] mostCommonWords = {"the", "of", "and", "to", "a", "in", "that", "is", "it", "was", "he", "for", "on", "are", "as", "with", "his", "they", "I", "at", "be", "this", "have", "from", "or", "one", "had", "by", "word", "but", "not", "what", " ", "were", "we", "when", "your", "can", "said", "there", "use", "an", "each", "which", "she", "do", "how", "their", "if", "will", "up", "other", "about", "out", "many", "then", "them", "these", "so", "some", "her", "would", "make", "like", "him", "into", "time", "has", "look", "two", "more", "write", "go", "see", "number", "no", "way", "could", "people", "my", "than", "first", "water", "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", "did", "get", "come", "made", "may"};
    private static final char ESCAPE_CHAR = 255;

    private static void compress()
    {
        boolean writingCodes = false;

        while (!BinaryStdIn.isEmpty())
        {
            char currentChar = BinaryStdIn.readChar();
            String currentWord = "";
            boolean foundCommonWord = false;

            // Create the next word
            while (currentChar != ' ')
            {
                // add if not empty
                if (BinaryStdIn.isEmpty())
                {
                    currentWord += currentChar;
                    break;
                }
                currentWord += currentChar;
                currentChar = BinaryStdIn.readChar();
            }

            // Go through the common words list and see if the current word matches any of them.
            for (int i = 0; i < mostCommonWords.length; i++)
            {
                if (currentWord.equals(mostCommonWords[i]))
                {
                    // If we are not writing codes, switch to codes.
                    if (!writingCodes)
                    {
                        BinaryStdOut.write(ESCAPE_CHAR);
                        writingCodes = true;
                    }
                    // Write the index of the common word in the array (this is the code).
                    BinaryStdOut.write((char) i);
                    foundCommonWord = true;
                    break;
                }
            }

            if (!foundCommonWord)
            {
                // Since we are now writing words, if we were writing codes, switch to writing words.
                if (writingCodes)
                {
                    BinaryStdOut.write(ESCAPE_CHAR);
                    writingCodes = false;
                }
                // Write the normal chars of the word.
                for (int i = 0; i < currentWord.length(); i++)
                {
                    BinaryStdOut.write(currentWord.charAt(i));
                }
            }
            // Write out the space.
            if (currentChar == ' ')
            {
                BinaryStdOut.write(currentChar);
            }
        }
        BinaryStdOut.close();
    }

    private static void expand()
    {
        boolean writingCodes = false;

        while (!BinaryStdIn.isEmpty())
        {
            char currentChar = BinaryStdIn.readChar();

            if (currentChar == ' ')
            {
                BinaryStdOut.write(' ');
            }
            else
            {
                // If we found the escape char, flip if we are writing codes or not and get the next char.
                if (currentChar == ESCAPE_CHAR)
                {
                    writingCodes = !writingCodes;
                    currentChar = BinaryStdIn.readChar();
                }

                if (writingCodes)
                {
                    BinaryStdOut.write(mostCommonWords[currentChar]);
                }
                else
                {
                    BinaryStdOut.write(currentChar);
                }
            }
        }

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
