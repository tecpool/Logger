package com.tecpool.logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by sumit on 09/03/2019.
 */

public class WebService {
    Context context;
    Activity activity;
    String baseUrl="";
    RequestQueue requestQueue;
    String params[][]=new String[][]{};
    String deviceId="";
    android.support.v7.app.AlertDialog alertDialog;
    ProgressDialog dialog;


    public WebService(Activity activity)
    {
        this.activity=activity;
        this.context=activity.getBaseContext();
        //baseUrl=context.getResources().getString(R.string.service);
        baseUrl=context.getResources().getString(R.string.service);

        requestQueue= Volley.newRequestQueue(context);
        deviceId= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void getString(String methodName, boolean isBackground, String loaderTitle, String loaderMessage, final VolleyCallback callback)
    {
        if(!isBackground)
            showProgressDialog(loaderTitle,loaderMessage);

        String p="&";
        for(int i=0;i<params.length;i++)
        {
            p=p+params[i][0]+"=";
            p=p+(params[i][1].replace(" ","%20").replace("\n","%0A").replace("'","%27%27").replace("=","equel%20to").replace("&","and"))+"&";
        }
        baseUrl=baseUrl+methodName+"?flag="+deviceId+""+(params.length<=0 ? "" : p.substring(0,p.length()-1));
        createRequest(callback,isBackground,loaderTitle,loaderMessage);
    }

    public void createRequest(final VolleyCallback callback, final boolean isBackground, final String loaderTitle, final String loaderMessage)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            try{dialog.dismiss();}catch (Exception e){}
                            callback.onSuccess(response.substring((response.indexOf(">",response.indexOf(">")+1)+1), response.length() - 9).replace("&amp;", "and"));
                        }catch (Exception e)
                        {
                            try{dialog.dismiss();}catch (Exception ee){}
                            callback.onSuccess(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.toString().contains("NoConnectionError"))
                {
                    try{dialog.dismiss();}catch (Exception e){}
                    if(isBackground)
                        callback.onSuccess("No Connection");
                    else
                        showMYDialog(loaderTitle,loaderMessage,"No Connection","Cannot connect to Internet...Please check your connection!",callback,isBackground);
                }
                else if(error.toString().contains("TimeoutError"))
                {
                    try{dialog.dismiss();}catch (Exception e){}
                    if(isBackground)
                        callback.onSuccess("Timeout Error");
                    else
                        showMYDialog(loaderTitle,loaderMessage,"Timeout Error","Connection TimeOut! Please check your internet connection.",callback,isBackground);
                }
                else if(error.toString().contains("ServerError"))
                {
                    try{dialog.dismiss();}catch (Exception e){}
                    if(isBackground)
                        callback.onSuccess("Server Error");
                    else
                        showMYDialog(loaderTitle,loaderMessage,"Server Error", "The server could not be found. Please try again after some time!!",callback,isBackground);
                }
                else
                {
                    try{dialog.dismiss();}catch (Exception e){}
                    if(isBackground)
                        callback.onSuccess("Something Wrong");
                    else
                        showMYDialog(loaderTitle,loaderMessage,"Something Wrong","Please try again after some time!!",callback,isBackground);
                }

            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,0,1f));
        requestQueue.add(stringRequest);
    }

    public void showMYDialog(final String loaderTitle, final String loaderMessage, String title, String msg, final VolleyCallback callback, final boolean isBackground){
        alertDialog = new android.support.v7.app.AlertDialog.Builder(activity)
                .setPositiveButton("Reload", null)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(msg)
                .show();
        Button b = alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                showProgressDialog(loaderTitle,loaderMessage);
                createRequest(callback,isBackground,loaderTitle,loaderMessage);
                //activity.finish();
            }
        });
    }

    public void showProgressDialog(String title, String message)
    {
        dialog=new ProgressDialog(activity);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
    }


}
