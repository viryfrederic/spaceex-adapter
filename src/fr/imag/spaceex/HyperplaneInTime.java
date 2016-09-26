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
import java.util.Collections;
import java.util.Comparator;

/** A class that represents an hyperplane evolving in time piecewise linearly.
 ** i.e. H = {x | d.x <= b(t)} and we have:
 ** For all t < tmin, b(t) = b(tmin), for all t > tmin, b(t) = b(tmax), affine between ti and ti+1, continue
 **/

public class HyperplaneInTime
{
    /** Create a new hyperplane evolving in time.
     ** @param d a normal direction of the hyperplane.
     **/
    public HyperplaneInTime(double[] d)
    {
        this.d = d;
    }
    
    /** Create a copy of the given hyperplane with a different direction.
     **/
    public HyperplaneInTime(HyperplaneInTime h, double[] d)
    {            
        this.d = d;
        for (double[] constraint : h.constraintsInTime) addConstraintInTime(constraint[0], constraint[1]);
    }
    
    /** Give the dimension of the vectorspace where lives this hyperplane.
     ** @return the dimension.
     **/
    public int getDimension()
    {
        return d.length;
    }
    
    /** Give the normal direction of the hyperplane.
     ** @return the direction.
     **/
    public double[] getNormalDirection()
    {
        return d;
    }
    
    /** Add a constraint at a specific time.
     **
     **/
    public void addConstraintInTime(double t, double b)
    {
        constraintsInTime.add(new double[] {t, b});
    }
    
    /** Evaluate b(t).
     ** @param t the time value.
     ** @return b(t).
     **/
    public double getConstraint(double t)
    {
        checkForSorted();
        /* searching for an interval t in [ti-1, ti] if possible */
        if (t < constraintsInTime.get(0)[0])
            return constraintsInTime.get(0)[1];
        int i = 1;
        int n = constraintsInTime.size();
        while (i < n && constraintsInTime.get(i)[0] < t) i++;
        if (i == n)
            return constraintsInTime.get(i-1)[1];
        /* if t is in an existent interval, where have to compute b(t) */
        else
        {
            double tiM1 = constraintsInTime.get(i-1)[0];
            double ti = constraintsInTime.get(i)[0];
            double tp = (t - tiM1)/(ti - tiM1);
            return (1-tp)*constraintsInTime.get(i-1)[1] + tp*constraintsInTime.get(i)[1];
        }
    }
    
    /** Give a list of the "time steps" of this hyperplane.
     ** @return the list.
     **/
    public List <Double> getTimeSteps()
    {
        List <Double> res = new ArrayList <Double> ();
        for (double[] constraint : constraintsInTime) res.add(constraint[0]);
        return res;
    }
    
    /** Give a textual description of the current hyperplane in time.
     ** @return the textual description.
     **/
    @Override
    public String toString()
    {
        String res = "( ";
        for (int i = 0 ; i < d.length ; i++) res += d[i] + " ";
        res += ") ";
        for (double[] constraint : constraintsInTime) res += "[" + constraint[0] + " -> " + constraint[1] + "] ";
        return res;
    }
    
    private void checkForSorted()
    {
        if (!sorted)
        {
            Collections.sort(constraintsInTime, new Comparator <double[]> ()
                {
                    public int compare(double[] a, double[] b)
                    {
                        if (a[0] < b[0])
                            return -1;
                        else if (a[0] == b[0])
                            return 0;
                        else return 1;
                    }
                });
        }
        sorted = true;
    }
    
    private double[] d;
    private List <double[]> constraintsInTime = new ArrayList <double[]>();
    private boolean sorted = false;
}
