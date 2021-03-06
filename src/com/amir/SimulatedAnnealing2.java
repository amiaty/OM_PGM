package com.amir;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Amir on 06/03/2017.
 */
@SuppressWarnings("Duplicates")
public class SimulatedAnnealing2 {
    private double[][] similarity;
    private double[][] similaritySup;
    private List<Integer> sol;
    private int row, col, cntGoods = 0;
    private double threshold = 0.80;

    private Random random;

    public SimulatedAnnealing2(double[][] sim, double[][] simSup){

        similarity = sim;
        similaritySup = simSup;
        row = sim.length;
        col = sim[0].length;
        //random = new Random(System.currentTimeMillis());
        random = new Random(0);
    }
    public void solve(int duration) {
        double deltaE, temperature = 1.0, alpha = 0.999;
        sol = generateInitSol();
        List<Integer> next, curr, best;
        curr = best = sol;
        double fitNext, fitCurr = getFitness(curr), fitBest = fitCurr;
        for (int t = 0; t < duration; ++t){
            curr = best;
            fitCurr = fitBest;
            for (int i = 0; i < 50; ++i) {
                next = successor(curr);
                fitNext = getFitness(next);
                deltaE = fitNext - fitCurr;
                if (deltaE > 0) {
                    curr = next;
                    fitCurr = fitNext;
                } else if (random.nextDouble() >= Math.exp(deltaE / temperature)) {
                    curr = next;
                    fitCurr = fitNext;
                }
                if(fitBest < fitCurr)
                {
                    fitBest = fitCurr;
                    best = curr;
                }
            }
            temperature = (temperature > 0.00001) ? (temperature * alpha) : 0.00001;
            System.out.print("\n" + (t + 1) + "\t: " + fitBest);
        }
        System.out.println("\n" + "Final temperature : " + temperature);
        sol = best;
    }
    public List<Pair<Integer, Integer>> getSolution() {
        return extractSolutionFinal(sol);
    }
    private List<Integer> successor(final List<Integer> curr) {
        int batchSz = Math.min(100, (row / 2) * 2);
        int[] randInx = random.ints(cntGoods, row).distinct().limit(batchSz).toArray();
        List<Integer> next = new ArrayList<>(curr);
        for (int i = 0; i < batchSz; i += 2)
            java.util.Collections.swap(next, randInx[i], randInx[i + 1]);
        return next;
    }
    private double getFitness(final List<Integer> S) {
        double sum1 = 0, sum2 = 0;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS) {
            double simVal = similarity[item.getL()][item.getR()];
            if(simVal > threshold) {
                sum1 += simVal;
                continue;
            }
            double simValSup = similaritySup[item.getL()][item.getR()];
            if (simVal > 0.6 && simValSup >= 1.0) {
                sum2 += simVal;
                continue;
            }
            if (simVal > 0.65 && simValSup >= 0.8)
                sum2 += simVal;
        }
        return sum1 * 200 + sum2 * 10;
    }
    private List<Integer> generateInitSol(){
        List<Integer> randOrder = random.ints(0, row).distinct().limit(row).boxed().collect(Collectors.toCollection(ArrayList::new));
        List<Integer> ans = new ArrayList<>(row);
        boolean[] selected = new boolean[col];
        boolean[] selected2 = new boolean[row];
        int maxInd;
        double maxVal;
        for (double thr = 1.0; thr >= 0.8; thr -= 0.1) {
            for (int i : randOrder) {
                if(selected2[i]) continue;
                maxInd = -1;
                maxVal = -1;
                for (int j = 0; j < col; ++j) {
                    if (similarity[i][j] > maxVal && !selected[j]) {
                        maxInd = j;
                        maxVal = similarity[i][j];
                    }
                }
                if (maxVal >= thr) {
                    selected[maxInd] = true;
                    selected2[i] = true;
                    cntGoods++;
                    ans.add(i);
                }
            }
        }
        for (int i : randOrder) if (!selected2[i]) ans.add(i);
        return ans;
    }
    private List<Pair<Integer, Integer>> extractSolutionFinal(final List<Integer> visitOrder){
        List<Pair<Integer, Integer>> SS = extractSolution(visitOrder);
        List<Pair<Integer, Integer>> res = new ArrayList<>();

        for (Pair<Integer, Integer> item: SS) {
            double simVal = similarity[item.getL()][item.getR()];
            if(simVal >= threshold) {
                res.add(item);
                continue;
            }
            double simValSup = similaritySup[item.getL()][item.getR()];
            if (simVal > 0.6 && simValSup >= 1.0) {
                res.add(item);
                continue;
            }
            if (simVal > 0.65 && simValSup >= 0.8)
                res.add(item);
        }
        return res;
    }
    private List<Pair<Integer, Integer>> extractSolution(final List<Integer> visitOrder){

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
            if(maxVal >= 0.0) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
}
