package com.example.tasteebackend.dto;

public class RestaurantOrderStatusResponse {

    private boolean manuallyOpen;
    private boolean acceptingOrders;
    private String dayOfWeek;
    private String openTime;
    private String closeTime;

    public RestaurantOrderStatusResponse() {
    }

    public RestaurantOrderStatusResponse(
            boolean manuallyOpen,
            boolean acceptingOrders,
            String dayOfWeek,
            String openTime,
            String closeTime
    ) {
        this.manuallyOpen = manuallyOpen;
        this.acceptingOrders = acceptingOrders;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public boolean isManuallyOpen() {
        return manuallyOpen;
    }

    public void setManuallyOpen(boolean manuallyOpen) {
        this.manuallyOpen = manuallyOpen;
    }

    public boolean isAcceptingOrders() {
        return acceptingOrders;
    }

    public void setAcceptingOrders(boolean acceptingOrders) {
        this.acceptingOrders = acceptingOrders;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }
}