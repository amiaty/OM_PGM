/*
    File: AMAlignment.java
    Copyright (C) Amir Ahooye Atashin, Ferdowsi Univerity of Mashhad, 2017 (1395-1396 Hijri-Shamsi)
    Authors:    Amir Ahooye Atashin,
                Majid Mohammadi
 */
package com.amir;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.ontosim.string.JWNLDistances;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

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
            throw new AlignmentException( "AMAlignment requires HeavyLoadedOntology ontology loader" );
    }
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            JWNLDistances Dist = new JWNLDistances();
            Dist.Initialize("./dict", "3.1");
            heavyOntology1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
            heavyOntology2 = (HeavyLoadedOntology<Object>)getOntologyObject2();

            int nbClasses1 = heavyOntology1.nbEntities();
            int nbClasses2 = heavyOntology2.nbEntities();

            double[][] matrix = new double[nbClasses1][nbClasses2];
            Object[] class1o = new Object[nbClasses1];
            Object[] class2o = new Object[nbClasses2];
            String[] class1s = new String[nbClasses1];
            String[] class2s = new String[nbClasses2];
            int i = 0;
            String str1;

            for ( Object ob : getOntologyObject1().getEntities() ) {
                //str1 = heavyOntology1.getEntityAnnotations(ob).iterator().next();
                str1 = heavyOntology1.getEntityName(ob);
                class1s[i] = str1.trim().replaceAll("_", " ").toLowerCase();
                class1o[i++] = ob;
            }

            int j = 0;
            for ( Object ob : getOntologyObject2().getEntities() ) {
                //str1 = heavyOntology2.getEntityAnnotations(ob).iterator().next();
                str1 = heavyOntology2.getEntityName(ob);
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
                    //m2 = 1.0 - StringDistances.equalDistance(class1s[i], class2s[j]);
                    m2 = Dist.wuPalmerSimilarity(class1s[i], class2s[j]);
                    m2 = Math.min(1.0, m2);
                    matrix[i][j] = Math.max(m1, m2);
                }
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }

            List<Set> supO1 = new ArrayList<>();
            List<Set> supO2 = new ArrayList<>();
            List<Set> subO1 = new ArrayList<>();
            List<Set> subO2 = new ArrayList<>();
            /*
            for( i = 0; i < nbClasses1; ++i ) {
                supO1.add(((OWLClassImpl)class1o[i]).getSuperClasses((OWLOntology) heavyOntology1.getOntology()));
                subO1.add(((OWLClassImpl)class1o[i]).getSubClasses((OWLOntology) heavyOntology1.getOntology()));
            }
            for( i = 0; i < nbClasses2; ++i ) {
                supO2.add(((OWLClassImpl)class2o[i]).getSuperClasses((OWLOntology) heavyOntology2.getOntology()));
                subO2.add(((OWLClassImpl)class2o[i]).getSubClasses((OWLOntology) heavyOntology2.getOntology()));
            }
*/
            System.out.println("\nRunning SA:");
            double threshold = 0.1;
            SimulatedAnnealing SA = new SimulatedAnnealing(matrix, supO1, supO2, subO1, subO2, class1o, class2o);
            SA.solve(1000);
            List<Pair<Integer, Integer>>  result = SA.getSolution();
            System.out.println("\nSA finished.");
            for (Pair<Integer, Integer> item: result)
                if (matrix[item.getL()][item.getR()] >= threshold)
                    addAlignCell(class1o[item.getL()], class2o[item.getR()], "=", matrix[item.getL()][item.getR()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
