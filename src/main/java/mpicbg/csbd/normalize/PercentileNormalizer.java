/*-
 * #%L
 * CSBDeep: CNNs for image restoration of fluorescence microscopy.
 * %%
 * Copyright (C) 2017 - 2018 Deborah Schmidt, Florian Jug, Benjamin Wilhelm
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package mpicbg.csbd.normalize;

import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class PercentileNormalizer< T extends RealType< T > & NativeType<T>> implements Normalizer< T > {

	private double[] percentiles = new double[]{3.0, 99.8};
	private float[] destValues = new float[]{0, 1};
	private List<T> resValues;
	private boolean clip = false;

	protected float min;
	protected float max;
	protected float factor;

	@Override
	public float normalize( final T val ) {
		if(clip) {
			if(val.getRealFloat() < min) {
				return min;
			}
			if(val.getRealFloat() > max) {
				return max;
			}
		}
		return (val.getRealFloat() - resValues.get(0).getRealFloat())*factor+min;
	}

	@Override
	public Img< FloatType > normalize(final RandomAccessibleInterval<T> im, OpService opService) {
		HistogramPercentile<T> percentile = new HistogramPercentile<>();
		resValues = percentile.computePercentiles(im, percentiles, opService);
		min = destValues[0];
		max = destValues[1];
		factor = (max - min) / (resValues.get(1).getRealFloat() - resValues.get(0).getRealFloat());
		final Img< FloatType > output = new ArrayImgFactory<FloatType>(new FloatType()).create(im);

		final RandomAccess< T > in = im.randomAccess();
		final Cursor< FloatType > out = output.localizingCursor();
		while ( out.hasNext() ) {
			out.fwd();
			in.setPosition( out );
			out.get().set( normalize( in.get() ) );
		}

		return output;
	}

	@Override
	public void setup(final double[] percentiles, final float[] destValues, boolean clip) {
		assert(percentiles.length == 2);
		assert(destValues.length == 2);
		this.percentiles = percentiles;
		this.destValues = destValues;
		this.clip = clip;
	}

}
