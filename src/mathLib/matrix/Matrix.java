package mathLib.matrix;

public final class Matrix {

    int M;             // number of rows
    int N;             // number of columns
    double[][] data;   // M-by-N array

    // create M-by-N matrix of 0's
    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
        for(int i=0; i<M; i++){
        	for(int j=0; j<N; j++){
        		data[i][j] = 0 ;
        	}
        }
    }

    // create matrix based on 2d array --> if data is complex
    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }


    // create matrix based on 1d array --> if data is complex
    public Matrix(double[] data) {
        M = 1; // default is row matrix
        N = data.length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[j];
    }

    public int[] getSize() {
    	return new int[] {M, N} ;
    }

    public int getNumRows() {
    	return M ;
    }

    public int getNumColumns() {
    	return N ;
    }
    
    public double[][] getData() {
    	return this.data ;
    }

    // create and return a random M-by-N matrix with values between 0 and 1
    public static Matrix random(int M, int N) {
        Matrix A = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[i][j] = Math.random() ;
        return A;
    }

    // create and return the N-by-N identity matrix
    public static Matrix identity(int N) {
        Matrix I = new Matrix(N, N);
        for(int i=0; i<N; i++){
        	for(int j=0; j<N; j++){
        		I.data[i][j] = 0 ;
        	}
        }
        for (int i = 0; i < N; i++){
            I.data[i][i] = 1 ;
        }
        return I;
    }

    // create and return the M-by-N constant matrix
    public static Matrix constant(int M, int N, double c) {
        Matrix C = new Matrix(M, N);
        for(int i=0; i<M; i++){
        	for(int j=0; j<N; j++){
        		C.data[i][j] = c ;
        	}
        }
        return C;
    }

    // create and return the M-by-N constant matrix
    public static Matrix diag(double[] var) {
    	int M = var.length ;
        Matrix C = new Matrix(M, M);
        for(int i=0; i<M; i++){
        	C.data[i][i] = var[i] ;
        }
        return C;
    }

    // swap rows i and j
    public void swap(int i, int j) {
        double[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
        Matrix A = new Matrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public Matrix plus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j] ;
        return C;
    }

    // return C = A + a
    public Matrix plus(double a) {
        Matrix A = this;
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + a ;
        return C;
    }

    // return C = A - B
    public Matrix minus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j] ;
        return C;
    }

    // return C = A - a
    public Matrix minus(double a) {
        Matrix A = this;
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - a ;
        return C;
    }

    // does A = B exactly?
    public boolean equals(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // does A = B within a threshold?
    public boolean equals(Matrix B, double tol) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (Math.abs(A.data[i][j]-B.data[i][j]) > tol) return false;
        return true;
    }

    // return C = A * B
    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for(int i=0; i<C.M; i++){
        	for(int j=0; j<C.N; j++){
        		C.data[i][j] = 0 ;
        	}
        }
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] = C.data[i][j] + (A.data[i][k] * B.data[k][j]) ;
        return C;
    }

    // returns multiplication by a double scalar
    public Matrix times(double a) {
        Matrix A = this;
        Matrix C = new Matrix(A.M, A.N);
        for(int i=0; i<C.M; i++){
        	for(int j=0; j<C.N; j++){
        		C.data[i][j] = A.data[i][j] * a ;
        	}
        }
        return C;
    }

    // print matrix to standard output
    public void show() {
        System.out.println(this.toString());
    }


    @Override
	public String toString() {
    	StringBuilder st = new StringBuilder() ;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++){
            	String sign = "" ;
            	String val = "" ;
            	if(data[i][j]>=0) {
            		sign = " " ;
            		val = String.format("%.4f", data[i][j]) ;
            	}
            	else {
            		sign = "-" ;
            		val = String.format("%.4f", Math.abs(data[i][j])) ;
            	}
            	st.append(sign) ;
            	st.append(val) ;
            	st.append("         ") ;
            }
            st.append("\n") ;
        }
        return st ;
	}

	// get the i,j element of the matrix (i=0,1,...  j=0,1,...)
    public double getElement(int i, int j){
    	return this.data[i][j] ;
    }

    // returning all data elements
    public double[][] getElements(){
    	return this.data ;
    }

    // conversion interfaces
    public static Jama.Matrix toJamaMatrix(Matrix A){
    	return new Jama.Matrix(A.data) ;
    }

    // element-wise operations

    public Matrix timesElement(Matrix B){
        Matrix A = this;
        Matrix C = new Matrix(A.M, A.N);
        for(int i=0; i<C.M; i++){
        	for(int j=0; j<C.N; j++){
        		C.data[i][j] = A.data[i][j] * B.data[i][j] ;
        	}
        }
        return C;
    }

    public Matrix divideElement(Matrix B){
        Matrix A = this;
        Matrix C = new Matrix(A.M, A.N);
        for(int i=0; i<C.M; i++){
        	for(int j=0; j<C.N; j++){
        		C.data[i][j] = A.data[i][j] / B.data[i][j] ;
        	}
        }
        return C;
    }

    /**
     * sub-Blocks of the matrix
     */

    public double[] getRow(int row) {
    	int rowSize = N ;
    	double[] selectedRow = new double[rowSize] ;
    	for(int i=0; i<rowSize; i++) {
    		selectedRow[i] = data[row][i] ;
    	}
    	return selectedRow ;
    }

    public double[] getColumn(int column) {
    	int columnSize = M ;
    	double[] selectedColumn = new double[columnSize] ;
    	for(int i=0; i<columnSize; i++) {
    		selectedColumn[i] = data[i][column] ;
    	}
    	return selectedColumn ;
    }

    // ************ operator overloading **********************

 	/**
 	 * Operator overload support: a+b
 	 */
 	public Matrix add(Matrix v) {
 		return this.plus(v) ;
 	}

 	public Matrix addRev(Matrix v) {
 		return v.plus(this) ;
 	}

 	public Matrix add(int v) {
 		return this.plus(v) ;
 	}

 	public Matrix addRev(int v) {
 		return this.plus(v) ;
 	}

 	public Matrix add(long v) {
 		return this.plus(v) ;
 	}

 	public Matrix addRev(long v) {
 		return this.plus(v) ;
 	}

 	public Matrix add(float v) {
 		return this.plus(v) ;
 	}

 	public Matrix addRev(float v) {
 		return this.plus(v) ;
 	}

 	public Matrix add(double v) {
 		return this.plus(v) ;
 	}

 	public Matrix addRev(double v) {
 		return this.plus(v) ;
 	}

 	/**
 	 * Operator overload support: a-b
 	 */
 	public Matrix subtract(Matrix v) {
 		return this.minus(v) ;
 	}

 	public Matrix subtractRev(Matrix v) {
 		return this.times(-1).plus(v) ;
 	}

 	public Matrix subtract(int v) {
 		return this.minus(v) ;
 	}

 	public Matrix subtractRev(int v) {
 		return this.times(-1).plus(v) ;
 	}

 	public Matrix subtract(long v) {
 		return this.minus(v) ;
 	}

 	public Matrix subtractRev(long v) {
 		return this.times(-1).plus(v) ;
 	}

 	public Matrix subtract(float v) {
 		return this.minus(v) ;
 	}

 	public Matrix subtractRev(float v) {
 		return this.times(-1).plus(v) ;
 	}

 	public Matrix subtract(double v) {
 		return this.minus(v) ;
 	}

 	public Matrix subtractRev(double v) {
 		return this.times(-1).plus(v) ;
 	}

 	/**
 	 * Operator overload support: a*b
 	 */
 	public Matrix multiply(Matrix v) {
 		return this.times(v);
 	}

 	public Matrix multiplyRev(Matrix v) {
 		return v.times(this);
 	}

 	public Matrix multiply(int v) {
 		return this.times(v);
 	}

 	public Matrix multiplyRev(int v) {
 		return this.times(v);
 	}

 	public Matrix multiply(long v) {
 		return this.times(v);
 	}

 	public Matrix multiplyRev(long v) {
 		return this.times(v);
 	}

 	public Matrix multiply(float v) {
 		return this.times(v);
 	}

 	public Matrix multiplyRev(float v) {
 		return this.times(v);
 	}

 	public Matrix multiply(double v) {
 		return this.times(v);
 	}

 	public Matrix multiplyRev(double v) {
 		return this.times(v);
 	}

 	/**
 	 * Operator overload support: a/b
 	 */
 	public Matrix divide(Matrix v) {
 		return this.divideElement(v) ;
 	}

 	public Matrix divideRev(Matrix v) {
 		return v.divideElement(this) ;
 	}

 	public Matrix divide(int v) {
 		return this.times(1/v) ;
 	}

 	public Matrix divideRev(int v) {
 		return constant(M, N, v).divideElement(this) ;
 	}

 	public Matrix divide(long v) {
 		return this.times(1/v) ;
 	}

 	public Matrix divideRev(long v) {
 		return constant(M, N, v).divideElement(this) ;
 	}

 	public Matrix divide(float v) {
 		return this.times(1/v) ;
 	}

 	public Matrix divideRev(float v) {
 		return constant(M, N, v).divideElement(this) ;
 	}

 	public Matrix divide(double v) {
 		return this.times(1/v) ;
 	}

 	public Matrix divideRev(double v) {
 		return constant(M, N, v).divideElement(this) ;
 	}

 	/**
 	 * Operator overload support: -a
 	 */
 	public Matrix negate() {
 		return this.times(-1) ;
 	}

 	// for test
 	public static void main(String[] args) {
 		double[][] d = new double[][] {{2.2222, -1.23}, {-5, -2.3656565656}} ;
		Matrix A = new Matrix(d) ;
		A.show();
		System.out.println(A);
	}

}
