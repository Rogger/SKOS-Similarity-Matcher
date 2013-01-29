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

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Simple Helper to load an ontology model from file
 * @author michaelrogger
 *
 */

public class ModelHelper {
	
	protected static Logger logger = Logger.getLogger(ModelHelper.class);
	
	public final static String FORMAT_TTL = "TTL";
	
	public static OntModel loadModelFromFile(String path, String format) {
		
		OntModel model = ModelFactory.createOntologyModel();
		model.read(path, format);
		return model;
	}

}
