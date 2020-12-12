package com.project.client;

import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.URL;
import java.util.Map;

public class Client {

    public String[] getKey(String key, String url, String requestSources) {
        int responseCode = 404;
        StringBuilder response = new StringBuilder();
        System.out.println("Sending 'GET' request to URL : " + url + "/" + key);
        try {
            HttpURLConnection httpClient =
                    (HttpURLConnection) new URL(url + "/" + key).openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.addRequestProperty("sources", requestSources);
            responseCode = httpClient.getResponseCode();
            System.out.println("Response Code : " + responseCode + " from " + url);

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpClient.getInputStream()))) {

                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                //print result
                System.out.println(response.toString());
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return new String[]{String.valueOf(responseCode), response.toString()};
    }


    public String[] setKey(final String key, final String value, final String endPoint,final String requestOrigin) throws IOException {

        try {
            HttpURLConnection httpClient =
                    (HttpURLConnection) new URL(endPoint + "/" + key).openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.addRequestProperty("sources", requestOrigin);
            httpClient.setDoOutput(true);
            OutputStream os = httpClient.getOutputStream();
            os.write(value.getBytes());
            os.flush();
            os.close();
            StringBuilder response;
            System.out.println("\nSending 'POST' request to URL : " + endPoint);
            int responseCode = httpClient.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            response = new StringBuilder();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpClient.getInputStream()));

            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            //print result
            System.out.println(response.toString());

            return new String[]{String.valueOf(responseCode), response.toString()};
        } catch (Exception e) {
            System.out.println("error to the put call " + endPoint);
            throw new RuntimeException(e.getCause());
        }

    }

    public String[] batchPut(final Map<String, String> kvMap, String endPoint) {
        System.out.println("Sending batch put to " + endPoint + "size : " + kvMap.size());
        int responseCode = 0;
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection httpClient =
                    (HttpURLConnection) new URL(endPoint).openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setDoOutput(true);
            OutputStream os = httpClient.getOutputStream();
            StringBuilder payLoadBuild = new StringBuilder();
            kvMap.forEach((k, v) -> {
                payLoadBuild.append(k + ":" + v + ",");
            });
            String payload = payLoadBuild.substring(0, payLoadBuild.length() - 1);
            os.write(payload.getBytes());
            os.flush();
            os.close();
            System.out.println("\nSending 'POST' request to URL : " + endPoint);
            responseCode = httpClient.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpClient.getInputStream()))) {

                response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                //print result
                System.out.println(response.toString());

            }
        } catch (Exception e) {
            System.out.println("Error doing batch put to " + endPoint);
            throw new RuntimeException();
        }
        return new String[]{String.valueOf(responseCode), response.toString()};
    }

    public String[] healthCheck(String address){
        int responseCode = 404;
        StringBuilder response = new StringBuilder("");
        try {
            HttpURLConnection httpClient =
                    (HttpURLConnection) new URL(address + "/" ).openConnection();
            httpClient.setRequestMethod("GET");
            responseCode = httpClient.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + address + "/");
            System.out.println("Response Code : " + responseCode);

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpClient.getInputStream()))) {

                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                //print result
                System.out.println(response.toString());
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getLocalizedMessage());
        }
        return new String[]{String.valueOf(responseCode), response.toString()};
    }



}
