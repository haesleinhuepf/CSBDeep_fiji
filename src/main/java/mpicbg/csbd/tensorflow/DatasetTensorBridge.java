package mpicbg.csbd.tensorflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;

import org.tensorflow.framework.TensorShapeProto;

public class DatasetTensorBridge {

	private final Dataset dataset;
	private TensorShapeProto inputTensorShape, outputTensorShape;

	AxisType[] axes = Axes.knownTypes();
	private final Map< AxisType, Integer > axisDataset;
	private final Map< AxisType, Integer > axisTF;
	private final Map< AxisType, Long > axisSize;

	private boolean mappingInitialized = false;

	public DatasetTensorBridge( final Dataset image ) {

		//check if image has unknown dimensions. if yes, assign unused dimensions
		assignUnknownDimensions( image );

		dataset = image;

		axisDataset = new HashMap<>();
		axisTF = new HashMap<>();
		axisSize = new HashMap<>();

		for ( int i = 0; i < axes.length; i++ ) {
			AxisType axis = axes[ i ];
			axisDataset.put( axis, image.dimensionIndex( axis ) );
			axisTF.put( axis, -1 );
			axisSize.put( axis, image.dimension( axis ) );
		}

	}

	private void assignUnknownDimensions( Dataset image ) {

		List< AxisType > unusedAxes = new ArrayList<>();
		List< Integer > unknownIndices = new ArrayList<>();
		for ( int j = 0; j < axes.length; j++ ) {
			boolean knownAxis = false;
			for ( int i = 0; i < image.numDimensions(); i++ ) {
				if ( image.axis( i ).type() == axes[ j ] ) {
					knownAxis = true;
					break;
				}
			}
			if ( !knownAxis ) unusedAxes.add( axes[ j ] );
		}

		for ( int i = 0; i < image.numDimensions(); i++ ) {
			boolean knownAxis = false;
			for ( int j = 0; j < axes.length; j++ ) {
				if ( image.axis( i ).type() == axes[ j ] ) {
					knownAxis = true;
					break;
				}
			}
			if ( !knownAxis ) unknownIndices.add( i );
		}

		for ( int i = 0; i < unknownIndices.size() && i < unusedAxes.size(); i++ ) {
			image.axis( unknownIndices.get( i ) ).setType( unusedAxes.get( i ) );
		}

	}

	public Long getDatasetDimSizeFromTFIndex( final int tfIndex5D ) {
		if ( axisTF.containsValue(
				tfIndex5D ) ) { return axisSize.get( getKeyByValue( axisTF, tfIndex5D ) ); }
		return ( long ) 1;
	}

	public Integer getDatasetDimIndexByTFIndex( final int tfIndex5D ) {
		if ( axisTF.containsValue(
				tfIndex5D ) ) { return axisDataset.get( getKeyByValue( axisTF, tfIndex5D ) ); }
		return null;
	}

	public String getDatasetDimNameByTFIndex( final int tfIndex5D ) {
		if ( axisTF.containsValue(
				tfIndex5D ) ) { return getDatasetDimName( getKeyByValue( axisTF, tfIndex5D ) ); }
		return null;
	}

	public Integer getTfIndexByDatasetDim( final int datasetDim ) {
		if ( axisDataset.containsValue(
				datasetDim ) ) { return axisTF.get( getKeyByValue( axisDataset, datasetDim ) ); }
		return -1;
	}

	public Integer getTfIndexByDimType( final AxisType type ) {
		if ( axisTF.containsKey( type ) ) { return axisTF.get( type ); }
		return -1;
	}

	public AxisType getDimTypeByDatasetDim( final int datasetDim ) {
		if ( axisDataset.containsValue(
				datasetDim ) ) { return getKeyByValue( axisDataset, datasetDim ); }
		return null;
	}

	public boolean isMappingInitialized() {
		return mappingInitialized;
	}

	public void setMappingInitialized( final boolean mappingInitialized ) {
		this.mappingInitialized = mappingInitialized;
	}

