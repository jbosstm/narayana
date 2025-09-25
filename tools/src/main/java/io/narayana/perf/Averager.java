/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.perf;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Averager {
    public static void main(String[] args) {
        Double[][] tests = {
           { 2.0, 5.0, 6.0, 9.0, 12.0 },
           { 2.0, 5.0, 6.0, 9.0, 12.0, 26.0 },
           { 2.0 }
        };

        for (Double[] vals : tests) {
            List<Double> v1 = new ArrayList<Double>(Arrays.asList(vals));
            List<Double> v2 = new ArrayList<Double>(Arrays.asList(vals));

            double median = getMedian(v1, 0, v1.size() - 1);
            double q1 = getQ1(v1);
            double q3 = getQ3(v1);

            removeOutliers(v1);

            for (Double d : v2)
                System.out.printf("%f, ", d);

            System.out.printf("%nq1=%f q2=%f median=%f%n", q1, q3, median);
            System.out.printf("Outliers:%n");

            for (Double d : v2)
                if (!v1.contains(d))
                    System.out.printf("\t%f%n", d);
        }
    }

    static double getAverage(List<Double> values) {

        double tot = 0;

        for (Double d : values)
            tot += d;

        return tot / values.size();
    }

    static void removeOutliers(List<Double> values) {
        Collections.sort(values);

        double q1 = getQ1(values); // first interquartile
        double q3 = getQ3(values); // third interquartile
        double iqr = q3 - q1; // the interquartile range
        double outlier = 1.5 * iqr; // an outlier is any data point more than 1.5 interquartile ranges (IQRs) below the first quartile or above the third quartile.
        double lb = q1 - outlier;
        double ub = q3 + outlier;

        // remove outliers from the data
        Iterator<Double> i = values.iterator();

        while (i.hasNext()) {
            double  value = i.next();

            if (value < lb || value > ub)
                i.remove();
        }
    }

    static Double getMedian(List<Double> values, int from, int to) {
        int sz = to - from + 1;

        if (sz % 2 == 0)
             return (values.get(from + sz/2) + values.get(from + (sz/2) - 1) ) / 2;
        else
            return values.get(from + (sz / 2));
    }

    static double getQ1(List<Double> values) {
        int sz = values.size();

        if (sz == 1)
            return values.get(0);
        else if (sz % 2 == 0)
            return getMedian(values, 0, sz / 2 - 1);
        else
            return getMedian(values, 0, sz / 2 - 1);
    }

    static double getQ3(List<Double> values) {
        int sz = values.size();

        if (sz == 1)
            return values.get(0);
        else if (sz % 2 == 0)
            return getMedian(values, sz / 2, sz - 1);
        else
            return getMedian(values, sz / 2 + 1, sz - 1);
    }

}