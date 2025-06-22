build.gradle(Module:app)
implementation 'org.apache.commons:commons-math3:3.6.1'
// https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j-epsg
implementation 'org.locationtech.proj4j:proj4j-epsg:1.3.0'
// https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j
implementation 'org.locationtech.proj4j:proj4j:1.3.0'
// https://mvnrepository.com/artifact/org.osgeo/proj4j
implementation group: 'org.osgeo', name: 'proj4j', version: '0.1.0'
// https://mvnrepository.com/artifact/org.locationtech.jts/jts-core
implementation 'org.locationtech.jts:jts-core:1.19.0'


settings.gradle(Project Settings)
maven{url 'https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j-epsg'}
maven{url 'https://mvnrepository.com/artifact/org.locationtech.proj4j/proj4j'}
maven {url 'https://mvnrepository.com/artifact/org.osgeo/proj4j'}
maven{url' https://mvnrepository.com/artifact/org.locationtech.jts/jts-core'}