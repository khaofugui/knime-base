/* 
 * -------------------------------------------------------------------
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
 *   01.02.2006 (sieb): created
 */
package de.unikn.knime.base.util.coordinate;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import de.unikn.knime.core.data.DataCell;
import de.unikn.knime.core.data.DataColumnSpec;

/**
 * This class represents a nominal coordinate defined by a given
 * <code>DataColumnSpec</code>. The nominal values are aranged equidistant
 * and in the order given in the <code>DataColumnSpec</code>.
 * 
 * @author Christoph Sieb, University of Konstanz
 */
public class NominalCoordinate extends Coordinate {

    /**
     * The number of different nominal values.
     */
    private int m_numberPossibleValues;

    /**
     * Holds the possible values as <code>DataCell</code>s.
     */
    private Vector<DataCell> m_possibleValues;

    /**
     * Constructs a nominal coordinate according to the given column spec.
     * 
     * @param dataColumnSpec the column spec to create this coordinate from
     */
    NominalCoordinate(final DataColumnSpec dataColumnSpec) {
        super(dataColumnSpec);

        // check the column type first.
        // to be nominal it must be possible to receive
        // string representations of all possible values
        Set<DataCell> possibleValues = dataColumnSpec.getDomain().getValues();

        if (possibleValues == null) {
            throw new IllegalArgumentException("The type of the given column "
                    + "must be a nominal one: "
                    + dataColumnSpec.getType().toString());
        }

        // now init the possible values vector
        // this is needed to rearange the nominal values later
        m_possibleValues = new Vector<DataCell>(possibleValues.size());
        Iterator<DataCell> possibleValuesIter = possibleValues.iterator();
        while (possibleValuesIter.hasNext()) {

            DataCell posValue = possibleValuesIter.next();
            m_possibleValues.add(posValue);
        }

        m_numberPossibleValues = possibleValues.size();
    }

    /**
     * Changes the position of a nominal value on the axis. This is due to
     * reordering nominal values which do not have a inherent order.
     * 
     * @param nominalValue the value to replace
     * @param index the position to set the value
     */
    public void changeValuePosition(final DataCell nominalValue,
            final int index) {

        // first remove the value to relocate
        m_possibleValues.remove(nominalValue);

        // re-add the value at the specified location
        m_possibleValues.add(index, nominalValue);
    }

    /**
     * Returns an array with the possition of all ticks and their corresponding
     * nominal domain values given an absolut length. The nominal values are
     * arranged equidistant with a half the distance at the begining of the
     * coordinate and half the distance at the end.
     * 
     * @param absolutLength the absolute length the domain is mapped on
     * @param naturalMapping if true the mapping values are rounded to the next
     *            integer equivalent.
     * 
     * @return the mapping of tick positions and coresponding domain values
     */
    @Override
    public CoordinateMapping[] getTickPositions(final double absolutLength,
            final boolean naturalMapping) {

        // the mapping to create and return
        NominalCoordinateMapping[] mappings = null;

        mappings = new NominalCoordinateMapping[m_numberPossibleValues];

        // calculate the distance between two nominal values and
        // calculate half of the equidistance for the initial tick
        double equiDistance = absolutLength / m_numberPossibleValues;
        double halfEquiDistance = equiDistance / 2;

        // if the mapping values must be equivalent to integers
        // the distance is rounded downwards (to ensure the ticks are
        // not leaving the absolutLength)
        if (naturalMapping) {
            equiDistance = Math.floor(equiDistance);
            halfEquiDistance = Math.floor(equiDistance / 2);
        }

        // Iterate over all possible values
        Iterator<DataCell> possibleValuesIter = m_possibleValues.iterator();

        // set first tick half the equi distance from the begining of the
        // mapping
        mappings[0] = new NominalCoordinateMapping(possibleValuesIter.next()
                .toString(), halfEquiDistance);

        // the rest of the values is set according to distance behind the first
        // mapping

        for (int i = 1; possibleValuesIter.hasNext(); i++) {

            String domainValue = possibleValuesIter.next().toString();
            double mappingValue = halfEquiDistance + equiDistance * i;
            mappings[i] = new NominalCoordinateMapping(domainValue,
                    mappingValue);
        }

        return mappings;
    }

    /**
     * Calculates a numeric mapping assuming a column with a given number of
     * possible values.
     * 
     * @see de.unikn.knime.base.util.coordinate.Coordinate
     * #calculateMappedValue(de.unikn.knime.core.data.DataCell, double, boolean)
     */
    @Override
    public double calculateMappedValue(final DataCell domainValueCell,
            final double absolutLength, final boolean naturalMapping) {

        // get the mapping for all values dependant on the absolute mapping
        // length
        CoordinateMapping[] mappings = getTickPositions(absolutLength,
                naturalMapping);

        // get the nominal domain value
        String nominalDomainValue = domainValueCell.toString();

        // get the mapping position of the domain value from the mapping array
        double mappedValue = -1;
        for (CoordinateMapping mapping : mappings) {

            if (mapping.getDomainValueAsString().equals(nominalDomainValue)) {

                mappedValue = mapping.getMappingValue();
                break;
            }
        }

        // if a natural mapping (natural numbers) are requested
        // the value is rounded
        if (naturalMapping) {
            mappedValue = Math.round(mappedValue);
        }
        return mappedValue;
    }

    /**
     * @see de.unikn.knime.base.util.coordinate.Coordinate#isNominal()
     */
    @Override
    public boolean isNominal() {
        return true;
    }

    /**
     * @see Coordinate#getUnusedDistBetweenTicks(double)
     */
    @Override
    public double getUnusedDistBetweenTicks(final double absoluteLength) {
        return absoluteLength / m_numberPossibleValues;
    }

}
