/*
    File: AMAlignment.java
    Copyright (C) Amir Ahooye Atashin, Ferdowsi Univerity of Mashhad, 2017 (1395-1396 Hijri-Shamsi)
    Authors:    Amir Ahooye Atashin,
                Majid Mohammadi
 */
package com.amir;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import fr.inrialpes.exmo.ontosim.util.HungarianAlgorithm;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class AMAlignment extends DistanceAlignment implements AlignmentProcess {

    private HeavyLoadedOntology<Object> heavyOntology1;
    private HeavyLoadedOntology<Object> heavyOntology2;
    public AMAlignment() {
        heavyOntology1 = heavyOntology2 = null;
        setType("**");
    }

    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
        super.init( o1, o2, ontologies );
        if ( !( getOntologyObject1() instanceof HeavyLoadedOntology  && getOntologyObject1() instanceof HeavyLoadedOntology ))
            throw new AlignmentException( "StrucSubsDistAlignment requires HeavyLoadedOntology ontology loader" );
    }
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            heavyOntology1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
            heavyOntology2 = (HeavyLoadedOntology<Object>)getOntologyObject2();

            int nbClasses1 = heavyOntology1.nbClasses();
            int nbClasses2 = heavyOntology2.nbClasses();

            double[][] matrix = new double[nbClasses1][nbClasses2];
            Object[] class1o = new Object[nbClasses1];
            Object[] class2o = new Object[nbClasses2];
            String[] class1s = new String[nbClasses1];
            String[] class2s = new String[nbClasses2];
            int i = 0;
            String str1;

            for ( Object ob : getOntologyObject1().getClasses() ) {
                str1 = heavyOntology1.getEntityAnnotations(ob).iterator().next();
                //str2 = heavyOntology1.getEntityName(ob);
                class1s[i] = str1.trim().toLowerCase();
                class1o[i++] = ob;
            }

            int j = 0;
            for ( Object ob : getOntologyObject2().getClasses() ) {
                str1 = heavyOntology2.getEntityAnnotations(ob).iterator().next();
                //str2 = heavyOntology2.getEntityName(ob);
                class2s[j] = str1.trim().toLowerCase();
                class2o[j++] = ob;
            }

            double ii = 0;
            int jj = -1;
            double step = 100.0 / nbClasses1;

            for( i = 0; i < nbClasses1; ++i ) {
                for (j = 0; j < nbClasses2; ++j)
                    matrix[i][j] = 1.0 - StringDistances.levenshteinDistance(class1s[i], class2s[j]);
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }
            double threshold = 0.7;
            //int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
            //List<Pair<Integer, Integer>>  result = greedyExtract( matrix, nbClasses1, nbClasses2, threshold);
            SimulatedAnnealing SA = new SimulatedAnnealing(matrix, heavyOntology1, heavyOntology2, class1o, class2o);
            SA.solve(200);
            List<Pair<Integer, Integer>>  result = SA.getSolution(threshold);
            System.out.println("\nOK");
            for (Pair<Integer, Integer> item: result)
                addAlignCell(class1o[item.getL()], class2o[item.getR()], "=", matrix[item.getL()][item.getR()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private List<Pair<Integer, Integer>> greedyExtract(double[][] mat, int row, int col) {
        return greedyExtract(mat, row, col, 0.0);
    }
    private List<Pair<Integer, Integer>> greedyExtract(double[][] mat, int row, int col, double threshold) {
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
    private List<Pair<Integer, Integer>> randomExtract(double[][] mat, int row, int col, double threshold){
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
}
