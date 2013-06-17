package org.jboss.narayana.blacktie.jatmibroker.xatmi;

/*
 * The Knuth-Morris-Pratt Algorithm for Pattern Matching
 * http://en.wikibooks.org/wiki/Algorithm_Implementation/String_searching/Knuth-Morris-Pratt_pattern_matcher
 */
public class KMPMatch {
    /**
     * Search the data byte array for the first occurrence of the byte array pattern.
     */
    public static int indexOf(byte[] data, byte[] pattern, int datalen) {
        int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = 0; i < datalen; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process, where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}
