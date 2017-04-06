/**
 * Created by Amir on 07/04/2017.
 */

package com.amir.utils;

import com.amir.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import fr.inrialpes.exmo.ontosim.util.HungarianAlgorithm;

@SuppressWarnings("Duplicates")
public class AssignmentMethods {
    public static List<Pair<Integer, Integer>> greedy(double[][] mat, int row, int col) {
        return greedy(mat, row, col, 0.0);
    }
    public static List<Pair<Integer, Integer>> greedy(double[][] mat, int row, int col, double threshold) {
        int maxInd;
        double maxVal;
        boolean[] selected = new boolean[col];
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i = 0; i < row; ++i) {
            maxInd = -1;
            maxVal = -1;
            for (int j = 0; j < col; ++j) {
                if(mat[i][j] > maxVal && !selected[j])
                {
                    maxInd = j;
                    maxVal = mat[i][j];
                }
            }
            if(maxInd != -1 && threshold <= maxVal) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
    public static List<Pair<Integer, Integer>> random(double[][] mat, int row, int col, double threshold){
        List<Integer> visitOrder = new ArrayList<>();
        for (int i = 0; i < row; ++i) visitOrder.add(i);

        Random random = new Random(System.currentTimeMillis());
        java.util.Collections.shuffle(visitOrder, random);

        int maxInd;
        double maxVal;
        boolean[] selected = new boolean[col];
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i : visitOrder) {
            maxInd = -1;
            maxVal = -1;
            for (int j = 0; j < col; ++j) {
                if(mat[i][j] > maxVal && !selected[j])
                {
                    maxInd = j;
                    maxVal = mat[i][j];
                }
            }
            if(maxInd != -1 && threshold <= maxVal) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
    public static List<Pair<Integer, Integer>> Hungarian(double[][] mat, int row, int col) {
        int[][] res = HungarianAlgorithm.hgAlgorithm( mat, "max" );
        List<Pair<Integer, Integer>>  result = new ArrayList<>();
        for (int[] re : res) result.add(new Pair<>(re[0], re[1]));
        return result;
    }
}
