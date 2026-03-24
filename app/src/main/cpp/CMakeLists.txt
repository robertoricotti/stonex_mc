cmake_minimum_required(VERSION 3.22.1)

project(nativeczechproj)

add_library(nativeczechproj SHARED
        native_czech_proj.cpp
        native_czech_grid.cpp)

find_library(log-lib log)

add_library(proj SHARED IMPORTED)
set_target_properties(proj PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libproj.so)

add_library(sqlite3 SHARED IMPORTED)
set_target_properties(sqlite3 PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libsqlite3.so)

target_include_directories(nativeczechproj PRIVATE
        ${CMAKE_SOURCE_DIR}/include)

target_link_libraries(nativeczechproj
        proj
        sqlite3
        ${log-lib})