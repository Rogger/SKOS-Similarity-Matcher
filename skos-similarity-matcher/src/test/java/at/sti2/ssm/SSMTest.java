/*	SSM - SKOS similarity matcher
    Copyright (C) 2012  Michael Rogger

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.sti2.ssm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class SSMTest {
	
	protected static Logger logger = Logger.getLogger(SSMTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSSM(){
		
		String NS = "http://sti2.at/ontologies/2011/10/crs/junit-taxonomy#";
		String rootConcept = NS+"R";
		double similarity;
		float k = 2.0f;
		
		//Load Taxonomy
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource("junit-taxonomy.ttl");
				
		OntModel model = ModelHelper.loadModelFromFile(resource.toString(), ModelHelper.FORMAT_TTL);
		
		SSM matcher = SSM.getInstance(model);
		
		
//		matcher.getLCA(R,R) == R
//		matcher.similarity(R,R) == 1
//		matcher.similarity(A, R).getLCA == R
//		matcher.similarity(R, A).getLCA == R
//		matcher.similarity(A, R) == 0.5
//		matcher.similarity(T1, T2).paths == 2
//		matcher.similarity(T1,T2) == 0.6875
		
		
		// R <-> R = 1
		similarity = matcher.similarity(NS+"R",NS+"R", rootConcept, k).getSimilarity();
		assertEquals(1,similarity, 0);
		
		// R <-> R = R
		Resource lca = matcher.similarity(NS+"R",NS+"R", rootConcept, k).getLca();
		assertTrue(lca.getLocalName().equals("R"));
		
		// A <-> R = R
		lca = matcher.similarity(NS+"A",NS+"R", rootConcept, k).getLca();
		assertTrue(lca.getLocalName().equals("R"));
		
		// R <-> A = R
		lca = matcher.similarity(NS+"R",NS+"A", rootConcept, k).getLca();
		assertTrue(lca.getLocalName().equals("R"));
		
		// A <-> R = 0.75
		similarity = matcher.similarity(NS+"A",NS+"R", rootConcept, k).getSimilarity();
		assertEquals(0.75, similarity, 0);
		
		//TODO T1 <-> T2 = 2 paths

		// T1 <-> T2 = 0.6875
		similarity = matcher.similarity(NS+"T1",NS+"T2", rootConcept, k).getSimilarity();
		assertEquals(0.6875, similarity, 0);
		
		double distance = matcher.milestoneDistance(NS+"T1", rootConcept, k);
		distance = matcher.milestoneDistance(NS+"T2", rootConcept, k);
		
	}

}
