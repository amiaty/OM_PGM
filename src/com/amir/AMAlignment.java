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
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.*;

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
                class1s[i] = str1.trim().replaceAll("_", " ").toLowerCase();
                class1o[i++] = ob;
            }

            int j = 0;
            for ( Object ob : getOntologyObject2().getClasses() ) {
                str1 = heavyOntology2.getEntityAnnotations(ob).iterator().next();
                class2s[j] = str1.trim().replaceAll("_", " ").toLowerCase();
                class2o[j++] = ob;
            }

            double ii = 0, m1, m2;
            int jj = -1;
            double step = 100.0 / nbClasses1;

            System.out.println("Preparing:");
            // make similarity matrix
            for( i = 0; i < nbClasses1; ++i ) {
                for (j = 0; j < nbClasses2; ++j) {
                    m1 = 1.0 - StringDistances.levenshteinDistance(class1s[i], class2s[j]);
                    m2 = 1.0 - StringDistances.needlemanWunsch2Distance(class1s[i], class2s[j]);
                    matrix[i][j] = Math.max(m1, m2);
                }
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }

            List<Set> supO1 = new ArrayList<>(nbClasses1);
            List<Set> supO2 = new ArrayList<>(nbClasses2);
            List<Set> subO1 = new ArrayList<>(nbClasses1);
            List<Set> subO2 = new ArrayList<>(nbClasses2);
            for( i = 0; i < nbClasses1; ++i ) {
                supO1.add(((OWLClassImpl)class1o[i]).getSuperClasses((OWLOntology) heavyOntology1.getOntology()));
                subO1.add(((OWLClassImpl)class1o[i]).getSubClasses((OWLOntology) heavyOntology1.getOntology()));
            }
            for( i = 0; i < nbClasses2; ++i ) {
                supO2.add(((OWLClassImpl)class2o[i]).getSuperClasses((OWLOntology) heavyOntology2.getOntology()));
                subO2.add(((OWLClassImpl)class2o[i]).getSubClasses((OWLOntology) heavyOntology2.getOntology()));
            }

            System.out.println("\nRunning SA:");
            double threshold = 0.75;
            //int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
            //List<Pair<Integer, Integer>>  result = greedyExtract( matrix, nbClasses1, nbClasses2, threshold);
            SimulatedAnnealing SA = new SimulatedAnnealing(matrix, supO1, supO2, subO1, subO2, class1o, class2o);
            SA.solve(1);
            List<Pair<Integer, Integer>>  result = SA.getSolution();
            System.out.println("\nSA finished.");
            for (Pair<Integer, Integer> item: result)
                if (matrix[item.getL()][item.getR()] >= threshold)
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

    private double ngramDistance1(String s, String t) {
        int n = 3; // tri-grams for the moment
        if (s == null || t == null) {
            //throw new IllegalArgumentException("Strings must not be null");
            return 1.;
        }
        int l1 = s.length()-n+1;
        int l2 = t.length()-n+1;
        int found = 0;
        for( int i=0; i < l1 ; i++ ){
            for( int j=0; j < l2; j++){
                int k = 0;
                for( ; ( k < n ) && ( s.charAt(i+k) == t.charAt(j+k) ); k++);
                if ( k == n ) found++;
            }
        }
        return 1.0 - (2*((double)found)/((double)(l1+l2)));
    }
}
