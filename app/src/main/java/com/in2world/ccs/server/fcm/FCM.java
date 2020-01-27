package com.in2world.ccs.server.fcm;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.in2world.ccs.RootApplcation;
import com.in2world.ccs.server.Result;
import com.in2world.ccs.service.SIP_Service;
import com.in2world.ccs.tools.GlobalData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCM {
    private static final String TAG = "FCM";

    public static final String SIP_RUN = "SIP_RUN";
    public static final String SIP_READY = "SIP_READY";
    public static final String FCM_TITLE = "title";
    public static final String FCM_NAME = "name";
    public static final String FCM_DATA = "data";

    public static String Server_key = "AAAAcAzvFC8:APA91bFM6RhxN4iAEnFqkz3ONbCKqdDGV6Uo5KbDOhlzQILFaLkyqS5qQGB6JRsnNYLq2gjiKwDkD3Vy46Igz_H2hLfg5Owbxn1qR6uJlEgMhgHjGLq_qFRa8f6cZTzA6OM5qtxOd7me";
    public static String token_sender = "";
    public static String token_resever = "";
    private Context context;
    private Result Result;

    public FCM(Context context, Result Result) {
        this.context = context;
        this.Result = Result;
    }

    public void pushMessage(String title, String name, String data, String token) {
        final String url = "https://fcm.googleapis.com/fcm/send";
        JSONObject notification_data = new JSONObject();
        try {

            JSONObject dataJSON = new JSONObject();
            dataJSON.put(FCM_TITLE, title);
            dataJSON.put(FCM_NAME, name);
            dataJSON.put(FCM_DATA, data);

            notification_data.put("data", dataJSON);
            notification_data.put("to", token);

            Log.d(TAG, "pushMessage: data "+notification_data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, notification_data,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);
                        Result.onResult(response, "pushToken", true, 200, "");

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: "+error);
                        Result.onResult(null, "pushToken", false, error.hashCode(), error.getMessage());

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + Server_key);
                params.put("Content-Type", "application/json");

                Log.d(TAG, "getHeaders: params"+params.toString());
                return params;
            }
        };
        RootApplcation.getmRootApplcation().getRequestQueue().add(jsObjRequest);
    }

    public static void sendToCall(Context context){
        new FCM(context, new Result() {
            @Override
            public void onResult(Object object, String function, boolean IsSuccess, int RequestStatus, String MessageStatus) {
                Log.d(TAG, "onResult: IsSuccess "+IsSuccess);
            }
        }).pushMessage(FCM.SIP_READY,"213","13123", GlobalData.mUser.getFcmToken());

    }


}
