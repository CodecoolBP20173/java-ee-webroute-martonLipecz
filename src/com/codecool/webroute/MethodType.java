package com.codecool.webroute;

public enum MethodType {
    GET, POST;

    public static MethodType identify(String str){
        switch(str.toLowerCase()){
            case "get" : return GET;
            case "post" : return POST;
            default: throw new IllegalArgumentException("Request method not supported");
        }
    }
}
