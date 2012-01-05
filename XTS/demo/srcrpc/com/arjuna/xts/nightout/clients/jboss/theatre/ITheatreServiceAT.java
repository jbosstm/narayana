/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.xts.nightout.clients.jboss.theatre ;

import java.rmi.Remote;
import java.rmi.RemoteException ;

public interface ITheatreServiceAT extends Remote
{

    /**
     * Book a specified number of seats in a specific area of the theatre.
     * @param numSeats The number of seats to book at the theatre.
     * @param area The area of the seats.
     * @throws RemoteException for communication errors.
     */
    public void bookSeats(final int numSeats, final int area)
            throws RemoteException ;

}
