package completetravelplannerserver;

import completetravelplannerserver.org.json.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author vinayakpalav
 */
public class DBHelper {

    private final String DBLINK = "jdbc:google:mysql://completetravelplanner:cpt-db/ctpdb?user=root";
    private Connection conn;
    private HashMap<String, Integer> columnDBType;
    private static DBHelper db = null;
    private static final Logger log = Logger.getLogger(DBHelper.class.getName());

	private DBHelper() {
		try {
			log.info("Connecting to Database ... ");
			Class.forName("com.mysql.jdbc.GoogleDriver");
			conn = DriverManager.getConnection(DBLINK);
			log.info("Connected to Database successfully");
			columnDBType = createDBTypesMap();
		} catch (Exception  ex) {
			log.severe(ex.toString());
		}
	}

    public static synchronized DBHelper getDBHelperInstance() {
        if (DBHelper.db == null) {
            DBHelper.db = new DBHelper();
        }
        DBHelper.db.connectIfClosed();
        return DBHelper.db;
    }

    private synchronized void connectIfClosed() {
    	try {
			if(conn.isClosed()){
				conn = DriverManager.getConnection(DBLINK);
			}
		} catch (SQLException ex) {
			log.severe(ex.toString());
		}
		
	}

	private HashMap<String, Integer> createDBTypesMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("trip_id", java.sql.Types.INTEGER);
        map.put("telephone", java.sql.Types.NUMERIC);
        map.put("tour_destination", java.sql.Types.VARCHAR);
        map.put("location", java.sql.Types.LONGVARCHAR);
        map.put("wiki_key", java.sql.Types.LONGVARCHAR);
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
        map.put("value", java.sql.Types.LONGVARCHAR);
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

    public boolean checkIfUserExist(Object telephone){
    	try {
    		String sql = "select count(*) from ctpdb.user_info " +
        			"where telephone = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setObject(1, telephone, columnDBType.get("telephone"));
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return (rs.getInt(1) > 0);
		} catch (SQLException ex) {
			log.severe(ex.toString());
		}
    	return false;
    }
    
