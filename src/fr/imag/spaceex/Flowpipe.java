/** SpaceEx Adapter for Polygonal Planar Projection LIBrary (3plib)
 ** Copyright © 2016 Frédéric Viry
 ** author: Frédéric Viry (Laboratoire Verimag, Grenoble, France)
 ** mail: frederic.viry@grenoble-inp.org
 **
 ** This file is part of spaceex-adapter.
 **
 ** spaceex-adapter is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU Lesser General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** at your option) any later version.
 ** 
 ** spaceex-adapter is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU Lesser General Public License for more details.
 ** 
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with spaceex-adapter. If not, see <http://www.gnu.org/licenses/>.
 **/

package fr.imag.spaceex;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import fr.imag.ppplib.*;

/** A class that represents a Flowpipe of SpaceEx, which makes it possible to compute some operations (e.g. the composition by cartesian product).
 ** It can give a List of Polyhedra representation of the Flowpipe, for 3plib.
 **/

public class Flowpipe
{
    /** Add a hyperplane in time.
     ** @param h the hyperplane.
     ** @exception FlowpipeException thrown if the added hyperplane does not live in the same vectorspace than the others.
     **/
    public void addHyperplane(HyperplaneInTime h)
    {
        /* init of the dimension (for exceptions) */
        if (!initialized)
            dim = h.getDimension();
        /* exception */
        if (h.getDimension() != dim)
            throw new FlowpipeException(nsvsMessage);
        /* add the hyperplane */
        hitl.add(h);
        initialized = true;
    }
    
    /** Perform the composition by cartesian product of the current Flowpipe with a given Flowpipe.
     ** @param f a flowpipe.
     ** @return the composed flowpipe.
     **/
    public Flowpipe composeWith(Flowpipe f)
    {
        /* init */
        int dim1 = dim;
        int dim2 = f.dim;
        int dimTot = dim1 + dim2;
        Flowpipe res = new Flowpipe();
        /* add each hyperplane of the current flowpipe */
        for (HyperplaneInTime h : hitl)
        {
            double[] d = h.getNormalDirection();
            double[] dir = new double[dimTot];
            for (int i = 0 ; i < dim1 ; i++) dir[i] = d[i];
            HyperplaneInTime hyp = new HyperplaneInTime(h, dir);
            res.addHyperplane(hyp);
        }
        /* add each hyperplane of f */
        for (HyperplaneInTime h : f.hitl)
        {
            double[] d = h.getNormalDirection();
            double[] dir = new double[dimTot];
            for (int i = dim1 ; i < dimTot ; i++) dir[i] = d[i-dim1];
            HyperplaneInTime hyp = new HyperplaneInTime(h, dir);
            res.addHyperplane(hyp);
        }
        return res;
    }
    
    /** Give the flowpipe as a list of polyhedra. Note that one dimension is added for time.
     ** @return the flowpipe as a list of polyhedra, ordened by time.
     **/
    public List <ConvexPolyhedronSupportFunction> polyhedralRepresentation()
    {
        // TODO: really not efficient solution and some DIY
        // to improve only if necessary
        
        /* construct the set of time steps, and sort it */
        Set <Double> timeSet = new HashSet <Double>();
        for (HyperplaneInTime h : hitl)
            for (Double t : h.getTimeSteps()) timeSet.add(t);
        Iterator <Double> it = timeSet.iterator();
        List <Double> timeSteps = new ArrayList <Double>();
        while (it.hasNext()) timeSteps.add(it.next());
        Collections.sort(timeSteps);
        
        /* construct each polyhedra (one for each step), adding a new dimension: time */
        List <ConvexPolyhedronSupportFunction> res = new ArrayList <ConvexPolyhedronSupportFunction>();
        for (Double t : timeSteps)
        {
            ConvexPolyhedronSupportFunction p = new ConvexPolyhedronSupportFunction();
            int n = 0;
            boolean initN = false;
            /* spatial constraints */
            for (HyperplaneInTime h : hitl)
            {
                if (!initN)
                    n = h.getDimension();
                initN = true;
                double[] d = h.getNormalDirection();
                double[] dir = new double[n+1];
                for (int i = 0 ; i < n ; i++) dir[i] = d[i];
                p.addLinearConstraint(dir, h.getConstraint(t));
            }
            /* time constraints */
            double[] dirTP = new double[n+1];
            dirTP[n] = 1.0;
            p.addLinearConstraint(dirTP, t);
            double[] dirTM = new double[n+1];
            dirTM[n] = -1.0;
            p.addLinearConstraint(dirTM, -t);
            /* add the constructed polyhedron */
            res.add(p);
        }
        return res;
    }
    
    /** Give a textual description of the current flowpipe.
     ** @return the textual description.
     **/
    @Override
    public String toString()
    {
        String res = "flowpipe:\n";
        for (HyperplaneInTime h : hitl) res += " " + h + "\n";
        return res;
    }
    
    private boolean initialized = false;
    private int dim;
    private List <HyperplaneInTime> hitl = new ArrayList <HyperplaneInTime>();
    private static final String nsvsMessage = "The added hyperplane does not live in the same vectorspace than the others.";
}
