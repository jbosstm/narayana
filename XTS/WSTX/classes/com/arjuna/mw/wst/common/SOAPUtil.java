/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wst.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import jakarta.xml.soap.SOAPElement;

/**
 * Utility class for SOAP.
 * @author kevin
 */
public class SOAPUtil
{
    /**
     * Get an iterator containing just child elements.
     * @param soapElement The parent soap element.
     * @return The iterator of SOAPElements.
     */
    public static Iterator getChildElements(final SOAPElement soapElement)
    {
        return new SOAPElementIterator(soapElement.getChildElements()) ;
    }
    
    /**
     * An iterator class that skips any nodes which are not SOAPElements.
     * @author kevin
     */
    private static final class SOAPElementIterator implements Iterator
    {
        /**
         * The wrapped iterator.
         */
        private final Iterator elementIter ;
        /**
         * The current object.
         */
        private Object current ;
        
        /**
         * Construct the iterator.
         * @param elementIter The iterator being wrapped.
         */
        SOAPElementIterator(final Iterator elementIter)
        {
            this.elementIter = elementIter ;
        }
        
        /**
         * Are there any more elements?
         * @return true if the iterator has more elements, false otherwise.
         */
        public boolean hasNext()
        {
            checkCurrent() ;
            return (current != null) ;
        }
        
        /**
         * Get the next element.
         * @return the next element.
         * @throws NoSuchElementException if there are no more elements.
         */
        public Object next()
            throws NoSuchElementException
        {
            checkCurrent() ;
            if (current == null)
            {
                throw new NoSuchElementException("No more elements in iterator") ;
            }
            final Object result = current ;
            current = null ;
            return result ;
        }
        
        /**
         * Remove the current object. &nbsp;This method is not supported on this iterator.
         * @throws UnsupportedOperationException if not supported.
         * @throws IllegalStateException if the next method has not been called or
         * remove has already been called on the current element.
         */
        public void remove()
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException("Remove not supported on this iterator") ;
        }
        
        /**
         * Check the current element.
         */
        private void checkCurrent()
        {
            if (current == null)
            {
                while (elementIter.hasNext())
                {
                    final Object  next = elementIter.next() ;
                    if (next instanceof SOAPElement)
                    {
                        current = next ;
                        break ;
                    }
                }
            }
        }
    }
}