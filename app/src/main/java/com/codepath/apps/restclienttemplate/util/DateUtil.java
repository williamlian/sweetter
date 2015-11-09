package com.codepath.apps.restclienttemplate.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    //"Sun Nov 08 00:06:36 +0000 2015"
    public static Date convertStringToDate(String dateString)
    {
        Date date = null;
        DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        try{
            date = df.parse(dateString);
        }
        catch ( Exception ex ){
            System.out.println(ex);
        }
        return date;
    }
}
