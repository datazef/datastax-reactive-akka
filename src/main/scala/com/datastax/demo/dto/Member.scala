package com.datastax.demo.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class Member(member_uuid: String, first_name: String, last_name: String, email: String, age: Int, created_date: Long)