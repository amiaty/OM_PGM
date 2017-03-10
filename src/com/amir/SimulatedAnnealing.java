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
    private double threshold = 0.5;
    private HeavyLoadedOntology<Object> heavyOntology1;
    private HeavyLoadedOntology<Object> heavyOntology2;
    private Object[] class1o;
    private Object[] class2o;

    public SimulatedAnnealing(double[][] sim, HeavyLoadedOntology<Object> o1, HeavyLoadedOntology<Object> o2, Object [] co1, Object [] co2){
        heavyOntology1 = o1;
        heavyOntology2 = o2;
        class1o = co1;
        class2o = co2;
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
            temperature = duration - t * 0.1;
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
            if(t % 50 == 0) System.out.println(t + " : " + getFitness(curr));
        }
        sol = curr;
    }
    public List<Pair<Integer, Integer>> getSolution(double thr) {
        threshold = thr;
        return extractSolution(sol);
    }
    private List<Integer> successor(List<Integer> curr) {
        List<Integer> next = new ArrayList<>(curr);
        int i = (int)(Math.random() * (row - 1));
        int j = (int)(Math.random() * (row - 1));
        java.util.Collections.swap(next, i, j);
        return next;
    }
    private double getFitness(List<Integer> S) {
        double sum = 0;
        double reward = 1;
        double penalty = -1;
        double sum1 = 0;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS) {
            Set supO1 = ((OWLClassImpl)class1o[item.getL()]).getSuperClasses((OWLOntology) heavyOntology1.getOntology());
            Set supO2 = ((OWLClassImpl)class2o[item.getR()]).getSuperClasses((OWLOntology) heavyOntology2.getOntology());
            //Iterator itr1 = supO1.iterator();
            //Iterator itr2 = supO2.iterator();

            for(Iterator itr1 = supO1.iterator(); itr1.hasNext();){
                Object leftClass = itr1.next();
                for(Iterator itr2=supO2.iterator();itr2.hasNext();){
                    Object rightClass = itr2.next();
                    if (isMatch(leftClass,rightClass,SS) || leftClass == rightClass) {
                        sum += reward;
                        break;
                    }
                }

            }

            Set subO1 = ((OWLClassImpl)class1o[item.getL()]).getSubClasses((OWLOntology) heavyOntology1.getOntology());
            Set subO2 = ((OWLClassImpl)class2o[item.getR()]).getSubClasses((OWLOntology) heavyOntology2.getOntology());
            //Iterator itr1 = supO1.iterator();
            //Iterator itr2 = supO2.iterator();

            for(Iterator itr1=subO1.iterator();itr1.hasNext();){
                Object leftClass = itr1.next();
                for(Iterator itr2=subO2.iterator();itr2.hasNext();){
                    Object rightClass = itr2.next();
                    if (isMatch(leftClass,rightClass,SS) || leftClass == rightClass) {
                        sum += reward;
                        break;
                    }
                }

            }

            /*  while(itr1.hasNext()){
                while(itr2.hasNext()){
                    int x = 0;
                }
            }
            */
            sum1 += similarity[item.getL()][item.getR()];
        }
        double sum2 = SS.size() * (10 * threshold);
        return sum1* 50 + sum * 5 + sum2;
    }
    private List<Integer> generateInitSol(){
        List<Integer> visitOrder = new ArrayList<>();
        for (int i = 0; i < row; ++i) visitOrder.add(i);
        //Random random = new Random(System.currentTimeMillis());
        Random random = new Random(0);
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
