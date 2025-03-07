/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   20 Dec 2022 (jasper): created
 */
package org.knime.base.node.preproc.valuelookup;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.knime.base.node.preproc.valuelookup.ValueLookupNodeSettings.StringMatching;
import org.knime.core.data.DataCell;
import org.knime.filehandling.core.util.WildcardToRegexUtil;

/**
 * Dictionary implementation that uses Regular Expressions to match lookup cells against a pattern in the dictionary
 * table. Supports both RegEx and Wildcard patterns.
 *
 * @author Jasper Krauter, KNIME GmbH, Konstanz, Germany
 */
class PatternDict extends ListDict<Pattern> {

    /**
     * The default flags that will be used when compiling patterns
     */
    private int m_flags = Pattern.MULTILINE | Pattern.DOTALL;

    /**
     * Create a new instance by providing the settings of a node instance
     *
     * @param settings the relevant settings instance
     */
    protected PatternDict(final ValueLookupNodeSettings settings) {
        super(settings);
        if (!m_settings.m_caseSensitive) {
            // Add additional case-insensitive flags
            m_flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
    }

    @Override
    boolean matches(final Pattern entry, final DataCell lookup) {
        return entry.matcher(lookup.toString()).matches();
    }

    @Override
    public Optional<Boolean> insertSearchPair(final DataCell key, final DataCell[] values)
        throws IllegalLookupKeyException {
        var patternAsStr = key.toString();
        if (m_settings.m_stringMatchBehaviour == StringMatching.WILDCARD) {
            patternAsStr = WildcardToRegexUtil.wildcardToRegex(patternAsStr);
        }
        try {
            var compiled = Pattern.compile(patternAsStr, m_flags);
            insertKVPair(compiled, values);
            return Optional.empty();
        } catch (PatternSyntaxException e) {
            throw new IllegalLookupKeyException("Invalid RegEx pattern: " + System.lineSeparator() + e.getMessage(), e);
        }
    }

}
