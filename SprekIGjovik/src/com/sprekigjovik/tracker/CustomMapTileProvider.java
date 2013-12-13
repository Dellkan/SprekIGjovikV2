package com.sprekigjovik.tracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

/**
 * Custom class used for getting custom tiles for the google map.
 * @author Jehans
 * @author John
 * @author Martin
 */
public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets;

    public CustomMapTileProvider(AssetManager assets) {
        mAssets = assets;
    }

    /**
     * Gets custom tile 256*256 pixels for given position.
     * @param int x 
     * @param int y 
     * @param int zoom
     * @return image to be used in google map
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
    	//y = fixYCoordinate(y,zoom);
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    /**
     * Reads image from assets
     * @param x
     * @param y
     * @param zoom
     * @return bytes which is generated to a image
     */
	private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {

        	in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

	/**
	 * gets filepath to image in assets.
	 * @param x
	 * @param y
	 * @param zoom
	 * @return filepath to image
	 */
    private String getTileFilename(int x, int y, int zoom) {
        String filePath =  "" + String.valueOf(zoom) + '/' + String.valueOf(x) + '/' + String.valueOf(y) + ".png";
        return filePath;
    }
}