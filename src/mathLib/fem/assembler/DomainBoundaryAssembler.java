package mathLib.fem.assembler;

import mathLib.fem.core.DOF;
import mathLib.fem.core.DOFOrder;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.NodeType;
import mathLib.fem.util.container.DOFList;
import mathLib.fem.util.container.ElementList;
import mathLib.fem.weakform.WeakForm;
import mathLib.matrix.algebra.SparseMatrixRowMajor;
import mathLib.matrix.algebra.SparseVectorHashMap;
import mathLib.matrix.algebra.intf.Matrix;
import mathLib.matrix.algebra.intf.Vector;

public class DomainBoundaryAssembler {
	BasicAssembler domainAss;
	BasicAssembler boundaryAss;
	
	Matrix gA;
	Vector gb;
	
	/**
	 * 
	 * @param domainWeakForm
	 * @param boundaryWeakForm
	 */
	public DomainBoundaryAssembler(Mesh mesh, WeakForm domainWeakForm, WeakForm boundaryWeakForm) {
		this.domainAss = new BasicAssembler(mesh, domainWeakForm);
		this.boundaryAss = new BasicAssembler(mesh, boundaryWeakForm);
	}
	
	/**
	 * Assemble local stiff and load on a give element
	 * @param e
	 */
	public void assembleLocal(Element e) {
		// Assemble on domain element
		domainAss.assembleLocal(e);
		// Assemble on boundary element
		for(Element be : e.getBorderElements()) {
			//Check node type
			NodeType nodeType = be.getBorderNodeType();
			if(nodeType == NodeType.Neumann || nodeType == NodeType.Robin) {
				this.boundaryAss.assembleLocal(be);
			}
		}
	}
	
	/**
	 * Assemble stiff matrix and load vector on a given mesh
	 * @param mesh
	 */
	public void assembleGlobal(Mesh mesh) {
		int dim = mesh.getNodeList().size();
		gA = new SparseMatrixRowMajor(dim,dim);
		gb = new SparseVectorHashMap(dim);
		assembleGlobal(mesh, gA, gb);
	}
	
	/**
	 * Assemble stiff matrix and load vector on a given mesh
	 * into parameter stiff and load.
	 * 
	 * Several assemblers can be chained by using this method
	 * to assemble stiff matrix and load vector
	 * 
	 * @param mesh
	 * @param stiff
	 * @param load
	 */
	public void assembleGlobal(Mesh mesh, Matrix stiff, Vector load) {
		ElementList eList = mesh.getElementList();
		
		for(Element e : eList) {
			
			// Assemble locally
			assembleLocal(e);
			
			// Get local-global indexing
			DOFList DOFs = e.getAllDOFList(DOFOrder.NEFV);
			for(int j=0;j<DOFs.size();j++) {
				DOF dofJ = DOFs.at(j+1);
				for(int i=0;i<DOFs.size();i++) {
					DOF dofI = DOFs.at(i+1);
					stiff.add(dofJ.getGlobalIndex(), dofI.getGlobalIndex(), this.domainAss.A[j][i]);
				}
				load.add(dofJ.getGlobalIndex(), this.domainAss.b[j]);
			}
			
			// Use BasicAssembler to assemble boundary elements
			for(Element be : e.getBorderElements()) {
				//Check node type
				NodeType nodeType = be.getBorderNodeType();
				if(nodeType == NodeType.Neumann || nodeType == NodeType.Robin) {
					// Get local-global indexing
					DOFList beDOFs = be.getAllDOFList(DOFOrder.NEFV);
					for(int j=0;j<beDOFs.size();j++) {
						DOF beDOFJ = beDOFs.at(j+1);
						for(int i=0;i<beDOFs.size();i++) {
							DOF beDOFI = beDOFs.at(i+1);
							stiff.add(beDOFJ.getGlobalIndex(), beDOFI.getGlobalIndex(), this.boundaryAss.A[j][i]);
						}
						load.add(beDOFJ.getGlobalIndex(), this.boundaryAss.b[j]);
					}
				}
			}
		}
		//update gA and gb
		this.gA = stiff;
		this.gb = load;
	}
	
	public double[][] getLocalStiffMatrix() {
		return this.domainAss.getLocalStiffMatrix();
	}
	
	public double[] getLocalLoadVector() {
		return this.domainAss.getLocalLoadVector();
	}

	public double[][] getLocalBoundaryStiffMatrix() {
		return this.boundaryAss.getLocalStiffMatrix();
	}
	
	public double[] getLocalBoundaryLoadVector() {
		return this.boundaryAss.getLocalLoadVector();
	}

	public Matrix getGlobalStiffMatrix() {
		return gA;
	}

	public Vector getGlobalLoadVector() {
		return gb;
	}
}
