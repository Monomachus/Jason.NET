package jason.asSemantics;


public interface CircumstanceListener {
	public void eventAdded(Event e);
	public void intentionAdded(Intention i);
	public void intentionDropped(Intention i);
}
