package Controller;

import Model.*;
import common.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DAO {
    private JSONArray resultData = null; //결과값을 받기 위한 변수
    private URL url = null;
    HttpURLConnection conn = null;
    BufferedReader br = null;
    OutputStreamWriter wr = null;
    JSONObject jsonObject;

    public boolean isCorrectID(String id, String pw) {
        String route = "login";
        JSONObject jsonObject = new JSONObject();
        if(id == null || pw == null) return false;
        jsonObject.put(Constants.ID_TXT, id);
        jsonObject.put(Constants.PASSWORD_TXT, pw);

        jsonObject = sendRequest(Constants.BASE_URL + route, jsonObject.toString(), Constants.POST_TXT);

        if(jsonObject == null)
            return false;
        System.out.println("isCorrectID : " + jsonObject.toString());
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    public String getID(String name, String email) {
        String route = "user/find/email";
        JSONObject jsonObject = new JSONObject();
        if(name == null || email == null) return null;
        jsonObject.put(Constants.NAME_TXT, name);
        jsonObject.put(Constants.EMAIL_TXT, email);

        jsonObject = sendRequest(Constants.BASE_URL + route, jsonObject.toString(), Constants.POST_TXT);

        if(jsonObject == null)
            return null;
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT)) {
            jsonObject = (JSONObject) jsonObject.get("user");
            System.out.println(jsonObject.toString());
            return jsonObject.get(Constants.ID_TXT).toString();
        }
        return null;
    }

    public boolean isDuplicateID(String id) {
        String route = "signup/";

        if(id == null) return false;

        jsonObject = sendGet(Constants.BASE_URL + route + id);

        if(jsonObject == null)
            return false;

        System.out.println("isDuplicateID : " + jsonObject.toString());

        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    public boolean signUp(UserDTO user) {
        String router = "signup";
        JSONObject jsonObject = new JSONObject();

        if(user == null) return false;

        jsonObject.put(Constants.ID_TXT, user.getId());
        jsonObject.put(Constants.PASSWORD_TXT, user.getPassword());
        jsonObject.put(Constants.NAME_TXT, user.getName());
        jsonObject.put(Constants.PHONE_TXT, user.getPhone());
        jsonObject.put(Constants.EMAIL_TXT, user.getEmail());

        jsonObject = sendRequest(Constants.BASE_URL + router, jsonObject.toString(), Constants.POST_TXT);

        if(jsonObject == null)
            return false;
        System.out.println("signUp : " + jsonObject.toString());
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    public ArrayList<LectureDTO> getFilterLecture(JSONObject jsonObject) {
        String route = "timetable";
        ArrayList<LectureDTO> lectureData = new ArrayList<LectureDTO>();

        resultData = sendPostAtArray(Constants.BASE_URL + route, jsonObject.toString());

        if(resultData == null || resultData.size() < 1)
            return lectureData;

        System.out.println("getFilterLecture : " + resultData.get(0).toString());

        lectureData = new ArrayList<LectureDTO>();
        for(int i = 0; i < resultData.size(); i++) {
            lectureData.add(new LectureDTO((JSONObject) resultData.get(i)));
        }
        return lectureData;
    }

    public ArrayList<LectureDTO> getMyLecture(UserDTO user) {
        String route = "reg/" + user.getId();
        ArrayList<LectureDTO> lectureData = new ArrayList<LectureDTO>();
        if(user == null) return lectureData;

        resultData = sendGetAtArray(Constants.BASE_URL + route);

        if(resultData == null || resultData.size() < 1)
            return lectureData;

        System.out.println("getMyLecture : " + resultData.get(0).toString());

        lectureData = new ArrayList<LectureDTO>();
        for(int i = 0; i < resultData.size(); i++)
            lectureData.add(new LectureDTO((JSONObject) resultData.get(i)));
        return lectureData;
    }

    public boolean applyLecture(UserDTO user, LectureDTO lecture) {
        String route = "reg";
        JSONObject jsonObject = new JSONObject();
        if(user == null || lecture == null) return false;
        jsonObject.put(Constants.ID_TXT, user.getId());
        jsonObject.put("lecture_no", lecture.getLectureNo());
        System.out.println("applyLecture : " + jsonObject.toString());

        jsonObject = sendRequest(Constants.BASE_URL + route, jsonObject.toString(), Constants.POST_TXT);

        if(jsonObject == null)
            return false;
        System.out.println("applyLecture : " + jsonObject.toString());
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    public boolean deleteLecture(UserDTO user, LectureDTO lecture) {
        String route = "reg";
        JSONObject jsonObject = new JSONObject();
        if(user == null || lecture == null) return false;
        jsonObject.put(Constants.ID_TXT, user.getId());
        jsonObject.put("lecture_no", lecture.getLectureNo());
        System.out.println("deleteLecture : " + jsonObject.toString());

        jsonObject = sendRequest(Constants.BASE_URL + route, jsonObject.toString(), Constants.DELETE_TXT);

        if(jsonObject == null)
            return false;
        System.out.println("deleteLecture : " + jsonObject.toString());
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    public boolean changePassword(UserDTO user, String pw) {
        String route = "user/update";
        JSONObject jsonObject = new JSONObject();
        if(user == null) return false;
        jsonObject.put(Constants.ID_TXT, user.getId());
        jsonObject.put(Constants.PASSWORD_TXT, pw);
        jsonObject.put(Constants.EMAIL_TXT, user.getEmail());
        System.out.println("changePassword : " + jsonObject.toString());

        jsonObject = sendRequest(Constants.BASE_URL + route, jsonObject.toString(), Constants.PUT_TXT);

        if(jsonObject == null)
            return false;
        System.out.println("changePassword : " + jsonObject.toString());
        if(jsonObject.get(Constants.SUCCESS_TXT).equals(Constants.TRUE_TXT))
            return true;
        return false;
    }

    private JSONObject sendRequest(String path, String params, String methodType) {
        JSONObject returnData = null;
        try {
            url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(methodType);
            conn.setRequestProperty("Content-Type", "application/json");

            if(methodType.equals(Constants.POST_TXT) || methodType.equals(Constants.PUT_TXT)) {
                conn.setDoOutput(true);
                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(params);
                wr.flush();
                wr.close();
            }
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            JSONParser parser = new JSONParser();
            returnData = (JSONObject) parser.parse(sb.toString());
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        return returnData;
    }

    private JSONArray sendGetAtArray(String path) {
        JSONArray returnData = null;
        try {
            url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            JSONParser parser = new JSONParser();
            returnData = (JSONArray) parser.parse(sb.toString());
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        return returnData;
    }

    private JSONArray sendPostAtArray(String path, String params) {
        JSONArray returnData = null;
        try {
            url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(params);
            wr.flush();
            wr.close();

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            JSONParser parser = new JSONParser();
            returnData = (JSONArray) parser.parse(sb.toString());
        } catch(Exception e) {
            System.out.println(e.toString());
        }
        return returnData;
    }
}

