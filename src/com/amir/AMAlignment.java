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

import java.util.Properties;

public class AMAlignment extends DistanceAlignment implements AlignmentProcess {

    private HeavyLoadedOntology<Object> heavyOntology1;
    private HeavyLoadedOntology<Object> heavyOntology2;
    public AMAlignment() {
        heavyOntology1 = heavyOntology2 = null;
        setType("**");
    }

    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
        super.init( o1, o2, ontologies );
        if ( !( getOntologyObject1() instanceof HeavyLoadedOntology
                && getOntologyObject1() instanceof HeavyLoadedOntology ))
            throw new AlignmentException( "StrucSubsDistAlignment requires HeavyLoadedOntology ontology loader" );
    }
    @SuppressWarnings("Duplicates")
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {

/*
            // *** Method 1 ***
            URI url1 = getOntology1URI();
            URI url2 = getOntology2URI();

            AlignmentProcess  al = new StringDistAlignment();
            al.init( url1, url2 );
            Properties properties = new Properties();
            properties.setProperty( "noinst", "1");
            properties.setProperty( "type", "11");
            //stringFunction: subStringDistance, levenshteinDistance,smoaDistance,teinDistance
            properties.setProperty("stringFunction", "subStringDistance");

            al.align(null, properties);
            al.harden(0.01);

            for ( Cell c : al ){
                addAlignCell(c.getObject1AsURI(al), c.getObject2AsURI(al), c.getRelation().getRelation(), c.getStrength());
            }
*/

/*
            // *** Method 2 ***
            double[][] matrix = new double[ontology1().nbClasses()][ontology2().nbClasses()];
            double ii = 0;
            int jj = -1;
            int nbClass1 = ontology2().nbClasses();
            double step = 100.0 / nbClass1;
            StringDistances stringDistances = new StringDistances();

            for (Object cl2 : ontology2().getClasses()) {
                for (Object cl1 : ontology1().getClasses()) {

                    double distVal = 1 - StringDistances.equalDistance(cl1.toString(), cl2.toString());
                    if(distVal > 0.1) addAlignCell(cl1, cl2, "=", distVal);
                }
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }
*/
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
            String str1, str2;

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

            for( i=0; i < nbClasses1; ++i ) {
                for (j = 0; j < nbClasses2; ++j) {
                    matrix[i][j] = 1.0 - StringDistances.levenshteinDistance(class1s[i], class2s[j]);
                }
                if((ii += step) >= (jj + 1)) System.out.print(String.format("\r%d%% completed!", ++jj));
            }
            double threshold = 0.3;
            int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
            //int[][] result = callHungarianMethod( matrix, nbClasses1, nbClasses2 );

            System.out.println("OK");
            for( i=0; i < result.length ; i++ ){
                double val = matrix[result[i][0]][result[i][1]];
                if( val > threshold ){
                    addAlignCell(class1o[result[i][0]], class2o[result[i][1]], "=", val );
                }
            }

            /*
            // Match classes


            // Match dataProperties
            for (Object p2 : ontology2().getDataProperties()) {
                for (Object p1 : ontology1().getDataProperties()) {
                    // add mapping into alignment object
                    addAlignCell(p1, p2, "=", match(p1, p2));
                }
            }
            // Match objectProperties
            for (Object p2 : ontology2().getObjectProperties()) {
                for (Object p1 : ontology1().getObjectProperties()) {
                    // add mapping into alignment object
                    addAlignCell(p1, p2, "=", match(p1, p2));
                }
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
