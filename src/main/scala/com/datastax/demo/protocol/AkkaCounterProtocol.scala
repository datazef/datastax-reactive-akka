package com.datastax.demo.protocol

trait AkkaCounterProtocol

case object Bootstrap extends AkkaCounterProtocol
case object GetNext extends AkkaCounterProtocol
