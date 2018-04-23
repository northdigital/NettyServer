package main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController {
  @FXML public TextField txtPort;
  @FXML public Button btnStartServer;
  @FXML public Button btnStopServer;
  @FXML public TextArea txtMessages;
  @FXML public Button btnSend;
  @FXML public TextField txtSendMsg;

  public MainController() {
  }

  public void log(String msg) {
    Platform.runLater(() -> txtMessages.appendText(msg + "\r\n"));
  }

  public int getPort() throws Exception {
    String portS = txtPort.getText();
    if(portS != null)
      return Integer.parseInt(portS);

    throw new Exception("Port not defined!");
  }

  private final MyServer myServer = new MyServer(this);

  @FXML
  public void btnStartServerClick(ActionEvent actionEvent) throws Exception {
    myServer.start();
  }

  @FXML
  public void btnStopServerClick(ActionEvent actionEvent) throws Exception {
    myServer.stop();
  }

  @FXML
  public void btnSendClick(ActionEvent actionEvent) {
    String msg = txtSendMsg.getText();
    myServer.send(msg + "\r\n");
  }
}
