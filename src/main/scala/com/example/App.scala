package com.example

import java.util.concurrent.atomic.AtomicInteger
import javafx.application.{Application, Platform}
import javafx.event.EventHandler
import javafx.geometry.{Insets, Orientation, Pos}
import javafx.scene.Scene
import javafx.scene.control.{ProgressBar, _}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{GridPane, StackPane}
import javafx.scene.text.{Font, FontWeight, Text}
import javafx.stage.Stage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class App extends Application {
  private val resultArea = new TextArea("TYPE    PORT    STATUS\n")
  private val title = new Text("Port Scanner")
  private val hostname = new Label("Hostname")
  private val hostnameField = new TextField()
  private val ports = new Label("Ports")
  private val portFromField = new TextField()
  private val portToField = new TextField()
  private val tcpBox = new CheckBox("TCP")
  private val udpBox = new CheckBox("UDP")
  private val synBox = new CheckBox("SYN Scan")
  private val progressBar = new ProgressBar(0.0)
  private val scan = new Button("Scan")

  override def stop(): Unit = {
    PortScanner.stop()
  }


  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Port Scan")

    val optionsPane = new GridPane()
    optionsPane.setAlignment(Pos.CENTER)
    optionsPane.setHgap(10)
    optionsPane.setVgap(10)
    optionsPane.setPadding(new Insets(25, 25, 25, 25))
    configureOptions(optionsPane)

    val resultPane = new StackPane()
    configureResult(resultPane)

    val mainPane = new SplitPane()
    mainPane.getItems.addAll(optionsPane, resultPane)
    mainPane.setOrientation(Orientation.VERTICAL)
    mainPane.setDividerPositions(0.5f)

    val scene = new Scene(mainPane)
    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private def configureResult(resultPane: StackPane) = {
    resultPane.getChildren.add(resultArea)
  }

  private def configureOptions(gridPane: GridPane): Unit = {
    title.setFont(Font.font("Consolas", FontWeight.BOLD, 20))
    gridPane.add(title, 0, 0)
    gridPane.add(hostname, 0, 1)
    gridPane.add(hostnameField, 1, 1, 2, 1)
    gridPane.add(ports, 0, 2)
    gridPane.add(portFromField, 1, 2)
    gridPane.add(portToField, 2, 2)
    gridPane.add(tcpBox, 0, 3, 2, 1)
    gridPane.add(udpBox, 1, 3, 2, 1)
    gridPane.add(synBox, 2, 3, 2, 1)
    gridPane.add(progressBar, 0, 4, 4, 1)
    gridPane.add(scan, 0, 5)

    progressBar.prefWidthProperty().bind(gridPane.widthProperty())

    object handler extends EventHandler[MouseEvent] {
      override def handle(event: MouseEvent): Unit = {
        if (event.getEventType == MouseEvent.MOUSE_CLICKED) {
          val udp = udpBox.isSelected
          val tcp = tcpBox.isSelected
          val syn = synBox.isSelected
          val hostname = hostnameField.getCharacters.toString
          val portFrom = portFromField.getCharacters.toString.toInt
          val portTo = portToField.getCharacters.toString.toInt
          val request = ScanRequest(hostname, portFrom to portTo, tcp, udp, syn)
          val response = PortScanner.scan(request)
          progressBar.setProgress(0)
          resultArea.setText("TYPE    PORT    STATUS\n")
//          val count = new AtomicInteger(0)
          def finish(mode: String)(port: Int)(t: Try[Boolean]): Unit = {
            Platform.runLater(() => {
//              println(count.incrementAndGet())
              progressBar.setProgress(progressBar.getProgress + 1.0 / (portTo - portFrom))
            })
            t match {
              case Success(v) if v =>
                Platform.runLater(() => {
                  val line = s"$mode     $port     OPEN\n"
                  resultArea.appendText(line)
                })
              case _ =>
            }
          }

          response.tcpResult.foreach {
            case (port, future) => (port, future.onComplete(finish("TCP")(port)))
          }
          response.udpResult.foreach {
            case (port, future) => (port, future.onComplete(finish("UDP")(port)))
          }
          response.synResult.foreach {
            case (port, future) => (port, future.onComplete(finish("SYN")(port)))
          }
        }
      }
    }
    scan.setOnMouseClicked(handler)
  }
}
