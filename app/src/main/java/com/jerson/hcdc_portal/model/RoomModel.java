package com.jerson.hcdc_portal.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RoomModel implements Serializable {
    @SerializedName("rooms")
   private List<rooms> rooms;

    public List<RoomModel.rooms> getRooms() {
        return rooms;
    }

    static public class rooms implements Serializable{
       @SerializedName("roomId")
       private String roomId;
       @SerializedName("room")
       private String room;
       @SerializedName("building")
       private String building;
       @SerializedName("previews")
       private List<previews> previews;

        public String getRoomId() {
            return roomId;
        }

        public String getRoom() {
            return room;
        }

        public String getBuilding() {
            return building;
        }

        public List<RoomModel.previews> getPreviews() {
            return previews;
        }
    }

    static public class previews implements Serializable {
        @SerializedName("description")
        private String description;
        @SerializedName("img")
        private String img;

        public String getDescription() {
            return description;
        }

        public String getImg() {
            return img;
        }
    }
}
