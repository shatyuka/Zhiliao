cmake_minimum_required(VERSION 3.10.2)
project("zhiliao")

add_library(zhiliao SHARED zhiliao.cpp)

find_package(dobby REQUIRED CONFIG)
target_link_libraries(zhiliao dobby::dobby)
