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
    private static final int BITS_PER_CODE = 12;
    private static final int NUM_CODES = 1 << BITS_PER_CODE;

    private static void compress()
    {
        initializeTST();

        String text = BinaryStdIn.readString();
        int index = 0;

        while (index < text.length())
        {
            String longestPrefix = tst.getLongestPrefix(text, index);
            // First, need to write out current code.

            int codeToWrite = tst.lookup(longestPrefix);

            BinaryStdOut.write(codeToWrite, BITS_PER_CODE);

            // Second, need to add new code for longestPrefix + next char.

            if (currentCodeToAdd < NUM_CODES)
            {
                tst.insert(longestPrefix + text.charAt(index + longestPrefix.length()), currentCodeToAdd++);
            }

            index += longestPrefix.length();
        }
        BinaryStdOut.write(EXIT_CODE, BITS_PER_CODE);

        BinaryStdOut.close();
    }

    private static void expand()
    {
        String[] codeStringMap = new String[NUM_CODES];

        for (int i = 0; i < EXIT_CODE; i++)
        {
            codeStringMap[i] = String.valueOf((char)i);
        }


        int currentCode = BinaryStdIn.readInt(BITS_PER_CODE);
        int lookAheadCode;

        // Why do I need the exit code? Why can't I just do this
        while (currentCode != EXIT_CODE)
        {
            String currentString = codeStringMap[currentCode];

            lookAheadCode = BinaryStdIn.readInt(BITS_PER_CODE);


            String lookAheadString = codeStringMap[lookAheadCode];

            // if edge case, lookaheadstring is cur
            if (codeStringMap[lookAheadCode] == null)
            {
                lookAheadString = currentString + currentString.charAt(0);
            }

            // Add a new code to the codeStringMap if we have room in the number of codes.
            if (currentCodeToAdd < NUM_CODES)
            {
                codeStringMap[currentCodeToAdd++] = currentString + lookAheadString.charAt(0);
            }
            // Write out the string corresponding to the current code.
            BinaryStdOut.write(codeStringMap[currentCode]);

            // Set the current code equal to the next code because we already read it in.
            currentCode = lookAheadCode;
        }
        BinaryStdOut.close();
    }

    private static void initializeTST()
    {
        for (int i = 0; i < EXIT_CODE; i++)
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
