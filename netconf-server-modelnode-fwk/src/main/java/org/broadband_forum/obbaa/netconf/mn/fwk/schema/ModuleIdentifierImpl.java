package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.net.URI;
import java.util.Optional;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;

public class ModuleIdentifierImpl implements ModuleIdentifier {
    
    private final QNameModule qnameModule;
    private final String name;

    private ModuleIdentifierImpl(final String name, final Optional<URI> namespace, final Optional<Revision> revision) {
        this.name = name;
        this.qnameModule = QNameModule.create(namespace.orElse(null), revision.orElse(null));
    }

    public static ModuleIdentifier create(final String name, final Optional<URI> namespace,
            final Optional<Revision> revision) {
        return new ModuleIdentifierImpl(name, namespace, revision);
    }
    
    public static ModuleIdentifier create(Module module) {
        return new ModuleIdentifierImpl(module.getName(), Optional.of(module.getNamespace()), module.getRevision());
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public Optional<Revision> getRevision() {
        return qnameModule.getRevision();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getNamespace() {
        return qnameModule.getNamespace();
    }

    @Override
    public String toString() {
        return "ModuleIdentifierImpl{"
            + "name='" + name + '\''
            + ", namespace=" + getNamespace()
            + ", revision=" + qnameModule.getRevision().orElse(null)
            + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleIdentifier)) {
            return false;
        }

        ModuleIdentifier other = (ModuleIdentifier) obj;

        if (!name.equals(other.getName())) {
            return false;
        }

        // only fail if this namespace is non-null
        if (getNamespace() != null && !getNamespace().equals(other.getNamespace())) {
            return false;
        }
        // only fail if this revision is non-null
        if (getRevision() != null && !getRevision().equals(other.getRevision())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
