package completetravelplannerserver;

import completetravelplannerserver.org.json.*;
import java.sql.*;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vinayakpalav
 */
public class DBHelper {

    private final String DBLINK = "jdbc:derby://localhost:1527/CompleteTravelPlanner";
    private final String DBUSER = "ctpdb";
    private final String DBPASS = "ctpdb";
    private Connection conn;
    private HashMap<String, Integer> columnDBType;
    private static DBHelper db = null;

    private DBHelper() {
        try {
            System.out.println("Connecting to Database ... ");
            conn = DriverManager.getConnection(DBLINK, DBUSER, DBPASS);
            System.out.println("Connected to Database successfully");
            columnDBType = createDBTypesMap();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static synchronized DBHelper getDBHelperInstance() {
        if (DBHelper.db == null) {
            DBHelper.db = new DBHelper();
        }
        return DBHelper.db;
    }

    private HashMap<String, Integer> createDBTypesMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("trip_id", java.sql.Types.INTEGER);
        map.put("telephone", java.sql.Types.NUMERIC);
        map.put("tour_destination", java.sql.Types.VARCHAR);
        map.put("location", java.sql.Types.VARCHAR);
        map.put("wiki_key", java.sql.Types.VARCHAR);
        map.put("item_name", java.sql.Types.VARCHAR);
        map.put("item_id", java.sql.Types.VARCHAR);
        map.put("added_by", java.sql.Types.VARCHAR);
        map.put("assigned_to", java.sql.Types.VARCHAR);
        map.put("type", java.sql.Types.VARCHAR);
        map.put("status", java.sql.Types.VARCHAR);
        map.put("quantity", java.sql.Types.INTEGER);
        map.put("reminder", java.sql.Types.DATE);
        map.put("amount", java.sql.Types.REAL);
        map.put("added_on", java.sql.Types.TIMESTAMP);
        map.put("modification_id", java.sql.Types.INTEGER);
        map.put("table_name", java.sql.Types.VARCHAR);
        map.put("column_name", java.sql.Types.VARCHAR);
        map.put("value", java.sql.Types.VARCHAR);
        map.put("action", java.sql.Types.VARCHAR);
        map.put("trip_name", java.sql.Types.VARCHAR);
        map.put("start_date", java.sql.Types.DATE);
        map.put("end_date", java.sql.Types.DATE);
        map.put("received_on", java.sql.Types.TIMESTAMP);
        map.put("is_sent", java.sql.Types.VARCHAR);
        map.put("name", java.sql.Types.VARCHAR);
        map.put("email_id", java.sql.Types.VARCHAR);
        map.put("gcm_id", java.sql.Types.LONGVARCHAR);
        return map;
    }

    public void insertUser(JSONObject object) {
        try {
            System.out.println("Inserting User ... " + object.getString("name"));
            String SQL = "INSERT INTO USER_INFO (NAME, TELEPHONE, EMAIL_ID, GCM_ID)"
                    + " VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("name"), columnDBType.get("name"));
            stmt.setObject(2, object.get("telephone"), columnDBType.get("telephone"));
            stmt.setObject(3, object.get("email_id"), columnDBType.get("email_id"));
            stmt.setObject(4, object.get("gcm_id"), columnDBType.get("gcm_id"));
            stmt.executeUpdate();
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int insertTrip(JSONObject object) {
        int tripid = getNextTripId();
        try {
            System.out.println("trip_creation " + object.toString());
            java.util.Date startDate = new SimpleDateFormat("dd-MM-yyyy")
                    .parse(object.getString("start_date"));
            java.util.Date endDate = new SimpleDateFormat("dd-MM-yyyy")
                    .parse(object.getString("end_date"));
            String SQL = "INSERT INTO CTPDB.TRIP_INFO (TRIP_NAME, TRIP_ID, "
                    + "START_DATE, END_DATE, CREATED_BY) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("trip_name"), columnDBType.get("trip_name"));
            stmt.setObject(2, tripid, columnDBType.get("trip_id"));
            stmt.setObject(3, new Date(startDate.getTime()), columnDBType.get("start_date"));
            stmt.setObject(4, new Date(endDate.getTime()), columnDBType.get("end_date"));
            stmt.setObject(5, object.get("created_by"), columnDBType.get("telephone"));
            stmt.executeUpdate();
            addMemberToTheTrip(tripid, object.get("created_by"));
        } catch (SQLException | JSONException | ParseException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tripid;
    }

    public ArrayList<String> getTripMembersID(Object trip_id, Object tel) {
        ArrayList<String> members = new ArrayList<>();
        try {
            String SQL = "SELECT TELEPHONE FROM "
                    + " TRIP_MEMBERS WHERE TRIP_ID = ? ";
            if (tel != null) {
                SQL += " and telephone != ?";
            }
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            if (tel != null) {
                stmt.setObject(2, tel, columnDBType.get("telephone"));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(rs.getObject("telephone", String.class));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return members;
    }

    /**
     *
     * @param object
     */
    public void recordModification(JSONObject object) {
        try {
            int mod_id = getNextModifationId();
            if(insertIntoUpdatesTracker(object.getString("trip_id"), mod_id,
                    object.get("telephone"))){
                insertIntoModication(object, mod_id);
            }
            String action = object.getString("action");
            switch (action) {
                case "INSERT":
                    insertDataIntoTable(object);
                    break;
                case "UPDATE":
                    updateDataIntoTable(object);
                    break;
                case "DELETE":
                    deleteDataFromTable(object);
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        } catch (JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNextTripId() {
        int id = 0;
        try {
            String SQL = "select max(trip_id) from ctpdb.trip_info";
            ResultSet rs = conn.createStatement().executeQuery(SQL);
            rs.next();
            id = rs.getInt(1);
            System.out.println("trip Id: " + id + 1);
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id + 1;
    }

    public int getNextModifationId() {
        int id = 0;
        try {
            String SQL = "select max(modification_id) from ctpdb.modification";
            ResultSet rs = conn.createStatement().executeQuery(SQL);
            rs.next();
            id = rs.getInt(1);
            System.out.println("Mod Id: " + id + 1);
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id + 1;
    }

    public boolean insertIntoUpdatesTracker(Object trip_id, int modId, Object tel) {
        try {
            ArrayList<String> users = getTripMembersID(trip_id, tel);
            if(users.isEmpty())
                return false;
            String SQL = "insert into updates_tracker "
                    + "(trip_id, telephone, modification_id, is_sent, "
                    + "received_on) values (?, ?, ?, ?, ?)";
            for (String telNo : users) {
                PreparedStatement stmt = conn.prepareStatement(SQL);
                stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
                stmt.setObject(2, telNo, columnDBType.get("telephone"));
                stmt.setObject(3, modId, columnDBType.get("modification_id"));
                stmt.setObject(4, "N", columnDBType.get("is_sent"));
                stmt.setObject(5, new Date(new java.util.Date().getTime()),
                        columnDBType.get("received_on"));
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    private void insertDataIntoTable(JSONObject object) {
        try {
            JSONArray data = object.getJSONArray("data");
            StringBuilder SQL = new StringBuilder("insert into ");
            SQL.append(object.getString("tablename")).append(" ( ");
            SQL.append(data.getJSONObject(0).getString("column"));
            for (int i = 1; i < data.length(); i++) {
                SQL.append(", ").append(data.getJSONObject(i).getString("column"));
            }
            SQL.append(") values (?");
            for (int i = 0; i < data.length() - 1; i++) {
                SQL.append(", ?");
            }
            SQL.append(")");
            System.out.println("Executing ... " + SQL.toString());
            PreparedStatement stmt = conn.prepareStatement(SQL.toString());
            for (int i = 0; i < data.length(); i++) {
                stmt.setObject(i + 1, data.getJSONObject(i).get("value"),
                        columnDBType.get(data.getJSONObject(i).getString("column")));
            }
            stmt.executeUpdate();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateDataIntoTable(JSONObject object) {
        try {
            JSONArray data = object.getJSONArray("data");
            JSONArray where = object.getJSONArray("where");

            //prepare SQL update statement
            StringBuilder SQL = new StringBuilder("update ");
            SQL.append(object.getString("tablename")).append(" set ");
            SQL.append(data.getJSONObject(0).getString("column")).append(" = ? ");
            for (int i = 1; i < data.length(); i++) {
                SQL.append(", ").append(data.getJSONObject(i)
                        .getString("column")).append(" = ? ");
            }
            SQL.append(" where ").append(where.getJSONObject(0)
                    .getString("column")).append(" = ? ");
            for (int i = 1; i < where.length(); i++) {
                SQL.append(" and ").append(where.getJSONObject(i)
                        .getString("column")).append(" = ? ");
            }
            System.out.println("Executing ... " + SQL.toString());
            int count = 0;
            PreparedStatement stmt = conn.prepareStatement(SQL.toString());
            for (int i = 0; i < data.length(); i++) {
                stmt.setObject(++count, data.getJSONObject(i).get("value"),
                        columnDBType.get(data.getJSONObject(i).getString("column")));
            }

            for (int i = 0; i < where.length(); i++) {
                stmt.setObject(++count, where.getJSONObject(i).get("value"),
                        columnDBType.get(where.getJSONObject(i).getString("column")));;
            }
            stmt.executeUpdate();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void deleteDataFromTable(JSONObject object) {
        try {
            JSONArray data = object.getJSONArray("data");

            //prepare SQL delete statement
            StringBuilder SQL = new StringBuilder("delete from ");
            SQL.append(object.getString("tablename")).append(" where ");
            SQL.append(data.getJSONObject(0).getString("column")).append(" = ? ");
            for (int i = 1; i < data.length(); i++) {
                SQL.append(" and ").append(data.getJSONObject(i)
                        .getString("column")).append(" = ? ");
            }
            System.out.println("Executing ... " + SQL.toString());
            PreparedStatement stmt = conn.prepareStatement(SQL.toString());
            for (int i = 0; i < data.length(); i++) {
                stmt.setObject(i + 1, data.getJSONObject(i).get("value"),
                        columnDBType.get(data.getJSONObject(i).getString("column")));
            }

            stmt.executeUpdate();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void insertIntoModication(JSONObject object, int mod_id) {
        try {
            String SQL = "insert into modification values (?, ?, ?, ?, ?, ?, ?)";
            JSONArray data = object.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                System.out.println("Modification Row ... "
                        + object.get("trip_id") + " "
                        + mod_id + " "
                        + object.get("telephone") + " "
                        + object.get("tablename") + " "
                        + data.getJSONObject(i).get("column") + " "
                        + data.getJSONObject(i).get("value") + " "
                        + object.get("action"));

                PreparedStatement stmt = conn.prepareStatement(SQL);
                stmt.setObject(1, object.get("trip_id"), columnDBType.get("trip_id"));
                stmt.setObject(2, mod_id, columnDBType.get("modification_id"));
                stmt.setObject(3, object.get("telephone"), columnDBType.get("telephone"));
                stmt.setObject(4, object.get("tablename"), columnDBType.get("table_name"));
                stmt.setObject(5, data.getJSONObject(i).get("column"),
                        columnDBType.get("column_name"));
                stmt.setObject(6, data.getJSONObject(i).get("value"),
                        columnDBType.get("value"));
                stmt.setObject(7, object.get("action"), columnDBType.get("action"));
                stmt.executeUpdate();
                conn.commit();
            }
            if (object.has("where")) {
                JSONArray where = object.getJSONArray("where");
                for (int i = 0; i < where.length(); i++) {
                    System.out.println("Modification Row ... "
                            + object.get("trip_id") + " "
                            + mod_id + " "
                            + object.get("telephone") + " "
                            + object.get("tablename") + " "
                            + where.getJSONObject(i).get("column") + " "
                            + where.getJSONObject(i).get("value") + " "
                            + object.get("action") + "_WHERE");

                    PreparedStatement stmt = conn.prepareStatement(SQL);
                    stmt.setObject(1, object.get("trip_id"), columnDBType.get("trip_id"));
                    stmt.setObject(2, mod_id, columnDBType.get("modification_id"));
                    stmt.setObject(3, object.get("telephone"), columnDBType.get("telephone"));
                    stmt.setObject(4, object.get("tablename"), columnDBType.get("table_name"));
                    stmt.setObject(5, where.getJSONObject(i).get("column"),
                            columnDBType.get("column_name"));
                    stmt.setObject(6, where.getJSONObject(i).get("value"),
                            columnDBType.get("value"));
                    stmt.setObject(7, object.getString("action") + "_WHERE", columnDBType.get("action"));
                    stmt.executeUpdate();
                    conn.commit();
                }
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void removeTripForUser(String trip_id, String telephone) {
        try {
            String deleteTripMembers = "delete from trip_members where trip_id = ? "
                    + " and telephone = ? ";
            PreparedStatement stmt = conn.prepareStatement(deleteTripMembers);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteUpdatesTracker = "delete from updates_tracker where trip_id = ? "
                    + " and telephone = ? ";
            stmt = conn.prepareStatement(deleteUpdatesTracker);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteModification = "delete from modification where trip_id = ? "
                    + " and telephone = ? ";
            stmt = conn.prepareStatement(deleteModification);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("trip_id"));
            stmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void removeTripCompletely(String trip_id) {
        try {
            String deleteTripInfo = "delete from trip_info where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteTripInfo);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteTripMembers = "delete from trip_members where trip_id = ?";
            stmt = conn.prepareStatement(deleteTripMembers);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteUpdatesTracker = "delete from updates_tracker where trip_id = ?";
            stmt = conn.prepareStatement(deleteUpdatesTracker);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteModification = "delete from modification where trip_id = ?";
            stmt = conn.prepareStatement(deleteModification);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteChecklist = "delete from checklist where trip_id = ?";
            stmt = conn.prepareStatement(deleteChecklist);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteExpenses = "delete from expenses where trip_id = ?";
            stmt = conn.prepareStatement(deleteExpenses);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteBestPlaces = "delete from best_places where trip_id = ?";
            stmt = conn.prepareStatement(deleteBestPlaces);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public JSONArray retrieveAllModifications(JSONObject object) {
        JSONArray json = new JSONArray();
        try {
            Object telephone = object.get("telephone");
            JSONArray array = retrieveModificationIds(telephone);
            for (int i = 0; i < array.length(); i++) {
                json.put(getModificationById(array.getJSONObject(i).get("trip_id"),
                        array.getJSONObject(i).getInt("mod_id")));
                updateIsSentForModification(array.getJSONObject(i)
                        .get("trip_id"), telephone);
            }
        } catch (JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    public JSONObject getModificationById(Object tripid, int modid) {
        JSONObject single = new JSONObject();
        JSONArray data = new JSONArray();
        JSONArray where = new JSONArray();
        try {
            String SQL = "select * from modification where trip_id = ? "
                    + " and modification_id = ? and "
                    + " action in ('INSERT', 'UPDATE', 'DELETE')"
                    + " order by modification_id";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            stmt.setInt(2, modid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                single.put("trip_id", rs.getObject("trip_id", String.class));
                single.put("action", rs.getObject("action", String.class));
                single.put("telephone", rs.getObject("telephone", String.class));
                single.put("tablename", rs.getObject("table_name", String.class));
                data.put(new JSONObject()
                        .put("column", rs.getObject("column_name", String.class))
                        .put("value", rs.getObject("value", String.class)));
            }
            single.put("data", data);

            SQL = "select column_name, value from modification where trip_id = ? "
                    + " and modification_id = ? and action = 'UPDATE_WHERE'";
            stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            stmt.setInt(2, modid);
            rs = stmt.executeQuery();
            boolean flag = false;
            while (rs.next()) {
                flag = true;
                where.put(new JSONObject()
                        .put("column", rs.getObject("column_name", String.class))
                        .put("value", rs.getObject("value", String.class)));
            }
            if (flag) {
                single.put("where", where);
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return single;
    }

    public JSONArray retrieveModificationIds(Object telephone) {
        JSONArray array = new JSONArray();
        try {
            String SQL = "select trip_id, modification_id from updates_tracker "
                    + " where is_sent = 'N' and telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                array.put(new JSONObject().put("trip_id", rs.getInt(1))
                        .put("mod_id", rs.getInt(2)));
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return array;
    }

    public void updateIsSentForModification(Object tripid, Object telephone) {
        try {
            String SQL = "update updates_tracker set is_sent = 'Y' "
                    + " where trip_id = ? and telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("telephone"));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void close() {
        try {
            System.out.println("Disconnecting from database ...");
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void saveUserCode(JSONObject object, int auth) {
        try {
            String SQL = "insert into user_auth_codes values (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("telephone"), columnDBType.get("telephone"));
            stmt.setInt(2, auth);
            stmt.executeUpdate();
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean autheticateUserCode(JSONObject object) {
        try {
            String SQL = "Select code from user_auth_codes where telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("telephone"), columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            boolean flag = (rs.getInt("code") == object.getInt("code"));

            if (flag) {
                SQL = "delete from user_auth_codes where telephone = ?";
                stmt = conn.prepareStatement(SQL);
                stmt.setObject(1, object.get("telephone"),
                        columnDBType.get("telephone"));
                stmt.executeUpdate();
            }
            return flag;
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public JSONObject addMemberToTheTrip(Object tripid, Object tel) {
        try {
            String sql = "insert into trip_members (trip_id, telephone) "
                    + "values (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            stmt.setObject(2, tel, columnDBType.get("telephone"));
            stmt.executeUpdate();
            return getUsersInfo(tel);
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public JSONObject getUsersInfo(Object telephone) {
        JSONObject object = new JSONObject();
        try {
            String SQL = "select name, telephone, email_id from user_info "
                    + "where telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            object.put("name", rs.getObject(1));
            object.put("telephone", rs.getObject(2));
            object.put("email_id", rs.getObject(3));
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    public JSONObject getTripInfo(Object tripid) {
        JSONObject object = new JSONObject();
        try {

            String SQL = "select * from trip_info where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            object.put("trip_id", rs.getObject("trip_id", String.class));
            object.put("trip_name", rs.getObject("trip_name", String.class));
            object.put("start_date", rs.getObject("start_date", String.class));
            object.put("end_date", rs.getObject("end_date", String.class));
            object.put("created_by", rs.getObject("created_by", String.class));
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    public void recordNewTripCreationModification(JSONObject trip, Object tel) {
        try {
            JSONObject object = new JSONObject();
            object.put("transaction_type", "MODIFICATION_PUSH");
            object.put("action", "INSERT");
            object.put("tablename", "trip_info");
            object.put("trip_id", trip.get("trip_id"));
            object.put("telephone", trip.get("created_by"));

            JSONArray array = new JSONArray();
            array.put(0, new JSONObject().put("column", "trip_id")
                    .put("value", trip.get("trip_id")));
            array.put(1, new JSONObject().put("column", "trip_name")
                    .put("value", trip.get("trip_name")));
            array.put(2, new JSONObject().put("column", "start_date")
                    .put("value", trip.get("start_date")));
            array.put(3, new JSONObject().put("column", "end_date")
                    .put("value", trip.get("end_date")));
            array.put(4, new JSONObject().put("column", "created_by")
                    .put("value", trip.get("created_by")));

            object.put("data", array);
            System.out.println(object.toString());
            insertObject(object, tel);
        } catch (JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getTripExpensesForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from expenses where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "expenses");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", trip.get("created_by"));
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", rs.getObject("trip_id")));
                array.put(1, new JSONObject().put("column", "item_id")
                        .put("value", rs.getObject("item_id")));
                array.put(2, new JSONObject().put("column", "item_name")
                        .put("value", rs.getObject("item_name")));
                array.put(3, new JSONObject().put("column", "added_on")
                        .put("value", rs.getObject("added_on")));
                array.put(4, new JSONObject().put("column", "added_by")
                        .put("value", rs.getObject("added_by")));
                array.put(5, new JSONObject().put("column", "amount")
                        .put("value", rs.getObject("amount")));
                object.put("data", array);
                System.out.println(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getTripChecklistForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from checklist where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "checklist");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", trip.get("created_by"));
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", rs.getObject("trip_id")));
                array.put(1, new JSONObject().put("column", "item_id")
                        .put("value", rs.getObject("item_id")));
                array.put(2, new JSONObject().put("column", "item_name")
                        .put("value", rs.getObject("item_name")));
                array.put(3, new JSONObject().put("column", "status")
                        .put("value", rs.getObject("status")));
                array.put(4, new JSONObject().put("column", "added_by")
                        .put("value", rs.getObject("added_by")));
                array.put(5, new JSONObject().put("column", "type")
                        .put("value", rs.getObject("type")));
                array.put(6, new JSONObject().put("column", "assigned_to")
                        .put("value", rs.getObject("assigned_to")));
                array.put(7, new JSONObject().put("column", "quantity")
                        .put("value", rs.getObject("quantity")));
                object.put("data", array);
                System.out.println(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getTripBestPlacesForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from best_places where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "best_places");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", trip.get("created_by"));
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", rs.getObject("trip_id")));
                array.put(1, new JSONObject().put("column", "tour_destination")
                        .put("value", rs.getObject("tour_destination")));
                array.put(2, new JSONObject().put("column", "location")
                        .put("value", rs.getObject("location")));
                array.put(3, new JSONObject().put("column", "wiki_key")
                        .put("value", rs.getObject("wiki_key")));
                object.put("data", array);
                System.out.println(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getTripMembersForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select trip_id, telephone from trip_members "
                    + " where trip_id = ? and telephone != ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            stmt.setObject(2, tel, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "trip_members");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", trip.get("created_by"));
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", rs.getObject("trip_id")));
                JSONObject user = getUsersInfo(rs.getObject("telephone"));
                array.put(1, new JSONObject().put("column", "telephone")
                        .put("value", user.get("telephone")));
                array.put(2, new JSONObject().put("column", "name")
                        .put("value", user.get("name")));
                array.put(3, new JSONObject().put("column", "email_id")
                        .put("value", user.get("email_id")));
                object.put("data", array);
                System.out.println(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertObject(JSONObject object, Object tel) {
        try {
            int mod_id = getNextModifationId();
            String SQL = "insert into updates_tracker "
                    + "(trip_id, telephone, modification_id, is_sent, "
                    + "received_on) values (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("trip_id"), columnDBType.get("trip_id"));
            stmt.setObject(2, tel, columnDBType.get("telephone"));
            stmt.setObject(3, mod_id, columnDBType.get("modification_id"));
            stmt.setObject(4, "N", columnDBType.get("is_sent"));
            stmt.setObject(5, new Date(new java.util.Date().getTime()),
                    columnDBType.get("received_on"));
            stmt.executeUpdate();
            insertIntoModication(object, mod_id);
        } catch (SQLException | JSONException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
