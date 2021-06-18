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
 *   Mar 3, 2021 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.filehandling.utility.nodes.metainfo.attributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.Function;

import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.meta.FSCapabilities;
import org.knime.filehandling.core.connections.meta.FSDescriptor;
import org.knime.filehandling.core.connections.meta.FSDescriptorRegistry;

/**
 * Class wrapping {@link BasicFileAttributes} and exposing additional functionality required by all
 * {@link KNIMEFileAttributesConverter}s.
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public final class KNIMEFileAttributesWithPermissions extends KNIMEFileAttributes {

    private final Boolean m_isReadable;

    private final Boolean m_isWritable;

    private final Boolean m_isExecutable;

    /**
     * Constructor allowing to calculate the overall size of the provided path if it is a folder as well as the
     * file/folder permissions.
     *
     * @param p the path
     * @param calcFolderSize if {@code true} the size of overall size of the folder will be calculated
     * @param fileAttributes the path's {@link BasicFileAttributes}
     * @throws IOException - If anything went wrong while calculating the folders size
     */
    public KNIMEFileAttributesWithPermissions(final FSPath p, final boolean calcFolderSize,
        final BasicFileAttributes fileAttributes) throws IOException {
        super(p, calcFolderSize, fileAttributes);
        final Optional<FSCapabilities> capabilities = getCapabilities(p);
        if (fileAttributes.isDirectory()) {
            m_isReadable = supportsOperation(capabilities, FSCapabilities::canCheckAccessReadOnDirectories)
                ? Files.isReadable(p) : null;
            m_isWritable = supportsOperation(capabilities, FSCapabilities::canCheckAccessWriteOnDirectories)
                ? Files.isWritable(p) : null;
            m_isExecutable = supportsOperation(capabilities, FSCapabilities::canCheckAccessExecuteOnDirectories)
                ? Files.isExecutable(p) : null;
        } else {
            m_isReadable =
                supportsOperation(capabilities, FSCapabilities::canCheckAccessReadOnFiles) ? Files.isReadable(p) : null;
            m_isWritable = supportsOperation(capabilities, FSCapabilities::canCheckAccessWriteOnFiles)
                ? Files.isWritable(p) : null;
            m_isExecutable = supportsOperation(capabilities, FSCapabilities::canCheckAccessExecuteOnFiles)
                ? Files.isExecutable(p) : null;
        }
    }

    KNIMEFileAttributesWithPermissions(final Path p, final boolean calcFolderSize,
        final BasicFileAttributes fileAttributes, final Boolean isReadable, final Boolean isWritable,
        final Boolean isExecutable) throws IOException {
        super(p, calcFolderSize, fileAttributes);
        m_isReadable = isReadable;
        m_isWritable = isWritable;
        m_isExecutable = isExecutable;
    }

    @SuppressWarnings("resource")
    private static Optional<FSCapabilities> getCapabilities(final FSPath p) {
        return FSDescriptorRegistry.getFSDescriptor(p.getFileSystem().getFSType())//
            .map(FSDescriptor::getCapabilities);
    }

    private static boolean supportsOperation(final Optional<FSCapabilities> capabilities,
        final Function<FSCapabilities, Boolean> operation) {
        return capabilities.map(operation).orElseGet(() -> false);
    }

    @Override
    Optional<Boolean> isReadable() {
        return Optional.ofNullable(m_isReadable);
    }

    @Override
    Optional<Boolean> isWritable() {
        return Optional.ofNullable(m_isWritable);
    }

    @Override
    Optional<Boolean> isExecutable() {
        return Optional.ofNullable(m_isExecutable);
    }

}
