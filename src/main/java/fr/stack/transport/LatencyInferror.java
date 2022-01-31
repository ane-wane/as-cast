package fr.stack.transport;



/**
 * Nothing fancy yet, only based on the distance with a minimum
 * penalty that represents going through hardware.
 */
public class LatencyInferror {
    
    double min = 0;
    
    public LatencyInferror (double min) {
	this.min = min;
    }

    public double infer(double xA, double xB, double yA, double yB) {
	double x = xA - xB;
	double y = yA - yB;
	return min + Math.sqrt(x*x + y*y);
    }
    
}
