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

import java.lang.RuntimeException;

/** Exception for SpaceExOutputParser issues.
 **/

public class SpaceExOutputParserException extends RuntimeException
{
    /** Create a new SpaceExOutputParserException specifying the reason by a message.
     ** @param mes the message.
     **/
    public SpaceExOutputParserException(String mes)
    {
        super(mes);
    }
}
