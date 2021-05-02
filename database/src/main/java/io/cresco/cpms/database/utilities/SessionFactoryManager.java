package io.cresco.cpms.database.utilities;

import org.hibernate.Session;

public interface SessionFactoryManager {
    public Session getSession();
    public void close();
}
