package com.example.mylibrary;

public class Impl_Needed {

    public Impl_Needed(){

    }
    public static String[] maven_url_list(){
            return new String[]{
                    "https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j-epsg",
                    "https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j",
                    "https://mvnrepository.com/artifact/org.osgeo/proj4j",
                    "https://mvnrepository.com/artifact/org.locationtech.jts/jts-core",
            };
    }
    public static String[] implementation_list(){
        return new String[]{
                "org.apache.commons:commons-math3:3.6.1",
                "org.locationtech.proj4j:proj4j-epsg:1.3.0",
                "org.locationtech.proj4j:proj4j:1.3.0",
                "group: 'org.osgeo', name: 'proj4j', version: '0.1.0'",
                "org.locationtech.jts:jts-core:1.19.0"
        };
    }
}
