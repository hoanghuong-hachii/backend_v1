package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Setter
@Getter
public class InventoryCheckDto {
    private String idInventoryCheck;
    private String nameProd;
    private String unitName;
    private int inventoryQuantity;
    private int actualQuantity;
    private int quantityDeviation;
    private String timeCheck;
    private String balanceDate;
    private double totalActualValue;
    private int totalQuantityDeviation;
    private int increasedDeviation;
    private int decreasedDeviation;
    private String note;
    private String status;
//    private String formattedDateTime;
//    private String formattedbalanceDate;

//    public void setTimeCheck(String timeCheck) {
//        this.timeCheck = timeCheck;
//        this.formattedDateTime = getFormattedDateTime();
//    }
//
//    public void setBalanceDate(String balanceDate) {
//        this.balanceDate = balanceDate;
//        this.formattedbalanceDate = getbalanceDate();
//    }
    List<ProductCheckDto> productCheckDtoList;
//
//    private String getFormattedDateTime() {
//        LocalDateTime dateTime = LocalDateTime.parse(timeCheck, DateTimeFormatter.ISO_DATE_TIME);
//        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
//        return dateTime.format(outputFormatter);
//    }
//
//    private String getbalanceDate() {
//        LocalDateTime dateTime = LocalDateTime.parse(balanceDate, DateTimeFormatter.ISO_DATE_TIME);
//        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
//        return dateTime.format(outputFormatter);
//    }


    private String getStatus(String status){
        if(status.equals("draft")){
            return "Tạm thời";
        }else if(status.equals("pending")){
            return "Chờ duyệt";
        } else if (status.equals("approved")) {
            return "Đã duyệt";
        } else if (status.equals("in_progress")) {
            return "Đang thực hiện";
        } else if (status.equals("completed")) {
            return "Hoàn thành";
        } else if (status.equals("cancelled")) {
            return "Đã hủy";
        } else {
            return "Đã hoàn tât";
        }
    }


}
