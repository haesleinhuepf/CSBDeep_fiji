
package org.csbdeep.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;

import javax.swing.*;

import org.csbdeep.task.Task;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Interval;

public class DatasetHelper {

	static AxisType[] axes = { Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL };

	public static void assignUnknownDimensions(final Dataset image) {

		final List<AxisType> unusedAxes = new ArrayList<>();
		final List<Integer> unknownIndices = new ArrayList<>();
		for (int j = 0; j < axes.length; j++) {
			boolean knownAxis = false;
			for (int i = 0; i < image.numDimensions(); i++) {
				if (image.axis(i).type() == axes[j]) {
					knownAxis = true;
					break;
				}
			}
			if (!knownAxis) unusedAxes.add(axes[j]);
		}

		for (int i = 0; i < image.numDimensions(); i++) {
			boolean knownAxis = false;
			for (int j = 0; j < axes.length; j++) {
				if (image.axis(i).type() == axes[j]) {
					knownAxis = true;
					break;
				}
			}
			if (!knownAxis) unknownIndices.add(i);
		}

		for (int i = 0; i < unknownIndices.size() && i < unusedAxes.size(); i++) {
			image.axis(unknownIndices.get(i)).setType(unusedAxes.get(i));
		}

	}

	public static AxisType[] getDimensionsAllAssigned(final Dataset image) {

		final List<AxisType> unusedAxes = new ArrayList<>();
		final List<Integer> unknownIndices = new ArrayList<>();
		for (int j = 0; j < axes.length; j++) {
			boolean knownAxis = false;
			for (int i = 0; i < image.numDimensions(); i++) {
				if (image.axis(i).type() == axes[j]) {
					knownAxis = true;
					break;
				}
			}
			if (!knownAxis) unusedAxes.add(axes[j]);
		}

		AxisType[] result = new AxisType[image.numDimensions()];
		for (int i = 0; i < image.numDimensions(); i++) {
			result[i] = image.axis(i).type();
			boolean knownAxis = false;
			for (int j = 0; j < axes.length; j++) {
				if (image.axis(i).type() == axes[j]) {
					knownAxis = true;
					break;
				}
			}
			if (!knownAxis) unknownIndices.add(i);
		}

		for (int i = 0; i < unknownIndices.size() && i < unusedAxes.size(); i++) {
			result[unknownIndices.get(i)] = unusedAxes.get(i);
		}

		return result;

	}

	public static boolean validate(final Dataset dataset, final String formatDesc, boolean headless,
	                            final OptionalLong... expectedDims) {
		try {
			return validateThrowingException(dataset, formatDesc, expectedDims);
		} catch (IOException e) {
			if(headless) {
				System.out.println(e.getMessage());
			} else {
				showError(e.getMessage());
			}
			return false;
		}

	}

	protected static void showError(final String errorMsg) {
		JOptionPane.showMessageDialog(null, errorMsg, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static boolean validateThrowingException(final Dataset dataset, final String formatDesc,
		final OptionalLong... expectedDims) throws IOException
	{
		if (dataset.numDimensions() != expectedDims.length) {
			//TODO ugly check if dataset has channel, if it does, ignore it for the dimension check.. fix this
			if (!(dataset.axis(Axes.CHANNEL).isPresent() && dataset.numDimensions()-1 == expectedDims.length)) {
				throw new IOException("Can not process " + dataset.numDimensions() +
						"D images.\nExpected format: " + formatDesc);
			}
		}
		for (int i = 0; i < expectedDims.length; i++) {
			if (expectedDims[i].isPresent() && expectedDims[i].getAsLong() != dataset
				.dimension(i))
			{
				throw new IOException("Can not process image. Dimension " + i +
					" musst be of size " + expectedDims[i].getAsLong() +
					".\nExpected format: " + formatDesc);
			}
		}
		return true;
	}

	public static void logDim(final Task task, final String title,
		final Interval img)
	{
		logDim(task, title, img, false);
	}

	public static void debugDim(final Task task, final String title,
		final Interval img)
	{
		logDim(task, title, img, true);
	}

	private static void logDim(final Task task, final String title,
		final Interval img, boolean debug)
	{
		final long[] dims = new long[img.numDimensions()];
		img.dimensions(dims);
		if (debug) {
			task.debug(title + ": " + Arrays.toString(dims));
		}
		else {
			task.log(title + ": " + Arrays.toString(dims));
		}
	}

}
