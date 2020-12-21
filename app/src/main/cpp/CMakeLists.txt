cmake_minimum_required(VERSION 3.10.2)
project("zhiliao")

add_library(zhiliao SHARED zhiliao.cpp)
target_link_libraries(zhiliao ${CMAKE_CURRENT_SOURCE_DIR}/libdobby.a)
