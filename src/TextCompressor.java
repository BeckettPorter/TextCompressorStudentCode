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
    private static int lastAddedCode = 127;

    private static void compress()
    {
        initializeTST();

        String text = BinaryStdIn.readString();
        int index = 0;

        while (index < text.length())
        {
            addPrefixCode(text.substring(index, index + 1));

            int codeToWrite = findPrefixCode(text, index);

            BinaryStdOut.write(codeToWrite);

            index++;
        }

        BinaryStdOut.close();
    }

    private static void addPrefixCode(String s)
    {
        tst.insert(s, ++lastAddedCode);
    }

    private static int findPrefixCode(String s, int index)
    {
        String prefix = tst.getLongestPrefix(s, index);

        if (tst.lookup(prefix) == TST.EMPTY)
        {
            tst.insert(prefix, ++lastAddedCode);
        }

        return tst.lookup(prefix);
    }

    private static void expand()
    {
        initializeTST();

        while (!BinaryStdIn.isEmpty())
        {


        }
        BinaryStdOut.close();
    }

    private static void initializeTST()
    {
        for (int i = 0; i < 128; i++)
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
