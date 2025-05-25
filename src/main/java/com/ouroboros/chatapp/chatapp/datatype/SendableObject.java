package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

interface SendableOjbect {
    public void sendObject(BufferedWriter out) throws IOException;
    public void receiveObject(BufferedReader in) throws IOException;
}
