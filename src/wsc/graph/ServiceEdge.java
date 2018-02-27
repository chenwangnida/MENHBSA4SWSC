package wsc.graph;

import java.util.ArrayList;
import java.util.List;

import org.jgraph.graph.DefaultEdge;

public class ServiceEdge extends DefaultEdge implements Cloneable {

	String SourceService;
	String TargetService;
	List<ParamterConn> pConnList = new ArrayList<ParamterConn>();

	// average matching type value from source vertice to target vertice
	double avgmt;

	// average semantic distance value value from source vertice to target
	// vertice
	double avgsdt;

	public ServiceEdge(double avgmt, double avgsdt) {
		super();
		source = this.getSource();
		target =this.getTarget();
		this.avgmt = avgmt;
		this.avgsdt = avgsdt;
	}


	@Override
	public void setSource(Object arg0) {
		// TODO Auto-generated method stub
		super.setSource(arg0);
	}


	@Override
	public void setTarget(Object arg0) {
		// TODO Auto-generated method stub
		super.setTarget(arg0);
	}


	@Override
	public Object getSource() {
		// TODO Auto-generated method stub
		return super.getSource();
	}


	@Override
	public Object getTarget() {
		// TODO Auto-generated method stub
		return super.getTarget();
	}


	public String getTargetService() {
		return TargetService;
	}

	public void setTargetService(String targetService) {
		TargetService = targetService;
	}


	public String getSourceService() {
		return SourceService;
	}

	public void setSourceService(String sourceService) {
		SourceService = sourceService;
	}

	public List<ParamterConn> getpConnList() {
		return pConnList;
	}

	public void setpConnList(List<ParamterConn> pConnList) {
		this.pConnList = pConnList;
	}

	public double getAvgmt() {
		return avgmt;
	}

	public void setAvgmt(double avgmt) {
		this.avgmt = avgmt;
	}

	public double getAvgsdt() {
		return avgsdt;
	}

	public void setAvgsdt(double avgsdt) {
		this.avgsdt = avgsdt;
	}

	@Override
	public ServiceEdge clone() {
		// TODO Auto-generated method stub
		return (ServiceEdge) super.clone();
	}

	@Override
	public String toString() {
		// return this.avgmt+";"+this.avgsdt + super.toString();
		return this.avgmt+";"+this.avgsdt + super.toString();
//		return super.toString();
	}


//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((SourceService == null) ? 0 : SourceService.hashCode());
//		result = prime * result + ((TargetService == null) ? 0 : TargetService.hashCode());
//		long temp;
//		temp = Double.doubleToLongBits(avgmt);
//		result = prime * result + (int) (temp ^ (temp >>> 32));
//		temp = Double.doubleToLongBits(avgsdt);
//		result = prime * result + (int) (temp ^ (temp >>> 32));
//		return result;
//	}
//
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ServiceEdge other = (ServiceEdge) obj;
//		if (SourceService == null) {
//			if (other.SourceService != null)
//				return false;
//		} else if (!SourceService.equals(other.SourceService))
//			return false;
//		if (TargetService == null) {
//			if (other.TargetService != null)
//				return false;
//		} else if (!TargetService.equals(other.TargetService))
//			return false;
//		if (Double.doubleToLongBits(avgmt) != Double.doubleToLongBits(other.avgmt))
//			return false;
//		if (Double.doubleToLongBits(avgsdt) != Double.doubleToLongBits(other.avgsdt))
//			return false;
//		return true;
//	}

}
