package nz.ac.auckland.concert.service.services;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS application subclass for the Concert Web service. This class is
 * discovered by the JAX-RS run-time and is used to obtain a reference to the
 * ConcertResource object that will process Web service requests.
 *
 * The base URI for the Concert Web service is:
 *
 * http://<host-name>:<port>/services.
 *
 */
@ApplicationPath("/services")
public class ConcertApplication extends Application {
    private Set<Object> _singletons = new HashSet<Object>();
    private Set<Class<?>> _classes = new HashSet<Class<?>>();

    public static final int RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;

    public ConcertApplication() {
        //clearDatabase();
        _singletons.add(new ConcertResource());//
        _singletons.add(new PersistenceManager());
        //PersistenceManager pm = new PersistenceManager().instance();

    }

    private void clearDatabase() {
        EntityManager em = null;
        try{
            em = PersistenceManager.instance().createEntityManager();
            em.getTransaction().begin();

            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM CreditCard").executeUpdate();
            em.createQuery("DELETE FROM Seat").executeUpdate();
            em.createQuery("DELETE FROM Reservation").executeUpdate();

            em.flush();
            em.clear();
            em.getTransaction().commit();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()){
                em.close();
            }
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        // Return a Set containing an instance of ConcertResource that will be
        // used to process all incoming requests on Concert resources.
        return _singletons;
    }
}