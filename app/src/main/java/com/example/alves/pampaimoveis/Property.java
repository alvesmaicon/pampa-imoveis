package com.example.alves.pampaimoveis;




import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by alves on 08/05/2017.
 */

public class Property {

    private String id;
    private String userId;
    private String type;
    private String neighborhood;
    private String city;
    private String street;
    private String cep;
    private String number;
    private String area;
    private String price;
    private String freevacancies;
    private String vacancies;
    private String rooms;
    private String bathrooms;
    private String pricepervacancy;
    private String complement;
    private String startdate;
    private Boolean available;

    private List<String> photoList = new ArrayList<>();
    private List<Vacancy> vacancyList = new ArrayList<>();
    // list of vacancy and price


    public Property(){

    }

    public Property(String UID){
        this.updateStartdate();
        this.setUserId(UID);
    }

    public String getId() {
        this.id = this.getCep() + "-" + this.getNumber();
        if(!this.getComplement().isEmpty())
            this.id += "-" + this.getComplement();
        return id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRooms() {
        return rooms;
    }

    public void setRooms(String rooms) {
        this.rooms = rooms;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(String bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getVacancies() {
        return vacancies;
    }

    public void setVacancies(String vacancies) {
        this.vacancies = vacancies;
    }

    public String getPricepervacancy() {
        return pricepervacancy;
    }

    public void setPricepervacancy(String pricepervacancy) {
        this.pricepervacancy = pricepervacancy;
    }

    public String getFreevacancies() {
        return freevacancies;
    }

    public void setFreevacancies(String freevacancies) {
        this.freevacancies = freevacancies;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public void updateStartdate(){
        DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar date = Calendar.getInstance();
        this.setStartdate(dateformat.format(date.getTime()));
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<Vacancy> getVacancyList() {
        return vacancyList;
    }

    public void setVacancyList(List<Vacancy> vacancyList) {
        this.vacancyList = vacancyList;
    }

    public List<String> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<String> photoList) {
        this.photoList = photoList;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }
}
