package mathLib.fem.element;

import mathLib.fem.core.DOF;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.shapefun.BilinearV_ConstantP;
import mathLib.fem.util.FutureyeException;

public class FEBilinearV_ConstantPOld implements FiniteElementType {
	protected static BilinearV_ConstantP[] shapeFun = new BilinearV_ConstantP[9];
	protected int nTotalNodes = -1;
	//p自由度计数器
	protected int nDOF_p = -1;
	
	public int getVectorShapeFunctionDim() {
		return 3;
	}
	
	public int getDOFNumOnElement(int vsfDim) {
		if(vsfDim <= 2)
			return 4;
		else
			return 1;
	}
	
	public FEBilinearV_ConstantPOld() {
		for(int i=0;i<9;i++)
			shapeFun[i] = new BilinearV_ConstantP(i+1);
	}
	
	/**
	 * Assign degree of freedom to element
	 * @param e
	 */
	public void assignTo(Element e) {
		if(nTotalNodes == -1 || nDOF_p == -1) {
			FutureyeException ex = new FutureyeException("Call initDOFIndex() first!");
			ex.printStackTrace();
			System.exit(-1);
		}
		//???元结点数
		int nNode = e.nodes.size();
		//Assign shape function to DOF
		for(int j=1;j<=nNode;j++) {
			//Asign shape function to DOF
			DOF dof_u1 = new DOF(
					j,//Local DOF index
					//Global DOF index, take global node index
					e.nodes.at(j).globalIndex,
					shapeFun[j-1]//Shape function 
					         );
			dof_u1.setVVFComponent(1);
			DOF dof_u2 = new DOF(
					nNode+j,//Local DOF index
					//Global DOF index, take this.nTotalNodes + global node index
					this.nTotalNodes+e.nodes.at(j).globalIndex,
					shapeFun[nNode+j-1]//Shape function 
					         );
			dof_u2.setVVFComponent(2);
			e.addNodeDOF(j, dof_u1);
			e.addNodeDOF(j, dof_u2);
		}
		
		//Assign shape function to DOF
		DOF dof = new DOF(
					2*nNode+1, //Local DOF index
					//this.nTotalNodes*2+nDOF_p, //Global DOF index for Pressure
					this.nTotalNodes*2+this.nDOF_p, //Global DOF index for Pressure
					shapeFun[2*nNode] //Shape function 
					);
		this.nDOF_p++;
		dof.setVVFComponent(3);	
		e.addVolumeDOF(dof);
	}
	
	@Override
	public int getDOFNumOnMesh(Mesh mesh, int vsfDim) {
		if(vsfDim<=2)
			return mesh.getNodeList().size();
		else
			return mesh.getElementList().size();
	}

	@Override
	public void initDOFIndexGenerator(Mesh mesh) {
		this.nTotalNodes = mesh.getNodeList().size();
		nDOF_p = 1;
	}	
}
