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
    PVector PolarToCartesian(float lat, float lon, float h) {
        float theta = radians(lat);
        float phi = radians(lon) + PI;

        // fix: in OpenGL, y & z axes are flipped from math notation of spherical
        // coordinates
        float x = h * cos(theta) * cos(phi);
        float y = -h * sin(theta);
        float z = -h * cos(theta) * sin(phi);

        return new PVector(x, y, z);
    }

    float earthRotation = 0;
    long lastUpdate = 0;
    PVector pos = new PVector(0, 0, 0);
    PImage earth;
    JSONObject json;
    PShape sphere;
    PShape satellite;
    Button updateButton;
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

    private void RefreshJson() {
        try {
            json = JSONObject.parse(GetRequest(
                    "https://api.n2yo.com/rest/v1/satellite/positions/25544/41.702/-76.014/0/2/&apiKey=CA7LJE-VCTPJC-E7DE79-4Y5S"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray positions = json.getJSONArray("positions");
        JSONObject object = (JSONObject) positions.get(0);

        pos = new PVector(object.getFloat("satlatitude"), object.getFloat("satlongitude"),
                object.getFloat("sataltitude"));
        lastUpdate = object.getLong("timestamp");
    }

    public void settings() {
        fullScreen(P3D, 3);
        smooth(8);

        earth = loadImage("earth.jpg");// requestImage(App.class.getClassLoader().getResource("earth.jpg").toString());
        RefreshJson();

        updateButton = new Button(this, this.width / 2f, this.height - 100f, 100f, 50f,
                new String[] { "Updating", "Update" }, new int[] { 230, 0, 0 }, new int[] { 255, 255, 255 },
                new Activate() {
                    public boolean clicked(boolean click) {
                        if (click) {
                            updateButton.selected = true;
                            RefreshJson();
                            updateButton.selected = false;
                        }
                        return false;
                    }
                }, false);
    }

    public void draw() {
        fill(255);
        background(0);
        textureMode(NORMAL);
        rectMode(PApplet.CENTER);
        if (sphere == null) {
            sphere = createShape(SPHERE, 200);
            satellite = createShape(BOX, 20, 20, 20);
        }

        // Draw the earth
        pushMatrix();
        sphere.setTexture(earth);
        sphere.setStroke(false);
        sphere.setShininess(WINDOWS, 5);
        sphere.setSpecular(ARGB, 2);
        translate(width / 2, height / 2);
        rotateY(earthRotation);
        shape(sphere);
        popMatrix();

        // Draw the satellite
        float lat = pos.x;
        float lon = pos.y;
        float h = pos.z;

        PVector currentPos = PolarToCartesian(lat, lon, h);
        PVector dir = new PVector(currentPos.x, currentPos.y, currentPos.z);
        float xAngle = PVector.angleBetween(X_AXIS, dir);
        PVector rotAxis = X_AXIS.cross(dir);

        pushMatrix();

        translate(width / 2, height / 2);
        rotateY(earthRotation);
        translate(currentPos.x, currentPos.y, currentPos.z);
        rotate(xAngle, rotAxis.x, rotAxis.y, rotAxis.z);
        fill(90, 90, 90);
        shape(satellite);
        popMatrix();

        pushMatrix();
        PShape outline = createShape(SPHERE, 250);
        outline.setStroke(false);
        outline.setStrokeWeight(2);
        translate(width / 2, height / 2, -270);
        shape(outline);
        popMatrix();

        fill(255, 255, 255);

        PFont information = createFont("Arial", 14, true);

        textFont(information, 14);
        fill(255);
        textAlign(LEFT);
        text("Current position of ISS (satid 25544). Coordinates of the satellite are :  [" + pos.x + ", " + pos.y
                + ", " + pos.z + "]", 0, 50);

        text("Last update : " + lastUpdate, 0, 70);
        textFont(information, 20);
        fill(255);
        textAlign(CENTER);
        text("Drag mouse to spin the Earth", width / 2, 80);

        updateButton.x = this.width / 2f;
        updateButton.y = this.height - 100f;
        updateButton.Draw();

        // Api calls left (1000 - json.info.transactionscount)
        textFont(information, 14);
        fill(255);
        textAlign(RIGHT);
        text("API calls left : " + (1000 - json.getJSONObject("info").getInt("transactionscount")), this.width / 2 + 60,
                this.height - 140f);
    }

    public void mouseDragged() {
        earthRotation += radians(mouseX - pmouseX);
    }

    public void mouseClicked() {
        updateButton.OnClick();
    }

    public static void main(String[] args) {
        PApplet.runSketch(new String[] { "App" }, new App());
    }
}
