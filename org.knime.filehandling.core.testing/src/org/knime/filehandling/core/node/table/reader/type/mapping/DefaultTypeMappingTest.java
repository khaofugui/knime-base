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
 *   Mar 30, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader.type.mapping;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.filehandling.core.node.table.reader.randomaccess.RandomAccessible;
import org.knime.filehandling.core.node.table.reader.spec.TypedReaderTableSpec;
import org.knime.filehandling.core.node.table.reader.type.mapping.TypeMappingTestUtils.TestReadAdapter;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Contains unit tests for the default {@link TypeMapping} implementation.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultTypeMappingTest {

    private DefaultTypeMapping<String> m_testInstance = null;

    @Mock
    private FileStoreFactory m_fsFactory = null;

    @Mock
    private RandomAccessible<String> m_randomAccessible = null;

    /**
     * Initializes the test instance.
     */
    @Before
    public void init() {
        m_testInstance =
            new DefaultTypeMapping<>(TestReadAdapter::new, TypeMappingTestUtils.mockProductionPaths("frieda", "berta"));
    }

    /**
     * Tests if {@code map} fails on a spec of incompatible length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMapFailsOnSpecOfIncompatibleSize() {
        m_testInstance.map(TypedReaderTableSpec.create("hans", "franz", "gunter"));
    }

    /**
     * Tests the {@code map} implementation.
     */
    @Test
    public void testMap() {
        DataTableSpec expected = new DataTableSpec("default", new String[]{"hans", "franz"},
            new DataType[]{StringCell.TYPE, StringCell.TYPE});
        DataTableSpec actual =
            m_testInstance.map(TypedReaderTableSpec.create(asList("hans", "franz"), asList("frieda", "berta")));
        assertEquals(expected, actual);
    }

    /**
     * Tests the {@code createTypeMapper} implementation.
     *
     * @throws Exception never thrown
     */
    @Test
    public void testCreateTypeMapper() throws Exception {
        TypeMapper<String> typeMapper = m_testInstance.createTypeMapper(m_fsFactory);
        final RowKey rowKey = new RowKey("test");
        DataRow expected = new DefaultRow(rowKey, "frieda", "berta");
        when(m_randomAccessible.size()).thenReturn(2);
        when(m_randomAccessible.get(0)).thenReturn("frieda");
        when(m_randomAccessible.get(1)).thenReturn("berta");
        DataRow actual = typeMapper.map(rowKey, m_randomAccessible);
        assertEquals(expected, actual);

    }

}
