package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.Config;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.domain.mappers.ConcertMapper;
import nz.ac.auckland.concert.service.domain.mappers.PerformerMapper;
import nz.ac.auckland.concert.service.domain.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static nz.ac.auckland.concert.service.Config.CLIENT_COOKIE;


@Path("/resource")
@Produces({javax.ws.rs.core.MediaType.APPLICATION_XML})
@Consumes({javax.ws.rs.core.MediaType.APPLICATION_XML})
public class ConcertResource {
    private static Logger _logger = LoggerFactory.getLogger(ConcertResource.class);

    /*@GET
    @Path("/concerts")
    public void testing(){
        PersistenceManager.instance().createEntityManager();

        System.out.println("Testing...");
    }*/

    @GET
    @Path("/concerts")
    public Response getAllConcerts(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Concert> query = em.createQuery("SELECT a FROM Concert a", Concert.class);

        List<Concert> concertsList = query.getResultList();
        List<ConcertDTO> concertDTOList = new ArrayList<>();

        for(Concert c: concertsList){
            concertDTOList.add(ConcertMapper.toDTO(c));
        }

        em.close();

        GenericEntity<List<ConcertDTO>> ge = new GenericEntity<List<ConcertDTO>>(concertDTOList) {};

        return Response.ok(ge)
                .build();

    }

    @GET
    @Path("/performers")
    public Response getAllPerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Performer> query = em.createQuery("SELECT p FROM Performer p", Performer.class);
        List<Performer> performers = query.getResultList();
        List<PerformerDTO> performerDTOS = new ArrayList<>();

        for(Performer c: performers){
            performerDTOS.add(PerformerMapper.toDTO(c));
        }
        em.close();

        GenericEntity<List<PerformerDTO>> ge = new GenericEntity<List<PerformerDTO>>(performerDTOS) {};

        return Response.ok(ge)
                .build();
    }

    @POST
    @Path("/users")
    public Response createUser(UserDTO user, @CookieParam(CLIENT_COOKIE) Cookie clientId){
        List<String> userDetails = new ArrayList<>();
        userDetails.add(user.getUsername());
        userDetails.add(user.getPassword());
        userDetails.add(user.getFirstname());
        userDetails.add(user.getLastname());
        for (String s: userDetails){
            if (s == null){
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                        .build());
            }
        }
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        if(userExists(user)){
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                    .build());
        }

        User newUser = new User(user.getUsername(), user.getPassword(), user.getFirstname(), user.getLastname());
        NewCookie newCookie = createCookie(clientId);
        String token = newCookie.getValue();
        newUser.setToken(token);

        em.persist(newUser);
        em.getTransaction().commit();

        return Response.created(URI.create("/users" + "/" + user.getUsername()))
                .entity(user)
                .cookie(newCookie)
                .build();
    }

    @POST
    @Path("/authenticate")
    public Response authenticateUser(UserDTO user, @CookieParam(CLIENT_COOKIE) Cookie clientId){
        if(user.getUsername() == null || user.getPassword() == null){
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS)
                    .build());
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        if(!userExists(user)){
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
                    .build());
        }

        User u = em.find(User.class, user.getUsername());

        if (!(user.getPassword().equals(u.getPassword()))){
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD)
                    .build());
        }
        NewCookie newCookie = createCookie(clientId);
        String token = newCookie.getValue();
        u.setToken(token);

        em.getTransaction().commit();
        em.close();

        return Response.ok(UserMapper.toDTO(u))
                .cookie(createCookie(clientId))
                .build();
    }

    @POST
    @Path("/creditCards")
    public Response addCreditCard(CreditCardDTO creditCard, @CookieParam(CLIENT_COOKIE) Cookie clientId){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try{
            em.getTransaction().begin();

            checkAuthenticationToken(clientId);

            String token = clientId.getValue();
            User authUser = (User) em.createQuery("SELECT u FROM User u WHERE u._token = :token")
                    .setParameter("token", token).getSingleResult();

            if (authUser == null){
                throw new BadRequestException(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build());
            }

            authUser.setCreditCard(new CreditCard(creditCard));
            em.persist(authUser.getCreditCard());
            em.merge(authUser);
            em.getTransaction().commit();

            return Response.accepted().build();
        } finally{
            em.close();
        }
    }


    private void checkAuthenticationToken(Cookie clientId) {
        if (clientId == null){
            throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Messages.UNAUTHENTICATED_REQUEST)
                    .build());
        }
    }

    private boolean userExists(UserDTO user) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        List<User> users = query.getResultList();
        for (User u : users) {
            if (user.getUsername().equals(u.getUsername())){
                return true;
            }
        }
        return false;
    }

    private NewCookie createCookie(Cookie clientId) {
        NewCookie newCookie;
        if (clientId == null){
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            _logger.info("Generated cookie: " + newCookie.getValue());
        }else{
            newCookie = new NewCookie(clientId);
        }
        return newCookie;
    }
}
