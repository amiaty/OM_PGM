/**
 * Created by Amir on 10/02/2017.
 * wnfunction:
 * BASICSYNDIST,COSYNSIM,BASICSYNSIM,WUPALMER,GLOSSOV
 * basicSynonymDistance,cosynonymySimilarity,basicSynonymySimilarity,wuPalmerSimilarity,glossOverlapSimilarity
 *
 *
 */
package com.amir;
import fr.inrialpes.exmo.align.ling.JWNLAlignment;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;



public class LingMatcher implements Iterable<Object[]> {

    Set<Object[]> result;

    public LingMatcher() {}

    @SuppressWarnings("Duplicates")
    public void match(URI u1, URI u2 ) {
        result = new HashSet<>();
        // Create matcher
        AlignmentProcess al = new JWNLAlignment();
        try {
            al.init( u1, u2 );
            Properties properties = new Properties();
            properties.put("wnvers", "3.1");
            properties.put("wnfunction", 0);
            properties.put("wndict", "./dict");
            // Run matcher
            al.align(null, properties );
            // Extract result
            for ( Cell c : al ) {
                Object[] r = new Object[4];
                r[0] = c.getObject1AsURI( al );
                r[1] = c.getObject2AsURI( al );
                r[2] = c.getRelation().toString();
                r[3] = c.getStrength();
                result.add( r );
            }
        } catch (AlignmentException ex) {
            Main.logger.debug(ex.getMessage());
        }
    }

    public Iterator<Object[]> iterator() {
        return result.iterator();
    }


}
