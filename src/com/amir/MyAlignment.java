/*
 * $Id: MyAlignment.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2010
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package com.amir;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import java.net.URI;
import java.util.Properties;

public class MyAlignment extends URIAlignment implements AlignmentProcess {

    public MyAlignment() {}
     
    /* init( onto1, onto2 ) is inherited */
	@SuppressWarnings("Duplicates")
    public void align( Alignment alignment, Properties params ) throws AlignmentException {

		URI url1 = getOntology1URI();
		URI url2 = getOntology2URI();

		LingMatcher matcher = new LingMatcher();
		matcher.match( url1, url2 );

		for ( Object[] c : matcher ){
			addAlignCell(c[0], c[1], (String)c[2], (Double) c[3]);
		}
    } 
}

 
