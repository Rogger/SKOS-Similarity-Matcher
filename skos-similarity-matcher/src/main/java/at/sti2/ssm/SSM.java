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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.ontology.OntTools.Path;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * SSM matches the similarity of two concepts in a SKOS taxonomy. The taxonomy
 * relations broader and narrower and multiple inheritance are supported.
 * Exponential decay can be used using milestone calculation.
 * 
 * For example launch the junit test
 * 
 * @author michaelrogger
 * 
 */
public class SSM {

	protected static Logger logger = Logger.getLogger(SSM.class);

	private OntModel model = null;
	
	private static SSM matchingFramework = null;

	private static final Property skosBroader = new PropertyImpl("http://www.w3.org/2004/02/skos/core#broader");
//	private static final Property skosNarrower = new PropertyImpl("http://www.w3.org/2004/02/skos/core#narrower");

	/**
	 * Create an instance of SSM
	 * @param model is the loaded taxonomy, can be loaded from file using ModelHelper
	 * @return single instance of SSM
	 */
	public static SSM getInstance(OntModel model){
		if (matchingFramework == null || matchingFramework.model != model) {
			matchingFramework = new SSM();
			matchingFramework.model = model;
		}
		return matchingFramework;
	}

	private SSM() {
	}

	/**
	 * Calculate the similarity between two concepts in the taxonomy
	 * @param concept1 is the first concept
	 * @param concept2 is the second concept
	 * @param rootConcept is the root concept of the taxonomy
	 * @param k is the factor that will be used for milestone calculation
	 * @return the similarity between concept1 and concept2. The results are returned as SSMResult object.
	 */
	public SSMResult similarity(String concept1, String concept2,
			String rootConcept, float k) {
		
		SSMResult matcherResult = null;

		Individual indRootConcept= model.getIndividual(rootConcept);
		Individual indConcept1 = model.getIndividual(concept1);
		Individual indConcept2 = model.getIndividual(concept2);
		
		if (model != null && indRootConcept != null && indConcept1 != null && indConcept2 != null) {

			int i=0;
//			Resource resRootConcept = model.getResource(rootConcept);
//			Resource resConcept1 = model.getResource(concept1);
//			Resource resConcept2 = model.getResource(concept2);
			
			// Nodes from concept1 -> rootConcept
			List<List<Resource>> nodes1 = findNodes(model, indConcept1, indRootConcept,
					new OntTools.PredicatesFilter(skosBroader));
			i = 0;
			logger.debug("Nodes from " + indConcept1 + " to "
					+ indRootConcept);
			for (List<Resource> node : nodes1) {
				i++;
				logger.debug("node " + i + ": " + getNodeString(node));
			}

			// Paths from concept2 -> rootConcept
			List<List<Resource>> nodes2 = findNodes(model, indConcept2, indRootConcept,
					new OntTools.PredicatesFilter(skosBroader));
			i = 0;
			logger.debug("Nodes from " + indConcept2 + " to "
					+ indRootConcept);
			for (List<Resource> node : nodes2) {
				i++;
				logger.debug("node " + i + ": " + getNodeString(node));
			}
			
			double similarityFinalScore = 0;
			
			// For all paths of concept1 and concept2 compute similarity
			for(List<Resource> node1 : nodes1){
				for(List<Resource> node2 : nodes2){
					logger.debug("Calculate LCA");
					logger.debug(getNodeString(node1));
					logger.debug(getNodeString(node2));
					Resource lca = getLCA(node1, node2);
					
					int milestonePositionLCANode1 = calculateMilestonePosition(lca, node1);
					int milestonePositionLCANode2 = calculateMilestonePosition(lca, node2);
					
					int pathLengthNode1 = node1.size()-1;
					int pathLengthNode2 = node2.size()-1;
												
					double similarity = ( calculateMilestone(milestonePositionLCANode1, k) - calculateMilestone(pathLengthNode1, k) ) 
									  + ( calculateMilestone(milestonePositionLCANode2, k) - calculateMilestone(pathLengthNode2, k) );
					similarity = 1 - similarity;
					
					logger.debug("Similarity: "+similarity+" "+concept1+" <-> "+concept2);
					
					// Select MAX
					if(similarity > similarityFinalScore){
						similarityFinalScore = similarity;
						matcherResult = new SSMResult(similarity, node1, node2, lca, milestonePositionLCANode1, milestonePositionLCANode2);
					}
					
				}
			}
			
			logger.info("-- Similarity: "+similarityFinalScore+" "+concept1+" <-> "+concept2+" --");
		}

		return matcherResult;
	}
	
