package nz.ac.auckland.concert.client.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nz.ac.auckland.concert.service.Config.CLIENT_COOKIE;

public class DefaultService implements ConcertService {

    // download paths
    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    private static final String USER_DIRECTORY = System
            .getProperty("user.home");
    private static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";

    // awes bucket
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // AWS keys
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";

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
            }else {
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
        Set<PerformerDTO> performers = getPerformers();
        File downloadDirectory = null;
        try {
            if (!(performers.contains(performer))) {
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            }

            downloadDirectory = new File(DOWNLOAD_DIRECTORY);
            downloadDirectory.mkdir();
            String imageName = performer.getImageName();

            // Create an AmazonS3 object that represents a connection with the
            // remote S3 service.
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
            AmazonS3 s3 = AmazonS3ClientBuilder
                    .standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .withCredentials(
                            new AWSStaticCredentialsProvider(awsCredentials))
                    .build();
            TransferManager tm = TransferManagerBuilder
                    .standard()
                    .withS3Client(s3)
                    .build();
            Download imageFile = tm.download(AWS_BUCKET, imageName, downloadDirectory);
            imageFile.waitForCompletion();
            tm.shutdownNow();
        } catch (ProcessingException e){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Toolkit.getDefaultToolkit().getImage(String.valueOf(downloadDirectory));
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

    private static void download(AmazonS3 s3, String imageName) {

        File downloadDirectory = new File(DOWNLOAD_DIRECTORY);
        System.out.println("Will download " + imageName + " to: " + downloadDirectory.getPath());


        File imageFile = new File(downloadDirectory, imageName);

        if (imageFile.exists()) {
            imageFile.delete();
        }
        System.out.print("Downloading " + imageName + "... ");
        GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, imageName);
        s3.getObject(req, imageFile);
        System.out.println("Complete!");

    }
}
