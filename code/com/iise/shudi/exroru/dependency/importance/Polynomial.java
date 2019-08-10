package com.iise.shudi.exroru.dependency.importance;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealVectorFormat;

import java.util.Arrays;

public class Polynomial extends RealVector {

    private static final RealVectorFormat DEFAULT_FORMAT = RealVectorFormat.getInstance();
    private double data[];

    public Polynomial() {
        data = new double[0];
    }

    public Polynomial(int size) {
        data = new double[size];
    }

    public Polynomial(double[] d) {
        data = d.clone();
    }

    public Polynomial(Polynomial p1, Polynomial p2) {
        data = new double[p1.data.length + p2.data.length];
        System.arraycopy(p1.data, 0, data, 0, p1.data.length);
        System.arraycopy(p2.data, 0, data, p1.data.length, p2.data.length);
    }

    public Polynomial(Polynomial p, RealVector v) {
        final int l1 = p.data.length;
        final int l2 = v.getDimension();
        data = new double[l1 + l2];
        System.arraycopy(p.data, 0, data, 0, l1);
        for (int i = 0; i < l2; ++i) {
            data[l1 + i] = v.getEntry(i);
        }
    }

    @Override
    public RealVector append(RealVector v) {
        try {
            return new Polynomial(this, (Polynomial) v);
        } catch (ClassCastException cce) {
            return new Polynomial(this, v);
        }
    }

    public Polynomial append(Polynomial p) {
        return new Polynomial(this, p);
    }

    @Override
    public RealVector append(double in) {
        final double[] out = new double[data.length + 1];
        System.arraycopy(data, 0, out, 0, data.length);
        out[data.length] = in;
        return new Polynomial(out);
    }

    @Override
    public Polynomial copy() {
        return new Polynomial(data);
    }

    @Override
    public Polynomial add(RealVector v) throws DimensionMismatchException {
        if (v instanceof Polynomial) {
            final double[] vData = ((Polynomial) v).data;
            final int dim = vData.length;
            checkVectorDimensions(dim);
            Polynomial result = new Polynomial(dim);
            double[] resultData = result.data;
            for (int i = 0; i < dim; ++i) {
                resultData[i] = data[i] + vData[i];
            }
            return result;
        } else {
            checkVectorDimensions(v);
            double[] out = data.clone();
            for (int i = 0; i < data.length; ++i) {
                out[i] += v.getEntry(i);
            }
            return new Polynomial(out);
        }
    }

    @Override
    public Polynomial subtract(RealVector v) throws DimensionMismatchException {
        if (v instanceof Polynomial) {
            final double[] vData = ((Polynomial) v).data;
            final int dim = vData.length;
            checkVectorDimensions(dim);
            Polynomial result = new Polynomial(dim);
            double[] resultData = result.data;
            for (int i = 0; i < dim; ++i) {
                resultData[i] = data[i] - vData[i];
            }
            return result;
        } else {
            checkVectorDimensions(v);
            double[] out = data.clone();
            for (int i = 0; i < data.length; ++i) {
                out[i] -= v.getEntry(i);
            }
            return new Polynomial(out);
        }
    }

    @Override
    public Polynomial ebeDivide(RealVector v) throws DimensionMismatchException {
        if (v instanceof Polynomial) {
            final double[] vData = ((Polynomial) v).data;
            final int dim = vData.length;
            checkVectorDimensions(dim);
            Polynomial result = new Polynomial(dim);
            double[] resultData = result.data;
            for (int i = 0; i < dim; ++i) {
                resultData[i] = data[i] / vData[i];
            }
            return result;
        } else {
            checkVectorDimensions(v);
            double[] out = data.clone();
            for (int i = 0; i < data.length; ++i) {
                out[i] /= v.getEntry(i);
            }
            return new Polynomial(out);
        }
    }

    @Override
    public Polynomial ebeMultiply(RealVector v)
            throws DimensionMismatchException {
        if (v instanceof Polynomial) {
            final double[] vData = ((Polynomial) v).data;
            final int dim = vData.length;
            checkVectorDimensions(dim);
            Polynomial result = new Polynomial(dim);
            double[] resultData = result.data;
            for (int i = 0; i < dim; ++i) {
                resultData[i] = data[i] * vData[i];
            }
            return result;
        } else {
            checkVectorDimensions(v);
            double[] out = data.clone();
            for (int i = 0; i < data.length; ++i) {
                out[i] *= v.getEntry(i);
            }
            return new Polynomial(out);
        }
    }