	public double milestoneDistance(String concept1, String rootConcept, float k) {
		
		Individual indRootConcept= model.getIndividual(rootConcept);
		Individual indConcept1 = model.getIndividual(concept1);

		double milestone=0;
		if (model != null && indRootConcept != null && indConcept1 != null) {

			int i=0;
			
			// Nodes from concept1 -> rootConcept
			List<List<Resource>> nodes1 = findNodes(model, indConcept1, indRootConcept,
					new OntTools.PredicatesFilter(skosBroader));
			i = 0;
			logger.debug("Nodes from " + indConcept1 + " to "
					+ indRootConcept);
			for (List<Resource> node : nodes1) {
				i++;
				logger.debug("node " + i + ": " + getNodeString(node));
			}
			
			for(List<Resource> node1 : nodes1){
				
				int pathLengthNode1 = node1.size()-1;
				double currentMilestone = calculateMilestone(pathLengthNode1, k);
				//store maximum milestone :> means shortes path from concept to root
				if(currentMilestone > milestone){
					milestone = currentMilestone;
				}
			}
			
			
			logger.info("-- Distance for concept: "+concept1+":"+milestone+" --");
		}
		return milestone;

	}

	private List<List<Resource>> findNodes(Model m, Resource start, RDFNode end,Filter<Statement> onPath) {
		
		List<Path> bfs = new LinkedList<Path>();
		Set<Resource> seen = new HashSet<Resource>();
		List<List<Resource>> solutions = new ArrayList<List<Resource>>();

		// trivial case
		if(start.equals(end)){
			List<Resource> nodes = new ArrayList<Resource>();
			nodes.add(start);
			solutions.add(nodes);
			
		}else{
			// initialise the paths
			for (Iterator<Statement> i = m.listStatements(start, (Property) null,
					(RDFNode) null).filterKeep(onPath); i.hasNext();) {
				bfs.add(new Path().append(i.next()));
			}
			
			// search
			while (!bfs.isEmpty()) {
				Path candidate = bfs.remove(0);
				
				if (candidate.hasTerminus(end)) {
					List<Resource> nodes = convertPathToNodes(candidate);
					solutions.add(nodes);
				} else {
					Resource terminus = candidate.getTerminalResource();
					if (terminus != null) {
						seen.add(terminus);
						
						// breadth-first expansion
						for (Iterator<Statement> i = terminus.listProperties()
								.filterKeep(onPath); i.hasNext();) {
							Statement link = i.next();
							
							// no looping allowed, so we skip this link if it takes
							// us to a node we've seen
							if (!seen.contains(link.getObject())) {
								bfs.add(candidate.append(link));
							}
						}
					}
				}
			}
		}

		return solutions;
	}

	private String getNodeString(List<Resource> path) {
		StringBuffer sb = new StringBuffer();
		for (Resource res : path) {
			sb.append(res.toString()).append(" -> ");
		}

		return sb.toString();
	}
	
	private List<Resource> convertPathToNodes(Path path1){
		List<Resource> resList = new ArrayList<Resource>();
		
		for(int i = 0 ; i < path1.size() ; i++){
			Resource subject = path1.get(i).getSubject();
			resList.add(subject);
			if(i == path1.size()-1){
				Resource object = path1.get(i).getObject().asResource();
				resList.add(object);
			}
		}
		
		return resList;
	}
	
	private Resource getLCA(List<Resource> nodes1, List<Resource> nodes2){
		
		Resource stm1LCA = null;
		Resource stm2LCA = null;
		int path1LCAIndex=-1;
		int path2LCAIndex=-1;

		for(int i = 0 ; i < nodes1.size(); i++){
			
			Resource res1 = nodes1.get(i);
			int j=0;
			
			if((j = nodes2.indexOf(res1)) != -1){
				
				path1LCAIndex = i;
				path2LCAIndex = j;
				stm1LCA = res1;
				stm2LCA = nodes2.get(j);
				
				break;
			
			}
		}
		
		if(stm1LCA!=null){
			logger.info("Found LCA "+ stm1LCA);			
		}
		
		return stm1LCA;
	}
	
	private double calculateMilestone(int n, double k){
		double power = Math.pow(k, n);
		return 0.5 / power;
	}
	
	private int calculateMilestonePosition(Resource LCA, List<Resource> path){
		int indexOfLCA = path.indexOf(LCA);
		int milestone=0;
		if(indexOfLCA!=-1){
			milestone = ( path.size() - 1 ) - indexOfLCA;			
		}
		return milestone;
	}

}
