package com.jerson.hcdc_portal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RoomModel {
    @SerializedName("rooms")
   private List<rooms> rooms;

    public List<RoomModel.rooms> getRooms() {
        return rooms;
    }

    static public class rooms{
       @SerializedName("roomId")
       private String roomId;
       @SerializedName("room")
       private String room;
       @SerializedName("location")
       private String location;
       @SerializedName("previews")
       private List<previews> previews;

       public String getRoomId() {
           return roomId;
       }

       public void setRoomId(String roomId) {
           this.roomId = roomId;
       }

       public String getRoom() {
           return room;
       }

       public void setRoom(String room) {
           this.room = room;
       }

       public String getLocation() {
           return location;
       }

       public void setLocation(String location) {
           this.location = location;
       }

       public List<RoomModel.previews> getPreviews() {
           return previews;
       }

       public void setPreviews(List<RoomModel.previews> previews) {
           this.previews = previews;
       }
   }

    static public class previews{
        @SerializedName("image")
        private String image;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
