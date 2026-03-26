cmake_minimum_required(VERSION 3.22.1)

project(nativeczechproj)

add_library(nativeczechproj SHARED native_czech_proj.cpp)

target_include_directories(nativeczechproj PRIVATE
    ${CMAKE_SOURCE_DIR}/third_party/arm64-v8a/include
)

add_library(proj STATIC IMPORTED)
set_target_properties(proj PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/third_party/arm64-v8a/lib/libproj.a
)

add_library(tiff STATIC IMPORTED)
set_target_properties(tiff PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/third_party/arm64-v8a/lib/libtiff.a
)

add_library(zlibstatic STATIC IMPORTED)
set_target_properties(zlibstatic PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/third_party/arm64-v8a/lib/libz.a
)

add_library(sqlite3static STATIC IMPORTED)
set_target_properties(sqlite3static PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/third_party/arm64-v8a/lib/libsqlite3.a
)

find_library(log-lib log)
find_library(android-lib android)
find_library(m-lib m)

target_link_libraries(nativeczechproj
    proj
    tiff
    zlibstatic
    sqlite3static
    ${log-lib}
    ${android-lib}
    ${m-lib}
)