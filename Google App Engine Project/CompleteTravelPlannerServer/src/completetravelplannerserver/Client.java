package completetravelplannerserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;

import completetravelplannerserver.org.json.JSONArray;
import completetravelplannerserver.org.json.JSONException;
import completetravelplannerserver.org.json.JSONObject;

/**
 *
 * @author Vinayak Palav
 */
public class Client {

    private DBHelper db;
    private HttpServletResponse resp;
    private static final Logger log = Logger.getLogger(Client.class.getName());
    
    public Client(HttpServletResponse resp) {
        this.resp = resp;
		this.db = DBHelper.getDBHelperInstance();
    }

    public void handleIncoming(String input) {
        JSONObject response = new JSONObject();
        try {
            JSONObject object = new JSONObject(input);
            log.info("Received ... " + object.toString());
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
                        NotifyAdminOfTripDeletion(tripid, object.getString("telephone"));
                    }
                    break;
                case "TRIP_ADD_MEMBER":
                    JSONObject data = db.addMemberToTheTrip(
                            object.get("trip_id"), object.get("telephone"));
                    if (data != null) {
                    	ProcessNewTripCreationForUser(object.get("trip_id"), object.get("telephone"),
                    			object.getString("added_by"));
                        response.put("response", "Success");
                        response.put("trip_id", object.get("trip_id"));
                        response.put("data", data);
                    } else {
                        response.put("response", "Failure");
                    }
                    break;
                case "NEW_USER_REGISTRATION":
                	String msg = registerNewUser(object);
                    if(msg == null){
                    	response.put("response", "Success");
                    } else {
                    	response.put("response", "Failure");
                    	response.put("message", msg);
                    }
                    break;
                case "EXISTING_USER_REGISTRATION":
                    if(registerExistingUser(object)){
                    	response.put("response", "Success");
                    } else {
                    	response.put("response", "Failure");
                    }
                    break;
                case "USER_PASSCODE_AUTH":
                    if (db.autheticateUserCode(object)) {
                    	if(object.getString("TYPE_USER").equals("NEW_USER")){
                    		if(db.insertUser(object)){
                    			response.put("response", "Success");
                    			response.put("email_id", object.get("email_id"));
                    			response.put("telephone", object.get("telephone"));
                    			response.put("name", object.get("name"));
                    		} else {
                    			response.put("response", "Failure");
                    		}
                    	} else if (object.getString("TYPE_USER").equals("EXISTING_USER")){
                    		response.put("response", "Success");
                			response.put("email_id", object.get("email_id"));
                			response.put("telephone", object.get("telephone"));
                			response.put("name", object.get("name"));
                    	}
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
                default:
                	response.put("message", "Unknown Transaction");
                    log.info("Unknown transaction type: "
                            + object.getString("transaction_type"));
            }
        } catch (JSONException ex) {
        	log.severe(ex.toString());
        } finally {
            log.info("Sending ... " + response.toString());
            resp.setContentType("text/json");
    		try {
				resp.getWriter().println(response.toString());
			} catch (IOException e) {
				log.severe(e.toString());
			}
        }
    }

    private void NotifyAdminOfTripDeletion(String tripid, String tel) {
        try {
        	JSONObject json = db.getTripInfo(tripid);
			JSONObject admin = db.getUsersInfo(json.get("created_by"));
			JSONObject user = db.getUsersInfo(tel);
			if(!admin.getString("telephone").equals(tel)){
				String body = "Hey " + admin.getString("name") + ",\nUser "
					+ user.getString("name") + " has deleted the trip " + json.getString("trip_name")
					+ " from Complete Travel Planner. This email is the notification of the same."
					+ "\n\nThanks,\nComplete Travel Planner Team";
				Client.sendEmail(admin.getString("email_id"), body, 
					"Trip Deletion Notification by User " + user.getString("name"));
			}
		} catch (JSONException ex) {
			log.severe(ex.toString());
		}
	}

	private String registerNewUser(JSONObject object) {
		try {
			if (!db.checkIfUserExist(object.get("telephone"))) {
				int authenticate = new Random().nextInt((9999 - 1000) + 1) + 1000;
				String body = "Thanks for registring with Complete Travel Planner.\n"
						+ "Your one time registration code is "
						+ authenticate
						+ ".\n\nThanks,\nComplete Travel Planner Team";
				Client.sendEmail(object.getString("email_id"), body,
						"Authentication of New User " + object.getString("name"));
				db.saveUserCode(object.get("telephone"), authenticate);
				return null;
			} else {
				JSONObject user = db.getUsersInfo(object.get("telephone"));
				return user.getString("telephone") + " already registered " +
						" with " + user.getString("email_id") 
						+ " Continue to get code on registered email";
			}
		} catch (JSONException ex) {
			log.severe(ex.toString());
		}
		return null;
	}
	
	private boolean registerExistingUser(JSONObject object) {
		try {
			if (db.checkIfUserExist(object.get("telephone"))) {
				JSONObject user = db.getUsersInfo(object.get("telephone"));
				int authenticate = new Random().nextInt((9999 - 1000) + 1) + 1000;
				String body = "Thanks for registring again with Complete Travel Planner.\n"
						+ "Your one time registration code is "
						+ authenticate
						+ ".\n\nThanks,\nComplete Travel Planner Team";
				Client.sendEmail(user.getString("email_id"), body,
						"Authentication of Existing User " + user.getString("name"));
				db.saveUserCode(object.get("telephone"), authenticate);
				return true;
			}
		} catch (JSONException ex) {
			log.severe(ex.toString());
		}
		return false;
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
            log.info("Sent message successfully....");
        } catch (MessagingException ex) {
        	log.severe(ex.toString());
        }
    }

	private void processModifications(JSONObject object) throws JSONException {
		if (db.recordModification(object)) {
			String action = object.getString("action");
			if (action.equals("INSERT")) {
				sendGCMForTrip(object.get("trip_id"), object.get("telephone"),
						getInsertActionText(object.getString("telephone"), 
								object.getString("trip_id"), object.getString("tablename")));
			} else if (action.equals("DELETE")) {
				sendGCMForTrip(object.get("trip_id"), object.get("telephone"),
						getDeleteActionText(object.getString("telephone"), 
								object.getString("trip_id"), object.getString("tablename")));
			} else if (action.equals("UPDATE")) {
				sendGCMForTrip(object.get("trip_id"), object.get("telephone"),
						getUpdateActionText(object.getString("telephone"), 
								object.getString("trip_id"), object.getString("tablename")));
			}
		}
	}
	
	private String getInsertActionText(Object telephone, Object tripid, String tablename) {
		try {
			JSONObject user = db.getUsersInfo(telephone);
			JSONObject trip = db.getTripInfo(tripid);
			switch (tablename) {
			case "expenses":
				return "New bill has been added to expenses by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "checklist":
				return "New shared item has been added to checklist by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "best_places":
				return "New location has been added in a trip "
						+ trip.getString("trip_name") + " by "
						+ user.getString("name");
			case "trip_members":
				return "New member has been added by " 
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			default:
				return "Trip " + trip.getString("trip_name")
						+ " has been modified by " + user.getString("name");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String getUpdateActionText(Object telephone, Object tripid, String tablename) {
		try {
			JSONObject user = db.getUsersInfo(telephone);
			JSONObject trip = db.getTripInfo(tripid);
			switch (tablename) {
			case "expenses":
				return "Bill has been modified in expenses by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "checklist":
				return "Shared item has been modified from checklist by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "best_places":
				return "Location has been modified from a trip "
						+ trip.getString("trip_name") + " by "
						+ user.getString("name");
			default:
				return "Trip " + trip.getString("trip_name")
						+ " has been modified by " + user.getString("name");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String getDeleteActionText(Object telephone, Object tripid, String tablename) {
		try {
			JSONObject user = db.getUsersInfo(telephone);
			JSONObject trip = db.getTripInfo(tripid);
			switch (tablename) {
			case "expenses":
				return "Bill has been deleted from expenses by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "checklist":
				return "Shared item has been deleted from checklist by "
						+ user.getString("name") + " in a trip "
						+ trip.getString("trip_name");
			case "best_places":
				return "Location has been deleted from a trip "
						+ trip.getString("trip_name") + " by "
						+ user.getString("name");
			case "trip_members":
				return "Member has been deleted from a trip "
						+ trip.getString("trip_name") + " by " + user.getString("name");
			default:
				return "Trip " + trip.getString("trip_name")
						+ " has been modified by " + user.getString("name");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

    private void ProcessNewTripCreationForUser(Object tripid, Object tel, Object added_by) {
        try {
            JSONObject json = db.getTripInfo(tripid);
            JSONObject data = db.getUsersInfo(json.get("created_by"));
            JSONObject user = db.getUsersInfo(tel);
            db.recordNewTripCreationModification(json, tel);
            db.getTripExpensesForNewMember(json, tel);
            db.getTripChecklistForNewMember(json, tel);
            db.getTripBestPlacesForNewMember(json, tel);
            db.getTripMembersForNewMember(json, tel, added_by);
            
            String body = "Hello " + user.getString("name").trim()
                    + ",\nWelcome to Complete Travel Planner."
                    + "\nYou have been added to the trip " + json.getString("trip_name").trim() 
                    + " starting from " + json.getString("start_date").trim() + " to " 
                    + json.getString("end_date").trim() + 
                    ".\nAdmin of this trip is " + data.getString("name").trim()
                    + ".\n\nThanks,\nComplete Travel Planner Team";
            sendEmail(user.getString("email_id"), body, "Trip Creation Notification");
            String gcmMessage = "You have been added to the trip " + json.getString("trip_name").trim();
            sendGCMToSingleUser(tel, gcmMessage);
            sendGCMForTrip(tripid, tel, getInsertActionText(added_by, tripid, "trip_members"));
        } catch (JSONException ex) {
        	log.severe(ex.toString());
        }
    }
    
	public void sendGCMToSingleUser(Object tel, String body) {
		String[] regIds = new String[1];
		Object userInfo = db.getGCMId(tel);
		if(userInfo != null){
			regIds[0] = (String) userInfo;
			GCMSender.sendGCMNotification(regIds, body);
		}
	}

	public void sendGCMForTrip(Object tripid, Object telephone, String message) {
		try {
			ArrayList<String> members = db.getTripMembersID(tripid, telephone);
			String[] regIds = new String[members.size()];
			for (int i = 0; i < members.size(); i++) {
				JSONObject userInfo = db.getUsersInfo(members.get(i));
				regIds[i] = userInfo.getString("gcm_id");
			}
			GCMSender.sendGCMNotification(regIds, message);
		} catch (JSONException ex) {
			log.severe(ex.toString());
		}
	}
}