    @Override
    public int getDimension() {
        return data.length;
    }

    @Override
    public double getEntry(int index) throws OutOfRangeException {
        try {
            return data[index];
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfRangeException(LocalizedFormats.INDEX, index, 0,
                    getDimension() - 1);
        }
    }

    @Override
    public Polynomial getSubVector(int index, int n)
            throws NotPositiveException, OutOfRangeException {
        if (n < 0) {
            throw new NotPositiveException(
                    LocalizedFormats.NUMBER_OF_ELEMENTS_SHOULD_BE_POSITIVE, n);
        }
        Polynomial out = new Polynomial(n);
        try {
            System.arraycopy(data, index, out.data, 0, n);
        } catch (IndexOutOfBoundsException e) {
            checkIndex(index);
            checkIndex(index + n - 1);
        }
        return out;
    }

    @Override
    public boolean isInfinite() {
        if (isNaN()) {
            return false;
        }

        for (double v : data) {
            if (Double.isInfinite(v)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isNaN() {
        for (double v : data) {
            if (Double.isNaN(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setEntry(int index, double value) throws OutOfRangeException {
        try {
            data[index] = value;
        } catch (IndexOutOfBoundsException e) {
            checkIndex(index);
        }
    }

    @Override
    public void addToEntry(int index, double increment)
            throws OutOfRangeException {
        try {
            data[index] += increment;
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfRangeException(LocalizedFormats.INDEX, index, 0,
                    data.length - 1);
        }
    }

    @Override
    public void setSubVector(int index, RealVector v)
            throws OutOfRangeException {
        if (v instanceof Polynomial) {
            setSubVector(index, ((Polynomial) v).data);
        } else {
            try {
                for (int i = index; i < index + v.getDimension(); ++i) {
                    data[i] = v.getEntry(i - index);
                }
            } catch (IndexOutOfBoundsException e) {
                checkIndex(index);
                checkIndex(index + v.getDimension() - 1);
            }
        }
    }

    public void setSubVector(int index, double[] v) throws OutOfRangeException {
        try {
            System.arraycopy(v, 0, data, index, v.length);
        } catch (IndexOutOfBoundsException e) {
            checkIndex(index);
            checkIndex(index + v.length - 1);
        }
    }

    @Override
    protected void checkVectorDimensions(RealVector v)
            throws DimensionMismatchException {
        checkVectorDimensions(v.getDimension());
    }

    @Override
    protected void checkVectorDimensions(int n)
            throws DimensionMismatchException {
        if (data.length != n) {
            throw new DimensionMismatchException(data.length, n);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] toArray() {
        return data.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return DEFAULT_FORMAT.format(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RealVector)) {
            return false;
        }
        RealVector rhs = (RealVector) other;
        if (this.getDimension() != rhs.getDimension()) {
            return false;
        }
        if (rhs.isNaN()) {
            return this.isNaN();
        }
        double norm = this.getNorm();
        double rhsNorm = rhs.getNorm();
        if (norm == 0 && rhsNorm == 0) {
            return true;
        } else if (norm == 0 || rhsNorm == 0) {
            return false;
        }
        for (int i = 0; i < this.getDimension(); ++i) {
            if (Math.abs(Math.abs(this.getEntry(i) / norm)
                    - Math.abs(rhs.getEntry(i) / rhsNorm)) > 1e-5) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (isNaN()) {
            return 9;
        }
        double[] data = new double[this.getDimension()];
        double norm = this.getNorm();
        if (norm == 0) {
            return Arrays.hashCode(this.toArray());
        }
        boolean neg = (this.getEntry(0) < 0);
        for (int i = 0; i < this.getDimension(); ++i) {
            data[i] = this.getEntry(i) / norm;
            if (neg && data[i] != 0) {
                data[i] = -data[i];
            }
        }
        return Arrays.hashCode(data);
    }

}
