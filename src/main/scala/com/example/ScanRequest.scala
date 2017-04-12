package com.example

/**
  * Created by zheng on 2017/4/9.
  * in port_scan. 
  */
case class ScanRequest(hostname: String, ports: Range, tcpScan: Boolean, udpScan: Boolean, synScan: Boolean)