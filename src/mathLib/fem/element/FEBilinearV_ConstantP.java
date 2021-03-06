/**
 * Copyright (c) 2010, nkliuyueming@gmail.com. All rights reserved.
 * 
 * 
 */
package mathLib.fem.element;


import static mathLib.func.symbolic.FMath.*;

import mathLib.fem.core.DOF;
import mathLib.fem.core.Element;
import mathLib.fem.core.Mesh;
import mathLib.fem.core.NodeType;
import mathLib.fem.core.RectAreaCoord;
import mathLib.fem.core.geometry.GeoEntity;
import mathLib.fem.core.intf.CoordTrans;
import mathLib.fem.core.intf.VecFiniteElement;
import mathLib.func.symbolic.basic.FX;
import mathLib.func.symbolic.basic.SpaceVectorFunction;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.func.symbolic.intf.VecMathFunc;

/**
 * 2D Q1/P0 Element
 * -Continuous bilinear velocity
 * -Piecewise constant pressure
 * 
 * * Velocity: Bilinear shape function: SFBilinearLocal2D
 * * 速度：四边形局部???标，???线性函数
 * 
 * 4----3
 * |    |
 * |    |
 * 1----2
 * 
 * NV = NV(r,s) = NV( r(x,y), s(x,y) )
 * NV1 = (1-r)*(1-s)/4
 * NV2 = (1+r)*(1-s)/4
 * NV3 = (1+r)*(1+s)/4
 * NV4 = (1-r)*(1+s)/4
 * 
 * * Pressure: Piecewise constant shape function: SFConstant1
 * * 压强：分片常数型函数
 * NP=1
 * 
 * * 2D vector valued shape functions
 * * 二维???元上的形函数，速度压强共9个自由度：
 * Ni = (u1,u2,p)', i=1,...,9
 * 
 * N1  =  (NV1, 0, 0)'
 * N2  =  (NV2, 0, 0)'
 * N3  =  (NV3, 0, 0)'
 * N4  =  (NV4, 0, 0)'
 * N5  =  (0, NV1, 0)'
 * N6  =  (0, NV2, 0)'
 * N7  =  (0, NV3, 0)'
 * N8 =   (0, NV4, 0)'
 * N9 =   (0, 0, NP)'
 *
 */
public class FEBilinearV_ConstantP implements VecFiniteElement {
	RectAreaCoord coord;
	
	//Construct a function with the coordinate of points in an element as parameters
	String[] argsOrder;
	
	public int nDOFs = 4+4+1;
	VecMathFunc[] shapeFuncs = new VecMathFunc[nDOFs];

	public FEBilinearV_ConstantP() {
		FX x1 = new FX("x1");
		FX x2 = new FX("x2");
		FX x3 = new FX("x3");
		FX x4 = new FX("x4");
		FX y1 = new FX("y1");
		FX y2 = new FX("y2");
		FX y3 = new FX("y3");
		FX y4 = new FX("y4");
		
		this.coord = new RectAreaCoord(x1,x2,x3,x4,y1,y2,y3,y4);
		
		MathFunc r = coord.getCoordR();
		MathFunc s = coord.getCoordS();
		
		this.argsOrder = new String[]{x1,x2,x3,x4,y1,y2,y3,y4,r,s};
		
		MathFunc NV1 = (1-r)*(1-s)/4;
		MathFunc NV2 = (1+r)*(1-s)/4;
		MathFunc NV3 = (1+r)*(1+s)/4;
		MathFunc NV4 = (1-r)*(1+s)/4;

		shapeFuncs[0] = new SpaceVectorFunction(NV1, C0, C0);
		shapeFuncs[1] = new SpaceVectorFunction(NV2, C0, C0);
		shapeFuncs[2] = new SpaceVectorFunction(NV3, C0, C0);
		shapeFuncs[3] = new SpaceVectorFunction(NV4, C0, C0);
		shapeFuncs[4] = new SpaceVectorFunction(C0, NV1, C0);
		shapeFuncs[5] = new SpaceVectorFunction(C0, NV2, C0);
		shapeFuncs[6] = new SpaceVectorFunction(C0, NV3, C0);
		shapeFuncs[7] = new SpaceVectorFunction(C0, NV4, C0);
		shapeFuncs[8] = new SpaceVectorFunction(C0, C0, C1);
	}

	@Override
	public VecMathFunc[] getShapeFunctions() {
		return this.shapeFuncs;
	}

	@Override
	public int getNumberOfDOFs() {
		return this.nDOFs;
	}

	@Override
	public String[] getArgsOrder() {
		return this.argsOrder;
	}
	
	@Override
	public VecFiniteElement getBoundaryFE() {
		return new FELinearV_ConstantPLine2D();
	}

	@Override
	public boolean isDOFCoupled(int idx1, int idx2) {
		if(idx1 == 8 || idx2 == 8)
			return true;
		if(idx1 <= 3 && idx2 >= 4)
			return false;
		if(idx2 <= 3 && idx1 >= 4)
			return false;
		return true;
	}
	
	public int getGlobalIndex(Mesh mesh, Element e, int localIndex) {
		if(localIndex>=1 && localIndex <= 4) {
			return e.vertices().at(localIndex).globalNode().getIndex();
		} else if(localIndex>=5 && localIndex<=8) {
			int nNode = mesh.getNodeList().size();
			return nNode + e.vertices().at(localIndex-4).globalNode().getIndex();
		} else if(localIndex == 9) {
			int nNode = mesh.getNodeList().size();
			return 2*nNode + e.globalIndex;
		} else {
			throw new RuntimeException("local index = "+localIndex+". It should be in 1...9");
		}
	}

	@Override
	public int getTotalNumberOfDOFs(Mesh mesh) {
		int nNode  = mesh.getNodeList().size();
		int nElement = mesh.getElementList().size();
		return 2*nNode+nElement;
	}
	
	public int getNumberOfNOFs(Mesh mesh, int nVVFComponentIndex) {
		int nNode  = mesh.getNodeList().size();
		int nElement = mesh.getElementList().size();
		if(nVVFComponentIndex == 1 || nVVFComponentIndex == 2)
			return 2*nNode;
		else if(nVVFComponentIndex == 3)
			return nElement;
		else
			throw new RuntimeException("nVVFComponentIndex should be 1, 2 or 3");
	}

	@Override
	public int getVVFComponentIndex(int localIndex) {
		if(localIndex >= 1 && localIndex <= 4)
			return 1;
		else if(localIndex >= 5 && localIndex <= 8)
			return 2;
		else if(localIndex == 9)
			return 3;
		else
			throw new RuntimeException("local index should be in the range of [1,"+(shapeFuncs.length+1)+"]");
	}

	@Override
	public CoordTrans getCoordTrans() {
		return this.coord;
	}

	@Override
	public NodeType getDOFType(Element e, int localIndex) {
		if(localIndex >= 1 && localIndex <= 4)
			return e.nodes.at(localIndex).getNodeType(1);
		else if(localIndex >= 5 && localIndex <= 8)
			return e.nodes.at(localIndex-6).getNodeType(2);
		else if(localIndex == 9)
//			return ???;
			return null;
		else
			throw new RuntimeException("local index should be in the range of [1,"+(shapeFuncs.length+1)+"]");
	}

	@Override
	public int getNumberOfDOFs(Mesh mesh, int nVVFComponentIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DOF getDOF(int localIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoEntity getGeoEntity(Element e, int localIndex) {
		// TODO Auto-generated method stub
		return null;
	}
}
