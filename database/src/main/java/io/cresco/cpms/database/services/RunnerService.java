package io.cresco.cpms.database.services;

import io.cresco.cpms.database.CPMSDatabaseException;
import io.cresco.cpms.database.models.LogState;
import io.cresco.cpms.database.models.Runner;
import io.cresco.cpms.database.models.RunnerLog;
import io.cresco.cpms.database.utilities.SessionFactoryManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.resource.transaction.spi.TransactionStatus;

@SuppressWarnings("unused")
public class RunnerService {
    private static SessionFactoryManager sessionFactoryManager;

    public static void setSessionFactoryManager(SessionFactoryManager newSessionFactoryManager) {
        sessionFactoryManager = newSessionFactoryManager;
    }

    public static synchronized Runner create(String region, String agent, String plugin, String identifier) throws CPMSDatabaseException {
        Runner object = getByCrescoAddrAndIdentifier(region, agent, plugin, identifier);
        if (object != null)
            throw new CPMSDatabaseException(
                    String.format(
                            "Runner with Cresco address [%s-%s-%s] and identifier [%s] already exists",
                            region, agent, plugin, identifier
                    )
            );
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            object = new Runner(region, agent, plugin, identifier);
            session.persist( object );
            session.getTransaction().commit();
            return object;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static Runner getById(String id) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            JpaCriteriaQuery<Runner> q = b.createQuery(Runner.class);
            JpaRoot<Runner> r = q.from(Runner.class);
            q.select(r).where(b.equal(r.get("id"), id));
            Query<Runner> query = session.createQuery(q);
            Runner object = query.uniqueResult();
            session.getTransaction().commit();
            return object;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static Runner getByCrescoAddrAndIdentifier(String region, String agent, String plugin, String identifier) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            JpaCriteriaQuery<Runner> q = b.createQuery(Runner.class);
            JpaRoot<Runner> r = q.from(Runner.class);
            q.select(r).where(
                    b.and(
                            b.equal(r.get("region"), region),
                            b.equal(r.get("agent"), agent),
                            b.equal(r.get("plugin"), plugin),
                            b.equal(r.get("identifier"), identifier)
                    )
            );
            Query<Runner> query = session.createQuery(q);
            Runner object = query.uniqueResult();
            session.getTransaction().commit();
            return object;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static Runner update(Runner object) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            session.merge( object );
            session.getTransaction().commit();
            return object;
        } catch (RuntimeException e) {
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return null;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean delete(String id) throws CPMSDatabaseException {
        Runner object = getById(id);
        if (object == null)
            throw new CPMSDatabaseException(String.format("No runner with id [%s] exists", id));
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            session.remove( object );
            session.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return false;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean delete(Runner object) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            session.remove( object );
            session.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return false;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean addLog(Runner runner, LogState state, String message) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            session.remove( new RunnerLog(runner, state, message) );
            session.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (session.getTransaction().getStatus() == TransactionStatus.ACTIVE ||
                    session.getTransaction().getStatus() == TransactionStatus.MARKED_ROLLBACK)
                session.getTransaction().rollback();
            return false;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }
}
