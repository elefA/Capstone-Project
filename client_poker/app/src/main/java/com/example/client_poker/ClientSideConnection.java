package com.example.client_poker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSideConnection {

    private static ClientSideConnection myCSC;
    private Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientSideConnection() {
        System.out.println("---Client---");
        try {
            setS(new Socket("192.168.1.242", 6789));
            //10.0.2.2
            setOut(new ObjectOutputStream(getS().getOutputStream()));
            getOut().flush();
            setIn(new ObjectInputStream(getS().getInputStream()));
            System.out.println("Streams are set up");

            System.out.println("Connected as player ");

        } catch (Exception e) {
            System.out.println("IOException in csc constructor");
        }
    }


    public Socket getS() {
        return s;
    }

    public void setS(Socket s) {
        this.s = s;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
}