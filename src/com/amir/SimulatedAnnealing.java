package com.amir;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Amir on 06/03/2017.
 */
@SuppressWarnings("Duplicates")
public class SimulatedAnnealing {
    private double[][] similarity;
    private List<Integer> sol;
    private int row, col;
    private double threshold = 0.01;
    public SimulatedAnnealing(double[][] sim){
        similarity = sim;
        row = sim.length;
        col = sim[0].length;
    }
    public void solve(int duration) {
        double deltaE, temperature;
        sol = generateInitSol();
        List<Integer> next, curr;
        curr = sol;
        for (int t = 0; t < duration; ++t){
            temperature = duration - t;
            next = successor(curr);
            deltaE = getFitness(next) - getFitness(curr);
            if (deltaE > 0){
                curr = next;
            }
            else {
                double p = Math.random();
                double Prob = Math.exp(deltaE / temperature);
                if(p >= Prob){
                    curr = next;
                }
            }
        }
        sol = curr;
    }
    public List<Pair<Integer, Integer>> getSolution() {
        return extractSolution(sol);
    }
    private List<Integer> successor(List<Integer> curr) {
        List<Integer> next = new ArrayList<>(curr);

        return next;
    }
    private double getFitness(List<Integer> S) {
        double sum = 0;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS)
            sum += similarity[item.getL()][item.getR()];
        return sum;
    }
    private List<Integer> generateInitSol(){
        List<Integer> visitOrder = new ArrayList<>();
        for (int i = 0; i < row; ++i) visitOrder.add(i);
        Random random = new Random(System.currentTimeMillis());
        java.util.Collections.shuffle(visitOrder, random);
        return visitOrder;
    }
    private List<Pair<Integer, Integer>> extractSolution(List<Integer> visitOrder){

        int maxInd;
        double maxVal;
        boolean[] selected = new boolean[col];
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i : visitOrder) {
            maxInd = -1;
            maxVal = -1;
            for (int j = 0; j < col; ++j) {
                if(similarity[i][j] > maxVal && !selected[j])
                {
                    maxInd = j;
                    maxVal = similarity[i][j];
                }
            }
            if(maxInd != -1 && threshold <= maxVal) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
}
