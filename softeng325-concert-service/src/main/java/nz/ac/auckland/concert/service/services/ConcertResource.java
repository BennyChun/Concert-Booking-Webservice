package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.Config;
import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.service.domain.mappers.*;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static nz.ac.auckland.concert.service.Config.CLIENT_COOKIE;
import static nz.ac.auckland.concert.service.services.ConcertApplication.RESERVATION_EXPIRY_TIME_IN_SECONDS;


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

    @POST
    @Path("/reservations")
    public Response reserveSeat(ReservationRequestDTO reservationRequestDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Reservation tempRes = null;
        try{
            em.getTransaction().begin();

            checkAuthenticationToken(clientId);

            User authUser = (User) em.createQuery("SELECT u FROM User u WHERE u._token = :token")
                    .setParameter("token", clientId.getValue()).getSingleResult();

            if (authUser == null){
                throw new BadRequestException(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build());
            }

            List<Object> resReqDetails = new ArrayList<>();
            resReqDetails.add(reservationRequestDTO.getConcertId());
            resReqDetails.add(reservationRequestDTO.getNumberOfSeats());
            resReqDetails.add(reservationRequestDTO.getSeatType());
            resReqDetails.add(reservationRequestDTO.getDate());
            for (Object o : resReqDetails){
                if (o == null){
                    throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
                            .build());
                }
            }

            Concert concert = (Concert) em.createQuery("SELECT c FROM Concert c WHERE c._id = :id")
                    .setParameter("id", reservationRequestDTO.getConcertId()).getSingleResult();
            if (concert == null){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
                        .build());
            }
            if (!(concert.getDates().contains(reservationRequestDTO.getDate()))){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                        .build());
            }

            // find all booked seats
            Set<SeatDTO> bookedSeats = new HashSet<>();
            TypedQuery<Seat> query = em.createQuery(
                    "SELECT s " +
                            "FROM Seat s " +
                            "WHERE s._date = :date " +
                            "AND s._price = :price " +
                            "AND s._reservation IS NOT NULL", Seat.class)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setParameter("price", reservationRequestDTO.getSeatType())
                    .setMaxResults(reservationRequestDTO.getNumberOfSeats());
            Set<Seat> seats = new HashSet<>(query.getResultList());
            bookedSeats = SeatMapper.toDTOSet(seats);

            Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(),
                    bookedSeats);

            if (availableSeats.isEmpty()){
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                        .build());
            }

            Set<Seat> resSeats = new HashSet<>();
            for (SeatDTO s : availableSeats){
                Seat rSeat = new Seat(s);
                resSeats.add(rSeat);
            }
            LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(RESERVATION_EXPIRY_TIME_IN_SECONDS);
            tempRes = new Reservation(resSeats, reservationRequestDTO.getSeatType(),
                    concert, reservationRequestDTO.getDate(), expiryTime, authUser, false);
            em.persist(tempRes);

            for (SeatDTO s : availableSeats){
                Seat rSeat = new Seat(s);
                rSeat.set_reservation(tempRes);
                em.persist(rSeat);
            }

            em.getTransaction().commit();

            return Response.created(URI.create("/reservations" + "/" + tempRes.toString()))
                    .entity(ReservationMapper.toDto(tempRes))
                    .build();
        } catch (OptimisticLockException e) {
            throw new WebApplicationException(Response.
                    status(Response.Status.PRECONDITION_FAILED)
                    .entity("RESOURCE LOCKED")
                    .build());
        }finally{
            em.close();
        }
    }

    @POST
    @Path("/reservations/confirm")
    public Response confirmReservation(ReservationDTO reservationDTO, @CookieParam(CLIENT_COOKIE) Cookie clientId){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            checkAuthenticationToken(clientId);

            User authUser = (User) em.createQuery("SELECT u FROM User u WHERE u._token = :token")
                    .setParameter("token", clientId.getValue()).getSingleResult();

            if (authUser == null){
                throw new BadRequestException(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build());
            }
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r" +
                            "WHERE r._id = :reservationId " +
                            "AND r._user = :user", Reservation.class)
                    .setParameter("reservationId", reservationDTO.getId())
                    .setParameter("user", authUser)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            Reservation reservation = query.getSingleResult();
            if (LocalDateTime.now().isAfter(reservation.get_expiryDate())){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(Messages.EXPIRED_RESERVATION)
                        .build());
            }

            return null;
        } finally {
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
