package com.example.alves.pampaimoveis;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Interest {

    private String id;
    private String userId;
    private String propertyid;
    private String addeddate;

    // fazer um hashmap aqui com id usuario + id casa de objeto interesse userId, PropertyId, startDate.

    public Interest(){

    }



    public Interest(String userId, Property property){
        DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar date = Calendar.getInstance();
        this.setAddeddate(dateformat.format(date.getTime()));
        this.setUserId(userId);
        this.setPropertyid(property.getId());
    }

    public String getId() {
        String InterestId = this.propertyid + "-" + this.userId;
        return InterestId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyid() {
        return propertyid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public String getAddeddate() {
        return addeddate;
    }

    public void setAddeddate(String addeddate) {
        this.addeddate = addeddate;
    }

    public void updateAddeddate() {
        DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar date = Calendar.getInstance();
        this.addeddate = dateformat.format(date.getTime());
    }
}
