package com.amir;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Amir on 06/03/2017.
 */
@SuppressWarnings("Duplicates")
public class SimulatedAnnealing2 {
    private double[][] similarity;
    private List<Integer> sol;
    private int row, col;
    private double threshold = 0.01;
    private List<Set> supO1;
    private List<Set> supO2;
    private List<Set> subO1;
    private List<Set> subO2;
    private Object[] class1o;
    private Object[] class2o;
    private Random random;

    public SimulatedAnnealing2(double[][] sim, List<Set> sp1, List<Set> sp2, List<Set> sb1, List<Set> sb2, Object [] co1, Object [] co2){
        supO1 = sp1;
        supO2 = sp2;
        subO1 = sb1;
        subO2 = sb2;
        class1o = co1;
        class2o = co2;
        similarity = sim;
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
        int batchSz = Math.min(4, row);
        int[] randInx = random.ints(0, row).distinct().limit(batchSz).toArray();
        List<Integer> next = new ArrayList<>(curr);
        for (int i = 0; i < batchSz; i += 2)
            java.util.Collections.swap(next, randInx[i], randInx[i + 1]);
        return next;
    }
    private double getFitness(List<Integer> S) {
        double sum2 = 0, sum3 = 0;
        double reward, sum1 = 0;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS) {

            if(similarity[item.getL()][item.getR()] > 0.8) {
                sum1 += similarity[item.getL()][item.getR()];
                continue;
            }

            if(supO1.size() != 0 && similarity[item.getL()][item.getR()] > 0.5) {
                for (Object leftClass : supO1.get(item.getL())) {
                    reward = .0;
                    for (Object rightClass : supO2.get(item.getR())) {
                        reward = Matched((IRI) leftClass, (IRI) rightClass, SS);
                        if (reward != -1.0) {
                            break;
                        }
                    }
                    if(reward > 0.9){
                        sum2 += similarity[item.getL()][item.getR()];
                        break;
                    }
                }
            }

            /*
            if(subO1.size() != 0) {
                for (Object leftClass : subO1.get(item.getL()))
                    for (Object rightClass : subO2.get(item.getR()))
                        if (isMatched(leftClass, rightClass, SS)) {
                            sum3 += reward;
                            break;
                        }
            }
            */

            //sum1 += similarity[item.getL()][item.getR()];
        }
        return sum1 * 150 + sum2 * 100 + sum3;
    }
    private List<Integer> generateInitSol(){
        return random.ints(0, row).distinct().limit(row).boxed().collect(Collectors.toCollection(ArrayList::new));
    }
    private List<Pair<Integer, Integer>> extractSolutionFinal(List<Integer> visitOrder){
        List<Pair<Integer, Integer>> SS = extractSolution(visitOrder);
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        double reward;
        for (Pair<Integer, Integer> item: SS) {

            if(similarity[item.getL()][item.getR()] > 0.8) {
                res.add(item);
                continue;
            }
            if(supO1.size() != 0 && similarity[item.getL()][item.getR()] > 0.5) {
                for (Object leftClass : supO1.get(item.getL())) {
                    reward = 0;
                    for (Object rightClass : supO2.get(item.getR())) {
                        reward = Matched((IRI) leftClass, (IRI) rightClass, SS);
                        if (reward != -1.0) {
                            break;
                        }
                    }
                    if(reward > 0.9)
                    {
                        res.add(item);
                        break;
                    }
                }
            }
        }
        return res;
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
            if(threshold <= maxVal) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
    private boolean isMatched(IRI o1, IRI o2, List<Pair<Integer, Integer>> ref){
        for(Pair<Integer, Integer> item: ref)
            if(((OWLClass)class1o[item.getL()]).getIRI() == o1 && ((OWLClass)class2o[item.getR()]).getIRI() == o2) return true;
        return false;
    }
    private double Matched(IRI o1, IRI o2, List<Pair<Integer, Integer>> ref){
        for(Pair<Integer, Integer> item: ref)
            if(((OWLClass)class1o[item.getL()]).getIRI() == o1 && ((OWLClass)class2o[item.getR()]).getIRI() == o2) return similarity[item.getL()][item.getR()];
        return -1;
    }
}
