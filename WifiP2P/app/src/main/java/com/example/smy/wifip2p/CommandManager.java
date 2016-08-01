package com.example.smy.wifip2p;

/**
 * Created by SMY on 2016/8/1.
 */
public class CommandManager {
    SocketConnection socketConnection;
    String hostAddress;
    int port;
    MainActivity activity;

    /*public static final String DEFAULT_HOST = "192.168.49.1";
    public static final int COMMAND_PORT = 7878;
    public static final int DATA_PORT = 8787;*/

    public CommandManager(String host, int port, MainActivity activity){
        this.hostAddress = host;
        this.port = port;
        this.activity = activity;
        socketConnection = new SocketConnection(activity);
    }

    public void stop(){
        socketConnection.stop();
        socketConnection = null;

    /*
        public void startSession(CameraCommandListener messageListener) {
            CameraMessage message = new CameraMessage(AmbaCommand.AMBA_START_SESSION, messageListener);
            message.put(AmbaParam.AMBA_MSG_FILED_PARAM, 0);
            message.put(AmbaParam.AMBA_HEARTBEAT, AmbaParam.AMBA_HEARTBEAT_CHECK_START);
            cameraSocket.start(mHostAddress,mHostPort);
            cameraSocket.sendMessage(message);

            Logger.print("debug_wifi", "sent AmbaCommand.AMBA_START_SESSION");
        }*/
    }

    /*public void start(){
        socketConnection.start(hostAddress, port);
    }*/

}
