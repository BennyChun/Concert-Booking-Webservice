package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static nz.ac.auckland.concert.service.Config.CLIENT_COOKIE;

public class DefaultService implements ConcertService {

    private static String WEB_SERVICE_URI = "http://localhost:10000/services/resource";

    private Set<String> cookieSet = new HashSet<>();

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Set<ConcertDTO> concerts = null;
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/concerts")
                    .request()
                    .accept(MediaType.APPLICATION_XML);
            Response res = builder.get();
            if (res.getStatus() == Response.Status.OK.getStatusCode()){
                concerts = res.readEntity(new GenericType<Set<ConcertDTO>>() {
                });
            }
            client.close();
            return concerts;
        } catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Set<PerformerDTO> performers = null;
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/performers")
                    .request(MediaType.APPLICATION_XML)
                    .accept(MediaType.APPLICATION_XML);
            Response res = builder.get();
            if (res.getStatus() == Response.Status.OK.getStatusCode()){
                performers = res.readEntity(new GenericType<Set<PerformerDTO>>() {
                });
            }
            client.close();
            return performers;
        } catch (Exception e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();
        UserDTO user = null;

        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/users")
                    .request()
                    .accept(MediaType.APPLICATION_XML);
            Response res = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            if (res.getStatus() == Response.Status.CREATED.getStatusCode()){
                user = res.readEntity(new GenericType<UserDTO>(){
                });
                storeCookie(res);
                return user;
            }else{
                throw new ServiceException(res.readEntity(String.class));
            }

        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    private void storeCookie(Response res) {
        Map<String, NewCookie> cookie =res.getCookies();
        if (cookie.containsKey(CLIENT_COOKIE)){
            cookieSet.add(cookie.get(CLIENT_COOKIE).getValue());
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();
        UserDTO authUser = null;
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/authenticate")
                    .request(MediaType.APPLICATION_XML)
                    .accept(MediaType.APPLICATION_XML);
            Response res = builder.post(Entity.entity(user, MediaType.APPLICATION_XML));

            if (res.getStatus() == Response.Status.OK.getStatusCode()){
                authUser = res.readEntity(new GenericType<UserDTO>(){
                });
                storeCookie(res);
                return authUser;
            }else{
                throw new ServiceException(res.readEntity(String.class));
            }
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        return null;
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        return null;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {

    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        Client client = ClientBuilder.newClient();
        try{
            Invocation.Builder builder = client.target(WEB_SERVICE_URI + "/creditCards")
                        .request();

            if (!(cookieSet.isEmpty())){
                builder.cookie(CLIENT_COOKIE, cookieSet.iterator().next());
            }

            Response res = builder.post(Entity.entity(creditCard, MediaType.APPLICATION_XML));
            if (res.getStatus() == Response.Status.ACCEPTED.getStatusCode()){
                return;
            } else{
                throw new ServiceException(res.readEntity(String.class));
            }

        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        return null;
    }
}
