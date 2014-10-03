package completetravelplannerserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import completetravelplannerserver.org.json.*;
import java.net.Socket;
import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author vinayakpalav
 */
public class CompleteTravelPlannerServer {

    private static final int portno = 6878;
    private static ServerSocket connect;
    private static final int maxThreads = 100;

    public static void main(String[] args) {
        try {
            final Executor exec = Executors.newFixedThreadPool(maxThreads);
            connect = new ServerSocket(portno);
            System.out.println("Server Started successfully " + connect.toString());
            while (true) {
                final Socket incoming = connect.accept();
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleRequest(incoming);
                    }
                });
            }
        } catch (IOException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            try {
                DBHelper.getDBHelperInstance().close();
                connect.close();
            } catch (IOException ex) {
                Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void handleRequest(Socket incoming) {
        Client client = new Client(incoming);
        try (Scanner scan = new Scanner(incoming.getInputStream())) {
            client.handleIncoming(scan.nextLine().trim());
        } catch (IOException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            client.close();
        }
    }

    public static void helperToInsert() {
        DBHelper db = DBHelper.getDBHelperInstance();
        try {
            JSONObject object = new JSONObject();
            object.put("transaction_type", "MODIFICATION_PUSH");
            object.put("action", "INSERT");
            object.put("tablename", "expenses");
            object.put("trip_id", "1");
            object.put("telephone", "18622299483");

            JSONArray array = new JSONArray();
            array.put(0, new JSONObject().put("column", "trip_id")
                    .put("value", "1"));
            array.put(1, new JSONObject().put("column", "item_name")
                    .put("value", "notebook"));
            array.put(2, new JSONObject().put("column", "amount")
                    .put("value", "100.50"));
            array.put(3, new JSONObject().put("column", "added_by")
                    .put("value", "18622299483"));
            array.put(4, new JSONObject().put("column", "added_on")
                    .put("value", "2014-04-20 10:10:10"));
            array.put(5, new JSONObject().put("column", "item_id")
                    .put("value", "12"));

            object.put("data", array);
            System.out.println(object.toString());
            db.recordModification(object);
        } catch (JSONException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public static void helperToUpdate() {
        DBHelper db = DBHelper.getDBHelperInstance();
        try {
            JSONObject object = new JSONObject();
            object.put("transaction_type", "MODIFICATION_PUSH");
            object.put("action", "UPDATE");
            object.put("tablename", "expenses");
            object.put("trip_id", "1");
            object.put("telephone", "18622299483");

            JSONArray array = new JSONArray();
            array.put(0, new JSONObject().put("column", "item_name")
                    .put("value", "notebook_new"));
            array.put(1, new JSONObject().put("column", "amount")
                    .put("value", "300"));

            JSONArray array2 = new JSONArray();
            array2.put(0, new JSONObject().put("column", "trip_id")
                    .put("value", "1"));
            array2.put(1, new JSONObject().put("column", "item_id")
                    .put("value", "12"));

            object.put("data", array);
            object.put("where", array2);
            System.out.println(object.toString());
            db.recordModification(object);
        } catch (JSONException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public static void helperToDelete() {
        DBHelper db = DBHelper.getDBHelperInstance();
        try {
            JSONObject object = new JSONObject();
            object.put("transaction_type", "MODIFICATION_PUSH");
            object.put("action", "DELETE");
            object.put("tablename", "expenses");
            object.put("trip_id", "1");
            object.put("telephone", "18622299483");

            JSONArray array = new JSONArray();
            array.put(0, new JSONObject().put("column", "trip_id")
                    .put("value", "1"));
            array.put(1, new JSONObject().put("column", "item_id")
                    .put("value", "12"));

            object.put("data", array);

            System.out.println(object.toString());
            db.recordModification(object);
        } catch (JSONException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public static void helperToPullData() {
        DBHelper db = DBHelper.getDBHelperInstance();
        try {
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray().put("1");
            object.put("transaction_type", "MODIFICATION_PULL");
            object.put("telephone", "18622299483");
            object.put("data", array);
            System.out.println(db.retrieveAllModifications(object).toString());
        } catch (JSONException ex) {
            Logger.getLogger(CompleteTravelPlannerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
