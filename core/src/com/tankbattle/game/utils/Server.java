package com.tankbattle.game.utils;

import com.badlogic.gdx.Gdx;
import com.tankbattle.game.MyGdxGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by aditya.hadi on 11/25/2016.
 */

public class Server extends Thread {

    private final int _portNumber = 9021;
    private ServerSocket serverSocket;
    private ConnectionRequestHandler handler[];
    private MyGdxGame theGame;
    private int numOfClient = 0;
    private boolean listening = true;


    public Server(MyGdxGame game) {
        theGame = game;
    }

    @Override
    public void run() {
        Gdx.app.log("Socket", "Server running...");

        try {
            handler = new ConnectionRequestHandler[2];
            serverSocket = new ServerSocket(_portNumber);

            while (listening){
                Socket sock = serverSocket.accept();
                handleClientRequest(sock);
            }
            serverSocket.close();
        } catch (Exception e){
            Gdx.app.log("Socket", "Could not listen on port: " + _portNumber);
            System.exit(-1);
        }
    }

    private void handleClientRequest(Socket serverSocket) {
        try {
            if (numOfClient < 2)
            {
                ConnectionRequestHandler hand = new ConnectionRequestHandler(serverSocket);
                hand.start();
                if (numOfClient < 1)
                    handler[0] = hand;
                else
                    handler[1] = hand;
            }
            else
            {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Handle client connection requests
     */
    public class ConnectionRequestHandler extends Thread {
        private Socket _socket;
        private BufferedReader buffer;
        private ObjectOutputStream _out = null;
        private ObjectInputStream _in = null;
        private Object obj = null;
        private int clientID = -1;
        private String userName = null;
        private boolean runClient = true;
        private boolean isRegistered = false;

        public ConnectionRequestHandler(Socket sock){
            _socket = sock;
            numOfClient++;
        }


        public void run(){
            Gdx.app.log("Socket", "Client " + (numOfClient-1) + " connected to _socket: "+ _socket.toString());

            try {
                _out = new ObjectOutputStream(_socket.getOutputStream());
                _in = new ObjectInputStream(_socket.getInputStream());

                // sending init information to client
                Gdx.app.log("Socket", "Server: sending init information");
                _out.writeObject(new byte[]{Define.SOCK_REQ_DO_INIT_CONNECTION, Define.SOCK_DATA_INIT_CONNECTION});

                while((obj = _in.readObject()) != null && runClient) {
                    processData(obj, _out);
                }
            } catch (IOException e) {
//                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }finally { //In case anything goes wrong we need to close our I/O streams and sockets.
                closeClient();
            }
        }

        public void closeClient() {
            try {
                //if (clientID >= 0 && clientID <= 1)
                //MainWindow.playerName[clientID] = null;

                if (_socket != null)
                {
                    Gdx.app.log("Socket", "Client: " + clientID + ", Name: "+ userName + " DISCONNECTED");

                    numOfClient--;
                    if(_out != null)
                        _out.close();
                    if(_in != null)
                        _in.close();
                    _socket.close();
                    _socket = null;
                    runClient = false;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private void processData(Object obj, ObjectOutputStream out) throws Exception{
            byte[] data = (byte[]) obj;

            if (data[0] == Define.SOCK_REQ_DO_INIT_CONNECTION) {
                clientID = data[1];
                if (clientID < 0 || clientID > 1) {
                    Gdx.app.log("Socket", "Client : " + clientID + " try to connect");
                    try {
                        out.writeObject("FAILED LOGIN");
                    } catch (Exception e) {}
                    closeClient();
                    return;
                }

                if (theGame.playerName[clientID].compareTo("NONE") != 0) {
                    Gdx.app.log("Socket", "Client : " + clientID + " try to connect");
                    try {
                        out.writeObject("FAILED LOGIN");
                    } catch (Exception e) {}
                    closeClient();
                    return;
                }

                try {
                    userName = new String(data, 2, Math.min(data.length - 2, 12), "UTF-8");
                } catch (Exception e) {
                    userName = new String(data, 2, Math.min(data.length - 2, 12));
                }

                Gdx.app.log("Socket", "Client: " + clientID + ", Name: "+ userName + " : CONNECTED");

                theGame.registerNewPlayer(clientID, userName);
                isRegistered = true;

                try {
                    out.writeObject(new byte[]{Define.SOCK_REQ_DO_INIT_CONNECTION, Define.SOCK_DATA_LOGIN_SUCCESS});
                } catch (Exception e) {}

            } else if (data[0] == Define.SOCK_REQ_CHECK_IS_ALLOWED_TO_ACT) {
                out.writeObject(theGame.isActionAllowed());

            } else if (data[0] == Define.SOCK_REQ_GET_CURRENT_POSITOIN) {
                out.writeObject(theGame.getCurrentPosition(clientID));

            } else if (data[0] == Define.SOCK_REQ_GET_MAP) {
                out.writeObject(theGame.getMap(clientID));

            } else if (data[0] == Define.SOCK_REQ_DO_MOVE) {
                theGame.move(clientID, data[1]);

            } else if (data[0] == Define.SOCK_REQ_DO_SHOOT) {
                theGame.shoot(clientID, data[1]);

            } else if (data[0] == Define.SOCK_REQ_CHECK_IS_GAME_STARTED) {
                out.writeObject(theGame.isGameStarted());

            } else if (data[0] == Define.SOCK_REQ_GET_CURRENT_HPs) {
                byte[] currHP = new byte[3];
                currHP[0] = Define.SOCK_REQ_GET_CURRENT_HPs;
                currHP[1] = (byte)theGame.getCurrentHP(clientID);
                currHP[2] = (byte)theGame.getTankPlayer(clientID).getBase().getActorHP();
                out.writeObject(currHP);
            }
        }
    }
}
