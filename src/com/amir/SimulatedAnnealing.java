package com.amir;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;

/**
 * Created by Amir on 06/03/2017.
 */
@SuppressWarnings("Duplicates")
public class SimulatedAnnealing {
    private double[][] similarity;
    private List<Integer> sol;
    private int row, col;
    private double threshold = 0.6;
    private HeavyLoadedOntology<Object> heavyOntology1;
    private HeavyLoadedOntology<Object> heavyOntology2;
    private Object[] class1o;
    private Object[] class2o;
    private Random random;

    public SimulatedAnnealing(double[][] sim, HeavyLoadedOntology<Object> o1, HeavyLoadedOntology<Object> o2, Object [] co1, Object [] co2){
        heavyOntology1 = o1;
        heavyOntology2 = o2;
        class1o = co1;
        class2o = co2;
        similarity = sim;
        row = sim.length;
        col = sim[0].length;
        //random = new Random(System.currentTimeMillis());
        random = new Random(0);
    }
    public void solve(int duration) {
        double deltaE, temperature = 1.0, alpha = 0.995;
        sol = generateInitSol();
        List<Integer> next, curr, best;
        curr = best = sol;
        double fitNext, fitCurr = getFitness(curr), fitBest = fitCurr;
        for (int t = 0; t < duration; ++t){
            curr = best;
            fitCurr = fitBest;
            for (int i = 0; i < 20; ++i) {
                temperature = (temperature > 0.00001) ? (temperature * alpha) : 0.00001;
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
            //if (true)
            System.out.print("\n" + t + "\t: " + fitBest);
        }
        System.out.println("\n" + "Final temperature : " + temperature);
        sol = best;
    }
    public List<Pair<Integer, Integer>> getSolution(double thr) {
        threshold = thr;
        return extractSolution(sol);
    }
    private List<Integer> successor(List<Integer> curr) {
        List<Integer> next = new ArrayList<>(curr);
        int i = random.nextInt(row);
        int j = random.nextInt(row);
        java.util.Collections.swap(next, i, j);
        return next;
    }
    private double getFitness(List<Integer> S) {
        double sum2 = 0;
        double sum3 = 0;
        double reward = 1;
        double sum1 = 0;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS) {
            Set supO1 = ((OWLClassImpl)class1o[item.getL()]).getSuperClasses((OWLOntology) heavyOntology1.getOntology());
            Set supO2 = ((OWLClassImpl)class2o[item.getR()]).getSuperClasses((OWLOntology) heavyOntology2.getOntology());

            for (Object leftClass : supO1)
                for (Object rightClass : supO2)
                    if (isMatch(leftClass, rightClass, SS) || leftClass == rightClass) {
                        sum2 += reward;
                        break;
                    }

            Set subO1 = ((OWLClassImpl)class1o[item.getL()]).getSubClasses((OWLOntology) heavyOntology1.getOntology());
            Set subO2 = ((OWLClassImpl)class2o[item.getR()]).getSubClasses((OWLOntology) heavyOntology2.getOntology());

            for (Object leftClass : subO1)
                for (Object rightClass : subO2)
                    if (isMatch(leftClass, rightClass, SS) || leftClass == rightClass) {
                        sum3 += reward;
                        break;
                    }

            sum1 += similarity[item.getL()][item.getR()];
        }
        double sum4 = SS.size() * (10 * threshold);
        return sum1 * 100 + sum2 * 1 + sum3 * 1 + sum4;
    }
    private List<Integer> generateInitSol(){
        List<Integer> visitOrder = new ArrayList<>();
        for (int i = 0; i < row; ++i) visitOrder.add(i);
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
    private boolean isMatch(Object o1,Object o2,List<Pair<Integer, Integer>> ref){
        for(Pair<Integer, Integer> item: ref)
            if(class1o[item.getL()] == o1 && class2o[item.getR()] == o2) return true;
        return false;
    }
}
