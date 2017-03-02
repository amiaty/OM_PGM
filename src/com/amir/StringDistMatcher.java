/*
 * Amir
 */

package com.amir;

import java.net.URI;
import java.lang.Iterable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.semanticweb.owl.align.*;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;


@SuppressWarnings("ALL")
public class StringDistMatcher implements Iterable<Object[]> {

    Set<Object[]> result;

    public StringDistMatcher() {}

    @SuppressWarnings("Duplicates")
    public void match(URI u1, URI u2 ) {
        result = new HashSet<>();
        // Create matcher
        AlignmentProcess  al = new StringDistAlignment();
        try {
            al.init( u1, u2 );

            // Run matcher
            Properties properties = new Properties();
            //properties.setProperty( "noinst", "1");
            //stringFunction: subStringDistance, levenshteinDistance,smoaDistance,teinDistance
            //properties.setProperty("stringFunction", "levenshteinDistance");
            al.align(null, properties);
            // Extract result
            //al.harden(0.8);
            //System.err.println(al.nbCells());

            for ( Cell c : al ) {
                Object[] r = new Object[4];
                r[0] = c.getObject1AsURI( al );
                r[1] = c.getObject2AsURI( al );
                r[2] = c.getRelation().toString();
                r[3] = c.getStrength();
                result.add( r );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Iterator<Object[]> iterator() {
	    return result.iterator();
    }


}

