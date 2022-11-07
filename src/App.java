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
    PVector velocity = new PVector(0, 0, 0);
    PVector pos = new PVector(0, 0, 0);
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
       
        JSONArray positions = json.getJSONArray("positions");
        JSONObject object = (JSONObject) positions.get(0);
        
        pos = new PVector(object.getFloat("satlatitude"), object.getFloat("satlongitude"), object.getFloat("sataltitude"));
        
        JSONObject object2 = (JSONObject) positions.get(1);
        PVector nextPos = new PVector(object2.getFloat("satlatitude"), object2.getFloat("satlongitude"), object2.getFloat("sataltitude"));
        velocity = PVector.sub(new PVector(abs(nextPos.x),abs(nextPos.y),abs(nextPos.z)), new PVector(abs(pos.x),abs(pos.y),abs(pos.z)));
      /*  if (pos.x < 0) {
            velocity.x *= -1;
            velocity.y *= -1;
        }
        */
        velocity.mult(100);
        velocity.z = 0;
        println(pos);
        println(velocity);
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

        pos.add(velocity);
        float lat = pos.x;
        float lon = pos.y;
        float h = pos.z;
        
        PVector currentPos = convert(lat, lon, h);
        PVector dir = new PVector(currentPos.x, currentPos.y, currentPos.z);
        float xAngle = PVector.angleBetween(X_AXIS, dir);
        PVector rotAxis = X_AXIS.cross(dir);

        pushMatrix();
        satellite = createShape(BOX,20, 20, 20);

        translate(width/2, height/2);
        translate(currentPos.x, currentPos.y / 10, currentPos.z);
        rotate(xAngle, rotAxis.x, rotAxis.y, rotAxis.z);
        fill(90,90,90);
        shape(satellite);

        popMatrix();
        fill(255,255,255);
        circle(width/2, height/2, 420);
    }
    public void mouseDragged() {
        // earthRotation += radians(mouseX - pmouseX);
    }
    

    
    public static void main(String[] args) {
        PApplet.runSketch(new String[] { "App" }, new App());
    }
}
