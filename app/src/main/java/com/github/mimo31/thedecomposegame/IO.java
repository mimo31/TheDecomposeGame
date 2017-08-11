package com.github.mimo31.thedecomposegame;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Viktor on 1/30/2016.
 */
public class IO {

    public static void saveData(Context context) {
        File saveFile = getSaveFile(context);
        try {
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            DataOutputStream dataOutput = new DataOutputStream(outputStream);
            dataOutput.writeInt(Level.maxLevel);
            for (int i = 0; i < Level.maxLevel + 1; i++) {
                dataOutput.writeInt(Level.bestTimes[i]);
            }
            dataOutput.flush();
            dataOutput.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadData(Context context) {
        File saveFile = getSaveFile(context);
        if (saveFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(saveFile);
                DataInputStream dataInput = new DataInputStream(inputStream);
                Level.maxLevel = dataInput.readInt();
                if (!(Level.maxLevel < Level.levels.length)) {
                    Level.maxLevel = Level.levels.length - 1;
                }
                for (int i = 0; i < Level.maxLevel + 1 ; i++) {
                    Level.bestTimes[i] = dataInput.readInt();
                }
                dataInput.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static File getSaveFile(Context context) {
        return new File(context.getFilesDir(), "Save.dat");
    }
}
