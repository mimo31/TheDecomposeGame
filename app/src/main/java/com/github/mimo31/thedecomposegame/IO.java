package com.github.mimo31.thedecomposegame;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Viktor on 1/30/2016.
 * <p>
 * Handles the saving and loading of all static data in the application.
 */
public class IO
{

    /**
     * Saves all static data (the maximum level and the best times).
     *
     * @param context application context
     */
    public static void saveData(Context context)
    {
        File saveFile = getSaveFile(context);
        try
        {
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            DataOutputStream dataOutput = new DataOutputStream(outputStream);

            // write maxLevel
            dataOutput.writeInt(Level.maxLevel);

            // write the best times
            for (int i = 0; i < Level.maxLevel + 1; i++)
            {
                dataOutput.writeInt(Level.bestTimes[i]);
            }

            dataOutput.flush();
            dataOutput.close();
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Loads all static data (the maximum level and the best times). If no save is found, does nothing.
     *
     * @param context application context
     */
    public static void loadData(Context context)
    {
        File saveFile = getSaveFile(context);
        if (saveFile.exists())
        {
            try
            {
                FileInputStream inputStream = new FileInputStream(saveFile);
                DataInputStream dataInput = new DataInputStream(inputStream);

                // read maxLevel
                Level.maxLevel = dataInput.readInt();

                // the maxLevel level is already completed and a next level is available, increment maxLevel
                if (Level.bestTimes[Level.maxLevel] != 0 && Level.maxLevel != Level.levels.length - 1)
                {
                    Level.maxLevel++;
                }

                // read the best times
                for (int i = 0; i < Level.maxLevel + 1; i++)
                {
                    Level.bestTimes[i] = dataInput.readInt();
                }

                dataInput.close();
                inputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param context application context
     * @return the File object pointing to the application's save file (even if the file actually doesn't exist)
     */
    private static File getSaveFile(Context context)
    {
        return new File(context.getFilesDir(), "Save.dat");
    }
}