    public boolean insertUser(JSONObject object) {
    	boolean flag = false;
        try {
            log.info("Inserting User ... " + object.getString("name"));
            String SQL = "insert into ctpdb.user_info (name, telephone, email_id, gcm_id)"
                    + " values (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("name"), columnDBType.get("name"));
            stmt.setObject(2, object.get("telephone"), columnDBType.get("telephone"));
            stmt.setObject(3, object.get("email_id"), columnDBType.get("email_id"));
            stmt.setObject(4, object.get("gcm_id"), columnDBType.get("gcm_id"));
            flag = (stmt.executeUpdate() > 0);
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
        return flag;
    }

    public int insertTrip(JSONObject object) {
        int tripid = getNextTripId();
        try {
            log.info("trip_creation " + object.toString());
            java.util.Date startDate = new SimpleDateFormat("dd-MM-yyyy")
                    .parse(object.getString("start_date"));
            java.util.Date endDate = new SimpleDateFormat("dd-MM-yyyy")
                    .parse(object.getString("end_date"));
            String SQL = "insert into ctpdb.trip_info (trip_name, trip_id, "
                    + "start_date, end_date, created_by) values (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("trip_name"), columnDBType.get("trip_name"));
            stmt.setObject(2, tripid, columnDBType.get("trip_id"));
            stmt.setObject(3, new Date(startDate.getTime()), columnDBType.get("start_date"));
            stmt.setObject(4, new Date(endDate.getTime()), columnDBType.get("end_date"));
            stmt.setObject(5, object.get("created_by"), columnDBType.get("telephone"));
            stmt.executeUpdate();
            addMemberToTheTrip(tripid, object.get("created_by"));
        } catch (SQLException | JSONException | ParseException ex) {
        	log.severe(ex.toString());
        }
        return tripid;
    }

    public ArrayList<String> getTripMembersID(Object trip_id, Object tel) {
        ArrayList<String> members = new ArrayList<>();
        try {
            String SQL = "select telephone from ctpdb.trip_members where trip_id = ? ";
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
        	log.severe(ex.toString());
        }
        return members;
    }

    /**
     *
     * @param object
     */
    public boolean recordModification(JSONObject object) {
    	boolean flag = false;
        try {
            String action = object.getString("action");
            switch (action) {
                case "INSERT":
                	flag = insertDataIntoTable(object);
                    break;
                case "UPDATE":
                	flag = updateDataIntoTable(object);
                    break;
                case "DELETE":
                	flag = deleteDataFromTable(object);
                    break;
                default:
                    log.info("Unknown action: " + action);
            }
            log.info("recordModification: " + flag);
            int mod_id = getNextModifationId();
            if(insertIntoUpdatesTracker(object.getString("trip_id"), mod_id,
                    object.get("telephone"))){
                flag = insertIntoModication(object, mod_id);
            }
        } catch (JSONException ex) {
        	log.severe(ex.toString());
        }
        return flag;
    }

    public int getNextTripId() {
        int id = 0;
        try {
            String SQL = "select max(trip_id) from ctpdb.trip_info";
            ResultSet rs = conn.createStatement().executeQuery(SQL);
            rs.next();
            id = rs.getInt(1);
            log.info("trip Id: " + id + 1);
        } catch (SQLException ex) {
        	log.severe(ex.toString());
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
            log.info("Mod Id: " + id + 1);
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
        return id + 1;
    }

    public boolean insertIntoUpdatesTracker(Object trip_id, int modId, Object tel) {
        try {
            ArrayList<String> users = getTripMembersID(trip_id, tel);
            if(users.isEmpty())
                return false;
            String SQL = "insert into ctpdb.updates_tracker "
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
        	log.severe(ex.toString());
        }
        return true;
    }

    private boolean insertDataIntoTable(JSONObject object) {
    	boolean flag = false;
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
            log.info("Executing ... " + SQL.toString());
            PreparedStatement stmt = conn.prepareStatement(SQL.toString());
            for (int i = 0; i < data.length(); i++) {
                stmt.setObject(i + 1, data.getJSONObject(i).get("value"),
                        columnDBType.get(data.getJSONObject(i).getString("column")));
            }
            flag = (stmt.executeUpdate() > 0);
        } catch (JSONException | SQLException ex) {
        	log.severe(ex.toString());
        }
        return flag;
    }

    private boolean updateDataIntoTable(JSONObject object) {
    	boolean flag = false;
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
            log.info("Executing ... " + SQL.toString());
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
            flag = (stmt.executeUpdate() > 0);
        } catch (JSONException | SQLException ex) {
        	log.severe(ex.toString());
        }
        return flag;
    }

