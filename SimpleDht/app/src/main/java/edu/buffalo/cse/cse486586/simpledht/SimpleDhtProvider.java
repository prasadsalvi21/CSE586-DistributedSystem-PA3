package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SimpleDhtProvider extends ContentProvider {
    static final String TAG = SimpleDhtProvider.class.getSimpleName();

    private int avd0 = 11108;
    private String dName = null;
    private String myPredId = "";
    private String mySuccId = "";
    private String myPortId;
    private int checkHashKey = -1;
    private int checkHashSucc = -1;
    private int checkHashPred = -1;
    private int hashKeyPred = -1;
    private int hashKeySucc = -1;
    private boolean flag = false;
    private boolean flag2 = false;
    private boolean flag3 = false;
    static final int SERVER_PORT = 10000;
    private Map<String, String> deviceName = new HashMap<String, String>();
    private List checkedKeys = new ArrayList();
    private List q_checkedKeys = new ArrayList();
    private List q1_checkedKeys = new ArrayList();
    private String result = "";
    private TreeMap<String, Node> treeMap = new TreeMap<String, Node>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }

    });

    private SimpleDhtHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SimpleDhtDB.SQLITE_TABLE);
        if (mySuccId.isEmpty() || mySuccId.equals(myPortId)) {

            String selection1 = null;
            selection1 = selection;
            String selection2 = SimpleDhtDB.key + " like '" + selection1 + "'";

            db.execSQL("DELETE FROM " + SimpleDhtDB.SQLITE_TABLE + " WHERE " + SimpleDhtDB.key + " like '" + selection1 + "'");
            Log.v("Delete Where clause", selection2);
        } else if(selection.equals("@"))
        {
            db.execSQL("DELETE FROM " + SimpleDhtDB.SQLITE_TABLE );
            return 0;
        }
        else if(selection.equals("*"))
        {
            db.execSQL("DELETE FROM " + SimpleDhtDB.SQLITE_TABLE );
            try {
                Socket socket5 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(mySuccId));

                String msgToSend = "DELETE*," + myPortId + "," + mySuccId;


                DataOutputStream os5 = new DataOutputStream(socket5.getOutputStream());
                os5.writeUTF(msgToSend);
                Log.d(TAG, "Sending Message to Server * : " + msgToSend);
                os5.flush();

                Thread.sleep(2000);

                DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket5.getInputStream()));
                String receivedMsg = is4.readUTF();
                Log.i("Got query result", receivedMsg);
            }
           catch (IOException e) {
                Log.e("IOExcepion query", "" + e);
            } catch (InterruptedException e) {
                Log.e("Timeout q", "" + e);
            }
        }
            else{
            try {
                Log.w(TAG, "Inside Delete");
                String q_hashedKey = genHash(selection);
                String q_hasheddName = genHash(dName);
                String q_hashedSucc = genHash(deviceName.get(mySuccId));
                String q_hashedPred = genHash(deviceName.get(myPredId));
                Log.i("Non hashed values", selection + ":" + dName);

                Log.i(TAG, myPortId + ":" + dName + ":" + myPredId + ":" + mySuccId);
                int q_checkHashKey = q_hashedKey.compareTo(q_hasheddName);
                Log.i("hashed values key", q_hashedKey + ":" + q_hasheddName + ":" + q_checkHashKey);
                int q_checkHashSucc = q_hasheddName.compareTo(q_hashedSucc);
                Log.i("hashed values Succ", q_hasheddName + ":" + q_hashedSucc + ":" + q_checkHashSucc);
                int q_checkHashPred = q_hasheddName.compareTo(q_hashedPred);
                Log.i("hashed values Pred", q_hasheddName + ":" + q_hashedPred + ":" + q_checkHashPred);
                int q_hashKeyPred = q_hashedKey.compareTo(q_hashedPred);
                int q_hashKeySucc = q_hashedKey.compareTo(q_hashedSucc);
                Log.i("hashed key Pred", q_hashedKey + ":" + q_hashedPred + ":" + q_hashKeyPred);
                Log.i("hashed key Succ", q_hashedKey + ":" + q_hashedSucc + ":" + q_hashKeySucc);

                if ((q_checkHashPred < 0) && (q_checkHashKey < 0 || q_hashKeyPred > 0)) {
                    flag2 = true;
                    Log.i("Flag set True for ", selection);
                }


                if (!flag2)
                    q_checkedKeys.add(selection);

                Log.i("FLAG 2", "" + flag2);
                if ((q_checkHashKey < 0 && q_hashKeyPred > 0) || flag2) {
                    String selection1 = null;

                    selection1 = selection;
                    String selection2 = SimpleDhtDB.key + " like '" + selection1 + "'";

                    db.execSQL("DELETE FROM " + SimpleDhtDB.SQLITE_TABLE + " WHERE " + SimpleDhtDB.key + " like '" + selection1 + "'");
                    Log.v("Delete Where clause", selection2);

                } else {
                    Log.i(TAG, "Forward Delete request to" + mySuccId);
                    Socket socket6 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(mySuccId));
                    String msgToSend = "DELETE," + myPortId + "," + mySuccId + "," + selection;


                    DataOutputStream os5 = new DataOutputStream(socket6.getOutputStream());
                    os5.writeUTF(msgToSend);
                    Log.d(TAG, "Sending Message to Server * : " + msgToSend);
                    os5.flush();

                    Thread.sleep(2000);

                    DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket6.getInputStream()));
                    String receivedMsg = is4.readUTF();
                    Log.i("Got query result", receivedMsg);


                    return 0;
                }
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "No such algo ins: " + e);
            } catch (IOException e) {
                Log.e("IOExcepion query", "" + e);
            } catch (InterruptedException e) {
                Log.e("Timeout q", "" + e);
            }
        }


        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        try {


            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("CREATE TABLE if not exists TEMP_TABLE2 (key,value);");
            String ins = values.toString();
            int index_key = ins.lastIndexOf("key=");
            int index_val = ins.lastIndexOf("value=");
            String key_value1 = ins.substring(index_key + 4, ins.length());
            String value = ins.substring(index_val + 6, index_key);
            Log.d("Key to be inserted", key_value1 + ":" + value);
            String key_value = null;
            // key_value=genHash(key_value1);
            key_value = key_value1;
            Log.d("Hash Key to be inserted", key_value);


            if (mySuccId.isEmpty() || mySuccId.equals(myPortId)) {
                Cursor c = query(uri, null, key_value, null, null);
                if (c.moveToFirst()) {
                    Log.d(TAG, "Key " + key_value + " already present in DB, so update Value corresponding to key");
                    update(uri, values, key_value, null);
                } else {
                    //long id = db.insert(SimpleDhtDB.SQLITE_TABLE, null, values);
                    final String Insert_Data = "INSERT INTO " + SimpleDhtDB.SQLITE_TABLE + " VALUES('" + key_value + "','" + value + "')";
                    db.execSQL(Insert_Data);
                    getContext().getContentResolver().notifyChange(uri, null);
                    Log.v("Inserted values in DB", value + ":" + key_value);
                }
            } else {

                try {
                    String hashedKey = genHash(key_value);
                    String hasheddName = genHash(dName);
                    String hashedSucc = genHash(deviceName.get(mySuccId));
                    String hashedPred = genHash(deviceName.get(myPredId));
                    Log.i("Non hashed values", key_value + ":" + dName);

                    Log.i(TAG, myPortId + ":" + dName + ":" + myPredId + ":" + mySuccId);
                    checkHashKey = hashedKey.compareTo(hasheddName);
                    Log.i("hashed values key", hashedKey + ":" + hasheddName + ":" + checkHashKey);
                    checkHashSucc = hasheddName.compareTo(hashedSucc);
                    Log.i("hashed values Succ", hasheddName + ":" + hashedSucc + ":" + checkHashSucc);
                    checkHashPred = hasheddName.compareTo(hashedPred);
                    Log.i("hashed values Pred", hasheddName + ":" + hashedPred + ":" + checkHashPred);
                    hashKeyPred = hashedKey.compareTo(hashedPred);
                    hashKeySucc = hashedKey.compareTo(hashedSucc);
                    Log.i("hashed key Pred", hashedKey + ":" + hashedPred + ":" + hashKeyPred);
                    Log.i("hashed key Succ", hashedKey + ":" + hashedSucc + ":" + hashKeySucc);

                    for (int i = 0; i < checkedKeys.size(); i++) {
                        if (checkedKeys.get(i).equals(key_value)) {
                            if ((checkHashKey > 0 && hashKeySucc > 0 && hashKeyPred > 0 && checkHashPred < 0) ||
                                    (checkHashKey < 0 && hashKeySucc < 0 && hashKeyPred < 0 && checkHashPred < 0)) {
                                flag = true;
                                Log.i("Flag set True for ", key_value);
                            }

                        }

                    }
                    if (!flag)
                        checkedKeys.add(key_value);


                    if ((checkHashKey < 0 && hashKeyPred > 0) || flag) {

                        //long id = db.insert(SimpleDhtDB.SQLITE_TABLE, null, values);
                        final String Insert_Data = "INSERT INTO " + SimpleDhtDB.SQLITE_TABLE + " VALUES('" + key_value + "','" + value + "')";
                        db.execSQL(Insert_Data);
                        getContext().getContentResolver().notifyChange(uri, null);
                        Log.v("Inserted values in DB", value + ":" + key_value);
                        //Log.i("Tree Map",treeMap.toString());

                        flag = false;
                    } else {
                        Log.i(TAG, "Forward request to" + mySuccId);
                        Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(mySuccId));

                        String msgToSend = "FORWARD," + myPortId + "," + mySuccId + "," + key_value + "," + value;

                        DataOutputStream os3 = new DataOutputStream(socket3.getOutputStream());
                        os3.writeUTF(msgToSend);
                        Log.d(TAG, "Sending Message to Server : " + msgToSend);
                        os3.flush();


                    }
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "No such algo ins: " + e);
                }
            }


        } catch (Exception e) {
            Log.e(TAG, "No such algo" + e);
        }

        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SimpleDhtDB.SQLITE_TABLE);
        Cursor cursor = null;
        if (selection.equals("*")) {
            Log.i("Quering *", selection);
            if (mySuccId.isEmpty() || myPortId.equals(mySuccId)) {
                cursor = queryBuilder.query(db, projection, null,
                        selectionArgs, null, null, sortOrder);
            } else {
                try {

                    Socket socket5 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(mySuccId));

                    String msgToSend = "QUERY*," + myPortId + "," + mySuccId + "," + result;


                    DataOutputStream os5 = new DataOutputStream(socket5.getOutputStream());
                    os5.writeUTF(msgToSend);
                    Log.d(TAG, "Sending Message to Server * : " + msgToSend);
                    os5.flush();

                    Thread.sleep(2000);

                    DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket5.getInputStream()));
                    String receivedMsg = is4.readUTF();
                    Log.i("Got query result", receivedMsg);
                    String result1 = "";
                    Cursor cursor2 = queryBuilder.query(db, projection, null,
                            selectionArgs, null, null, sortOrder);
                    if (cursor2.moveToFirst()) {
                        do {

                            String column1 = cursor2.getString(0);
                            String column2 = cursor2.getString(1);

                            result1 = result1 + column1 + ":" + column2 + ";";
                            Log.i("Appended", column1 + ":" + column2);
                            ;


                        } while (cursor2.moveToNext());
                    }
                    Log.i("AVD res", result1);
                    String a[] = receivedMsg.split(",");
                    String r_ack = a[0];
                    String r_res = a[1];
                    r_res = r_res + result1;
                    String r_fin[] = r_res.split(";");
                    Log.i("Q* res", r_ack + ":" + r_res + ":" + r_fin);
                    db.execSQL("CREATE TABLE if not exists TEMP_TABLE1 (key,value);");

                    for (int l = 0; l < r_fin.length; l++) {
                        String r_temp[] = r_fin[l].split(":");
                        String r_key = r_temp[0];
                        String r_val = r_temp[1];
                        Log.i("Q* res 1", r_key + ":" + r_val + ":" + r_temp);
                        String Insert_Data = "INSERT INTO TEMP_TABLE1 VALUES('" + r_key + "','" + r_val + "')";
                        db.execSQL(Insert_Data);
                        getContext().getContentResolver().notifyChange(uri, null);
                    }

                    Cursor cursor1 = db.rawQuery("SELECT * FROM TEMP_TABLE1", null);
                    Log.v("Query Where clause1", "*");

                    os5.close();

                    is4.close();
                    socket5.close();

                    return cursor1;

                } catch (InterruptedException e) {
                    Log.e("Intrupt ", "" + e);
                } catch (IOException e)

                {
                    Log.e("IO in *", "" + e);
                }


            }


        } else if (selection.equals("@")) {
            Log.i("Quering @", selection);


            cursor = queryBuilder.query(db, projection, null,
                    selectionArgs, null, null, sortOrder);
            Log.v("Query Where clause", "@");
            return cursor;
        } else if (mySuccId.isEmpty() || mySuccId.equals(myPortId)) {

            String selection1 = null;

            selection1 = selection;
            String selection2 = SimpleDhtDB.key + " like '" + selection1 + "'";

            cursor = queryBuilder.query(db, projection, selection2,
                    selectionArgs, null, null, sortOrder);
            Log.v("Query Where clause", selection2);
        } else {

            try {
                Log.w(TAG, "Inside Query");
                String q_hashedKey = genHash(selection);
                String q_hasheddName = genHash(dName);
                String q_hashedSucc = genHash(deviceName.get(mySuccId));
                String q_hashedPred = genHash(deviceName.get(myPredId));
                Log.i("Non hashed values", selection + ":" + dName);

                Log.i(TAG, myPortId + ":" + dName + ":" + myPredId + ":" + mySuccId);
                int q_checkHashKey = q_hashedKey.compareTo(q_hasheddName);
                Log.i("hashed values key", q_hashedKey + ":" + q_hasheddName + ":" + q_checkHashKey);
                int q_checkHashSucc = q_hasheddName.compareTo(q_hashedSucc);
                Log.i("hashed values Succ", q_hasheddName + ":" + q_hashedSucc + ":" + q_checkHashSucc);
                int q_checkHashPred = q_hasheddName.compareTo(q_hashedPred);
                Log.i("hashed values Pred", q_hasheddName + ":" + q_hashedPred + ":" + q_checkHashPred);
                int q_hashKeyPred = q_hashedKey.compareTo(q_hashedPred);
                int q_hashKeySucc = q_hashedKey.compareTo(q_hashedSucc);
                Log.i("hashed key Pred", q_hashedKey + ":" + q_hashedPred + ":" + q_hashKeyPred);
                Log.i("hashed key Succ", q_hashedKey + ":" + q_hashedSucc + ":" + q_hashKeySucc);

                if ((q_checkHashPred < 0) && (q_checkHashKey < 0 || q_hashKeyPred > 0)) {
                    flag2 = true;
                    Log.i("Flag set True for ", selection);
                }


                if (!flag2)
                    q_checkedKeys.add(selection);

                Log.i("FLAG 2", "" + flag2);
                if ((q_checkHashKey < 0 && q_hashKeyPred > 0) || flag2) {
                    String selection1 = null;

                    selection1 = selection;
                    String selection2 = SimpleDhtDB.key + " like '" + selection1 + "'";
                    Log.i("Selection", selection2);
                    cursor = queryBuilder.query(db, projection, selection2,
                            selectionArgs, null, null, sortOrder);
                    Log.v("Query Where clause", selection2);
                    flag2 = false;
                    return cursor;
                } else {
                    Log.i(TAG, "Forward Query request to" + mySuccId);
                    Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(mySuccId));

                    String msgToSend = "QUERY," + myPortId + "," + mySuccId + "," + selection;


                    DataOutputStream os4 = new DataOutputStream(socket4.getOutputStream());
                    os4.writeUTF(msgToSend);
                    Log.d(TAG, "Sending Message to Server : " + msgToSend);
                    os4.flush();

                    Thread.sleep(500);

                    DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket4.getInputStream()));
                    String receivedMsg = is4.readUTF();
                    Log.i("Got query result", receivedMsg);
                    String a[] = receivedMsg.split(",");
                    String r_key = a[0];
                    String r_val = a[1];
                    db.execSQL("CREATE TABLE if not exists TEMP_TABLE (key,value);");


                    String Insert_Data = "INSERT INTO TEMP_TABLE VALUES('" + r_key + "','" + r_val + "')";
                    db.execSQL(Insert_Data);
                    getContext().getContentResolver().notifyChange(uri, null);

                    Cursor cursor1 = db.rawQuery("SELECT * FROM TEMP_TABLE WHERE key = '" + r_key + "'", null);
                    Log.v("Query Where clause1", r_key);


                    return cursor1;
                }
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "No such algo ins: " + e);
            } catch (IOException e) {
                Log.e("IOExcepion query", "" + e);
            } catch (InterruptedException e) {
                Log.e("Timeout q", "" + e);
            }
        }


        return cursor;

    }

    @Override
    public boolean onCreate() {

        dbHelper = new SimpleDhtHelper(getContext());
        deviceName.put("11108", "5554");
        deviceName.put("11112", "5556");
        deviceName.put("11116", "5558");
        deviceName.put("11120", "5560");
        deviceName.put("11124", "5562");
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        for (Map.Entry<String, String> entry : deviceName.entrySet()) {
            if (entry.getKey().equals(myPort)) {
                dName = entry.getValue();
                Log.i("Device name", dName + ":" + myPort);
            }
        }
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);


            String msg = "JOIN," + dName + "," + myPort;
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);

        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket", e);
            return false;
        }
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SimpleDhtDB.SQLITE_TABLE);
        String selection2 = SimpleDhtDB.key + " like '" + selection + "'";
        //Log.i("Selection",selection2);
        String msg = values.getAsString(SimpleDhtDB.value);
        values.put(SimpleDhtDB.value, msg);
        Log.v("DB updated with value", msg);
        db.update(SimpleDhtDB.SQLITE_TABLE, values, selection2, null);

        return 0;
    }


    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        int seqNumber = 0;

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            try {
                ServerSocket serverSocket = sockets[0];
                Socket socket = null;
                String msg, ack;

                while (true) {
                    ack = null;
                    socket = serverSocket.accept();

                    DataInputStream objectInputStream =
                            new DataInputStream(socket.getInputStream());
                    String message = objectInputStream.readUTF();
                    Log.i("Recieved msg client", message);
                    String[] parts = message.split(",");
                    String action = parts[0];
                    String deviceName2 = parts[1];
                    String portNumber = parts[2];
                    Log.i("Action & port", action + " " + deviceName2);
                    if (action.equals("JOIN")) {
                        String hashNodeId = genHash(String.valueOf(Integer.parseInt(deviceName2)));
                        Node n = new Node();
                        n.setMyPort2(portNumber);
                        n.setdName2(deviceName2);
                        n.setPred(portNumber);
                        n.setSucc(portNumber);
                        n.setH_pred(hashNodeId);
                        n.setH_succ(hashNodeId);
                        Node n1 = new Node();

                        Log.i("Before Tree Map", treeMap.toString());


                        for (int i = 0; i < treeMap.size(); i++) {
                            String a = treeMap.keySet().toArray()[i].toString();
                            int check = hashNodeId.compareTo(a);
                            if (check < 0 && i == 0 && treeMap.size() == 1) {
                                n.setH_succ(a);
                                n.setH_pred(treeMap.lastKey());
                                n.setSucc(treeMap.get(a).succ);
                                Map.Entry<String, Node> lastEntry = treeMap.lastEntry();
                                n.setPred(lastEntry.getValue().getPred());
                                n1.setMyPort2(treeMap.get(a).myPort2);
                                n1.setdName2(treeMap.get(a).dName2);
                                n1.setPred(portNumber);
                                n1.setSucc(portNumber);
                                n1.setH_succ(hashNodeId);
                                n1.setH_pred(hashNodeId);
                                treeMap.put(a, n1);
                                Log.i("tree update size= 1<", n.toString() + ":" + n1.toString());
                                break;

                            } else if (check < 0 && i == 0) {
                                n.setH_pred(treeMap.lastKey());
                                n.setH_succ(a);
                                n.setSucc(treeMap.get(a).myPort2);
                                Map.Entry<String, Node> lastEntry = treeMap.lastEntry();
                                n.setPred(lastEntry.getValue().myPort2);
                                n1.setMyPort2(treeMap.get(a).myPort2);
                                n1.setdName2(treeMap.get(a).dName2);
                                n1.setPred(portNumber);
                                n1.setSucc(treeMap.get(a).succ);
                                n1.setH_pred(hashNodeId);
                                n1.setH_succ(treeMap.get(a).h_succ);
                                Node n2 = new Node();
                                n2.setMyPort2(treeMap.get(treeMap.lastKey()).myPort2);
                                n2.setdName2(treeMap.get(treeMap.lastKey()).dName2);
                                n2.setSucc(portNumber);
                                n2.setH_succ(hashNodeId);
                                n2.setH_pred(treeMap.get(treeMap.lastKey()).h_pred);
                                n2.setPred(treeMap.get(treeMap.lastKey()).pred);

                                treeMap.put(a, n1);
                                treeMap.put(treeMap.lastKey(), n2);
                                Log.i("tree update check<0", n.toString() + ":" + n1.toString() + ":" + n2.toString());
                                break;

                            } else if (check > 0 && i == treeMap.size() - 1 && treeMap.size() == 1) {
                                n.setH_pred(a);
                                n.setH_succ(treeMap.firstKey());
                                n.setPred(treeMap.get(a).pred);
                                Map.Entry<String, Node> firstEntry = treeMap.firstEntry();
                                n.setSucc(firstEntry.getValue().succ);
                                n1.setMyPort2(treeMap.get(a).myPort2.toString());
                                n1.setdName2(treeMap.get(a).dName2);
                                n1.setSucc(portNumber);
                                n1.setPred(portNumber);
                                n1.setH_pred(hashNodeId);
                                n1.setH_succ(hashNodeId);
                                treeMap.put(a, n1);
                                Log.i("tree update size=1>", n.toString() + ":" + n1.toString());
                                break;
                            } else if (check > 0 && i == treeMap.size() - 1) {
                                n.setH_pred(a);
                                n.setH_succ(treeMap.firstKey());
                                n.setPred(treeMap.get(a).myPort2);
                                Map.Entry<String, Node> firstEntry = treeMap.firstEntry();
                                n.setSucc(firstEntry.getValue().myPort2);
                                n1.setMyPort2(treeMap.get(a).myPort2.toString());
                                n1.setdName2(treeMap.get(a).dName2);
                                n1.setSucc(portNumber);
                                n1.setPred(treeMap.get(a).pred);
                                n1.setH_pred(treeMap.get(a).h_pred);
                                n1.setH_succ(hashNodeId);
                                Node n2 = new Node();
                                n2.setMyPort2(treeMap.get(treeMap.firstKey()).myPort2.toString());
                                n2.setdName2(treeMap.get(treeMap.firstKey()).dName2);
                                n2.setSucc(treeMap.get(treeMap.firstKey()).succ);
                                n2.setH_succ(treeMap.get(treeMap.firstKey()).h_succ);
                                n2.setH_pred(hashNodeId);
                                n2.setPred(portNumber);
                                treeMap.put(a, n1);
                                treeMap.put(treeMap.firstKey(), n2);
                                Log.i("tree update check>0", n.toString() + ":" + n1.toString() + ":" + n2.toString());
                                break;
                            } else if (check < 0) {
                                String ik = treeMap.keySet().toArray()[i].toString();
                                n.setSucc(treeMap.get(ik).myPort2);
                                String ij = treeMap.keySet().toArray()[i - 1].toString();
                                n.setPred(treeMap.get(ij).myPort2);
                                n.setH_succ(treeMap.keySet().toArray()[i].toString());
                                n.setH_pred(treeMap.keySet().toArray()[i - 1].toString());
                                Node n2 = new Node();
                                n1.setMyPort2(treeMap.get(a).myPort2.toString());
                                n1.setdName2(treeMap.get(a).dName2);
                                n1.setSucc(treeMap.get(a).succ);
                                n1.setPred(portNumber);
                                n1.setH_pred(hashNodeId);
                                n1.setH_succ(treeMap.get(a).h_succ);
                                String b = treeMap.keySet().toArray()[i - 1].toString();
                                n2.setMyPort2(treeMap.get(b).myPort2.toString());
                                n2.setdName2(treeMap.get(b).dName2);
                                n2.setSucc(portNumber);
                                n2.setPred(treeMap.get(b).pred);
                                n2.setH_pred(treeMap.get(b).h_pred);
                                n2.setH_succ(hashNodeId);
                                treeMap.put(a, n1);
                                treeMap.put(treeMap.keySet().toArray()[i - 1].toString(), n2);
                                Log.i("tree update check<>0", n.toString() + ":" + n1.toString() + ":" + n2.toString());
                                break;
                            }


                        }
                        treeMap.put(hashNodeId, n);
                        Log.w("After Tree Map", treeMap.toString());
                        broadcast(treeMap);
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        ack = "server ack";
                        os.writeUTF(ack);
                        os.flush();
                        Log.d(TAG, "Sending ack to Client : " + ack);
                        objectInputStream.close();
                        os.close();
                        socket.close();
                    } else if (action.equals("BROADCAST")) {
                        myPortId = parts[2];
                        myPredId = parts[3];
                        mySuccId = parts[4];
                        Log.i(TAG, "Server Broadcast" + myPortId + ":" + dName + ":" + myPredId + ":" + mySuccId);
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        ack = "server ack";
                        os.writeUTF(ack);
                        os.flush();
                        Log.d(TAG, "Sending ack to Client : " + ack);
                        objectInputStream.close();
                        os.close();
                        socket.close();


                    } else if (action.equals("FORWARD")) {

                        String f_myPortId = parts[1];
                        String f_mySuccsId = parts[2];
                        String f_key = parts[3];
                        String f_values = parts[4];
                        Log.i("Forwared Request", f_myPortId + ":" + f_mySuccsId + ":" + f_key + ":" + f_values);
                        ContentValues keyValueToInsert = new ContentValues();
                        keyValueToInsert.put("key", f_key);
                        keyValueToInsert.put("value", f_values);
                        Uri uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                        Uri newUri = insert(uriAddress, keyValueToInsert);
                        Log.i(TAG, "Sent fwded request");
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        ack = "server ack";
                        os.writeUTF(ack);
                        os.flush();
                        Log.d(TAG, "Sending ack to Client : " + ack);
                        objectInputStream.close();
                        os.close();
                        socket.close();

                    } else if (action.equals("QUERY")) {
                        String q_myPortId = parts[1];
                        String q_mySuccsId = parts[2];
                        String q_selection = parts[3];
                        Log.i("Query Request", q_myPortId + ":" + q_mySuccsId + ":" + q_selection);
                        Log.i("Current AVD ", dName);
                        int checkhash = genHash(q_selection).compareTo(genHash(dName));
                        String q_hashedKey = genHash(q_selection);
                        String q_hasheddName = genHash(dName);
                        String q_hashedSucc = genHash(deviceName.get(mySuccId));
                        String q_hashedPred = genHash(deviceName.get(myPredId));
                        Log.i("Non hashed values", q_selection + ":" + dName);

                        Log.i(TAG, myPortId + ":" + dName + ":" + myPredId + ":" + mySuccId);
                        int q_checkHashKey = q_hashedKey.compareTo(q_hasheddName);
                        Log.i("hashed values key", q_hashedKey + ":" + q_hasheddName + ":" + q_checkHashKey);
                        int q_checkHashSucc = q_hasheddName.compareTo(q_hashedSucc);
                        Log.i("hashed values Succ", q_hasheddName + ":" + q_hashedSucc + ":" + q_checkHashSucc);
                        int q_checkHashPred = q_hasheddName.compareTo(q_hashedPred);
                        Log.i("hashed values Pred", q_hasheddName + ":" + q_hashedPred + ":" + q_checkHashPred);
                        int q_hashKeyPred = q_hashedKey.compareTo(q_hashedPred);
                        int q_hashKeySucc = q_hashedKey.compareTo(q_hashedSucc);
                        Log.i("hashed key Pred", q_hashedKey + ":" + q_hashedPred + ":" + q_hashKeyPred);
                        Log.i("hashed key Succ", q_hashedKey + ":" + q_hashedSucc + ":" + q_hashKeySucc);


                        if ((q_checkHashPred < 0) && (q_checkHashKey < 0 || q_hashKeyPred > 0)) {
                            flag3 = true;
                            Log.i("Flag set True for ", q_selection);
                        }


                        if (!flag3)
                            q1_checkedKeys.add(q_selection);

                        Log.i("q1", q1_checkedKeys.toString());
                        if ((q_checkHashKey < 0 && q_checkHashPred > 0) || flag3) {
                            Uri uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                            Cursor resultCursor = query(uriAddress, null,
                                    q_selection, null, null);
                            int keyIndex = resultCursor.getColumnIndex("key");
                            int valueIndex = resultCursor.getColumnIndex("value");
                            Log.i("Cursor index", keyIndex + ":" + valueIndex);
                            resultCursor.moveToFirst();
                            String returnKey = resultCursor.getString(keyIndex);
                            String returnValue = resultCursor.getString(valueIndex);
                            Log.i("Result found at ", dName + ":" + returnKey + returnValue);
                            DataOutputStream os4 = new DataOutputStream(socket.getOutputStream());
                            String result = returnKey + "," + returnValue;
                            os4.writeUTF(result);
                            Log.d(TAG, "Sending Query result to Server : " + result);
                            os4.flush();

                        } else {
                            Log.i(TAG, "Forward Query request to" + mySuccId);
                            Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(mySuccId));

                            String msgToSend = "QUERY," + q_myPortId + "," + mySuccId + "," + q_selection;

                            DataOutputStream os4 = new DataOutputStream(socket4.getOutputStream());
                            os4.writeUTF(msgToSend);
                            Log.d(TAG, "Sending Message to Server : " + msgToSend);
                            os4.flush();

                            Thread.sleep(500);
                            DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket4.getInputStream()));
                            String receivedMsg = is4.readUTF();
                            Log.i("received query reply", receivedMsg);


                            DataOutputStream os5 = new DataOutputStream(socket.getOutputStream());

                            os5.writeUTF(receivedMsg);
                            Log.d(TAG, "Sending Query result to Server : " + receivedMsg);
                            os5.flush();
                        }
                        Log.i(TAG, "Sent Query request");
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        ack = "server ack";
                        os.writeUTF(ack);
                        os.flush();
                        Log.d(TAG, "Sending ack to Client : " + ack);
                        objectInputStream.close();
                        os.close();
                        socket.close();

                    } else if (action.equals("QUERY*")) {
                        Log.i(TAG, "In Query *");
                        String q_myPortId = parts[1];
                        String q_mySuccsId = parts[2];


                        if (!myPortId.equals(q_myPortId)) {
                            Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(mySuccId));
                            String msgToSend = "QUERY*," + q_myPortId + "," + mySuccId;

                            DataOutputStream os4 = new DataOutputStream(socket4.getOutputStream());
                            os4.writeUTF(msgToSend);
                            Log.d(TAG, "Sending Message to Server : " + msgToSend);
                            os4.flush();

                            Thread.sleep(500);
                            DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket4.getInputStream()));
                            String receivedMsg = is4.readUTF();
                            Log.i("received query ack", receivedMsg);
                            String t2[] = receivedMsg.split(",");
                            if (receivedMsg.length() == 8)
                                t2[0] = receivedMsg;
                            String result1 = "";

                            if (t2[0].equals("ACK Q OK")) {
                                Uri uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                                Cursor resultCursor = query(uriAddress, null,
                                        "@", null, null);
                                if (receivedMsg.length() > 9)
                                    result1 = t2[1];
                                if (resultCursor.moveToFirst()) {
                                    do {
                                        String column1 = resultCursor.getString(0);
                                        String column2 = resultCursor.getString(1);

                                        result1 = result1 + column1 + ":" + column2 + ";";
                                        Log.i("Appended", column1 + ":" + column2);


                                    } while (resultCursor.moveToNext());
                                }
                                Log.i("Result", result1);

                            }
                            os4 = new DataOutputStream(socket.getOutputStream());
                            os4.writeUTF("ACK Q OK," + result1);
                            Log.d(TAG, "Sending ACK Q to Server : " + "ACK Q OK," + result1);
                            os4.flush();
                            os4.close();
                            if (q_myPortId.equals(myPredId))
                                Thread.sleep(5000);
                            socket4.close();
                            socket.close();


                        } else {
                            DataOutputStream os4 = new DataOutputStream(socket.getOutputStream());
                            os4.writeUTF("ACK Q OK");
                            Log.d(TAG, "Sending ACK Q to Server : " + socket.toString());
                            os4.flush();
                            os4.close();

                            objectInputStream.close();

                            socket.close();


                        }

                    } else if (action.equals("DELETE")) {
                        Log.i(TAG, "Delete request");
                        String f_myPortId = parts[1];
                        String f_mySuccsId = parts[2];
                        String f_key = parts[3];

                        Log.i("Forwared Request", f_myPortId + ":" + f_mySuccsId + ":" + f_key);

                        Uri uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                        int r = delete(uriAddress, f_key, null);
                        Log.i(TAG, "Sent delete request");
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        ack = "server ack";
                        os.writeUTF(ack);
                        os.flush();
                        Log.d(TAG, "Sending ack to Client : " + ack);
                        objectInputStream.close();
                        os.close();
                        socket.close();
                    }
                    else if(action.equals("DELETE*"))
                    {
                        Log.i(TAG, "Delete * request");
                        String q_myPortId = parts[1];
                        String q_mySuccsId = parts[2];
                        if (!myPortId.equals(q_myPortId)) {
                            Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(mySuccId));
                            String msgToSend = "DELETE*," + q_myPortId + "," + mySuccId;

                            DataOutputStream os4 = new DataOutputStream(socket4.getOutputStream());
                            os4.writeUTF(msgToSend);
                            Log.d(TAG, "Sending Message to Server : " + msgToSend);
                            os4.flush();

                            Thread.sleep(500);
                            DataInputStream is4 = new DataInputStream(new BufferedInputStream(socket4.getInputStream()));
                            String receivedMsg = is4.readUTF();
                            Log.i("received query ack", receivedMsg);

                            if (receivedMsg.equals("ACK D OK")) {
                                Uri uriAddress = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                                int r = delete(uriAddress, "@", null);
                                Log.i("Result", ""+r);

                            }
                            os4 = new DataOutputStream(socket.getOutputStream());
                            os4.writeUTF("ACK D OK");
                            Log.d(TAG, "Sending ACK D to Server : " + "ACK D OK");
                            os4.flush();
                            os4.close();
                            if (q_myPortId.equals(myPredId))
                                Thread.sleep(5000);
                            socket4.close();
                            socket.close();


                        } else {
                            DataOutputStream os4 = new DataOutputStream(socket.getOutputStream());
                            os4.writeUTF("ACK D OK");
                            Log.d(TAG, "Sending ACK D to Server : " + "ACK D OK");
                            os4.flush();
                            os4.close();

                            objectInputStream.close();

                            socket.close();


                        }

                    }

                    objectInputStream.close();
                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ServerTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ServerTask socket IOException" + Log.getStackTraceString(e));
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "ServerTask No Such Algo" + e);
            } catch (InterruptedException e) {
                Log.e("Timewe", "" + e);
            }
            return null;
        }

        protected void onProgressUpdate(String... strings) {


            return;
        }
    }

    public void broadcast(TreeMap<String, Node> t) {
        String dName2 = null;
        for (int i = 0; i < t.size(); i++) {


            String key = t.keySet().toArray()[i].toString();
            String avdName = t.get(key).getdName2();
            for (Map.Entry<String, String> entry : deviceName.entrySet()) {
                if (entry.getValue().equals(avdName)) {
                    dName2 = entry.getKey();
                    Log.i("Device name 2", dName2 + ":" + avdName);
                }
            }
            try {

                String ack1 = null;
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(dName2));

                String b_myport = t.get(key).getMyPort2();
                String b_dname = dName2;
                String b_pred = t.get(key).getPred();
                String b_succ = t.get(key).getSucc();
                String msgToSend = "BROADCAST," + b_myport + "," + b_dname + "," + b_pred + "," + b_succ;
                DataOutputStream os1 = new DataOutputStream(socket1.getOutputStream());
                os1.writeUTF(msgToSend);
                Log.d(TAG, "Sending Message to Server2 : " + msgToSend);
                os1.flush();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException broad " + e);
            }
        }

    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                //Log.i("RemotePort",rp);
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        avd0);

                String msgToSend = msgs[0];
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                String ack = null;
                do {
                    ack = null;
                    DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                    os.writeUTF(msgToSend);
                    Log.d(TAG, "Sending Message to Server : " + msgToSend);
                    os.flush();

                    DataInputStream is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    ack = is.readUTF();
                    Log.d(TAG, "Receiving ack from Server : " + ack);
                }
                while (!ack.equals("server ack"));
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException" + e);
            }

            return null;
        }
    }
}

