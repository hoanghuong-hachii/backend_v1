package com.api19_4.api19_4.models;

public class Constants {
    public static final int NAME_MAX_LENGTH = 100;
    public static final int NAME_MIN_LENGTH = 5;

    public static final String PATTERN_USERNAME = "^(?=[a-zA-Z0-9-._]{5,50}$)(?!.*[-_.]{2})[^-_.].*[^-_.]$";

    public static final int PASSWORD_MAX_LENGTH = 256;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String PATTERN_PHONENUMBER = "^\\+?[0-9]{2,20}$";
    public static final String DEFAULT_DATE_FORMAT_DATE = "yyyy-MM-dd";
    public static final Integer DEFAULT_PAGE_SIZE_MAX = 1000;
    public static final int BRAND_MAX_LENGTH = 1000;
}
