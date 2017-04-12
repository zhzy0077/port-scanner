package com.example

import scala.concurrent.Future

/**
  * Created by zheng on 2017/4/9.
  * in port_scan. 
  */
case class ScanResponse(tcpResult: Seq[(Int, Future[Boolean])],
                        udpResult: Seq[(Int, Future[Boolean])],
                        synResult: Seq[(Int, Future[Boolean])])