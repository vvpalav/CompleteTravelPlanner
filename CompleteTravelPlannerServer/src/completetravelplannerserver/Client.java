package completetravelplannerserver;

import completetravelplannerserver.org.json.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *
 * @author vinayakpalav
 */
public class Client {

    private DBHelper db;
    private PrintWriter print;
    private Socket socket;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.db = DBHelper.getDBHelperInstance();
            this.print = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleIncoming(String input) {
        JSONObject response = new JSONObject();
        try {
            JSONObject object = new JSONObject(input);
            System.out.println("Received ... " + object.toString());
            String action = object.getString("transaction_type");
            response.put("transaction_type", action + "_RESPONSE");
            switch (action) {
                case "TRIP_CREATION":
                    int id = db.insertTrip(object);
                    response.put("response", "Success");
                    response.put("trip_name", object.getString("trip_name"));
                    response.put("start_date", object.getString("start_date"));
                    response.put("end_date", object.getString("end_date"));
                    response.put("created_by", object.getString("created_by"));
                    response.put("trip_id", id);
                    break;
                case "TRIP_DELETION":
                    String tripid = object.getString("trip_id");
                    if (db.getTripMembersID(tripid, null).size() <= 1) {
                        db.removeTripCompletely(tripid);
                    } else {
                        db.removeTripForUser(tripid, object.getString("telephone"));
                    }
                    break;
                case "TRIP_ADD_MEMBER":
                    JSONObject data = db.addMemberToTheTrip(
                            object.get("trip_id"), object.get("telephone"));
                    if (data != null) {
                        NotifyTripCreationToUser(object.get("trip_id"), object.get("telephone"));
                        response.put("response", "Success");
                        response.put("trip_id", object.get("trip_id"));
                        response.put("data", data);
                    } else {
                        response.put("response", "Failure");
                    }
                    break;
                case "USER_REGISTRATION":
                    registerUser(object);
                    response.put("response", "Success");
                    break;
                case "USER_PASSCODE_AUNTH":
                    if (db.autheticateUserCode(object)) {
                        db.insertUser(object);
                        response.put("response", "Success");
                        response.put("email_id", object.get("email_id"));
                        response.put("telephone", object.get("telephone"));
                        response.put("name", object.get("name"));
                    } else {
                        response.put("response", "Failure");
                    }
                    break;
                case "MODIFICATION_PULL":
                    JSONArray array = db.retrieveAllModifications(object);
                    response.put("response", "Success");
                    response.put("data", array);
                    break;
                case "MODIFICATION_PUSH":
                    processModifications(object);
                    response.put("response", "Success");
                    break;
                case "USER_DELETION":
                    response.put("response", "Success");
                    break;
                default:
                    System.out.println("Unknown transaction type: "
                            + object.getString("transaction_type"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Sending ... " + response.toString());
            print.println(response.toString());
            print.flush();
        }
    }

    private void registerUser(JSONObject object) {
        try {
            int authenticate = new Random().nextInt((9999 - 1000) + 1) + 1000;
            String body = "Thanks for registring with Complete Travel Planner\n"
                    + "Your one time registration code is " + authenticate;
            Client.sendEmail(object.getString("email_id"), 
                    body, "Authentication of User");
            db.saveUserCode(object, authenticate);
        } catch (JSONException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public static void sendEmail(String to, String body, String sub) {
        final String from = "completetravelplanner@gmail.com";
        final String host = "smtp.gmail.com";
        final String username = "completetravelplanner@gmail.com";
        final String password = "admin_cpt";

        // Setup mail server
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                username, password);
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(sub);
            message.setText(body);
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processModifications(JSONObject object) {
        db.recordModification(object);
    }

    public void close() {
        try {
            System.out.println("client shutting down " + socket.toString());
            print.flush();
            print.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void NotifyTripCreationToUser(Object tripid, Object tel) {
        try {
            JSONObject json = db.getTripInfo(tripid);
            JSONObject data = db.getUsersInfo(json.get("created_by"));
            JSONObject user = db.getUsersInfo(tel);
            db.recordNewTripCreationModification(json, tel);
            db.getTripExpensesForNewMember(json, tel);
            db.getTripChecklistForNewMember(json, tel);
            db.getTripBestPlacesForNewMember(json, tel);
            db.getTripMembersForNewMember(json, tel);
            
            String body = "Hello " + user.getString("name")
                    + ",\nWelcome to Complete Travel Planner"
                    + "\nYou have been added to the trip " + json.getString("trip_name") 
                    + " starting from " + json.getString("start_date") + " to "
                    + json.getString("end_date") + "\nAdmin of this trip is "
                    + data.getString("name");
            sendEmail(user.getString("email_id"), body, "Trip Creation Notification");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}