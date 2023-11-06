package io.cresco.cpms.database.services;

import io.cresco.cpms.database.CPMSDatabaseException;
import io.cresco.cpms.database.models.*;
import io.cresco.cpms.database.utilities.SessionFactoryManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.resource.transaction.spi.TransactionStatus;

@SuppressWarnings("unused")
public class TaskService {
    private static SessionFactoryManager sessionFactoryManager;

    public static void setSessionFactoryManager(SessionFactoryManager newSessionFactoryManager) {
        sessionFactoryManager = newSessionFactoryManager;
    }

    public static synchronized Task create(Pipeline pipeline, String name, String script) throws CPMSDatabaseException {
        if (pipeline == null)
            throw new CPMSDatabaseException("You must supply a pipeline to which this task belongs");
        if (name == null)
            throw new CPMSDatabaseException("You must supply a name for this task");
        if (script == null)
            throw new CPMSDatabaseException("You must supply the script for this task");
        Task object = getByPipelineAndName(pipeline, name);
        if (object != null)
            throw new CPMSDatabaseException(
                    String.format("Task with pipeline [%s] and name [%s] already exists", pipeline.getName(), name)
            );
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            object = new Task(pipeline, name, script);
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

    public static Task getById(String id) throws CPMSDatabaseException {
        if (id == null)
            throw new CPMSDatabaseException("You must supply the id of the task");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            JpaCriteriaQuery<Task> q = b.createQuery(Task.class);
            JpaRoot<Task> r = q.from(Task.class);
            q.select(r).where(b.equal(r.get("id"), id));
            Query<Task> query = session.createQuery(q);
            Task object = query.uniqueResult();
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

    public static Task getByPipelineAndName(Pipeline pipeline, String name) throws CPMSDatabaseException {
        if (pipeline == null)
            throw new CPMSDatabaseException("You must supply the pipeline for the task");
        if (name == null)
            throw new CPMSDatabaseException("You must supply the name of the task");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            JpaCriteriaQuery<Task> q = b.createQuery(Task.class);
            JpaRoot<Task> r = q.from(Task.class);
            q.select(r).where(b.and(b.equal(r.get("pipeline"), pipeline), b.equal(r.get("name"), name)));
            Query<Task> query = session.createQuery(q);
            Task object = query.uniqueResult();
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

    public static Task update(Task object) throws CPMSDatabaseException {
        if (object == null)
            throw new CPMSDatabaseException("You must supply the task to update");
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
        if (id == null)
            throw new CPMSDatabaseException("You must supply the id of the task");
        Task object = getById(id);
        if (object == null)
            throw new CPMSDatabaseException(String.format("No task with id [%s] exists", id));
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

    public static boolean delete(Task object) throws CPMSDatabaseException {
        if (object == null)
            throw new CPMSDatabaseException("You must supply the task");
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

    public static synchronized TaskRun createRun(PipelineRun pipelineRun, Task task) throws CPMSDatabaseException {
        if (pipelineRun == null)
            throw new CPMSDatabaseException("You must supply the pipeline run instance for the task run instance");
        if (task == null)
            throw new CPMSDatabaseException("You must supply the task");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            TaskRun object = new TaskRun(pipelineRun, task);
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

    public static TaskRun getRunById(String id) throws CPMSDatabaseException {
        if (id == null)
            throw new CPMSDatabaseException("You must supply the id of the task run instance");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            JpaCriteriaQuery<TaskRun> q = b.createQuery(TaskRun.class);
            JpaRoot<TaskRun> r = q.from(TaskRun.class);
            q.select(r).where(b.equal(r.get("id"), id));
            Query<TaskRun> query = session.createQuery(q);
            TaskRun object = query.uniqueResult();
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

    public static boolean addLog(TaskRun taskRun, LogState state, String message) throws CPMSDatabaseException {
        if (taskRun == null)
            throw new CPMSDatabaseException("You must supply the task run instance");
        if (state == null)
            throw new CPMSDatabaseException("You must supply the state of the task run log entry");
        if (message == null)
            throw new CPMSDatabaseException("You must supply the message of the task run log entry");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            session.persist( new TaskRunLog(taskRun, state, message) );
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

    public static boolean addOutput(TaskRun taskRun, String output) throws CPMSDatabaseException {
        if (taskRun == null)
            throw new CPMSDatabaseException("You must supply the task run instance");
        if (output == null)
            throw new CPMSDatabaseException("You must supply the output log of the task run");
        Session session = sessionFactoryManager.getSession();
        if (session == null)
            throw new CPMSDatabaseException("Failed to create database session");
        try {
            session.getTransaction().begin();
            HibernateCriteriaBuilder b = session.getCriteriaBuilder();
            session.persist( new TaskRunOutput(taskRun, output) );
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

    public static boolean deleteRun(String id) throws CPMSDatabaseException {
        TaskRun object = getRunById(id);
        if (object == null)
            throw new CPMSDatabaseException(String.format("Task run with id [%s] does not exist", id));
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

    public static boolean deleteRun(TaskRun object) throws CPMSDatabaseException {
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
}