	public void setMappingDefaults() {
		System.out.println( "setmappingdefaults" );
		setMappingInitialized( true );
		if ( inputTensorShape.getDimCount() == 5 ) {
			axisTF.put( Axes.TIME, 0 );
			axisTF.put( Axes.Z, 1 );
			axisTF.put( Axes.Y, 2 );
			axisTF.put( Axes.X, 3 );
			axisTF.put( Axes.CHANNEL, 4 );
		} else {
			if ( inputTensorShape.getDimCount() == 4 ) {
				axisTF.put( Axes.unknown(), 0 );
				axisTF.put( Axes.Y, 2 );
				axisTF.put( Axes.X, 3 );
				if ( dataset.dimension( Axes.Z ) > 1 ) {
					axisTF.put( Axes.Z, 1 );
					if ( dataset.dimension( Axes.CHANNEL ) > 1 ) {
						axisTF.put( Axes.CHANNEL, 4 );
					} else {
						axisTF.put( Axes.TIME, 4 );
					}
				} else {
					if ( dataset.dimension( Axes.CHANNEL ) > 1 ) {
						axisTF.put( Axes.CHANNEL, 1 );
						axisTF.put( Axes.TIME, 4 );
					} else {
						axisTF.put( Axes.TIME, 1 );
						axisTF.put( Axes.CHANNEL, 4 );
					}
				}
			}
		}
		printMapping();
	}

	public void setTFMappingByKnownAxesIndex( int tfIndex5D, int knownAxesIndex ) {
		if ( knownAxesIndex < axes.length ) {
			axisTF.put( axes[ knownAxesIndex ], tfIndex5D );
		}
	}

	public void setInputTensorShape( final TensorShapeProto shape ) {
		inputTensorShape = shape;
		System.out.println(
				"DatasetTensorBridge::setInputTensorShape: " + shape.getDimList() + "]" );
	}

	public void setOutputTensorShape( final TensorShapeProto shape ) {
		outputTensorShape = shape;
		System.out.println(
				"DatasetTensorBridge::setOutputTensorShape: " + shape.getDimList() + "]" );
	}

	public TensorShapeProto getAbstractInputTensorShape() {
		return inputTensorShape;
	}

	public TensorShapeProto getAbstractOutputTensorShape() {
		return outputTensorShape;
	}

	public int numDimensions() {
		return axes.length;
	}

	public long getDatasetDimSize( final int knownAxesIndex ) {
		if ( axes.length > knownAxesIndex ) { return axisSize.get(
				axes[ knownAxesIndex ] ); }
		return 1;
	}

	public String getDatasetDimName( final AxisType axis ) {
		return axis.getLabel().substring( 0, 1 );
	}

	public String getDatasetDimName( final int knownAxesIndex ) {
		return getDatasetDimName( axes[ knownAxesIndex ] );
	}

	public boolean complete() {
		return inputTensorShape != null && dataset != null;
	}

	public void printMapping() {
		System.out.println( "--------------" );
		System.out.println( "axisDataset:" + axisDataset.toString() );
		System.out.println( "axisSize:" + axisSize.toString() );
		System.out.println( "axisTF:" + axisTF.toString() );
		System.out.println( "--------------" );
	}

	public void handleDimensionReduction() {
		if ( inputTensorShape.getDimCount() == outputTensorShape.getDimCount() + 1 ) {
			removeZFromMapping();
		}
	}

	public boolean removeZFromMapping() {
		boolean shift = false;
		printMapping();
		if ( axisTF.containsKey( Axes.Z ) ) {
			int tfindex = axisTF.get( Axes.Z );
			if ( tfindex >= 0 ) {
				axisTF.put( Axes.Z, -1 );
				shift = true;
				for ( int i = tfindex + 1; i < numDimensions(); i++ ) {
					if ( axisTF.containsValue( i ) ) {
						axisTF.put( getKeyByValue( axisTF, i ), i - 1 );
					}
				}
			}
		}
		printMapping();
		return shift;
	}

	public static < T, E > Set< T > getKeysByValue( Map< T, E > map, E value ) {
		Set< T > keys = new HashSet<>();
		for ( Entry< T, E > entry : map.entrySet() ) {
			if ( Objects.equals( value, entry.getValue() ) ) {
				keys.add( entry.getKey() );
			}
		}
		return keys;
	}

	public static < T, E > T getKeyByValue( Map< T, E > map, E value ) {
		for ( Entry< T, E > entry : map.entrySet() ) {
			if ( Objects.equals( value, entry.getValue() ) ) { return entry.getKey(); }
		}
		return null;
	}

}