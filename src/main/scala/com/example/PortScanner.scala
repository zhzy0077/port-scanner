package com.example

import java.net._
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by zheng on 2017/4/9.
  * in port_scan. 
  */
object PortScanner {
  private val threadPool = Executors.newFixedThreadPool(128)
  def stop(): Unit = {
    threadPool.shutdownNow()
  }
  def scan(scanRequest: ScanRequest): ScanResponse = {
    var tcpResult: Seq[(Int, Future[Boolean])] = Seq.empty
    var udpResult: Seq[(Int, Future[Boolean])] = Seq.empty
    var synResult: Seq[(Int, Future[Boolean])] = Seq.empty
    implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)
    val ip = InetAddress.getByName(scanRequest.hostname).getHostAddress
    println(s"${scanRequest.hostname} resolved to $ip")
    if (scanRequest.tcpScan) {
      tcpResult = tcpScan(ip, scanRequest.ports)
    }
    if (scanRequest.udpScan) {
      udpResult = udpScan(ip, scanRequest.ports)
    }
    if (scanRequest.synScan) {
      synResult = synScan(ip, scanRequest.ports)
    }
    ScanResponse(tcpResult, udpResult, synResult)
  }

  private def tcpScan(ip: String, ports: Range)(implicit executionContext: ExecutionContext): Seq[(Int, Future[Boolean])] = {
    var futures = Seq.empty[(Int, Future[Boolean])]
    for (port <- ports) {
      val future = Future(isTcpListen(ip, port))
      futures = (port -> future) +: futures
    }
    futures
  }

  private def udpScan(ip: String, ports: Range)(implicit executionContext: ExecutionContext): Seq[(Int, Future[Boolean])] = {
    var futures = Seq.empty[(Int, Future[Boolean])]
    for (port <- ports) {
      val future = Future(isUdpListen(ip, port))
      futures = (port -> future) +: futures
    }
    futures
  }

  private def synScan(ip: String, ports: Range)(implicit executionContext: ExecutionContext): Seq[(Int, Future[Boolean])] = {
    var futures = Seq.empty[(Int, Future[Boolean])]
    for (port <- ports) {
      val future = Future(isSynListen(ip, port))
      futures = (port -> future) +: futures
    }
    futures
  }

  private def isTcpListen(hostname: String, port: Int): Boolean = {
    var socket: Socket = null
    try {
      socket = new Socket()
      socket.connect(new InetSocketAddress(hostname, port), 3000)
      true
    } catch {
      case _: Exception => false
    } finally {
      if (socket != null) {
        socket.close()
      }
    }
  }

  private def isSynListen(hostname: String, port: Int): Boolean = {
    isTcpListen(hostname, port)
  }

  private def isUdpListen(hostname: String, port: Int): Boolean = {
    var datagramSocket: DatagramSocket = null
    val datagramPacket = new DatagramPacket(new Array[Byte](128), 128)
    try {
      datagramSocket = new DatagramSocket()
      datagramSocket.connect(InetAddress.getByName(hostname), port)
      datagramSocket.setSoTimeout(1000)
      datagramSocket.send(datagramPacket)
      datagramSocket.receive(datagramPacket)
      true
    } catch {
      case _: Exception =>
        false
    } finally {
      if (datagramSocket != null) {
        datagramSocket.close()
      }
    }
  }
}
