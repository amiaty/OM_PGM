/*
    File: AMAlignment.java
    Copyright (C) Amir Ahooye Atashin, Ferdowsi Univerity of Mashhad, 2017 (1395-1396 Hijri-Shamsi)
    Authors:    Amir Ahooye Atashin,
                Majid Mohammadi
 */
package com.amir;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.method.*;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;

import org.semanticweb.owl.align.Cell;

import java.net.URI;
import java.util.Properties;

public class AMAlignment extends DistanceAlignment implements AlignmentProcess {

    public AMAlignment() {
    }
    @SuppressWarnings("Duplicates")
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
/*
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

            //double[][] matrix = new double[ontology1().nbClasses()][ontology2().nbClasses()];
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

            int nbclasses1 = getOntologyObject1().nbClasses();
            int nbclasses2 = getOntologyObject2().nbClasses();
            double[][] matrix = new double[nbclasses1][nbclasses2];
            Object[] class1 = new Object[nbclasses1];
            Object[] class2 = new Object[nbclasses2];
            int i = 0;
            for ( Object ob : getOntologyObject1().getClasses() ) {
                class1[i++] = ob;
            }
            i = 0;
            for ( Object ob : getOntologyObject2().getClasses() ) {
                class2[i++] = ob;
            }

            double threshold = 0.5;
            int[][] result = callHungarianMethod( matrix, nbclasses1, nbclasses2 );
            for( i=0; i < result.length ; i++ ){
                double val = 1 - stringDistances.equalDistance(class1[result[i][0]].toString(),class2[result[i][1]].toString());
                if( val > threshold ){
                    addAlignCell(class1[result[i][0]], class2[result[i][1]], "=", val );
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
