package com.jerson.hcdc_portal.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.google.gson.Gson;
import com.jerson.hcdc_portal.PortalApp;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.network.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;

public class RoomRepo {

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

   /* public LiveData<RoomModel> getRoom(MutableLiveData<Throwable> err){
        MutableLiveData<RoomModel> data = new MutableLiveData<>();
        ResponseBody responseBody = HttpClient.getInstance().getClient().newCall()
    }*/

}
