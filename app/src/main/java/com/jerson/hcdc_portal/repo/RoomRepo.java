package com.jerson.hcdc_portal.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.google.gson.Gson;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.listener.OnHttpResponseListener;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.network.HttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RoomRepo {
    private final String url = "https://raw.githubusercontent.com/Jerson2000/jerson2000/portal_assets/room.json";

    /* this is for assets */
    public LiveData<RoomModel> getRooms(MutableLiveData<Throwable> err) {
        MutableLiveData<RoomModel> data = new MutableLiveData<>();
        try {
            InputStream inputStream = PortalApp.getAppContext().getAssets().open("rooms.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            RoomModel room = new Gson().fromJson(reader, RoomModel.class);
            data.setValue(room);
        } catch (IOException e) {
            err.setValue(e);
        }

        return data;
    }


    public LiveData<RoomModel> getRoomsx(MutableLiveData<Throwable> err) {
        MutableLiveData<RoomModel> data = new MutableLiveData<>();
        File file = new File(PortalApp.getAppContext().getFilesDir(), "rooms.json");
        String filePath = file.getAbsolutePath();

        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            RoomModel room = new Gson().fromJson(inputStreamReader, RoomModel.class);
            data.setValue(room);

            inputStreamReader.close();
        } catch (IOException e) {
            err.setValue(e);
        }

        return data;
    }

    public LiveData<RoomModel> getRoom(MutableLiveData<Throwable> err){
        MutableLiveData<RoomModel> data = new MutableLiveData<>();

        HttpClient.getInstance().GET_ResponseBody(url, new OnHttpResponseListener<ResponseBody>() {
            @Override
            public void onResponse(ResponseBody response) {
                try {
                    RoomModel model = new Gson().fromJson(response.string(),RoomModel.class);
                    data.setValue(model);

                }catch (IOException e){
                    err.setValue(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                err.setValue(e);
            }

            @Override
            public void onResponseCode(int code) {

            }
        });


        return  data;
    }

}
