/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   18.07.2005 (ohl): created
 */
package de.unikn.knime.base.node.filter.row.rowfilter;

/**
 * An exception thrown by a row filter to indicate that the current and all
 * following rows from now on are to be included into the result table.
 * 
 * @author ohl, University of Konstanz
 */
public class IncludeFromNowOn extends Exception {
    /**
     * Creates a new Exception with no message.
     */
    public IncludeFromNowOn() {
        super();
    }

    /**
     * Creates a new exception object with a message.
     * 
     * @param msg the message to store in the exception object.
     */
    public IncludeFromNowOn(final String msg) {
        super(msg);
    }

}
