package com.ouroboros.chatapp.chatapp.serverside;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.*;
import java.security.Key;
import java.util.*;

public class ApiUtils {
    public static String readBody(BufferedReader in) throws IOException {
        String line;
        int contentLength = 0;
        while (!(line = in.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        return new String(body);
    }

    public static void sendResponse(BufferedWriter out, int status, String body) throws IOException {
        out.write("HTTP/1.1 " + status + " \r\n");
        out.write("Content-Type: text/plain\r\n");
        out.write("Access-Control-Allow-Origin: http://localhost:3000\r\n");
        out.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
        out.write("Access-Control-Allow-Headers: Content-Type, Authorization\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    public static String extractJsonField(String json, String field) {
        if (json == null) return null;
        int idx = json.indexOf('"'+field+'"');
        if (idx == -1) return null;
        int start = json.indexOf(':', idx);
        if (start == -1) return null;
        int q1 = json.indexOf('"', start);
        int q2 = json.indexOf('"', q1+1);
        if (q1 == -1 || q2 == -1) return null;
        return json.substring(q1+1, q2);
    }
    public static String getQueryParam(String path, String key) {
        int qIdx = path.indexOf('?');
        if (qIdx == -1) return "";
        String[] params = path.substring(qIdx+1).split("&");
        for (String p : params) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return "";
    }
    public static List<Integer> parseJsonArrayOfInts(String json) {
        List<Integer> ids = new ArrayList<>();
        int arrStart = json.indexOf('[');
        int arrEnd = json.indexOf(']');
        if (arrStart == -1 || arrEnd == -1) return ids;
        String arr = json.substring(arrStart+1, arrEnd);
        for (String s : arr.split(",")) {
            try { ids.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
        }
        return ids;
    }
    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v instanceof String) sb.append("\"").append(v).append("\"");
            else if (v instanceof List) sb.append(v.toString());
            else sb.append(v);
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    public static String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> m : list) {
            if (!first) sb.append(",");
            sb.append(toJson(m));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static final String JWT_SECRET = DatabaseUtils.getEnvVar("JWT_SECRET");
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    public static String createJwtToken(long userId, String username, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valid for 1 day
                .signWith(SECRET_KEY)
                .compact();
    }

    static String handleJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
