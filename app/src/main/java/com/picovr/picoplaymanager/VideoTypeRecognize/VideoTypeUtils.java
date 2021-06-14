package com.picovr.picoplaymanager.VideoTypeRecognize;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by goodman.ye on 2017/7/14.
 */

public class VideoTypeUtils {
	private static final String TAG = "VideoTypeUtils";

	/**
	 * compare the fuzzy values of vertical direction and horizontal direction
	 *
	 * @param bitmap
	 * @return
	 */
	private static Float[] fuzzyCompareImage(Bitmap bitmap) {
		Bitmap zoomBimtap = zoomImage(bitmap, 8, 8);
		List<Bitmap> horizontalBitmaps = ImageSplitter
				.splitHorizontal(zoomBimtap);
		String horizontalSimilarity = FuzzyBitmapCompare
				.similarity(horizontalBitmaps.get(0), horizontalBitmaps.get(1));
		List<Bitmap> verticalBitmaps = ImageSplitter.splitVertical(zoomBimtap);
		String verticalSimilarity = FuzzyBitmapCompare
				.similarity(verticalBitmaps.get(0), verticalBitmaps.get(1));
		Log.i(TAG, "HorizontalSimilarity：" + horizontalSimilarity + "***verticalSimilarity："
				+ verticalSimilarity);
		float horizontalSimilarityFloat = Float.parseFloat(horizontalSimilarity
				.substring(0, horizontalSimilarity.length() - 1));
		float verticalSimilarityFloat = Float.parseFloat(verticalSimilarity
				.substring(0, verticalSimilarity.length() - 1));
		return new Float[] { horizontalSimilarityFloat,
				verticalSimilarityFloat };
	}

	/***
	 * Zoom Image
	 *
	 * @param bgimage   ：source image resource
	 * @param newWidth  ：width after zoom
	 * @param newHeight ：height after zoom
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		//get width and height
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// create Matrix object of source image
		Matrix matrix = new Matrix();
		// compute new width and height
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// zoom
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
	}

	public static int getVideoType(String videoPath) {
		// File video = new File(videoPath);
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(videoPath);
		int height = Integer.parseInt(retriever.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); // video height
		int width = Integer.parseInt(retriever.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); // video width
		Log.d(TAG, "height:" + height + "***width:" + width);
		DecimalFormat df = new DecimalFormat("#.#"); // #.# instead of 0.0
		float radio = Float.parseFloat(df.format((double) width / height).toString()); // double cast + toString added
		Log.d(TAG, "radio = " + radio);
		Bitmap bitmap = retriever.getFrameAtTime();
		Float[] compareResultArray = fuzzyCompareImage(bitmap);
		return getType(radio, compareResultArray[0],
				compareResultArray[1]);
	}

	/**
	 * get video type on the basis of video aspect ratio and result of compare the fuzzy values of vertical direction and horizontal direction
	 * @param radio
	 * @param horizontalCompareResult
	 * @param verticalCompareResult
	 * @return
	 */
	public static int getType(Float radio, Float horizontalCompareResult,
			Float verticalCompareResult) {
		Log.i(TAG,
				"radio = " + radio + "**horizontalCompareResult = "
						+ horizontalCompareResult + "**verticalCompareResult = "
						+ verticalCompareResult);
		if (radio == 1.0) {
			if (verticalCompareResult > Constant.ABSOLUTE_CRITICAL_VALUE
					&& verticalCompareResult
							- horizontalCompareResult > Constant.RELATIVE_CRITICAL_VALUE) {
				//
				 return MovieType.TYPE_3D360_TB;
//				return "360°3D上下";
			} else {
				// 180°
				 return MovieType.TYPE_180;
//				return "180°";
			}
		}
		if (radio == 2.0) {
			if (horizontalCompareResult > Constant.ABSOLUTE_CRITICAL_VALUE
					&& horizontalCompareResult
							- verticalCompareResult > Constant.RELATIVE_CRITICAL_VALUE) {
				// 180°3D left right
				 return MovieType.TYPE_3D180_LR;
//				return "180°3D LR";
			} else if (verticalCompareResult > Constant.ABSOLUTE_CRITICAL_VALUE
					&& verticalCompareResult
							- horizontalCompareResult > Constant.RELATIVE_CRITICAL_VALUE) {
				// 360°3D top bottom;
				 return MovieType.TYPE_3D360_TB;
			} else {
				// 360
				 return MovieType.TYPE_360;
//				return "360°";
			}
		}
		if (radio == 4.0) {
			// 360°3D left right
			 return MovieType.TYPE_3D360_LR;
		}
		if (radio == 0.5) {
			// 180°3D top bottom
			 return MovieType.TYPE_3D180_TB;
		}
		if ((Math.abs(radio - 1.85) <= 0.5)
				&& verticalCompareResult > Constant.ABSOLUTE_CRITICAL_VALUE
				&& verticalCompareResult
						- horizontalCompareResult > Constant.RELATIVE_CRITICAL_VALUE) {
			// 360°3D top bottom;
			 return MovieType.TYPE_3D360_TB;
		}
		if (horizontalCompareResult > Constant.ABSOLUTE_CRITICAL_VALUE
				&& horizontalCompareResult
						- verticalCompareResult > Constant.RELATIVE_CRITICAL_VALUE) {
			// 3D left right
			 return MovieType.TYPE_3D_LR;
		}
		// return "2D";
		 return MovieType.TYPE_2D;
//		return "2D";
	}

}
