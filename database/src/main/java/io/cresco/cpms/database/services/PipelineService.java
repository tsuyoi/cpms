package io.cresco.cpms.database.services;

import io.cresco.cpms.database.CPMSDatabaseException;
import io.cresco.cpms.database.models.Pipeline;
import io.cresco.cpms.database.models.PipelineRun;
import io.cresco.cpms.database.utilities.SessionFactoryManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@SuppressWarnings("unused")
public class PipelineService {
    private static SessionFactoryManager sessionFactoryManager;

    public static void setSessionFactoryManager(SessionFactoryManager newSessionFactoryManager) {
        sessionFactoryManager = newSessionFactoryManager;
    }

    public static synchronized Pipeline create(String name, String script) throws CPMSDatabaseException {
        Pipeline object = getByName(name);
        if (object != null)
            throw new CPMSDatabaseException(String.format("Pipeline with name [%s] already exists", name));
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            object = new Pipeline(name, script);
            session.save( object );
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

    public static Pipeline getById(String id) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            CriteriaQuery<Pipeline> q = b.createQuery(Pipeline.class);
            Root<Pipeline> r = q.from(Pipeline.class);
            q.select(r).where(b.equal(r.get("id"), id));
            Query<Pipeline> query = session.createQuery(q);
            Pipeline object = query.uniqueResult();
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

    public static Pipeline getByName(String name) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            CriteriaQuery<Pipeline> q = b.createQuery(Pipeline.class);
            Root<Pipeline> r = q.from(Pipeline.class);
            q.select(r).where(b.equal(r.get("name"), name));
            Query<Pipeline> query = session.createQuery(q);
            Pipeline object = query.uniqueResult();
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

    public static Pipeline update(Pipeline object) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            session.update( object );
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
        Pipeline object = getById(id);
        if (object == null)
            throw new CPMSDatabaseException(String.format("No pipeline with id [%s] exists", id));
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            session.delete( object );
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

    public static boolean delete(Pipeline object) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            session.delete( object );
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

    public static synchronized PipelineRun createRun(Pipeline pipeline) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            PipelineRun object = new PipelineRun(pipeline);
            session.save( object );
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

    public static PipelineRun getRunById(String id) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            CriteriaQuery<PipelineRun> q = b.createQuery(PipelineRun.class);
            Root<PipelineRun> r = q.from(PipelineRun.class);
            q.select(r).where(b.equal(r.get("id"), id));
            Query<PipelineRun> query = session.createQuery(q);
            PipelineRun object = query.uniqueResult();
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

    public static boolean deleteRun(String id) throws CPMSDatabaseException {
        PipelineRun object = getRunById(id);
        if (object == null)
            throw new CPMSDatabaseException(String.format("Pipeline run with id [%s] does not exist", id));
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            session.delete( object );
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

    public static boolean deleteRun(PipelineRun object) throws CPMSDatabaseException {
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            CriteriaBuilder b = session.getCriteriaBuilder();
            session.delete( object );
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
