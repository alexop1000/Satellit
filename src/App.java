import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.*;

import processing.core.*;
import processing.opengl.*;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class App extends PApplet {
    PVector convert(float lat, float lon, float h ) {
        float theta = radians(lat);
        float phi = radians(lon) + PI;
      
        // fix: in OpenGL, y & z axes are flipped from math notation of spherical coordinates
        float x = h * cos(theta) * cos(phi);
        float y = -h * sin(theta);
        float z = -h * cos(theta) * sin(phi);
      
        return new PVector(x, y, z);
    }
    float earthRotation = 0;
    PImage earth;
    JSONObject json;
    PShape sphere;
    PShape satellite;
    static final PVector X_AXIS = new PVector(1, 0, 0);

    // Get a JSON object from the web
    private String GetRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public void settings() {    
        fullScreen(P3D, 2);
        smooth(8);
        earth = loadImage("earth.jpg");//requestImage(App.class.getClassLoader().getResource("earth.jpg").toString());
        try {
            json = JSONObject.parse(GetRequest("https://api.n2yo.com/rest/v1/satellite/positions/25544/41.702/-76.014/0/2/&apiKey=CA7LJE-VCTPJC-E7DE79-4Y5S"));
        } catch (IOException e) {
            e.printStackTrace();
        }
       
    }
    public void draw() {
        fill(255);
        background(0);
        // Set earth.jpg as the image texture
        textureMode(NORMAL);
        // Draw the earth
        pushMatrix();
        sphere = createShape(SPHERE, 200);
        sphere.setTexture(earth);
        sphere.setStroke(false);
        sphere.setShininess(WINDOWS, 5);
        sphere.setSpecular(ARGB, 2);
        translate(width/2, height/2);
        rotateY(earthRotation);
        lights();
        shape(sphere);
        popMatrix();
        fill(255,255,255);
        circle(width/2, height/2, 420);

        JSONArray positions = json.getJSONArray("positions");
        JSONObject object = (JSONObject) positions.get(0);
        float lat = object.getFloat("satlatitude");
        float lon = object.getFloat("satlongitude");
        float h = object.getFloat("sataltitude");
        
        PVector currentPosition = convert(lat, lon, h + 200);
        PVector dir = new PVector(-currentPosition.x, -currentPosition.z, currentPosition.y);
        float xAngle = PVector.angleBetween(X_AXIS, dir);
        PVector rotAxis = X_AXIS.cross(dir);

        pushMatrix();
        satellite = createShape(BOX,20);

        translate(currentPosition.x, currentPosition.y, currentPosition.z);
        rotate(xAngle, rotAxis.x, rotAxis.y, rotAxis.z);
        shape(satellite);
       

        popMatrix();
    }
    public void mouseDragged() {
        earthRotation += radians(mouseX - pmouseX);
    }
    

    
    public static void main(String[] args) {
        PApplet.runSketch(new String[] { "App" }, new App());
    }
}
