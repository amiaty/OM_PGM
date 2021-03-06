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
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class PaperAlignment extends DistanceAlignment implements AlignmentProcess {

    private LoadedOntology<Object> heavyOntology1;
    private LoadedOntology<Object> heavyOntology2;
    public PaperAlignment() {
        heavyOntology1 = heavyOntology2 = null;
        setType("**");
    }

    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
        super.init( o1, o2, ontologies );
    }
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            heavyOntology1 = getOntologyObject1();
            heavyOntology2 = getOntologyObject2();

            int nbClasses1 = heavyOntology1.nbClasses();
            int nbClasses2 = heavyOntology2.nbClasses();

            int methodNum = 1;
            switch (param.getProperty("StringDistanceMethod"))
            {
                case "Levenshtein":
                    methodNum = 1;
                    break;
                case "SMOA":
                    methodNum = 2;
                    break;
                case "JaroWinkler":
                    methodNum = 3;
                    break;
                case "N-gram":
                    methodNum = 4;
                    break;
                case "Equal":
                    methodNum = 5;
                    break;
                case "Hamming":
                    methodNum = 6;
                    break;
                case "Jaro":
                    methodNum = 7;
                    break;
                case "NeedlemanWunsch2":
                    methodNum = 8;
                    break;
                case "SubString":
                    methodNum = 9;
                    break;
            }

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
                for (j = 0; j < nbClasses2; ++j) {
                    switch (methodNum)
                    {
                        case 1:
                            matrix[i][j] = 1.0 - StringDistances.levenshteinDistance(class1s[i], class2s[j]);
                            break;
                        case 2:
                            matrix[i][j] = 1.0 - StringDistances.smoaDistance(class1s[i], class2s[j]);
                            break;
                        case 3:
                            matrix[i][j] = 1.0 - StringDistances.jaroWinklerMeasure(class1s[i], class2s[j]);
                            break;
                        case 4:
                            matrix[i][j] = 1.0 - StringDistances.ngramDistance(class1s[i], class2s[j]);
                            break;
                        case 5:
                            matrix[i][j] = 1.0 - StringDistances.equalDistance(class1s[i], class2s[j]);
                            break;
                        case 6:
                            matrix[i][j] = 1.0 - StringDistances.hammingDistance(class1s[i], class2s[j]);
                            break;
                        case 7:
                            matrix[i][j] = 1.0 - StringDistances.jaroMeasure(class1s[i], class2s[j]);
                            break;
                        case 8:
                            matrix[i][j] = 1.0 - StringDistances.needlemanWunsch2Distance(class1s[i], class2s[j]);
                            break;
                        case 9:
                            matrix[i][j] = 1.0 - StringDistances.subStringDistance(class1s[i], class2s[j]);
                            break;
                    }
                }
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }
            //int[][] result = callHungarianMethod(matrix, nbClasses1, nbClasses2);
            int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
            //List<Pair<Integer, Integer>>  result = greedyExtract( matrix, nbClasses1, nbClasses2);
            /*System.out.println("\nOK");
            for (Pair<Integer, Integer> item: result)
                if(matrix[item.getL()][item.getR()] <= 1.0)
                    addAlignCell(class1o[item.getL()], class2o[item.getR()], "=", matrix[item.getL()][item.getR()]);
                else
                    addAlignCell(class1o[item.getL()], class2o[item.getR()], "=", 1);
            */
            for (i = 0; i < result.length ; ++i)
                if (matrix[result[i][0]][result[i][1]] <= 1.0)
                    addAlignCell(class1o[result[i][0]], class2o[result[i][1]], "=", matrix[result[i][0]][result[i][1]]);
                else
                    addAlignCell(class1o[result[i][0]], class2o[result[i][1]], "=", 1.0);
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
}
