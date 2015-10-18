package com.firstmate.android.util;



import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mookie on 12/7/14.
 */
public class TransparentUrlTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    private String url;
    private Paint opacityPaint = new Paint();


    private AssetManager mAssets;

    public TransparentUrlTileProvider(String url,int defaultOpacity){
        this.url = url;
        setOpacity(defaultOpacity);
    }

    public void setOpacity(int opacity){
        int alpha = (int)Math.round(opacity * 2.55);// 2.55 = 255 * 0.1
        opacityPaint.setAlpha(alpha);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        URL tileUrl = getTileUrl(x, y, zoom);

        Tile tile = null;
        ByteArrayOutputStream stream = null;

        try
        {
            Bitmap image = BitmapFactory.decodeStream(tileUrl.openConnection().getInputStream());
            image = adjustOpacity(image);

            stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);

            byte[] byteArray = stream.toByteArray();

            tile = new Tile(TILE_WIDTH, TILE_HEIGHT, byteArray);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(stream != null)
            {
                try
                {
                    stream.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return tile;
    }


    /**
     * Helper method that returns the {@link URL} of the tile image for the given x/y/zoom location.
     *
     * <p>This method assumes the URL string provided in the constructor contains three placeholders for the x-
     * and y-positions as well as the zoom level of the desired tile; <code>{x}</code>, <code>{y}</code>, and
     * <code>{zoom}</code>. An example for an OpenWeatherMap URL would be:
     * http://tile.openweathermap.org/map/precipitation/{zoom}/{x}/{y}.png</p>
     *
     * @param x The x-position of the tile
     * @param y The y-position of the tile
     * @param zoom The zoom level of the tile
     *
     * @return The {@link URL} of the desired tile image
     */
    private URL getTileUrl(int x, int y, int zoom)
    {
        final int ymax = 1 << zoom;
        y = ymax - y - 1;

        String tileUrl = url
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{zoom}", Integer.toString(zoom));

        try
        {
            return new URL(tileUrl);
        }
        catch(MalformedURLException e)
        {
            throw new AssertionError(e);
        }
    }


    private Bitmap adjustOpacity(Bitmap bitmap)
    {
        Bitmap adjustedBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(adjustedBitmap);
        canvas.drawBitmap(bitmap, 0, 0, opacityPaint);

        return adjustedBitmap;
    }
}
