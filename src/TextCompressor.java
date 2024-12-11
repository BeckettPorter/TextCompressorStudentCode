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
    // Instance variables.
    private static TST tst = new TST();
    private static final int EXIT_CODE = 256;
    // Set the initial current code to add to one more than the exit code.
    private static int currentCodeToAdd = EXIT_CODE + 1;
    // 12 bits per code works well for this.
    private static final int BITS_PER_CODE = 12;
    // Set the max number of codes equal to 2^BITS_PER_CODE.
    private static final int NUM_CODES = 1 << BITS_PER_CODE;

    private static void compress()
    {
        // Call to helper method that initializes the TST with values from 0 to EXIT_CODE.
        initializeTST();

        // Read the entire string in at once.
        String text = BinaryStdIn.readString();
        // Only use an index to shift the window we are looking at, faster than using substring.
        int index = 0;

        // Go while there is still more of the text to read in.
        while (index < text.length())
        {
            // First, get the longest prefix.
            String longestPrefix = tst.getLongestPrefix(text, index);

            // Get the code to write by looking up the corresponding code for the prefix.
            int codeToWrite = tst.lookup(longestPrefix);

            // Write out the code using BITS_PER_CODE number of bits.
            BinaryStdOut.write(codeToWrite, BITS_PER_CODE);

            // Second, add a new code for longestPrefix + next char if it won't overflow the max number of codes
            // or the length of the text.
            if (currentCodeToAdd < NUM_CODES && index + longestPrefix.length() < text.length())
            {
                tst.insert(longestPrefix + text.charAt(index + longestPrefix.length()), currentCodeToAdd++);
            }

            // Move index forward by the prefix length.
            index += longestPrefix.length();
        }
        // Write out the exit code with bits per code.
        BinaryStdOut.write(EXIT_CODE, BITS_PER_CODE);

        BinaryStdOut.close();
    }

    private static void expand()
    {
        // Map that has integer codes corresponding to Strings.
        // Make it the max length of codes we can store (NUM_CODES).
        String[] codeStringMap = new String[NUM_CODES];

        // Fill up the first EXIT_CODE number of slots in the map.
        for (int i = 0; i < EXIT_CODE; i++)
        {
            codeStringMap[i] = String.valueOf((char)i);
        }

        // Read in the first code.
        int currentCode = BinaryStdIn.readInt(BITS_PER_CODE);
        int lookAheadCode;

        // Go until the exit code is found.
        while (currentCode != EXIT_CODE)
        {
            // Set the current string equal to the string corresponding to the current code in the map.
            String currentString = codeStringMap[currentCode];

            // The look ahead code is the next code we read in.
            lookAheadCode = BinaryStdIn.readInt(BITS_PER_CODE);

            // The look ahead string is the string corresponding to this look ahead code in the map.
            String lookAheadString = codeStringMap[lookAheadCode];

            // This check prevents the edge case issue, where if the code hasn't been initialized in the map yet,
            // set the look ahead string to the current string + the first letter of the current string.
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

    // Initializes the TST with values from 0 to EXIT_CODE.
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
