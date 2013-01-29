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

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class SSMResult {
	double similarity;
	List<Resource> pathsC1;
	List<Resource> pathsC2;
	Resource lca;
	int milestoneLCAC1;
	int milestoneLCAC2;
	
	public SSMResult(double similarity, List<Resource> pathsC1, List<Resource> pathsC2, Resource lca, int milestoneLCAC1, int milestoneLCAC2) {
		this.similarity = similarity;
		this.pathsC1 = pathsC1;
		this.pathsC2 = pathsC2;
		this.lca = lca;
		this.milestoneLCAC1 = milestoneLCAC1;
		this.milestoneLCAC2 = milestoneLCAC2;
	}
	
	public double getSimilarity() {
		return similarity;
	}
	public List<Resource> getPathsC1() {
		return pathsC1;
	}
	public List<Resource> getPathsC2() {
		return pathsC2;
	}
	public Resource getLca() {
		return lca;
	}
	public int getMilestoneLCAC1() {
		return milestoneLCAC1;
	}
	public int getMilestoneLCAC2() {
		return milestoneLCAC2;
	}
	
	
}
