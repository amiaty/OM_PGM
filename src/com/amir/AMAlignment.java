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
            throw new AlignmentException( "AMAlignment requires HeavyLoadedOntology ontology loader" );
    }
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            JWNLDistances Dist = new JWNLDistances();
            Dist.Initialize("./dict", "3.1");
            heavyOntology1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
            heavyOntology2 = (HeavyLoadedOntology<Object>)getOntologyObject2();
            String p1 = param.getProperty("ObjType", "all");
            int nbEntities1;
            int nbEntities2;
            Object[] entity1o;
            Object[] entity2o;
            switch (p1) {
                case "class":
                    nbEntities1 = heavyOntology1.nbClasses();
                    nbEntities2 = heavyOntology2.nbClasses();
                    entity1o = heavyOntology1.getClasses().toArray();
                    entity2o = heavyOntology2.getClasses().toArray();
                    break;
                case "property":
                    nbEntities1 = heavyOntology1.nbProperties();
                    nbEntities2 = heavyOntology2.nbProperties();
                    entity1o = heavyOntology1.getProperties().toArray();
                    entity2o = heavyOntology2.getProperties().toArray();
                    break;
                case "data_property":
                    nbEntities1 = heavyOntology1.nbDataProperties();
                    nbEntities2 = heavyOntology2.nbDataProperties();
                    entity1o = heavyOntology1.getDataProperties().toArray();
                    entity2o = heavyOntology2.getDataProperties().toArray();
                    break;
                case "object_property":
                    nbEntities1 = heavyOntology1.nbObjectProperties();
                    nbEntities2 = heavyOntology2.nbObjectProperties();
                    entity1o = heavyOntology1.getObjectProperties().toArray();
                    entity2o = heavyOntology2.getObjectProperties().toArray();
                    break;
                case "individual":
                    nbEntities1 = heavyOntology1.nbIndividuals();
                    nbEntities2 = heavyOntology2.nbIndividuals();
                    entity1o = heavyOntology1.getIndividuals().toArray();
                    entity2o = heavyOntology2.getIndividuals().toArray();
                    break;
                default:
                    nbEntities1 = heavyOntology1.nbEntities();
                    nbEntities2 = heavyOntology2.nbEntities();
                    entity1o = heavyOntology1.getEntities().toArray();
                    entity2o = heavyOntology2.getEntities().toArray();
                    break;
            }

            double[][] matrix = new double[nbEntities1][nbEntities2];
            String[] entity1s = new String[nbEntities1];
            String[] entity2s = new String[nbEntities2];
            int i = 0;
            String str1;

            for ( Object ob : entity1o ) {
                //str1 = heavyOntology1.getEntityAnnotations(ob).iterator().next();
                str1 = heavyOntology1.getEntityName(ob);
                entity1s[i++] = str1.trim().replaceAll("_", " ").toLowerCase();
            }

            int j = 0;
            for ( Object ob : entity2o ) {
                //str1 = heavyOntology2.getEntityAnnotations(ob).iterator().next();
                str1 = heavyOntology2.getEntityName(ob);
                entity2s[j++] = str1.trim().replaceAll("_", " ").toLowerCase();
            }

            double ii = 0, m1, m2;
            double step = 100.0 / nbEntities1;

            System.out.println("Preparing:");
            // make similarity matrix
            for( i = 0; i < nbEntities1; ++i, ii += step) {
                for (j = 0; j < nbEntities2; ++j) {
                    m1 = 1.0 - StringDistances.levenshteinDistance(entity1s[i], entity2s[j]);
                    m2 = 1.0 - StringDistances.smoaDistance(entity1s[i], entity2s[j]);
                    //m2 = Dist.computeSimilarity(entity1s[i], entity2s[j]);
                    m2 = Math.min(1.0, m2);
                    matrix[i][j] = Math.max(m1, m2);
                }
                System.out.print(String.format("\r%d%% completed!", (int)(ii + step)));
            }

            List<Set> supO1 = new ArrayList<>();
            List<Set> supO2 = new ArrayList<>();
            List<Set> subO1 = new ArrayList<>();
            List<Set> subO2 = new ArrayList<>();

            if(Objects.equals(p1, "class")) {
                for (i = 0; i < nbEntities1; ++i) {
                    supO1.add(((OWLClassImpl) entity1o[i]).getSuperClasses((OWLOntology) heavyOntology1.getOntology()));
                    //subO1.add(((OWLClassImpl)entity1o[i]).getSubClasses((OWLOntology) heavyOntology1.getOntology()));
                }
                for (i = 0; i < nbEntities2; ++i) {
                    supO2.add(((OWLClassImpl) entity2o[i]).getSuperClasses((OWLOntology) heavyOntology2.getOntology()));
                    //subO2.add(((OWLClassImpl)entity2o[i]).getSubClasses((OWLOntology) heavyOntology2.getOntology()));
                }
            }
            System.out.println("\nRunning SA:");
            double threshold = 0.5;
            SimulatedAnnealing SA = new SimulatedAnnealing(matrix, supO1, supO2, subO1, subO2, entity1o, entity2o);
            SA.solve(100);
            List<Pair<Integer, Integer>>  result = SA.getSolution();
            System.out.println("\nSA finished.");
            for (Pair<Integer, Integer> item: result)
                if (matrix[item.getL()][item.getR()] >= threshold)
                    addAlignCell(entity1o[item.getL()], entity2o[item.getR()], "=", matrix[item.getL()][item.getR()]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
