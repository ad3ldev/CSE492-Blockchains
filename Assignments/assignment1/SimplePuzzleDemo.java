import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SimplePuzzleDemo {
    static boolean randomSolve = false;

    public static void main(String args[]) {

        int numPuzzles = 10000;

        // target = 2^(256 - difficultyLevel)
        int difficultyLevel = 9;
        BigInteger target = BigInteger.ONE.shiftLeft(256 - difficultyLevel);

        byte[] puzzleId = new byte[32];
        SecureRandom rnd = new SecureRandom(); // Initialize a secure random number generator to generate the puzzle IDs
        int sum = 0; // Needed for average computation
        for (int i = 0; i < numPuzzles; i++) {
            rnd.nextBytes(puzzleId); // Generate a random puzzle ID
            sum += solvePuzzle(puzzleId, target); // Add the number of attempts needed to solve the puzzle
        }
        System.out.println(
                "Random is " + randomSolve + " : Average number of attempts to solve " + numPuzzles
                        + " puzzles at difficulty level "
                        + difficultyLevel + " = " + sum * 1.0 / numPuzzles);
    }

    /**
     * This method should return the number of attempts needed to solve the
     * following puzzle:
     * Find nonce that makes H(puzzleId || nonce) < targetValue
     * || above denotes concatenation
     */

    private static int solvePuzzle(byte[] puzzleId, BigInteger targetValue) {
        /*
         * ToDo: Write code to find a nonce that solves the above puzzle.
         * (a) You can try nonces sequentially or randomly.
         * (b) You can use concat to concatenate the puzzleId and the nonce bytes each
         * time. While this is definitely not efficient, this is allowed for simplicity.
         * (c) computeSHA256() returns an array of bytes. You can use the
         * toPositiveBigInteger() method below to convert it to a
         * positive big integer in order to be able to compare it with target.
         */
        SecureRandom rnd = new SecureRandom();
        int numOfAttempts = 0;
        byte[] nonce = new byte[32];
        for (numOfAttempts = 0; numOfAttempts <= Integer.MAX_VALUE; numOfAttempts++) {
            if (randomSolve) {
                rnd.nextBytes(nonce);
            } else {
                incrementBytes(nonce, 31);
            }
            byte[] hashed = computeSHA256(concat(puzzleId, nonce));
            if (toPositiveBigInteger(hashed).compareTo(targetValue) < 0) {
                break;
            }
        }
        return numOfAttempts;
    }

    private static byte[] computeSHA256(byte[] input) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }

    private static byte[] concat(byte[] a1, byte[] a2) {
        byte[] a = new byte[a1.length + a2.length];
        for (int i = 0; i < a.length; i++) {
            if (i < a1.length)
                a[i] = a1[i];
            else
                a[i] = a2[i - a1.length];
        }
        return a;
    }

    private static BigInteger toPositiveBigInteger(byte[] a) {
        return new BigInteger(1, a);
    }

    static private void incrementBytes(byte[] array, int index) {
        if (array[index] == 127) {
            array[index] = 0;
            if (index > 0)
                incrementBytes(array, index - 1);
        } else {
            array[index]++;
        }
    }
}