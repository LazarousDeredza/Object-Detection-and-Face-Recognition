package com.example.eyesyhopefyp;

public class Constants {

    //db name
    public static  final String DB_NAME="MY_RECORDS_DB.db";

    //db version
    public static  final int DB_VERSION=1;




    public  static final String C_ID="ID";
    public  static final String C_PHONE="PHONE";
    public  static final String C_EMAIL="EMAIL";
    public static final String NAME ="NAME" ;
    public static final String BlindName ="BlindName" ;

    //Table Users

    public static  final String Users="users";


;
    //Create table query
    public static final String CREATE_TABLE="CREATE TABLE "+Users+" ("
            +C_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +NAME+" TEXT,"
            +C_PHONE+" TEXT,"
            +C_EMAIL +" TEXT,"
            +BlindName +" TEXT"
            +" )";





}
