
package org.csbdeep.tiling;

import java.util.List;
import java.util.stream.Collectors;

import org.csbdeep.task.DefaultTask;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public class DefaultInputTiler<T extends RealType<T>> extends DefaultTask
	implements InputTiler<T>
{

	@Override
	public List<AdvancedTiledView<T>> run(
		final List<RandomAccessibleInterval<T>> input, final AxisType[] axes,
		final Tiling tiling, final Tiling.TilingAction[] tilingActions)
	{

		setStarted();

		final List output = input.stream().map(image -> tiling.preprocess(image,
			axes, tilingActions, this)).collect(Collectors.toList());

		setFinished();

		return output;

	}

}