    private boolean deleteDataFromTable(JSONObject object) {
    	boolean flag = false;
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
            log.info("Executing ... " + SQL.toString());
            PreparedStatement stmt = conn.prepareStatement(SQL.toString());
            for (int i = 0; i < data.length(); i++) {
                stmt.setObject(i + 1, data.getJSONObject(i).get("value"),
                        columnDBType.get(data.getJSONObject(i).getString("column")));
            }

            flag = (stmt.executeUpdate() > 0);
        } catch (JSONException | SQLException ex) {
        	log.severe(ex.toString());
        }
        return flag;
    }

    private boolean insertIntoModication(JSONObject object, int mod_id) {
    	boolean flag = false;
        try {
            String SQL = "insert into ctpdb.modification (trip_id, telephone," +
            		"modification_id, table_name, column_name, value, action) " +
            		"values (?, ?, ?, ?, ?, ?, ?)";
            JSONArray data = object.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                log.info("Modification Row ... "
                        + object.get("trip_id") + " "
                        + mod_id + " "
                        + object.get("telephone") + " "
                        + object.get("tablename") + " "
                        + data.getJSONObject(i).get("column") + " "
                        + data.getJSONObject(i).get("value") + " "
                        + object.get("action"));

                PreparedStatement stmt = conn.prepareStatement(SQL);
                stmt.setObject(1, object.get("trip_id"), columnDBType.get("trip_id"));
                stmt.setObject(2, object.get("telephone"), columnDBType.get("telephone"));
                stmt.setObject(3, mod_id, columnDBType.get("modification_id"));
                stmt.setObject(4, object.get("tablename"), columnDBType.get("table_name"));
                stmt.setObject(5, data.getJSONObject(i).get("column"),
                        columnDBType.get("column_name"));
                stmt.setObject(6, data.getJSONObject(i).get("value"),
                        columnDBType.get("value"));
                stmt.setObject(7, object.get("action"), columnDBType.get("action"));
                flag = (stmt.executeUpdate() > 0);
            }
            if (object.has("where")) {
                JSONArray where = object.getJSONArray("where");
                for (int i = 0; i < where.length(); i++) {
                    log.info("Modification Row ... "
                            + object.get("trip_id") + " "
                            + mod_id + " "
                            + object.get("telephone") + " "
                            + object.get("tablename") + " "
                            + where.getJSONObject(i).get("column") + " "
                            + where.getJSONObject(i).get("value") + " "
                            + object.get("action") + "_WHERE");

                    PreparedStatement stmt = conn.prepareStatement(SQL);
                    stmt.setObject(1, object.get("trip_id"), columnDBType.get("trip_id"));
                    stmt.setObject(2, object.get("telephone"), columnDBType.get("telephone"));
                    stmt.setObject(3, mod_id, columnDBType.get("modification_id"));
                    stmt.setObject(4, object.get("tablename"), columnDBType.get("table_name"));
                    stmt.setObject(5, where.getJSONObject(i).get("column"),
                            columnDBType.get("column_name"));
                    stmt.setObject(6, where.getJSONObject(i).get("value"),
                            columnDBType.get("value"));
                    stmt.setObject(7, object.getString("action") + "_WHERE", columnDBType.get("action"));
                    flag = (stmt.executeUpdate() > 0);
                }
            }
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
        log.info("insertIntoModication: " + flag);
        return flag;
    }

    public void removeTripForUser(String trip_id, String telephone) {
        try {
            String deleteTripMembers = "delete from ctpdb.trip_members where trip_id = ? "
                    + " and telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteTripMembers);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("telephone"));
            stmt.executeUpdate();

            String deleteModification = "delete from ctpdb.modification where trip_id = ? "
                    + " and telephone = ?";
            stmt = conn.prepareStatement(deleteModification);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("telephone"));
            stmt.executeUpdate();
            
            String deleteUpdatesTracker = "delete from ctpdb.updates_tracker where trip_id = ? "
                    + " and telephone = ?";
            stmt = conn.prepareStatement(deleteUpdatesTracker);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("telephone"));
            stmt.executeUpdate();
            
            collectTripDeletionData(trip_id, telephone);
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
    }

    public void collectTripDeletionData(String trip_id, String telephone){
    	try {
	    	String sql = "select telephone from ctpdb.trip_members where trip_id = ?";
    		PreparedStatement stmt = conn.prepareStatement(sql);
    		stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
    		ResultSet rs = stmt.executeQuery();
    		while(rs.next()){
    			String tel = rs.getObject("telephone", String.class);
    			JSONObject object = new JSONObject();
    			object.put("transaction_type", "MODIFICATION_PUSH");
    			object.put("action", "DELETE");
    			object.put("trip_id", trip_id);
    			object.put("telephone", tel);
    			object.put("tablename", "trip_members");
    			JSONArray data = new JSONArray();
    			data.put(0, new JSONObject().put("column", "trip_id").put("value", trip_id));
    			data.put(1, new JSONObject().put("column", "telephone").put("value", telephone));
    			object.put("data", data);
    			insertObject(object, tel);
    		}
		} catch (SQLException | JSONException ex) {
			log.severe(ex.toString());
		}
    }
    
    public void removeTripCompletely(String trip_id) {
        try {
            String deleteTripMembers = "delete from ctpdb.trip_members where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteTripMembers);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteChecklist = "delete from ctpdb.checklist where trip_id = ?";
            stmt = conn.prepareStatement(deleteChecklist);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteExpenses = "delete from ctpdb.expenses where trip_id = ?";
            stmt = conn.prepareStatement(deleteExpenses);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteBestPlaces = "delete from ctpdb.best_places where trip_id = ?";
            stmt = conn.prepareStatement(deleteBestPlaces);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteUpdatesTracker = "delete from ctpdb.updates_tracker where trip_id = ?";
            stmt = conn.prepareStatement(deleteUpdatesTracker);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();

            String deleteModification = "delete from ctpdb.modification where trip_id = ?";
            stmt = conn.prepareStatement(deleteModification);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();
            
            String deleteTripInfo = "delete from ctpdb.trip_info where trip_id = ?";
            stmt = conn.prepareStatement(deleteTripInfo);
            stmt.setObject(1, trip_id, columnDBType.get("trip_id"));
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
        	log.severe(ex.toString());
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
        	log.severe(ex.toString());
        }
        return json;
    }

    public JSONObject getModificationById(Object tripid, int modid) {
        JSONObject single = new JSONObject();
        JSONArray data = new JSONArray();
        JSONArray where = new JSONArray();
        try {
            String SQL = "select * from ctpdb.modification where trip_id = ? "
                    + " and modification_id = ? and "
                    + " action in ('INSERT', 'UPDATE', 'DELETE')";
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

            SQL = "select column_name, value from ctpdb.modification where trip_id = ? "
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
        	log.severe(ex.toString());
        }
        return single;
    }

    public JSONArray retrieveModificationIds(Object telephone) {
        JSONArray array = new JSONArray();
        try {
            String SQL = "select trip_id, modification_id from ctpdb.updates_tracker "
                    + " where is_sent = 'N' and telephone = ? "
                    + " order by trip_id, modification_id";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                array.put(new JSONObject().put("trip_id", rs.getInt(1))
                        .put("mod_id", rs.getInt(2)));
            }
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
        return array;
    }

    public void updateIsSentForModification(Object tripid, Object telephone) {
        try {
            String SQL = "update ctpdb.updates_tracker set is_sent = 'Y', sent_on = CURRENT_TIMESTAMP"
                    + " where trip_id = ? and telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, tripid, columnDBType.get("trip_id"));
            stmt.setObject(2, telephone, columnDBType.get("telephone"));
            stmt.executeUpdate();
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
    }

    public void close() {
        try {
            log.info("Disconnecting from database ...");
            conn.close();
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
    }

    void saveUserCode(Object telephone, int auth) {
        try {
        	String SQL = "delete from ctpdb.user_auth_codes where telephone = ?";
        	PreparedStatement stmt = conn.prepareStatement(SQL);
        	stmt.setObject(1, telephone, columnDBType.get("telephone"));
        	stmt.executeUpdate();
            SQL = "insert into ctpdb.user_auth_codes (telephone, code) values (?, ?)";
            stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            stmt.setInt(2, auth);
            stmt.executeUpdate();
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
    }

    public boolean autheticateUserCode(JSONObject object) {
        try {
            String SQL = "Select code from ctpdb.user_auth_codes where telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, object.get("telephone"), columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            boolean flag = (rs.getInt("code") == object.getInt("code"));

            if (flag) {
                SQL = "delete from ctpdb.user_auth_codes where telephone = ?";
                stmt = conn.prepareStatement(SQL);
                stmt.setObject(1, object.get("telephone"),
                        columnDBType.get("telephone"));
                stmt.executeUpdate();
            }
            return flag;
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
        return false;
    }

    public JSONObject addMemberToTheTrip(Object tripid, Object tel) {
        try {
        	if(checkIfUserExist(tel)){
        		String sql = "insert into ctpdb.trip_members (trip_id, telephone) "
                    + "values (?, ?)";
        		PreparedStatement stmt = conn.prepareStatement(sql);
        		stmt.setObject(1, tripid, columnDBType.get("trip_id"));
        		stmt.setObject(2, tel, columnDBType.get("telephone"));
        		stmt.executeUpdate();
        		return getUsersInfo(tel);
        	}
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
        return null;
    }

    public JSONObject getUsersInfo(Object telephone) {
        JSONObject object = new JSONObject();
        try {
            String SQL = "select name, telephone, email_id, gcm_id from ctpdb.user_info "
                    + "where telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            object.put("name", rs.getObject(1));
            object.put("telephone", rs.getObject(2));
            object.put("email_id", rs.getObject(3));
            object.put("gcm_id", rs.getObject(4));
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
        return object;
    }
    
    public Object getGCMId(Object telephone){
    	try {
            String SQL = "select gcm_id from ctpdb.user_info where telephone = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, telephone, columnDBType.get("telephone"));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Object gcm = rs.getObject(1);
            if(gcm != null)
            	return gcm;
        } catch (SQLException ex) {
        	log.severe(ex.toString());
        }
    	return null;
    }

    public JSONObject getTripInfo(Object tripid) {
        JSONObject object = new JSONObject();
        try {

            String SQL = "select * from ctpdb.trip_info where trip_id = ?";
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
        	log.severe(ex.toString());
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
            object.put("telephone", tel);

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
            log.info(object.toString());
            insertObject(object, tel);
        } catch (JSONException ex) {
        	log.severe(ex.toString());
        }
    }

    public void getTripExpensesForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from ctpdb.expenses where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "expenses");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", tel);
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
                log.info(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
    }

    public void getTripChecklistForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from ctpdb.checklist where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "checklist");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", tel);
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
                log.info(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
    }

    public void getTripBestPlacesForNewMember(JSONObject trip, Object tel) {
        try {
            String SQL = "select * from ctpdb.best_places where trip_id = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setObject(1, trip.get("trip_id"), columnDBType.get("trip_id"));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "best_places");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", tel);
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", rs.getObject("trip_id")));
                array.put(1, new JSONObject().put("column", "item_id")
                        .put("value", rs.getObject("item_id")));
                array.put(2, new JSONObject().put("column", "location")
                        .put("value", rs.getObject("location")));
                array.put(3, new JSONObject().put("column", "wiki_key")
                        .put("value", rs.getObject("wiki_key")));
                object.put("data", array);
                log.info(object.toString());
                insertObject(object, tel);
            }
        } catch (SQLException | JSONException ex) {
        	log.severe(ex.toString());
        }
    }

    public void getTripMembersForNewMember(JSONObject trip, Object tel, Object added_by) {
        try {
        	ArrayList<String> members = getTripMembersID(trip.get("trip_id"), tel);
            for  (String member : members) {
            	JSONObject user = getUsersInfo(member);
                JSONObject object = new JSONObject();
                object.put("transaction_type", "MODIFICATION_PUSH");
                object.put("action", "INSERT");
                object.put("tablename", "trip_members");
                object.put("trip_id", trip.get("trip_id"));
                object.put("telephone", tel);
                JSONArray array = new JSONArray();
                array.put(0, new JSONObject().put("column", "trip_id")
                        .put("value", trip.get("trip_id")));                
                array.put(1, new JSONObject().put("column", "telephone")
                        .put("value", user.get("telephone")));
                array.put(2, new JSONObject().put("column", "name")
                        .put("value", user.get("name")));
                array.put(3, new JSONObject().put("column", "email_id")
                        .put("value", user.get("email_id")));
                object.put("data", array);
                log.info(object.toString());
                insertObject(object, tel);
                
				if (!member.equals(added_by)) {
					JSONObject newMember = getUsersInfo(tel);
					object = new JSONObject();
					object.put("transaction_type", "MODIFICATION_PUSH");
					object.put("action", "INSERT");
					object.put("tablename", "trip_members");
					object.put("trip_id", trip.get("trip_id"));
					object.put("telephone", user.getString("telephone"));
					array = new JSONArray();
					array.put(0, new JSONObject().put("column", "trip_id").put(
									"value", trip.get("trip_id")));
					array.put(1, new JSONObject().put("column", "telephone")
							.put("value", newMember.get("telephone")));
					array.put(2, new JSONObject().put("column", "name").put("value",
									newMember.get("name")));
					array.put(3, new JSONObject().put("column", "email_id")
							.put("value", newMember.get("email_id")));
					object.put("data", array);
					log.info(object.toString());
					insertObject(object, user.getString("telephone"));
				}
            }
        } catch (JSONException ex) {
        	log.severe(ex.toString());
        }
    }

    private void insertObject(JSONObject object, Object tel) {
        try {
            int mod_id = getNextModifationId();
            String SQL = "insert into ctpdb.updates_tracker "
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
        	log.severe(ex.toString());
        }
    }
}