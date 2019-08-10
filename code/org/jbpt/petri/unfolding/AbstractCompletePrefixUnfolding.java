package org.jbpt.petri.unfolding;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.order.*;

import java.util.*;


/**
 * (An abstract) implementation of a complete prefix unfloding of a net system.<br/><br/>
 * <p>
 * This class implements techniques described in:
 * - Javier Esparza, Stefan Roemer, Walter Vogler: An Improvement of McMillan's Unfolding Algorithm. Formal Methods in System Design (FMSD) 20(3):285-310 (2002).
 * - Victor Khomenko: Model Checking Based on Prefixes of Petri Net Unfoldings. PhD Thesis. February (2003).
 *
 * @author Artem Polyvyanyy
 */
public class AbstractCompletePrefixUnfolding<BPN extends IBPNode<N>, C extends ICondition<BPN, C, E, F, N, P, T, M>, E extends IEvent<BPN, C, E, F, N, P, T, M>, F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends AbstractBranchingProcess<BPN, C, E, F, N, P, T, M>
        implements ICompletePrefixUnfolding<BPN, C, E, F, N, P, T, M> {

    // setup to use when constructing this complete prefix unfolding
    protected CompletePrefixUnfoldingSetup setup = null;
    // map of cutoff events to corresponding events
    protected Map<E, E> cutoff2corr = new HashMap<E, E>();
    // total order used to construct this complete prefix unfolding
    protected List<T> totalOrderTs = null;
    // adequate order used to construct this complete prefix unfolding
    protected IAdequateOrder<BPN, C, E, F, N, P, T, M> ADEQUATE_ORDER = null;
    // set of possible extensions updates
    private Set<E> UPE = null;

    /**
     * Empty constructor.
     */
    protected AbstractCompletePrefixUnfolding() {
    }

    /**
     * Constructor with default setup.
     *
     * @param sys Net system to construct complete prefix unfolding for.
     */
    public AbstractCompletePrefixUnfolding(INetSystem<F, N, P, T, M> sys) {
        this(sys, new CompletePrefixUnfoldingSetup());
    }

    /**
     * Constructor with specified setup.
     *
     * @param sys   Net system to construct complete prefix unfolding for.
     * @param setup Setup to use when constructing complete prefix unfolding.
     */
    public AbstractCompletePrefixUnfolding(INetSystem<F, N, P, T, M> sys, CompletePrefixUnfoldingSetup setup) {
        super(sys);

        // net system must be different from null
        if (this.sys == null) return;
        // initial branching process must not be empty
        this.constructInitialBranchingProcess();
        if (this.iniBP.isEmpty()) return;

        // initialise
        this.totalOrderTs = new ArrayList<T>(sys.getTransitions());
        this.setup = setup;

        switch (this.setup.ADEQUATE_ORDER) {
            case ESPARZA_FOR_ARBITRARY_SYSTEMS:
                this.ADEQUATE_ORDER = new EsparzaAdequateOrderForArbitrarySystems<BPN, C, E, F, N, P, T, M>();
                break;
            case ESPARZA_FOR_SAFE_SYSTEMS:
                this.ADEQUATE_ORDER = new EsparzaAdequateTotalOrderForSafeSystems<BPN, C, E, F, N, P, T, M>();
                break;
            case MCMILLAN:
                this.ADEQUATE_ORDER = new McMillanAdequateOrder<BPN, C, E, F, N, P, T, M>();
                break;
            case UNFOLDING:
                this.ADEQUATE_ORDER = new UnfoldingAdequateOrder<BPN, C, E, F, N, P, T, M>();
                break;
            default:
                this.ADEQUATE_ORDER = new EsparzaAdequateTotalOrderForSafeSystems<BPN, C, E, F, N, P, T, M>();
                break;
        }

        // construct unfolding
        if (this.setup.SAFE_OPTIMIZATION)
            this.constructSafe();
        else
            this.constructSafe();
    }

    protected void constructSafe() {
        IPossibleExtensions<BPN, C, E, F, N, P, T, M> pe = getInitialPossibleExtensions();    // get possible extensions of the initial branching process
        while (!pe.isEmpty()) {                                        // while extensions exist
            if (this.events.size() >= this.setup.MAX_EVENTS) return;    // track number of events in unfolding
            E e = pe.getMinimal();                                        // event to use for extending unfolding
            pe.remove(e);                                                // remove 'e' from the set of possible extensions

            if (!this.appendEvent(e)) return;                            // add event 'e' to unfolding
            E corr = this.checkCutoffA(e);                                // check if 'e' is a cutoff event
            if (corr != null)
                this.addCutoff(e, corr);                                    // record cutoff
            else
                pe.addAll(this.updatePossibleExtensions(e));            // update the set of possible extensions
        }
    }

    // map a condition to a set of cuts that contain the condition
    //protected Map<C,Collection<ICut<P,T,C,E>>> c2cut = new HashMap<C,Collection<ICut<P,T,C,E>>>();
    // maps of transitions/places to sets of events/conditions (occurrences of transitions/places)
    //protected Map<T,Set<E>> t2es	= new HashMap<T,Set<E>>();
    //protected Map<P,Set<C>> p2cs	= new HashMap<P,Set<C>>();

    /**
     * Construct unfolding.
     * <p>
     * This method closely follows the algorithm described in:
     * Javier Esparza, Stefan Roemer, Walter Vogler: An Improvement of McMillan's Unfolding Algorithm. Formal Methods in System Design (FMSD) 20(3):285-310 (2002).
     */
    /*protected void construct() {
        if (this.sys==null) return;

		// CONSTRUCT INITIAL BRANCHING PROCESS
		M M0 = this.sys.getMarking();
		for (P p : this.sys.getMarking().toMultiSet()) {
			C c = this.createCondition(p,null);
			this.addCondition(c);
			this.initialBranchingProcess.add(c);
		}
		if (!this.addCut(initialBranchingProcess)) return;
		
		//  Event cutoffIni = null; Event corrIni = null;				// for special handling of events that induce initial markings
		
		// CONSTRUCT UNFOLDING
		Set<Event> pe = getPossibleExtensionsA();						// get possible extensions of initial branching process
		while (!pe.isEmpty()) { 										// while extensions exist
			if (this.countEvents>=this.setup.MAX_EVENTS) return;		// track number of events in unfolding
			Event e = this.setup.ADEQUATE_ORDER.getMinimal(pe);			// event to use for extending unfolding
			
			if (!this.overlap(cutoff2corr.keySet(),e.getLocalConfiguration())) {
				if (!this.addEvent(e)) return;							// add event to unfolding
				
				Event corr = this.checkCutoffA(e);						// check for cutoff event
				if (corr!=null) this.addCutoff(e,corr);					// e is cutoff event
				
				// The following functionality is not captured by Esparza's algorithm !!!
				// The code handles situation when there exist a cutoff event which induces initial marking
				// The identification of such cutoff was postponed to the point until second event which induces initial marking is identified
				//if (corrIni == null) {
					//boolean isCutoffIni = e.getLocalConfiguration().getMarking().equals(this.net.getMarking());
					//if (cutoffIni == null && isCutoffIni) cutoffIni = e;
					//else if (cutoffIni != null && corrIni == null && isCutoffIni) {
						//corrIni = e;
						//this.cutoff2corr.put(cutoffIni, corrIni);
					//}
				//}
				
				pe = getPossibleExtensionsA();							// get possible extensions of branching process
			}
			else {
				pe.remove(e);	
			}
		}
	}*/
    protected IPossibleExtensions<BPN, C, E, F, N, P, T, M> getInitialPossibleExtensions() {
        IPossibleExtensions<BPN, C, E, F, N, P, T, M> result = new AbstractPossibleExtensions<BPN, C, E, F, N, P, T, M>(this.ADEQUATE_ORDER);

        for (T t : this.sys.getTransitions()) {
            ICoSet<BPN, C, E, F, N, P, T, M> coset = this.containsPlaces(this.getInitialCut(), this.sys.getPreset(t));

            if (coset != null) { // if there exists such a co-set
                result.add(this.createEvent(t, coset));
            }
        }

        return result;
    }

    private Set<E> updatePossibleExtensions(E e) {
        this.UPE = new HashSet<E>();

        T u = e.getTransition();
        Set<T> upp = new HashSet<T>(this.sys.getPostsetTransitions(this.sys.getPostset(u)));
        Set<P> pu = new HashSet<P>(this.sys.getPreset(u));
        pu.removeAll(this.sys.getPostset(u));
        upp.removeAll(this.sys.getPostsetTransitions(pu));

        for (T t : upp) {
            ICoSet<BPN, C, E, F, N, P, T, M> preset = this.createCoSet();
            for (C b : e.getPostConditions()) {
                if (this.sys.getPreset(t).contains(b.getPlace()))
                    preset.add(b);
            }
            Set<C> C = this.getConcurrentConditions(e);
            this.cover(C, t, preset);
        }

        return this.UPE;
    }

    @SuppressWarnings("unchecked")
    private void cover(Set<C> CC, T t, ICoSet<BPN, C, E, F, N, P, T, M> preset) {
        if (this.sys.getPreset(t).size() == preset.size()) {
            this.UPE.add(this.createEvent(t, preset));
        } else {
            Set<P> pre = new HashSet<P>(this.sys.getPreset(t));
            pre.removeAll(this.getPlaces(preset));
            P p = pre.iterator().next();

            for (C d : CC) {
                // add "!d.isCutoffPost()"
                // by Shudi Wang
                if (!d.isCutoffPost() && d.getPlace().equals(p)) {
                    Set<C> C2 = new HashSet<C>();
                    for (C dd : CC)
                        if (this.areConcurrent((BPN) d, (BPN) dd))
                            C2.add(dd);
                    ICoSet<BPN, C, E, F, N, P, T, M> preset2 = this.createCoSet();
                    preset2.addAll(preset);
                    preset2.add(d);
                    this.cover(C2, t, preset2);
                }
            }
        }
    }

    private Set<P> getPlaces(ICoSet<BPN, C, E, F, N, P, T, M> coset) {
        Set<P> result = new HashSet<P>();

        for (C c : coset)
            result.add(c.getPlace());

        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<C> getConcurrentConditions(E e) {
        Set<C> result = new HashSet<C>();

        for (C c : this.getConditions()) {
            if (this.areConcurrent((BPN) e, (BPN) c))
                result.add(c);
        }

        return result;
    }

    protected ICoSet<BPN, C, E, F, N, P, T, M> containsPlaces(ICoSet<BPN, C, E, F, N, P, T, M> coset, Collection<P> places) {
        ICoSet<BPN, C, E, F, N, P, T, M> result = this.createCoSet();

        for (P p : places) {
            boolean flag = false;
            for (C c : coset) {
                if (c.getPlace().equals(p)) {
                    flag = true;
                    result.add(c);
                    break;
                }
            }
            if (!flag) return null;
        }

        return result;
    }

    protected E checkCutoffA(E cutoff) {
        ILocalConfiguration<BPN, C, E, F, N, P, T, M> lce = cutoff.getLocalConfiguration();

        for (E f : this.getEvents()) {
            if (f.equals(cutoff)) continue;
            ILocalConfiguration<BPN, C, E, F, N, P, T, M> lcf = f.getLocalConfiguration();
            if (lce.getMarking().equals(lcf.getMarking()) && this.ADEQUATE_ORDER.isSmaller(lcf, lce))
                return this.checkCutoffB(cutoff, f); // check cutoff extended conditions
        }

        return null;
    }

    protected E checkCutoffB(E cutoff, E corr) {
        return corr;
    }

    protected void addCutoff(E e, E corr) {
        this.cutoff2corr.put(e, corr);
        // add by Shudi Wang 15/05/26 to create mapping conditions
        while (this.cutoff2corr.containsKey(corr)) {
            corr = this.cutoff2corr.get(corr);
        }
        for (C newC : e.getPostConditions()) {
            newC.setCutoffPost(true);
            for (C oldC : corr.getPostConditions()) {
                if (oldC.getPlace() == newC.getPlace()) {
                    if (oldC.getMappingConditions() == null) {
                        oldC.setMappingConditions(new HashSet<C>());
                        oldC.getMappingConditions().add(oldC);
                    }
                    oldC.getMappingConditions().add(newC);
                    newC.setMappingConditions(oldC.getMappingConditions());
                    newC.setCorrespondingCondition(oldC);
                }
            }
        }
    }

    @Override
    public Set<E> getCutoffEvents() {
        return this.cutoff2corr.keySet();
    }

    @Override
    public boolean isCutoffEvent(E event) {
        return this.cutoff2corr.containsKey(event);
    }

    @Override
    public E getCorrespondingEvent(E event) {
        return this.cutoff2corr.get(event);
    }

    @Override
    public List<T> getTotalOrderOfTransitions() {
        return this.totalOrderTs;
    }

    @Override
    public IOccurrenceNet<BPN, C, E, F, N, P, T, M> getOccurrenceNet() {
        try {
            @SuppressWarnings("unchecked")
            IOccurrenceNet<BPN, C, E, F, N, P, T, M> occ = (IOccurrenceNet<BPN, C, E, F, N, P, T, M>) OccurrenceNet.class.newInstance();
            occ.setCompletePrefixUnfolding(this);
            return occ;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	/*protected Set<Event> getPossibleExtensionsA() {
		Set<Event> result = new HashSet<Event>();
		
		// iterate over all transitions of the originative net
		for (Transition t : this.sys.getTransitions()) {
			// iterate over all places in the preset
			Collection<Place> pre = this.sys.getPreset(t);
			Place p = pre.iterator().next();
			// get cuts that contain conditions that correspond to the place
			Collection<Cut> cuts = this.getCutsWithPlace(p);
			// iterate over cuts
			for (Cut cut : cuts) {
				// get co-set of conditions that correspond to places in the preset (contained in the cut)
				CoSet coset = this.containsPlaces(cut,pre);
				if (coset!=null) { // if there exists such a co-set
					// check if there already exists an event that corresponds to the transition with the preset of conditions which equals to coset 
					boolean flag = false;
					if (t2es.get(t)!=null) {
						for (IEvent e : t2es.get(t)) {
							//if (this.areEqual(e.getPreConditions(),coset)) {
							if (coset.equals(e.getPreConditions())) {
								flag = true;
								break;
							}
						}
					}
					if (!flag) { // we found possible extension !!!
						Event e = new Event(this,t,coset);
						result.add(e);
					}
				}
			}
		}
		
		result.addAll(this.getPossibleExtensionsB(result));
		
		return result;
	}*/
	
	/*protected Set<Event> getPossibleExtensionsB(Set<Event> pe) {
		return new HashSet<Event>();
	}*/	
	
	/*private void updateConcurrency(Cut cut) {
		for (Condition c1 : cut) {
			if (this.co.get(c1)==null) this.co.put(c1, new HashSet<BPNode>());
			Event e1 = c1.getPreEvent();
			if (e1 != null && this.co.get(e1)==null) this.co.put(e1, new HashSet<BPNode>());
			for (Condition c2 : cut) {
				if (this.co.get(c2)==null) this.co.put(c2, new HashSet<BPNode>());
				this.co.get(c1).add(c2);
				
				Event e2 = c2.getPreEvent();
				if (e1!=null && e2!=null && !this.ca.get(e1).contains(e2) && !this.ca.get(e2).contains(e1)) this.co.get(e1).add(e2);
				if (!c1.equals(c2) && e1!=null && !this.ca.get(c2).contains(e1) && !this.ca.get(e1).contains(c2)) {
					this.co.get(c2).add(e1);
					this.co.get(e1).add(c2);
				}
			}
		}
	}*/
	
	

	/*protected Set<Cut> getCutsWithPlace(Place p) {
		Set<Cut> result = new HashSet<Cut>();
		
		Collection<Condition> cs = p2cs.get(p);
		if (cs==null) return result;
		for (ICondition c : cs) {
			Collection<Cut> cuts = c2cut.get(c);
			if (cuts!=null) result.addAll(cuts);	
		}
		
		return result;
	}*/
	
	/*protected boolean contains(Collection<Condition> cs1, Collection<Condition> cs2) {
		for (ICondition c1 : cs2) {
			boolean flag = false;
			for (ICondition c2 : cs1) {
				if (c1.equals(c2)) {
					flag = true;
					break;
				}
			}
			if (!flag) return false;
		}
		
		return true;
	}*/

	/*protected boolean addCut(ICut<N,P,T,C,E> cut) {
		this.updateConcurrency(cut);
		
		Map<Place,Integer> p2i = new HashMap<Place,Integer>();
		
		for (Condition c : cut) {
			// check bound
			Integer i = p2i.get(c.getPlace());
			if (i==null) p2i.put(c.getPlace(),1);
			else {
				if (i == this.setup.MAX_BOUND) return false;
				else p2i.put(c.getPlace(),i+1);
			}
			
			if (c2cut.get(c)!=null) c2cut.get(c).add(cut);
			else {
				Collection<Cut> cuts = new ArrayList<Cut>();
				cuts.add(cut);
				c2cut.put(c,cuts);
			}
		}
		
		return true;
	}*/
	
	/*@Override
	public CompletePrefixUnfoldingSetup getSetup() {
		return this.setup;
	}*/
	 
	
	 
	
	

	
	 
	/*@Override
	public OrderingRelationType getOrderingRelation(BPNode n1, BPNode n2) {
		if (this.areCausal(n1,n2)) return OrderingRelationType.CAUSAL;
		if (this.areInverseCausal(n1,n2)) return OrderingRelationType.INVERSE_CAUSAL;
		if (this.areInConflict(n1,n2)) return OrderingRelationType.CONFLICT;
		return OrderingRelationType.CONCURRENT;
	}*/
	 
	/*@Override
	public IOccurrenceNet getOccurrenceNet() {
		this.occNet = new OccurrenceNet(this); 
		return this.occNet; 
	}*/
	
	/*public void printOrderingRelations() {
		List<BPNode> ns = new ArrayList<BPNode>();
		ns.addAll(this.getConditions());
		ns.addAll(this.getEvents());
		
		System.out.println(" \t");
		for (BPNode n : ns) System.out.print("\t"+n.getName());
		System.out.println();
		
		for (BPNode n1 : ns) {
			System.out.print(n1.getName()+"\t");
			for (BPNode n2 : ns) {
				String rel = "";
				if (this.areCausal(n1,n2)) rel = ">";
				if (this.areInverseCausal(n1,n2)) rel = "<";
				if (this.areConcurrent(n1,n2)) rel = "@";
				if (this.areInConflict(n1,n2)) rel = "#";
				System.out.print(rel + "\t");
			}
			System.out.println();
		}
	}*/
	 
	/*@Override
	public Set<Event> getCutoffEvents() {
		return this.cutoff2corr.keySet();
	}*/
	 
	/*@Override
	public boolean isCutoffEvent(IEvent e) {
		return this.cutoff2corr.containsKey(e);
	}*/
	 
	/*@Override
	public IEvent getCorrespondingEvent(IEvent e) {
		return this.cutoff2corr.get(e);
	}*/

	/*@Override
	public Set<C> getConditions(P place) {
		// TODO Auto-generated method stub
		return null;
	}*/

	/*@Override
	public Set<E> getEvents(T transition) {
		// TODO Auto-generated method stub
		return null;
	}*/

    @SuppressWarnings("unchecked")
    @Override
    public E createEvent(T transition, ICoSet<BPN, C, E, F, N, P, T, M> preConditions) {
        E e = null;
        try {
            e = (E) Event.class.newInstance();
            e.setTransition(transition);
            e.setPreConditions(preConditions);
            e.setCompletePrefixUnfolding(this);
            // Add by Shudi Wang 15/05/26
            for (C c : preConditions) {
                c.getPostE().add(e);
            }
            return e;
        } catch (InstantiationException exception) {
            exception.printStackTrace();
            return e;
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return e;
        }
    }

    @Override
    public boolean isHealthyCutoffEvent(E event) {
        E corr = this.getCorrespondingEvent(event);
        if (corr == null) return false;

        Set<C> ecs = new HashSet<C>(event.getLocalConfiguration().getCut());
        Set<C> ccs = new HashSet<C>(corr.getLocalConfiguration().getCut());

        ecs.removeAll(event.getPostConditions());
        ccs.removeAll(corr.getPostConditions());

        if (ecs.equals(ccs))
            return true;

        return false;
    }

    @Override
    public boolean isProper() {
        for (E e : this.getEvents()) {
            E corr = this.getCorrespondingEvent(e);
            if (corr == null) continue;

            if (!this.isHealthyCutoffEvent(e))
                return false;
        }
        return true;
    }
}
