cmake_minimum_required(VERSION 3.10.2)
project("zhiliao")

find_package(cxx REQUIRED CONFIG)
link_libraries(cxx::cxx)

macro(SET_OPTION option value)
    set(${option} ${value} CACHE INTERNAL "" FORCE)
endmacro()

SET_OPTION(DOBBY_GENERATE_SHARED OFF)
if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    SET_OPTION(DOBBY_DEBUG OFF)
endif ()

add_subdirectory(external/Dobby)

target_include_directories(dobby PUBLIC external/Dobby/include)

add_library(zhiliao SHARED zhiliao.cpp)

target_link_libraries(zhiliao dobby)
