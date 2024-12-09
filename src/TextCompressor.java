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
    private static TST tst = new TST();
    private static int currentCodeToAdd = 257;
    private static final int EXIT_CODE = 256;

    private static void compress()
    {
        initializeTST();

        String text = BinaryStdIn.readString();
        int index = 0;

        while (index < text.length() - 1)
        {
            String longestPrefix = tst.getLongestPrefix(text, index);
            // First, need to write out current code.

            int codeToWrite = tst.lookup(longestPrefix);

            BinaryStdOut.write(codeToWrite);

            // Second, need to add new code for longestPrefix + next char.

            if (index + longestPrefix.length() < text.length())
            {
                tst.insert(longestPrefix + text.charAt(index + longestPrefix.length()), ++currentCodeToAdd);
            }

            index++;
        }
        BinaryStdOut.write(EXIT_CODE);

        BinaryStdOut.close();
    }

    private static void expand()
    {
        initializeTST();

        String text = BinaryStdIn.readString();
        int index = 0;

        String lookAheadCode;

        while (text.charAt(index + 1) != EXIT_CODE)
        {
            String longestPrefix = tst.getLongestPrefix(text, index);

            lookAheadCode = text.substring(index + longestPrefix.length(), index + longestPrefix.length() + 1);

            tst.insert(longestPrefix + lookAheadCode, currentCodeToAdd);


            BinaryStdOut.write(tst.lookup(text.substring(index, index + longestPrefix.length())));

            index++;
        }
        BinaryStdOut.close();
    }

    private static void initializeTST()
    {
        for (int i = 0; i < 256; i++)
        {
            String s = String.valueOf((char) i);
            tst.insert(s, i);
        }
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
