package dxf;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DXFLayerFinder {

    public static String[] findLayers(String filePath) {
        Set<String> layers = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("LAYER")) {
                    while ((line = br.readLine()) != null && !line.trim().equals("0")) {
                        if (line.trim().equals("2")) {
                            layers.add(br.readLine().trim());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return layers.toArray(new String[0]);
    }


}