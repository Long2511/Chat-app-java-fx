package com.ouroboros.chatapp.chatapp.serverside;

public class Utils {
    public static String readPacket(BufferedReader in) {
        StringBuilder packet = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    break; // End of packet
                }
                packet.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet.toString().trim(); // Return the complete packet as a string
    }
}
