package com.example.notesapplication.Utils;

/**
 */
public class Config {
    //URL to our login.php file
    public static final String LOGIN_URL = "http://silptech.eu5.org/login.php";
    public static final String REGISTER_URL = "http://silptech.eu5.org/register.php";
    public static final String ADDCMT_URL = "http://silptech.eu5.org/addcomment.php";
    public static final String VIEWCMT_URL = "http://silptech.eu5.org/comments.php";
    public static final String DELETE_CMT = "http://silptech.eu5.org/delete.php";


    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_EMAIL = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AUTHOR = "username";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TEXT = "message";
    public static final String KEY_DELETE = "delete";
    public static final String KETPOSTID = "post_id";

    //If server response is equal to this that means login is successful
    public static final String LOGIN_SUCCESS = "success";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "myloginapp";

    //This would be used to store the email of current logged in user
    public static final String USER_SHARED_PREF = "email";

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";
}
