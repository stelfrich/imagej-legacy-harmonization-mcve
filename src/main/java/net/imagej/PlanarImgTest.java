/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package net.imagej;

import java.util.ArrayList;

import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.PointOverlay;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * TODO Documentation
 */
@Plugin(type = Command.class,
	menuPath = "Plugins>PlanarImg Test", initializer="run")
public class PlanarImgTest<T extends RealType<T>> extends ContextCommand
{

	@Parameter(label = "Noise", min = "0", max = "50", style = "slider", callback = "noiseChanged")
	private int noise = 30;

	@Parameter
	private OpService opService;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset blueDataset;
	
	@Parameter
	private DisplayService displayService;

	@Parameter
	private DatasetService datasetService;
	
	@Parameter
	private OverlayService overlayService;
	
	private Localizable blueCenter = new Point(200, 200);

	private int radius = 100;

	@SuppressWarnings("unused")
	private ImageDisplay createDisplay;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run() {
		// Create blue image with spot
		Img<UnsignedByteType> blue = opService.create().img(new FinalInterval(new long[] {0, 0}, new long[] {400, 400}), new UnsignedByteType());

		// Add noise
		UnaryComputerOp<UnsignedByteType, UnsignedByteType> noiseOp = (UnaryComputerOp) opService.op(Ops.Filter.AddNoise.class, UnsignedByteType.class, UnsignedByteType.class, 0d, 255d, (double) noise);
		IterableInterval<UnsignedByteType> noisyBlue = opService.map((IterableInterval) blue, noiseOp, new UnsignedByteType());
		Img<UnsignedByteType> noisyBlueCopy = PlanarImgs.unsignedBytes(Intervals.dimensionsAsLongArray(noisyBlue));
		opService.copy().iterableInterval(noisyBlueCopy, noisyBlue);

		// display result
		blueDataset = datasetService.create(noisyBlueCopy);
		createDisplay = (ImageDisplay) displayService.createDisplay("Blue", blueDataset);

		// add some rois
		long width = blueDataset.dimension(0);
		long height = blueDataset.dimension(1);
		
		ArrayList<Overlay> overlays = new ArrayList<>();
		for (int i=0; i<width; i+=10) {
			for (int j=0; j<height; j+=10) {
				PointOverlay point = new PointOverlay(getContext(), new double[] {i, j});
				overlays.add(point);
			}
		}

		overlayService.addOverlays(createDisplay, overlays);
	}

	/**
	 * This main function serves for development purposes. It allows you to run
	 * the plugin immediately out of your integrated development environment
	 * (IDE).
	 *
	 * @param args whatever, it's ignored
	 * @throws Exception
	 */
	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		// invoke the plugin
		ij.command().run(PlanarImgTest.class, true);
	}

	@SuppressWarnings("unused")
	private void noiseChanged() {
		// Create blue image with spot
		Img<UnsignedByteType> blue = opService.create().img(new FinalInterval(new long[] {0, 0}, new long[] {400, 400}), new UnsignedByteType());

		// Add noise
		UnaryComputerOp<UnsignedByteType, UnsignedByteType> noiseOp = (UnaryComputerOp<UnsignedByteType, UnsignedByteType>) opService.op(Ops.Filter.AddNoise.class, UnsignedByteType.class, UnsignedByteType.class, 0d, 255d, (double) noise);
		IterableInterval<UnsignedByteType> noisyBlue = opService.map((IterableInterval<UnsignedByteType>) blue, noiseOp, new UnsignedByteType());
//		Img<UnsignedByteType> noisyBlueCopy = opService.create().img(noisyBlue);
		Img<UnsignedByteType> noisyBlueCopy = PlanarImgs.unsignedBytes(Intervals.dimensionsAsLongArray(noisyBlue)); 
		opService.copy().iterableInterval(noisyBlueCopy, noisyBlue);
		
		Dataset temp = datasetService.create(noisyBlueCopy);
		blueDataset.copyDataFrom(temp);
	}

}
