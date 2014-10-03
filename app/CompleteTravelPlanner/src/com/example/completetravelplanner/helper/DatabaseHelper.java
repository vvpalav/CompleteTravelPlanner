package com.example.completetravelplanner.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.completetravelplanner.models.*;

public class DatabaseHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "TripPlanner";

	// Table Names
	public static final String TABLE_TRIP_INFO = "trip_info";
	public static final String TABLE_TRIP_MEMBERS = "trip_members";
	public static final String TABLE_BEST_PLACES = "best_places";
	public static final String TABLE_CHECKLIST = "checklist";
	public static final String TABLE_EXPENSES = "expenses";

	// Trip Table - column names
	public static final String KEY_TRIP_ID = "trip_id";
	public static final String KEY_TRIP_NAME = "trip_name";
	public static final String KEY_START_TRIP_DATE = "start_date";
	public static final String KEY_END_TRIP_DATE = "end_date";
	public static final String KEY_CREATED_BY = "created_by";

	// user_info - columns
	public static final String KEY_NAME = "name";
	public static final String KEY_EMAIL = "email_id";
	public static final String KEY_GCM = "gcm_id";

	// trip_member table column names
	public static final String KEY_TELEPHONE = "telephone";

	// best_places columns
	public static final String KEY_TOUR_DEST = "tour_destination";
	public static final String KEY_lOCATION = "location";
	public static final String KEY_WIKI = "wiki_key";

	// check list columns

	public static final String KEY_ITEM_ID = "item_id";
	public static final String KEY_ITEM_NAME = "item_name";
	public static final String KEY_ADDED_BY = "added_by";
	public static final String KEY_ASSIGNED_TO = "assigned_to";
	public static final String KEY_TYPE = "type";
	public static final String KEY_QUANTITY = "quantity";
	public static final String KEY_STATUS = "status";
	public static final String KEY_REMINDER = "reminder";

	// expenses columns
	public static final String KEY_AMOUNT = "amount";
	public static final String KEY_ADDED_ON = "added_on";
	//
	// Trip table create statement
	private static final String CREATE_TABLE_TRIP_INFO = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRIP_INFO + "("
			+ KEY_TRIP_ID + " INTEGER PRIMARY KEY,"
			+ KEY_TRIP_NAME + " TEXT,"
			+ KEY_START_TRIP_DATE + " DATETIME,"
			+ KEY_END_TRIP_DATE + " DATETIME," 
			+ KEY_CREATED_BY + " TEXT" + ")";

	// trip_member table create statement
	private static final String CREATE_TABLE_MEMBER = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_TRIP_MEMBERS + "("
			+ KEY_TRIP_ID + " INTEGER REFERENCES " + TABLE_TRIP_INFO + "(" + KEY_TRIP_ID + "),"
			+ KEY_TELEPHONE + " TEXT, "
			+ KEY_NAME + " TEXT,"
			+ KEY_EMAIL + " TEXT" + ")";

	// best places table create statement
	private static final String CREATE_TABLE_BEST_PLACES = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_BEST_PLACES + "("
			+ KEY_TRIP_ID + " INTEGER REFERENCES " + TABLE_TRIP_INFO + "(" + KEY_TRIP_ID + "),"
			+ KEY_ITEM_ID 	+ " TEXT PRIMARY KEY,"
			+ KEY_lOCATION + " TEXT," 
			+ KEY_WIKI + "TEXT" + ")";

	// check list columns
	private static final String CREATE_TABLE_CHECKLIST = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_CHECKLIST + "("
			+ KEY_TRIP_ID	+ " INTEGER REFERENCES " + TABLE_TRIP_INFO	+ "(" + KEY_TRIP_ID + "),"
			+ KEY_ITEM_ID 	+ " TEXT PRIMARY KEY,"
			+ KEY_ITEM_NAME + " TEXT,"
			+ KEY_QUANTITY	+ " TEXT,"
			+ KEY_TYPE		+ " TEXT,"
			+ KEY_ASSIGNED_TO + " TEXT,"
			+ KEY_ADDED_BY	+ " TEXT,"
			+ KEY_STATUS 	+ " TEXT" + ")";

	// expenses
	private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_EXPENSES + "(" 
			+ KEY_TRIP_ID	+ " INTEGER REFERENCES " + TABLE_TRIP_INFO + "("	+ KEY_TRIP_ID	+ "),"
			+ KEY_ITEM_ID + " TEXT PRIMARY KEY,"
			+ KEY_ITEM_NAME  + " TEXT,"
			+ KEY_ADDED_BY	+ " TEXT," 
			+ KEY_AMOUNT + " REAL," 
			+ KEY_ADDED_ON + " DATETIME" + ")";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		// creating required tables
		db.execSQL(CREATE_TABLE_TRIP_INFO);
		db.execSQL(CREATE_TABLE_MEMBER);
		db.execSQL(CREATE_TABLE_BEST_PLACES);
		db.execSQL(CREATE_TABLE_CHECKLIST);
		db.execSQL(CREATE_TABLE_EXPENSES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_INFO);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_MEMBERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEST_PLACES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
		onCreate(db);
	}

	/*
	 * Creating a trip
	 */
	public long createTrip(JSONObject object) {
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_TRIP_ID, object.getLong("trip_id"));
			values.put(KEY_TRIP_NAME, object.getString("trip_name"));
			values.put(KEY_START_TRIP_DATE, object.getString("start_date"));
			values.put(KEY_END_TRIP_DATE, object.getString("end_date"));
			values.put(KEY_CREATED_BY, object.getString("created_by"));
			return this.getWritableDatabase().insert(TABLE_TRIP_INFO, null, values);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void deleteTrip(long tripID) {
		this.getWritableDatabase().delete(TABLE_TRIP_MEMBERS, KEY_TRIP_ID + "=" + tripID, null);
		this.getWritableDatabase().delete(TABLE_BEST_PLACES, KEY_TRIP_ID + "=" + tripID, null);
		this.getWritableDatabase().delete(TABLE_EXPENSES, KEY_TRIP_ID + "=" + tripID, null);
		this.getWritableDatabase().delete(TABLE_CHECKLIST, KEY_TRIP_ID + "=" + tripID, null);
		this.getWritableDatabase().delete(TABLE_TRIP_INFO, KEY_TRIP_ID + "=" + tripID, null);
	}

	public ArrayList<Trip> getTripList() {

		String selectQuery = "SELECT  * FROM " + TABLE_TRIP_INFO;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		ArrayList<Trip> tripList = new ArrayList<Trip>();
		try {
			if (cursor.moveToFirst()) {
				do {
					Trip trip = new Trip();
					trip.setTripId(cursor.getLong(0));
					trip.setTripName(cursor.getString(1));
					trip.setStartDate(cursor.getString(2));
					trip.setEndDate(cursor.getString(3));

					tripList.add(trip);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return tripList;
	}
	
	public long createCheckList(CheckList checkList) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TRIP_ID, checkList.getTripId());
		values.put(KEY_ITEM_ID, checkList.getItemId());
		values.put(KEY_ITEM_NAME, checkList.getItemName());
		values.put(KEY_QUANTITY, checkList.getQuantity());
		values.put(KEY_TYPE, checkList.getCheckListType());
		values.put(KEY_ASSIGNED_TO, checkList.getAssignedTo());
		values.put(KEY_ADDED_BY, checkList.getAddedBy());
		values.put(KEY_STATUS, checkList.getStatus());
	
		// insert row
		long checkListId = db.insert(TABLE_CHECKLIST, null, values);
		return checkListId;
	}

	
	public String getUserName(String phoneNumber) {
		Log.i("dbhelper_tel", phoneNumber);
		String selectQuery = "SELECT  distinct("+KEY_NAME+") FROM "
				+ TABLE_TRIP_MEMBERS + " where " + KEY_TELEPHONE + " = " + phoneNumber;
		Log.i("executing", selectQuery);
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		String userName = "";
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				userName = cursor.getString(0);
			}
			Log.i("dbhelper_name", userName);
			return userName;
		} finally {
			cursor.close();
		}
	}
	
	public String checkIfUserExists(String phoneNumber) {
		long tripId = SharedUtil.getActiveTripId();
		String selectQuery = "SELECT  " + KEY_TELEPHONE + " FROM "
				+ TABLE_TRIP_MEMBERS + " where " + KEY_TELEPHONE + " = '"
				+ phoneNumber + "' and " + KEY_TRIP_ID + " = '" + tripId + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		String userName = "";
		try {
			if (cursor.moveToFirst()) {
				do {
					userName = cursor.getString(0);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return userName;
	}
	
	public Person getPerson(String phoneNumber) {
		String selectQuery = "SELECT name, email_id FROM " + TABLE_TRIP_MEMBERS
				+ " where " + KEY_TELEPHONE + " = '" + phoneNumber + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		long numb = SharedUtil.getPhoneNumber(phoneNumber);
		String userName = null;
		String email = null;
		try {
			if (cursor.moveToFirst()) {
				do {
					userName = cursor.getString(0);
					email = cursor.getString(1);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return new Person(userName, numb, email);
	}

	
	public JSONArray getTripIds() {
		String selectQuery = "SELECT  " + KEY_TRIP_ID + " FROM "
				+ TABLE_TRIP_INFO;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		JSONArray array = new JSONArray();
		try {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				array.put(cursor.getLong(0));
			}
		} finally {
			cursor.close();
		}
		return array;
	}
	
	public ArrayList<Expenses> getExpensesList(long activeTripId) {

		String selectQuery = "SELECT  * FROM " + TABLE_EXPENSES + " WHERE "
				+ KEY_TRIP_ID + " = '" + activeTripId + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		ArrayList<Expenses> expensesList = new ArrayList<Expenses>();
		try {
			if (cursor.moveToFirst()) {
				do {
					long tripId = cursor.getLong(0);
					String itemId = cursor.getString(1);
					String itemName = cursor.getString(2);
					String addedBy = cursor.getString(3);
					float amount = cursor.getFloat(4);
					String strAddedOn = cursor.getString(5);
					try {
						Date addedOn = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss").parse(strAddedOn);
						Expenses expenseItem = new Expenses(itemId, tripId,
								itemName, amount, addedBy, addedOn);
						expensesList.add(expenseItem);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return expensesList;
	}

	public ArrayList<Person> getMembersList(long tripId) {

		String selectQuery = "SELECT  * FROM " + TABLE_TRIP_MEMBERS + " WHERE "
				+ KEY_TRIP_ID + " = '" + tripId + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		ArrayList<Person> membersList = new ArrayList<Person>();
		try {
			if (cursor.moveToFirst()) {
				do {
					String phoneNumber = cursor.getString(1);
					long ph = SharedUtil.getPhoneNumber(phoneNumber);
					String name = cursor.getString(2);
					String emailId = cursor.getString(3);
					Person memberItem = new Person(name, ph, emailId);
					membersList.add(memberItem);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		
		return membersList;
	}
	
	
	public ArrayList<CheckList> getCheckList(String type, long tripId) {
		ArrayList<CheckList> allCheckList = new ArrayList<CheckList>();
		String selectQuery = "SELECT  * FROM " + TABLE_CHECKLIST + " where "
				+ KEY_TYPE + " = '" + type + "' and " + KEY_TRIP_ID + " = "
				+ tripId;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					CheckList checkList = new CheckList();
					checkList.setItemId(cursor.getString(1));
					checkList.setItemName(cursor.getString(2));
					checkList.setQuantity(cursor.getString(3));
					checkList.setCheckListType(cursor.getString(4));
					checkList.setAssignedTo(cursor.getString(5));
					checkList.setAddedBy(cursor.getString(6));
					checkList.setStatus(cursor.getString(7));
					allCheckList.add(checkList);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return allCheckList;
	}

	public Boolean deleteCheckList(long tripId, String itemId) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_CHECKLIST, KEY_TRIP_ID + "='" + tripId
				+ "' and " + KEY_ITEM_ID + "='" + itemId + "'", null) > 0;
	}
	
	public Boolean deleteMemberItem(long itemName) {
		SQLiteDatabase db = this.getWritableDatabase();
		long tripId = SharedUtil.getActiveTripId();
		return db.delete(TABLE_TRIP_MEMBERS, KEY_TRIP_ID + "='" + tripId
				+ "' and " + KEY_TELEPHONE + "='" + itemName + "'", null) > 0;
	}

	public void insertDataIntoDB(JSONObject object) {
		try {
			JSONArray data = object.getJSONArray("data");
			String[] args = new String[data.length()];

			// prepare SQL insert
			StringBuilder SQL = new StringBuilder("insert into ");
			SQL.append(object.getString("tablename")).append(" (");
			SQL.append(data.getJSONObject(0).getString("column"));
			args[0] = data.getJSONObject(0).getString("value");
			for (int i = 1; i < data.length(); i++) {
				SQL.append(", ").append(
						data.getJSONObject(i).getString("column"));
				args[i] = data.getJSONObject(i).getString("value");
			}
			SQL.append(") values (?");
			for (int i = 0; i < data.length() - 1; i++) {
				SQL.append(", ?");
			}
			SQL.append(")");
			Log.i("inserting_stmt", SQL.toString());
			String line = "";
			for(String str : args){
				 line +=  " " + str;
			}
			Log.i("inserting_value", line);
			this.getWritableDatabase().execSQL(SQL.toString(), args);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void updateDataIntoDB(JSONObject object) {
		try {
			JSONArray data = object.getJSONArray("data");
			JSONArray where = object.getJSONArray("where");
			String[] args = new String[data.length() + where.length()];
			int count = 0;

			// prepare SQL update statement
			StringBuilder SQL = new StringBuilder("update ");
			SQL.append(object.getString("tablename")).append(" set ");
			SQL.append(data.getJSONObject(0).getString("column")).append(
					" = ? ");
			args[count++] = data.getJSONObject(0).getString("value");
			for (int i = 1; i < data.length(); i++) {
				SQL.append(", ").append(
						data.getJSONObject(i).getString("column") + " = ? ");
				args[count++] = data.getJSONObject(i).getString("value");
			}
			SQL.append(" where ")
					.append(where.getJSONObject(0).getString("column"))
					.append(" = ? ");
			args[count++] = where.getJSONObject(0).getString("value");
			for (int i = 1; i < where.length(); i++) {
				SQL.append(" and ")
						.append(where.getJSONObject(i).getString("column"))
						.append(" = ? ");
				args[count++] = where.getJSONObject(i).getString("value");
			}
			//SQL.append(")");
			Log.i("update_stmt", SQL.toString());
			String line = "";
			for(String str : args){
				 line +=  " " + str;
			}
			Log.i("update_value", line);
			this.getWritableDatabase().execSQL(SQL.toString(), args);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void deleteDataFromDB(JSONObject object) {
		try {
			JSONArray data = object.getJSONArray("data");
			String[] args = new String[data.length()];

			// prepare SQL delete statement
			StringBuilder SQL = new StringBuilder("delete from ");
			SQL.append(object.getString("tablename")).append(" where ");
			args[0] = data.getJSONObject(0).getString("value");
			SQL.append(data.getJSONObject(0).getString("column")).append(
					" = ? ");
			for (int i = 1; i < data.length(); i++) {
				SQL.append(" and ")
						.append(data.getJSONObject(i).getString("column"))
						.append(" = ? ");
				args[i] = data.getJSONObject(i).getString("value");
			}
			Log.i("delete_stmt", SQL.toString());
			String line = "";
			for(String str : args){
				 line +=  " " + str;
			}
			Log.i("delete_value", line);
			this.getWritableDatabase().execSQL(SQL.toString(), args);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Boolean deleteTourPlaces(long tripId,String itemId) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_BEST_PLACES, KEY_TRIP_ID + "='" + tripId
				+ "' and " + KEY_ITEM_ID + "='" + itemId + "'", null) > 0;
	}
	
	public void deleteAll() {
		Log.i("deteleAll", "Deleting data");
		this.getWritableDatabase().execSQL("delete from trip_members");
		this.getWritableDatabase().execSQL("delete from trip_info");
		this.getWritableDatabase().execSQL("delete from expenses");
		this.getWritableDatabase().execSQL("delete from checklist");
		this.getWritableDatabase().execSQL("delete from best_places");
	}
	
	public void insertTripMembers(JSONObject object){
		try{
			JSONObject data = object.getJSONObject("data");
			ContentValues cv = new ContentValues();
			cv.put("name", data.getString("name"));
			cv.put("trip_id", object.getLong("trip_id"));
			cv.put("telephone", data.getLong("telephone"));
			cv.put("email_id", data.getString("email_id"));
			this.getWritableDatabase().insert(TABLE_TRIP_MEMBERS, null, cv);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public long createTourPlaces(TourPlaces tourPlaces) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TRIP_ID, tourPlaces.getTripId());
		values.put(KEY_ITEM_ID, tourPlaces.getItemID());
		values.put(KEY_lOCATION, tourPlaces.getLocation());
		// insert row
		long tourPlacesId = db.insert(TABLE_BEST_PLACES, null, values);
		return tourPlacesId;
	}
	public ArrayList<TourPlaces> getTourPlaces(long tripId) {
		ArrayList<TourPlaces> allTourPlaces = new ArrayList<TourPlaces>();
		String selectQuery = "SELECT  * FROM " + TABLE_BEST_PLACES + " where "+ KEY_TRIP_ID + " = "
				+ tripId;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					TourPlaces tourPlaces = new TourPlaces();
					tourPlaces.setItemID(cursor.getString(1));
					tourPlaces.setLocation(cursor.getString(2));
					allTourPlaces.add(tourPlaces);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return allTourPlaces;
	}

	public int getNumberOfMembers(long tripId) {
		int numberOfMembers = 0;
		String selectQuery = "SELECT  count(*) FROM " + TABLE_TRIP_MEMBERS + " where "+ KEY_TRIP_ID + " = "
				+ tripId;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		try {
			if (cursor.moveToFirst()) {
				numberOfMembers = cursor.getInt(0) + 1;
			}
		} finally {
			cursor.close();
		}
		return numberOfMembers;
	}

	
}
