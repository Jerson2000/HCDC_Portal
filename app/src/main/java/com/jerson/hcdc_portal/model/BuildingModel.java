package com.jerson.hcdc_portal.model;

import java.io.Serializable;
import java.util.List;

public class BuildingModel implements Serializable {
    private String id;
    private String name;
    private String history;
    private String located;
    private String floor_desc;
    private String image_prev;
    private List<Floor> floors;

    public BuildingModel(String id, String name, String history, String located, String floor_desc, String image_prev, List<Floor> floors) {
        this.id = id;
        this.name = name;
        this.history = history;
        this.located = located;
        this.floor_desc = floor_desc;
        this.image_prev = image_prev;
        this.floors = floors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getLocated() {
        return located;
    }

    public void setLocated(String located) {
        this.located = located;
    }

    public String getFloor_desc() {
        return floor_desc;
    }

    public void setFloor_desc(String floor_desc) {
        this.floor_desc = floor_desc;
    }

    public String getImage_prev() {
        return image_prev;
    }

    public void setImage_prev(String image_prev) {
        this.image_prev = image_prev;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }



    public static class Floor implements Serializable{
        private int floor_no;
        private List<Room> rooms;

        public Floor(int floor_no, List<Room> rooms) {
            this.floor_no = floor_no;
            this.rooms = rooms;
        }

        public int getFloor_no() {
            return floor_no;
        }

        public void setFloor_no(int floor_no) {
            this.floor_no = floor_no;
        }

        public List<Room> getRooms() {
            return rooms;
        }

        public void setRooms(List<Room> rooms) {
            this.rooms = rooms;
        }




        public static class Room implements Serializable{
            private String room_no;
            private String desc;
            private List<Preview> preview;

            public Room(String room_no, String desc, List<Preview> preview) {
                this.room_no = room_no;
                this.desc = desc;
                this.preview = preview;
            }

            public String getRoom_no() {
                return room_no;
            }

            public void setRoom_no(String room_no) {
                this.room_no = room_no;
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public List<Preview> getPreview() {
                return preview;
            }

            public void setPreview(List<Preview> preview) {
                this.preview = preview;
            }




            public static class Preview implements Serializable{
                private String desc;
                private String img_url;

                public Preview(String desc, String img_url) {
                    this.desc = desc;
                    this.img_url = img_url;
                }

                public String getDesc() {
                    return desc;
                }

                public void setDesc(String desc) {
                    this.desc = desc;
                }

                public String getImg_url() {
                    return img_url;
                }

                public void setImg_url(String img_url) {
                    this.img_url = img_url;
                }
            }
        }
    }
}

