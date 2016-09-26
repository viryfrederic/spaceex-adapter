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

import fr.imag.ppplib.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;

/** A class which provides a tool to parse a SpaceEx txt flowpipe into a list of SupportFunction.
 **/

public class SpaceExOutputParser
{
    /** Parse the given txt SpaceEx output file, and compute workable flowpipes for the 3plib.
     ** @exception SpaceExOutputParserException thrown if the file has not been found, or if the file is corrupted.
     **/
    public SpaceExOutputParser(String fileName)
    {
        try
        {
            File f = new File(fileName);
            br = new BufferedReader(new FileReader(f));
            
            /* File reading and splitting into flowpipes */
            StringBuffer sb = new StringBuffer((int)(f.length()));
            String l;
            while((l = br.readLine()) != null) sb.append(l);
            String[] fps = pNewFlowpipe.split(sb);
            
            /* Variables and dimension reading (in the first flowpipe) */
            Matcher mVarDom = pVarDomain.matcher(fps[0]);
            mVarDom.find();
            mVarDom = pExtractVarDomain.matcher(mVarDom.group());
            mVarDom.find();
            String varsString = mVarDom.group();
            varsString = varsString.substring(1, varsString.length()-1);
            String[] vars = pComma.split(varsString);
            d = vars.length;
            varMap = new HashMap <String, Integer>(d);
            for (int i = 0 ; i < d ; i++)
            {
                Matcher m = pVarName.matcher(vars[i].trim());
                m.find();
                varMap.put(m.group(), i);
            }
            
            /* Data reading */
            for (int i = 0 ; i < fps.length ; i++)
            {
                /* 1 flowpipe reading */
                String[] dirsAndSecondMember = pNewDir.split(fps[i]);
                /* Flowpipe creation */
                Flowpipe fp = new Flowpipe();
                /* NB : The first string is not a direction */
                for (int j = 1 ; j < dirsAndSecondMember.length ; j++)
                {
                    /* Direction extraction */
                    double[] dir = new double[d];
                    Matcher mDir = pExtractDir.matcher(dirsAndSecondMember[j]);
                    mDir.find();
                    Matcher mDirExtracted = pExtractVarDomain.matcher(mDir.group());
                    mDirExtracted.find();
                    String dirExtractedString = mDirExtracted.group();
                    dirExtractedString = dirExtractedString.substring(1, dirExtractedString.length()-1);
                    String[] coordString = pComma.split(dirExtractedString);
                    for (int k = 0 ; k < coordString.length ; k++)
                    {
                        String[] varAndCoord = pExtractCoord.split(coordString[k]);
                        dir[varMap.get(varAndCoord[0])] = Double.parseDouble(varAndCoord[1]);
                    }
                    /* Construct the hyperplane in time */
                    HyperplaneInTime h = new HyperplaneInTime(dir);
                    /* Constraints in time extraction */
                    Matcher mSecMemb = pExtractSecMemb.matcher(dirsAndSecondMember[j]);
                    mSecMemb.find();
                    Matcher mSecMemb2 = pExtractSecMemb2.matcher(mSecMemb.group());
                    mSecMemb2.find();
                    Matcher mSecMembNumb = pExtractNumb.matcher(mSecMemb2.group());
                    while (mSecMembNumb.find())
                    {
                        double t = Double.parseDouble(mSecMembNumb.group());
                        mSecMembNumb.find();
                        double s1 = Double.parseDouble(mSecMembNumb.group());
                        mSecMembNumb.find();
                        double s2 = Double.parseDouble(mSecMembNumb.group());
                        mSecMembNumb.find();
                        double s3 = Double.parseDouble(mSecMembNumb.group());
                        double b = Math.max(s1,Math.max(s2, s3));
                        h.addConstraintInTime(t, b);
                    }
                    /* Add to the current FlowPipe */
                    fp.addHyperplane(h);
                }                
                /* Add the flowpipe to the result */
                flowpipesResult.add(fp);
            }
        }
        catch(FileNotFoundException e)
        {
            throw new SpaceExOutputParserException(e.getMessage());
        }
        catch(Exception e)
        {
            throw new SpaceExOutputParserException(cfMessage);
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /** Give flowpipes, as list of polyhedron for each one.
     ** @return the flowpipe list.
     **/
    public List <Flowpipe> getFlowpipes()
    {
        return flowpipesResult;
    }
    
    /** Give the dimension of the vectorspace where live the flowpipes
     ** @return the dimension.
     **/
    public int getDimension()
    {
        return d;
    }
    
    /** Give a vector that represents the direction of a given variable.
     ** @return the vector.
     ** @exception SpaceExOutputParserException thrown if the given variable doesn't exist in the given system.
     **/
    public double[] getVectorFromVariable(String varName)
    {
        double[] res = new double [d+1];
        if (varName.equals(new String("t")))
        {
            res[d] = 1.0;
        }
        else
        {
            try
            {
                res[varMap.get(varName.trim())] = 1.0;
            }
            catch(Exception e)
            {
                throw new SpaceExOutputParserException(ivMessage);
            }
        }
        return res;
    }
    
    private BufferedReader br;
    private List <Flowpipe> flowpipesResult = new ArrayList <Flowpipe>();
    private Map <String, Integer> varMap;
    private int d;
    private final String numbRegex = "-?(\\d+(\\.\\d+)?(e-?(\\d+))?)";
    private final Pattern pNewFlowpipe = Pattern.compile("\\|");
    private final Pattern pVarDomain = Pattern.compile("domain.*locked");
    private final Pattern pExtractVarDomain = Pattern.compile("\\[.*\\]");
    private final Pattern pVarName = Pattern.compile("((\\d)|(\\w)|(\\.))*");
    private final Pattern pComma = Pattern.compile(",");
    private final Pattern pNewDir = Pattern.compile("direction");
    private final Pattern pExtractDir = Pattern.compile(".*plif");
    private final Pattern pExtractCoord = Pattern.compile("=");
    private final Pattern pExtractNumb = Pattern.compile(numbRegex);
    private final Pattern pExtractSecMemb = Pattern.compile("Upper\\sFunction.*error");
    private final Pattern pExtractSecMemb2 = Pattern.compile("points.*error");
    private final Pattern pOuterConstraints = Pattern.compile("outer constraints:.*");
    private final Pattern pAnd = Pattern.compile("&");
    private final Pattern pExtractFormula = Pattern.compile("(>=)|(<=)|(==)");
    private final Pattern pExtractStrictFormula = Pattern.compile(">|<");
    private final Pattern pExtractRelation = Pattern.compile("(>=)|(<=)|(==)|>|<");
    private final Pattern pSplitPlus = Pattern.compile("\\+");
    private final Pattern pSplitStar = Pattern.compile("\\*");
    private final VectorCalculator vc = ImplementationFactory.getNewVectorCalculator();
    private static final String cfMessage = "The given file is corrupted.";
    private static final String ivMessage = "The given variable doesn't exist in the given system.";
}
