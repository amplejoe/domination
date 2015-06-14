package net.yura.domination.engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtil
{

	/**
	 * @see net.yura.domination.mapstore.MapChooser#createImage(java.io.InputStream)
	 */
	public static BufferedImage read(InputStream in) throws IOException {
		try {
			BufferedImage img = ImageIO.read(in);
			if (img == null) {
				throw new IOException("ImageIO.read returned null");
			}
			return img;
		} finally {
			try {
				in.close();
			} catch (Throwable th) {
			}
		}
	}
}
