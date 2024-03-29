package com.jerson.hcdc_portal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerson.hcdc_portal.model.BuildingDataModel;
import com.jerson.hcdc_portal.model.BuildingModel;
import com.jerson.hcdc_portal.model.RoomModel;
import com.jerson.hcdc_portal.repo.RoomRepo;

public class RoomViewModel extends ViewModel {
    private MutableLiveData<Throwable> err  = new MutableLiveData<>();
    private RoomRepo repo;

    public RoomViewModel() {
        repo = new RoomRepo();
    }

    public MutableLiveData<Throwable> getErr() {
        return err;
    }

    public LiveData<RoomModel> getRooms(){
        return repo.getRooms(err);
    }

    public LiveData<RoomModel> getRoom(){
        return repo.getRoom(err);
    }
    public LiveData<RoomModel> getRoomx(){
        return repo.getRoomsx(err);
    }
    public LiveData<BuildingDataModel> getBuilding(){
        return repo.getBuilding(err);
    }
}
