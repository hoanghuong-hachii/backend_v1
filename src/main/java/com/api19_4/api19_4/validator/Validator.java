package com.api19_4.api19_4.validator;

import com.api19_4.api19_4.models.Constants;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Validator {
    public static boolean isDateInstance(String dateStr) throws Exception {
        try {
            new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT_DATE).parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> boolean isHaveDataLs(List<T> types) {
        if (types == null || types.isEmpty() || types.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isHaveDataString(String types) {
        if (types == null || types.isEmpty() || types.length() == 0 || types.isBlank()) {
            return false;
        }
        return true;
    }

    public static boolean isRange(final Object from, final Object to) {
        try {
            if (from == null || to == null) {
                throw new NullPointerException();
            } else {
                if (from instanceof LocalDateTime) {
                    LocalDateTime c1 = (LocalDateTime) from;
                    LocalDateTime c2 = (LocalDateTime) to;
                    return (c2.compareTo(c1) >= 0) ? true : false;
                } else if (from instanceof Date) {
                    Date c1 = (Date) from;
                    Date c2 = (Date) to;
                    return (c2.getTime() - c1.getTime() >= 0) ? true : false;
                } else if (from instanceof Long || from instanceof Integer) {
                    Long c1 = (Long) from;
                    Long c2 = (Long) to;
                    return c2.compareTo(c1) >= 0;
                } else if (from instanceof Double || from instanceof Float) {
                    Double c1 = (Double) from;
                    Double c2 = (Double) to;
                    return (c2.compareTo(c1) >= 0) ? true : false;
                } else if (from instanceof LocalDate) {
                    LocalDate c1 = (LocalDate) from;
                    LocalDate c2 = (LocalDate) to;
                    return (c2.compareTo(c1) >= 0) ? true : false;
                } else if (from instanceof LocalTime) {
                    LocalTime c1 = (LocalTime) from;
                    LocalTime c2 = (LocalTime) to;
                    return (c2.compareTo(c1) >= 0) ? true : false;
                } else {
                    return false;
                }
            }
        } catch (final Exception ex) {
            return false;
        }
    }

    public static boolean isTimesteps(final String timesteps) {
        try {
            Integer num = Integer.valueOf(timesteps.substring(0, timesteps.length() - 2));
            String val = timesteps.substring(timesteps.length() - 1);
            if (!(val.endsWith("m") || val.endsWith("h") || val.endsWith("d"))) {
                return false;
            } else {
                switch (val) {
                    case "m":
                        if (num >= 10 && num <= 30) {
                            return true;
                        }
                        break;
                    case "h":
                        if (num >= 1 && num <= 20) {
                            return true;
                        }
                        break;
                    case "d":
                        if (num >= 1 && num <= 30) {
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static Boolean dateFormaterISO8610(String dateTime) {
        // định dạng dd/MM/yyyy
        String dateTimeConvert = dateTime + " " + "00:00:00";
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime.parse(dateTimeConvert, format);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean checkFormatDate(String dateTime) {
        // định dạng dd/MM/yyyy
        String dateTimeConvert = dateTime + " " + "00:00:00";
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime.parse(dateTimeConvert, format);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean checkStringIsFormatDate(String dateTime) throws Exception {
        try {
            if (checkFormatDate(dateTime)) {
                String a = dateTime.substring(0, 2);
                String b = dateTime.substring(3, 5);
                String c = dateTime.substring(6, 10);
                if (isInteger(a) && isInteger(b) && isInteger(c)) {
                    Integer d = Integer.valueOf(a);
                    Integer e = Integer.valueOf(b);
                    Integer f = Integer.valueOf(c);
                    if (((e == 1 || e == 3 || e == 5 || e == 7 || e == 8 || e == 10 || e == 12) && d <= 31)
                            || ((e == 4 || e == 6 || e == 9 || e == 11) && d <= 30)) {
                        return true;
                    } else {
                        if ((f % 100 != 0 && f % 4 == 0 && e == 2 && d <= 29)
                                || (f % 100 != 0 && f % 4 != 0 && e == 2 && d <= 28)
                                || (f % 400 == 0 && e == 2 && d <= 29)
                                || (f % 100 == 0 && f % 400 != 0 && e == 2 && d <= 28)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static Boolean isInteger(String a) throws Exception {
        try {
            if (Integer.valueOf(a) instanceof Integer) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
