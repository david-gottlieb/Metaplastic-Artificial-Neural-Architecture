package Java.org.network.mana.base_components.sparse;

/**
 * A class that represents a sparse set of values but uses the coordinates of a true InterleavedSparseMatrix, allows
 * for some values to be not interleaved with the other in an InterleavedSparseMatrix without having to increase
 * the memory overhead of copies of the ptr and index arrays.
 *
 * @author Zoë Tosi
 */
public class InterleavedSparseAddOn {

    public final InterleavedSparseMatrix coordMat;

    public double [] values;

    private final int nilFac;


    public InterleavedSparseAddOn(final InterleavedSparseMatrix coordMat, final int nilFac) {
        this.coordMat = coordMat;
        this.nilFac = nilFac;
        values = new double[coordMat.getNnz() * nilFac];
    }

    /**
     * Given some new ordering reorder values (especially for altering the number
     * of datums). -1 in newCoos indicates a new datum in that location in
     * the new values. The length of newCoos is going to be the new length of
     * values divided by the interleaving factor and coordinates in newCoos are
     * in the range [-1 values.length] indicating that the value in that location
     * in values should be in the same place it exists in newCoos in the new values.
     * @param newCoos
     */
    public void rearrange(int[] newCoos) {
        double [] newVals = new double[newCoos.length * nilFac];
        for(int ii=0, n=newCoos.length; ii<n; ++ii) {
            if (newVals[ii] == -1) continue;
            for(int jj=0; jj<nilFac; ++jj) {
                newVals[ii*nilFac + jj] = values[newCoos[ii]*nilFac + jj];
            }
        }
    }

    /**
     * This returns the array of pointers to the major ordered coordinates, size will be
     * same as either how many source neurons or how many target neurons for this synapse
     * matrix depending upon the order type. --edits to the returned array will
     * alter values in the synapse matrix accordingly
     * @return
     */
    public int[] getRawPtrs() {
        return  coordMat.getRawPtrs();
    }

    /**
     * This returns the array of minor coordinates--edits to the returned array will
     * alter values in the synapse matrix accordingly
     * @return
     */
    public int[] getRawOrdIndices() {
        return coordMat.getRawOrdIndices();
    }

    public int getStartIndex(int num) {
        return coordMat.getStartIndex(num, nilFac);
    }

    public int getEndIndex(int num) {
        return coordMat.getEndIndex(num, nilFac);
    }

    public int getInc() {
        return nilFac;
    }

    public void setValue(int baseIndex, double val, int offset) {
        values[baseIndex * nilFac + offset] = val;
    }


    /**
     * adds up the values along the major ordering and accumulates them in the array argument
     * @param su
     * @param offset
     */
    public void accumSums(final double [] su, int offset) {
        for(int ii = 0, n=su.length; ii<n; ++ii) {
            for(int jj=getStartIndex(ii); jj<getEndIndex(ii); ++jj) {
                su[ii] += values[jj*nilFac+offset];
            }
        }
    }
//
//    public int getOffsetMajor() {
//        return coordMat.offsetMajor;
//    }

//    public int getOffsetMinor() {
//        return coordMat.offsetMinor;
//    }
}
