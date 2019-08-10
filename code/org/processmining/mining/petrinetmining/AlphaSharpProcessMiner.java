/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.mining.petrinetmining;

import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.ui.*;
import org.processmining.mining.*;
import org.processmining.mining.logabstraction.*;
import cern.colt.list.*;
import cern.colt.matrix.*;
import java.io.*;

public class AlphaSharpProcessMiner
    extends LogRelationBasedAlgorithm
{
    private LogRelations relations;
    private int nme; //Number of model elements
    private int nme_old; //old number of model elements

    public String getName()
    {
        return "Alpha# algorithm plugin";
    }

    public MiningResult mine(LogReader log, LogRelations theRelations, Progress progress)
    {
        PetriNet petrinet;

        if (theRelations == null || progress.isCanceled())
            return null;
        relations = theRelations;
        nme = relations.getLogEvents().size();
        nme_old = nme;

        progress.setMinMax(0, 13);

        progress.setProgress(1);
        if (progress.isCanceled())
            return null;
        //enumate all process instances
        addLoopRelation(log, 0);

        progress.setProgress(2);
        if (progress.isCanceled())
            return null;
        //add begin/end task
        addBeginEnd();

        progress.setProgress(3);
        if (progress.isCanceled())
            return null;
        //find skip/redo/switch tasks
        ArrayList alFalseDep = new ArrayList();
        ArrayList alRedunDep = getFalseDep(alFalseDep);

        progress.setProgress(4);
        if (progress.isCanceled())
            return null;
        //add skip_redo_switch invisible tasks
        ArrayList alInvTask = new ArrayList();
        addSkipRedo(alFalseDep, alRedunDep, alInvTask);

        progress.setProgress(5);
        ArrayList tuples = new ArrayList();
        for (int i = 0; i < nme; i++)
        {
            for (int j = 0; j < nme; j++)
            {
                if (relations.getCausalFollowerMatrix().get(i, j) == 0)
                    continue;

                IntArrayList A = new IntArrayList();
                A.add(i);

                // j is a causal follower of i
                IntArrayList B = new IntArrayList();
                B.add(j);
                // Now, we have a startingpoint to expand the tree,
                // since {i} -> {j}
                ExpandTree(tuples, A, B, 0, 0);
            }
            // In tuples, we now have a collection of ArrayList[2]'s each of which
            // contains information to build the places
        }

        progress.setProgress(6);
        if (progress.isCanceled())
            return null;
        petrinet = new PetriNet();
        // First, we can write all transitions
        for (int i = 0; i < nme; i++)
        {
            LogEvent e = relations.getLogEvents().getEvent(i);
            Transition t = new Transition(e, petrinet);
            petrinet.addTransition(t);
        }

        // Second, we can write all places (check for duplicates)
        progress.setProgress(7);
        if (progress.isCanceled())
            return null;
        RemoveDuplicates(tuples);
        for (int i = 0; i < tuples.size(); i++)
        {
            petrinet.addPlace("p" + i);
        }
        petrinet.addPlace("pstart");
        petrinet.addPlace("pend");

        // Third, we write all arcs not for one loops
        progress.setProgress(8);
        if (progress.isCanceled())
            return null;
        for (int i = 0; i < tuples.size(); i++)
        {
            IntArrayList[] tuple = ( (IntArrayList[]) (tuples.get(i)));
            for (int j = 0; j < tuple[0].size(); j++)
            {
                petrinet.addEdge(petrinet.findRandomTransition(relations.getLogEvents().getEvent(
                    tuple[0].
                    get(j))),
                                 petrinet.findPlace("p" + i));
            }
            for (int j = 0; j < tuple[1].size(); j++)
            {
                petrinet.addEdge(petrinet.findPlace("p" + i),
                                 petrinet.findRandomTransition(relations.getLogEvents().getEvent(
                                     tuple[1].
                                     get(j))));
            }
        }

        progress.setProgress(9);
        if (progress.isCanceled())
            return null;
        for (int i = 0; i < nme; i++)
        {
            if (relations.getStartInfo().get(i) == 0)
                continue;
            petrinet.addEdge(petrinet.findPlace("pstart"),
                             petrinet.findRandomTransition(relations.getLogEvents().getEvent(i)));
        }
        for (int i = 0; i < nme; i++)
        {
            if (relations.getEndInfo().get(i) == 0)
                continue;
            petrinet.addEdge(petrinet.findRandomTransition(relations.getLogEvents().getEvent(i)),
                             petrinet.findPlace("pend"));
        }
        for (int i = nme_old; i < nme; i++)
        {
            Transition t = petrinet.findRandomTransition(relations.getLogEvents().getEvent(i));
            if(i >= nme_old)
                t.setLogEvent(null);
        }

        // Now write clusters.
        progress.setProgress(10);
        if (progress.isCanceled())
            return null;
        petrinet.makeClusters();

        for (int i = relations.getLogEvents().size() - 1; i >= nme_old; i--)
            relations.getLogEvents().remove(i);

        return new PetriNetResult(log, petrinet, this);
    }

    private void addSkipRedo(ArrayList alFalseDep, ArrayList alRedunDep, ArrayList alInvTask)
    {
        //add skip_redo invisible tasks
        Hashtable htSucc = new Hashtable();
        Hashtable htPred = new Hashtable();
        for (int i = 0; i < alFalseDep.size(); i++)
        {
            PredSucc ps = (PredSucc) alFalseDep.get(i);
            //construct all the succ places associated with invisible tasks
            if (htSucc.get("" + ps.getSucc()) == null)
            {
                ArrayList alSuccPred = genInputPlaces(ps.getSucc());
                htSucc.put("" + ps.getSucc(), alSuccPred);
            }
            //construct all the pred places associated with invisible tasks
            if (htPred.get("" + ps.getPred()) == null)
            {
                ArrayList sarPredSucc = genOutputPlaces(ps.getPred());
                htPred.put("" + ps.getPred(), sarPredSucc);
            }
        }
        for (int i = 0; i < alRedunDep.size(); i++)
        {
            PredSucc ps = (PredSucc) alRedunDep.get(i);
            //construct all the succ places associated with invisible tasks
            if (htSucc.get("" + ps.getSucc()) == null)
            {
                ArrayList alSuccPred = genInputPlaces(ps.getSucc());
                htSucc.put("" + ps.getSucc(), alSuccPred);
            }
            //construct all the pred places associated with invisible tasks
            if (htPred.get("" + ps.getPred()) == null)
            {
                ArrayList sarPredSucc = genOutputPlaces(ps.getPred());
                htPred.put("" + ps.getPred(), sarPredSucc);
            }
        }
        int nInvNum = 0;
        //construct all the invsible tasks for normal false dependencies
        for (int i = 0; i < alFalseDep.size(); i++)
        {
            PredSucc ps = (PredSucc) alFalseDep.get(i);
            ArrayList sarPred = (ArrayList) htPred.get("" + ps.getPred());
            ArrayList sarSucc = (ArrayList) htSucc.get("" + ps.getSucc());
            for (int j = 0; j < sarPred.size(); j++)
            {
                DoubleSet dsPred = (DoubleSet) sarPred.get(j);
                for (int k = 0; k < sarSucc.size(); k++)
                {
                    DoubleSet dsSucc = (DoubleSet) sarSucc.get(k);
                    //create a new invisible task
                    IntArrayList sarPredSucc = dsPred.getB();
                    IntArrayList sarSuccPred = dsSucc.getA();
                    IntArrayList sarPredPred = dsPred.getA();
                    IntArrayList sarSuccSucc = dsSucc.getB();
                    //should create a new invisible task
                    if (!existPara(sarPredSucc, sarSuccPred) && !existPara(sarPredPred, sarSuccSucc))
                    {
                        String t = "__skip_redo_" + (nInvNum + 1) + "__";
                        InvisibleTask it = new InvisibleTask(t);
                        it.addPred(dsPred);
                        it.addSucc(dsSucc);
                        //record the new invisible task
                        if (addInvTask(alInvTask, it, alFalseDep))
                        {
                            nInvNum++;
                        }
                    }
                }
            }
        }
        //add invisible tasks for parallel redundant false dependency
        for (int i = 0; i < alRedunDep.size(); i++)
        {
            PredSucc ps = (PredSucc) alRedunDep.get(i);
            ArrayList sarPred = (ArrayList) htPred.get("" + ps.getPred());
            ArrayList sarSucc = (ArrayList) htSucc.get("" + ps.getSucc());
            for (int j = 0; j < sarPred.size(); j++)
            {
                DoubleSet dsPred = (DoubleSet) sarPred.get(j);
                for (int k = 0; k < sarSucc.size(); k++)
                {
                    DoubleSet dsSucc = (DoubleSet) sarSucc.get(k);
                    //create a new invisible task
                    IntArrayList sarPredPred = dsPred.getA();
                    IntArrayList sarPredSucc = dsPred.getB();
                    IntArrayList sarSuccPred = dsSucc.getA();
                    //should create a new invisible task
                    if (!existPara(sarPredSucc, sarSuccPred) && !existPara(sarPredPred, sarSuccPred))
                    {
                        String t = "__skip_redo_" + (nInvNum + 1) + "__";
                        InvisibleTask it = new InvisibleTask(t);
                        it.addPred(dsPred);
                        it.addSucc(dsSucc);
                        //if exist a path between two places dsPred and dsSucc
                        if (!existPath(it, alInvTask))
                        {
                            //record the new invisible task
                            if (addInvTask(alInvTask, it, alFalseDep))
                                nInvNum++;
                        }
                    }
                }
            }
        }
        //add successive relations between invisible tasks and visible tasks
        if (alInvTask.size() > 0)
        {
            int nmeNew = nme + alInvTask.size();
            DoubleMatrix1D dmOneLoop = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmOneLoop.set(i, relations.getOneLengthLoopsInfo().get(i));
            DoubleMatrix2D dmCausal = DoubleFactory2D.sparse.make(nmeNew, nmeNew, 0);
            for (int i = 0; i < nme; i++)
                for (int j = 0; j < nme; j++)
                    dmCausal.set(i, j, relations.getCausalFollowerMatrix().get(i, j));
            DoubleMatrix2D dmParallel = DoubleFactory2D.sparse.make(nmeNew, nmeNew, 0);
            for (int i = 0; i < nme; i++)
                for (int j = 0; j < nme; j++)
                    dmParallel.set(i, j, relations.getParallelMatrix().get(i, j));
            DoubleMatrix1D dmStart = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmStart.set(i, relations.getStartInfo().get(i));
            DoubleMatrix1D dmEnd = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmEnd.set(i, relations.getEndInfo().get(i));
            LogEvents leEvents = relations.getLogEvents();

            //sequence relations between invisible tasks and visible tasks
            for (int i = 0; i < alInvTask.size(); i++)
            {
                InvisibleTask it = (InvisibleTask) alInvTask.get(i);
                String t = it.getName();
                int id = nme + i;
                it.setId(id);

                //add to log events
                LogEvent le = new LogEvent(t, "auto");
                leEvents.add(le);

                //add to relation matrix
                IntArrayList sarPred = it.getPredPred();
                for (int j = 0; j < sarPred.size(); j++)
                    dmCausal.set(sarPred.get(j), id, 1);
                IntArrayList sarSucc = it.getSuccSucc();
                for (int j = 0; j < sarSucc.size(); j++)
                    dmCausal.set(id, sarSucc.get(j), 1);
            }
            //create a totally new log relations object
            nme = nmeNew;
            relations = new LogRelationsImpl(dmCausal, dmParallel, dmEnd, dmStart, dmOneLoop, leEvents);
        }
        //add successive relations between invisible tasks
        for (int i = 0; i < alInvTask.size(); i++)
        {
            InvisibleTask iti = (InvisibleTask) alInvTask.get(i);
            int ti = iti.getId();
            for (int j = 0; j < alInvTask.size(); j++)
            {
                if (i == j)
                    continue;
                InvisibleTask itj = (InvisibleTask) alInvTask.get(j);
                int tj = itj.getId();
                DoubleSet[] sarPred = itj.getPred();
                for (int k = 0; k < sarPred.length; k++)
                {
                    if (iti.containedInSucc(sarPred[k]))
                    {
                        relations.getCausalFollowerMatrix().set(ti, tj, 1);
                        break;
                    }
                }
            }
        }
        //add parallel relations between invisible tasks
        for (int i = 0; i < alInvTask.size(); i++)
        {
            InvisibleTask iti = (InvisibleTask) alInvTask.get(i);
            DoubleSet[] sarPredI = iti.getPred();
            for (int j = i + 1; j < alInvTask.size(); j++)
            {
                InvisibleTask itj = (InvisibleTask) alInvTask.get(j);
                DoubleSet[] sarPredJ = itj.getPred();
                boolean isParaPred = true;
                boolean isParaSucc = true;
                for (int m = 0; m < sarPredI.length; m++)
                {
                    for (int n = 0; n < sarPredJ.length; n++)
                    {
                        if (isParaPred && !existPara(sarPredI[m].getA(), sarPredJ[n].getA()))
                            isParaPred = false;
                        if (isParaSucc && !existPara(sarPredI[m].getB(), sarPredJ[n].getB()))
                            isParaSucc = false;
                    }
                }
                if (isParaPred || isParaSucc)
                {
                    relations.getParallelMatrix().set(iti.id, itj.id, 1);
                    relations.getParallelMatrix().set(itj.id, iti.id, 1);
                }
            }
        }
        //add parallel relations between invisible tasks and visible tasks
        for (int i = 0; i < alInvTask.size(); i++)
        {
            InvisibleTask iti = (InvisibleTask) alInvTask.get(i);
            DoubleSet[] sarPredI = iti.getPred();
            //enumerate all tasks except invisible tasks
            for (int k = 0; k < nme - alInvTask.size(); k++)
            {
                IntArrayList ialT = new IntArrayList();
                ialT.add(k);
                boolean isParaPred = true;
                boolean isParaSucc = true;
                for (int m = 0; m < sarPredI.length; m++)
                {
                    if (isParaPred && !existPara(sarPredI[m].getA(), ialT))
                        isParaPred = false;
                    if (isParaSucc && !existPara(sarPredI[m].getB(), ialT))
                        isParaSucc = false;
                }
                if (isParaPred || isParaSucc)
                {
                    relations.getParallelMatrix().set(iti.id, k, 1);
                    relations.getParallelMatrix().set(k, iti.id, 1);
                }
            }
        }
    }

    private boolean existPath(InvisibleTask it, ArrayList alInvTask)
    {
        DoubleSet dsPred = it.getPred()[0];
        DoubleSet dsSucc = it.getSucc()[0];

        //test whether there is a path from dsPred to dsSucc
        ArrayList alPred = new ArrayList();
        alPred.add(dsPred);
        //record all the visited start place
        ArrayList alVisited = new ArrayList();
        while (alPred.size() != 0)
        {
            dsPred = (DoubleSet) alPred.remove(0);
            alVisited.add(dsPred);
            for (int i = 0; i < alInvTask.size(); i++)
            {
                InvisibleTask iti = (InvisibleTask) alInvTask.get(i);
                if (iti.containedInPred(dsPred))
                {
                    if (iti.containedInSucc(dsSucc))
                        return true;
                    DoubleSet[] sarSucc = iti.getSucc();
                    for (int j = 0; j < sarSucc.length; j++)
                        if (!alVisited.contains(sarSucc[j]))
                            alPred.add(sarSucc[j]);
                }
            }
        }

        return false;
    }

    private ArrayList genInputPlaces(int ith)
    {
        ArrayList tuples = new ArrayList();
        //get all predecessors
        IntArrayList ialPred = getPred(ith, false);
        //enumerate all the relevant causual dependencies
        for (int i = 0; i < ialPred.size(); i++)
        {
            int pred = ialPred.get(i);
            IntArrayList ialSucc = getSucc(pred, false);
            for (int j = 0; j < ialSucc.size(); j++)
            {
                int succ = ialSucc.get(j);
                IntArrayList A = new IntArrayList();
                A.add(pred);
                IntArrayList B = new IntArrayList();
                B.add(succ);
                ExpandTree(tuples, A, B, 0, 0);
            }
        }

        return tuple2Place(ith, tuples, 1);
    }

    private ArrayList genOutputPlaces(int ith)
    {
        ArrayList tuples = new ArrayList();
        //get all successors
        IntArrayList ialSucc = getSucc(ith, false);
        //enumerate all the relevant causual dependencies
        for (int i = 0; i < ialSucc.size(); i++)
        {
            int succ = ialSucc.get(i);
            IntArrayList ialPred = getPred(succ, false);
            for (int j = 0; j < ialPred.size(); j++)
            {
                int pred = ialPred.get(j);
                IntArrayList A = new IntArrayList();
                A.add(pred);
                IntArrayList B = new IntArrayList();
                B.add(succ);
                ExpandTree(tuples, A, B, 0, 0);
            }
        }

        return tuple2Place(ith, tuples, 0);
    }

    private ArrayList tuple2Place(int ith, ArrayList tuples, int pos)
    {
        RemoveDuplicates(tuples);
        ArrayList alPlaces = new ArrayList();
        for (int i = 0; i < tuples.size(); i++)
        {
            IntArrayList[] place = (IntArrayList[]) tuples.get(i);
            if (!place[pos].contains(ith))
                continue;
            DoubleSet ds = new DoubleSet();
            ds.addToA(place[0]);
            ds.addToB(place[1]);
            alPlaces.add(ds);
        }
        if(tuples.size() == 0 && pos == 0)//successor
        {
            DoubleSet ds = new DoubleSet();
            ds.addToA(ith);
            alPlaces.add(ds);
        }
        else if(tuples.size() ==0 && pos == 1)//predecessor
        {
            DoubleSet ds = new DoubleSet();
            ds.addToB(ith);
            alPlaces.add(ds);
        }

        return alPlaces;
    }

    private ArrayList getFalseDep(ArrayList alFalseDep)
    {
        //add skip/redo/switch tasks
        for (int i = 0; i < nme; i++)
        {
            //get all its predecessors
            IntArrayList ialPred = getPred(i, false);
            if (ialPred.size() <= 1)
                continue;
            //enumerate all its predecessors
            for (int j = 0; j < ialPred.size(); j++)
            {
                int tj = ialPred.get(j);
                boolean isDone = false;
                for (int k = 0; k < ialPred.size() && !isDone; k++)
                {
                    //skip the same task
                    if( k == j)
                        continue;
                    int tk = ialPred.get(k);
                    //if tj --> t, tk \> t, i\|| t, there is an inv task from tj to i
                    IntArrayList ialSuccTJ = getSucc(tj, false);
                    IntArrayList ialSuccTK = getSucc(tk, false);
                    ialSuccTJ.removeAll(ialSuccTK);
                    for(int m=0; m<ialSuccTJ.size() && !isDone; m++)
                    {
                        int t = ialSuccTJ.get(m);
                        //find one
                        if(relations.getCausalFollowerMatrix().get(tk, t) == 0 &&
                           relations.getParallelMatrix().get(tk, t) == 0 &&
                           relations.getParallelMatrix().get(t, i) == 0)
                        {
                            PredSucc ps = new PredSucc(tj, i);
                            if (!alFalseDep.contains(ps))
                                alFalseDep.add(ps);
                            isDone = true;
                        }
                    }
                }
            }
        }
        //remove all false dependencies due to skip/redo/switch invisible tasks
        for (int i = 0; i < alFalseDep.size(); i++)
        {
            PredSucc ps = (PredSucc) alFalseDep.get(i);
            relations.getCausalFollowerMatrix().set(ps.getPred(), ps.getSucc(), 0);
            if(ps.pred == ps.succ)
            {
                relations.getOneLengthLoopsInfo().set(ps.pred, 0);
                relations.getParallelMatrix().set(ps.pred, ps.pred, 0);
            }
        }
        //identify all the redundant false dependencies
        ArrayList alRedunDep = new ArrayList();
        for (int i = 0; i < alFalseDep.size(); i++)
        {
            PredSucc psi = (PredSucc) alFalseDep.get(i);
            IntArrayList ialPred = getPred(psi.getSucc(), false);
            for (int j = 0; j < ialPred.size(); j++)
            {
                int tj = ialPred.get(j);
                for (int k = 0; k < alFalseDep.size(); k++)
                {
                    PredSucc psk = (PredSucc) alFalseDep.get(k);
                    if (tj == psk.getPred())
                    {
                        PredSucc psNew = new PredSucc(psi.getPred(), psk.getSucc());
                        if (!alRedunDep.contains(psNew))
                        {
                            alRedunDep.add(psNew);
                        }
                    }
                }
                for (int k = alRedunDep.size() - 1; k >= 0; k--)
                {
                    PredSucc psk = (PredSucc) alRedunDep.get(k);
                    if (tj == psk.getPred())
                    {
                        PredSucc psNew = new PredSucc(psi.getPred(), psk.getSucc());
                        if (!alRedunDep.contains(psNew))
                        {
                            alRedunDep.add(psNew);
                        }
                    }
                }
            }
        }
        //remove all the redundant false dependencies
        alFalseDep.removeAll(alRedunDep);

        //fix parallel matrix about length-one-loop tasks
        for(int i=0; i<nme; i++)
        {
            if(relations.getOneLengthLoopsInfo().get(i) > 0)
                relations.getParallelMatrix().set(i, i, 0);
        }

        return alRedunDep;
    }

    private void addLoopRelation(LogReader log, int minValue)
    {
        LogAbstraction abstraction = new LogAbstractionImpl(log);
        DoubleMatrix2D directSuccession, twoStepCloseIn;

		try {
			directSuccession = abstraction.getFollowerInfo(1);
			twoStepCloseIn = abstraction.getCloseInInfo(2);
			// First, build causal relations
			for (int i = 0; i < nme; i++) {
				for (int j = 0; j < nme; j++) {
					if (i == j) {
						continue;
					}
					if (relations.getOneLengthLoopsInfo().get(i) == 0 &&
							relations.getOneLengthLoopsInfo().get(j) == 0) {
						continue;
					}
					// No loop of length two:
					if ((directSuccession.get(i, j) > minValue) &&
							(directSuccession.get(j, i) <= minValue)) {
						relations.getCausalFollowerMatrix().set(i, j, 1);
					}
					// Loop of length two:
					if ((directSuccession.get(i, j) > minValue) &&
							(directSuccession.get(j, i) > minValue) &&
							((twoStepCloseIn.get(i, j) > 0) && (twoStepCloseIn.get(j, i) > 0))) {
						relations.getCausalFollowerMatrix().set(i, j, 1);
					}
				}
			}
			// Now, rebuild parallel relations
			for (int i = 0; i < nme; i++) {
				for (int j = 0; j < nme; j++) {
					if (i == j) {
						continue;
					}
					if (relations.getOneLengthLoopsInfo().get(i) == 0 &&
							relations.getOneLengthLoopsInfo().get(j) == 0) {
						continue;
					}
					//clear to 0
					relations.getParallelMatrix().set(i, j, 0);
					//reset the parallel relation
					if ((directSuccession.get(i, j) > minValue) &&
							(directSuccession.get(j, i) > minValue) &&
							((twoStepCloseIn.get(i, j) == 0) || (twoStepCloseIn.get(j, i) == 0))) {
						if (relations.getOneLengthLoopsInfo().get(i) > 0 &&
								relations.getOneLengthLoopsInfo().get(j) > 0) {
							relations.getCausalFollowerMatrix().set(i, j, 1);
						} else {
							relations.getParallelMatrix().set(i, j, 1);
						}
					}
				}
			}
			//fix causal relation
			for (int i = 0; i < nme; i++) {
				for (int j = 0; j < nme; j++) {
					if (i == j) {
						continue;
					}
					if ((directSuccession.get(i, j) > minValue) &&
							(directSuccession.get(j, i) > minValue) &&
							((twoStepCloseIn.get(i, j) > 0) && (twoStepCloseIn.get(j, i) > 0))) {
						if (relations.getOneLengthLoopsInfo().get(i) > 0 &&
								relations.getOneLengthLoopsInfo().get(j) > 0) {
							boolean isParallel = false;
							IntArrayList alSucc = getSucc(i, false);
							for (int k = 0; k < alSucc.size(); k++) {
								if (j != alSucc.get(k) && relations.getParallelMatrix().get(j,
										alSucc.get(k)) > 0) {
									isParallel = true;
									break;
								}
							}
							if (isParallel) {
								relations.getCausalFollowerMatrix().set(i, j, 0);
								relations.getParallelMatrix().set(i, j, 1);

								continue;
							}
							IntArrayList alPred = getPred(i, false);
							for (int k = 0; k < alPred.size(); k++) {
								if (alPred.get(k) != j &&
										relations.getParallelMatrix().get(alPred.get(k), j) > 0) {
									isParallel = true;
									break;
								}
							}
							if (isParallel) {
								relations.getCausalFollowerMatrix().set(i, j, 0);
								relations.getParallelMatrix().set(i, j, 1);
							}
						}
					}
				}
			}

			DoubleMatrix1D startInfo = abstraction.getStartInfo();
			DoubleMatrix1D endInfo = abstraction.getEndInfo();
			for (int i = 0; i < nme; i++) {
				if (relations.getOneLengthLoopsInfo().get(i) > 0) {
					relations.getCausalFollowerMatrix().set(i, i, 1);
				}

				//start tasks
				if (startInfo.get(i) > 0) {
					relations.getStartInfo().set(i, startInfo.get(i));
				}
				//end tasks
				if (endInfo.get(i) > 0) {
					relations.getEndInfo().set(i, endInfo.get(i));
				}
			}
		} catch (IOException ex) {
		}
    }

    private void addBeginEnd()
    {
        DoubleMatrix1D startInfo = relations.getStartInfo();
        DoubleMatrix1D endInfo = relations.getEndInfo();
        DoubleMatrix1D loopInfo = relations.getOneLengthLoopsInfo();
        DoubleMatrix2D causalInfo = relations.getCausalFollowerMatrix();
        DoubleMatrix2D parallelInfo = relations.getParallelMatrix();

        ArrayList alFirstGroup = new ArrayList();
        for (int i = 0; i < nme; i++)
        {
            if (startInfo.get(i) == 0)
                continue;
            IntArrayList ialPred = new IntArrayList();
            for(int j=0; j<nme; j++)
            {
                if(j == i)
                    continue;
                if(causalInfo.get(j, i) > 0 || parallelInfo.get(j, i) > 0)
                    ialPred.add(j);
            }
            if (loopInfo.get(i) > 0 && !ialPred.contains(i))
                ialPred.add(i);
            if(ialPred.size() == 0)
                continue;
            boolean isFound = false;
            for (int j = 0; j < alFirstGroup.size(); j++)
            {
                DoubleSet ds = (DoubleSet) alFirstGroup.get(j);
                if (ds.equalsA(ialPred) || existPara(ds.alA, ialPred))
                {
                    ds.addToB(i);
                    isFound = true;
                    break;
                }
            }
            if (!isFound)
            {
                DoubleSet ds = new DoubleSet();
                ds.addToA(ialPred);
                ds.addToB(i);
                alFirstGroup.add(ds);
            }
        }
        ArrayList alLastGroup = new ArrayList();
        for (int i = 0; i < nme; i++)
        {
            if(endInfo.get(i) == 0)
                continue;
            IntArrayList ialSucc = new IntArrayList();
            for(int j=0; j<nme; j++)
            {
                if(j == i)
                    continue;
                if(causalInfo.get(i, j) > 0 || parallelInfo.get(i, j) > 0)
                    ialSucc.add(j);
            }
            if (loopInfo.get(i) > 0 && !ialSucc.contains(i))
                ialSucc.add(i);
            if(ialSucc.size() == 0)
                continue;
            boolean isFound = false;
            for (int j = 0; j < alLastGroup.size(); j++)
            {
                DoubleSet ds = (DoubleSet) alLastGroup.get(j);
                if (ds.equalsB(ialSucc) || existPara(ds.alB, ialSucc))
                {
                    ds.addToA(i);
                    isFound = true;
                    break;
                }
            }
            if (!isFound)
            {
                DoubleSet ds = new DoubleSet();
                ds.addToA(i);
                ds.addToB(ialSucc);
                alLastGroup.add(ds);
            }
        }
        //add invisible tasks of begin/end type
        if (alFirstGroup.size() > 0 || alLastGroup.size() > 0)
        {
            int nmeNew = nme + alFirstGroup.size() + alLastGroup.size();
            DoubleMatrix1D dmOneLoop = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmOneLoop.set(i, relations.getOneLengthLoopsInfo().get(i));
            DoubleMatrix2D dmCausal = DoubleFactory2D.sparse.make(nmeNew, nmeNew, 0);
            for (int i = 0; i < nme; i++)
                for (int j = 0; j < nme; j++)
                    dmCausal.set(i, j, relations.getCausalFollowerMatrix().get(i, j));
            DoubleMatrix2D dmParallel = DoubleFactory2D.sparse.make(nmeNew, nmeNew, 0);
            for (int i = 0; i < nme; i++)
                for (int j = 0; j < nme; j++)
                    dmParallel.set(i, j, relations.getParallelMatrix().get(i, j));
            DoubleMatrix1D dmStart = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmStart.set(i, relations.getStartInfo().get(i));
            DoubleMatrix1D dmEnd = DoubleFactory1D.sparse.make(nmeNew, 0);
            for (int i = 0; i < nme; i++)
                dmEnd.set(i, relations.getEndInfo().get(i));
            LogEvents leEvents = relations.getLogEvents();

            for (int i = 0; i < alFirstGroup.size(); i++)
            {
                DoubleSet ds = (DoubleSet) alFirstGroup.get(i);
                int id = nme + i;
                String t = "__begin_" + (i + 1) + "__";

                //add to log events
                LogEvent le = new LogEvent(t, "auto");
                leEvents.add(le);

                //add to relation matrix
                IntArrayList sarSucc = ds.getB();
                for (int j = 0; j < sarSucc.size(); j++)
                {
                    dmCausal.set(id, sarSucc.get(j), 1);
                    dmStart.set(sarSucc.get(j), 0);
                }

                dmStart.set(id, 1);
            }
            for (int i = 0; i < alLastGroup.size(); i++)
            {
                DoubleSet ds = (DoubleSet) alLastGroup.get(i);
                int id = nme + alFirstGroup.size() + i;
                String t = "__end_" + (i + 1) + "__";

                //add to log events
                LogEvent le = new LogEvent(t, "auto");
                leEvents.add(le);

                //add to relation matrix
                IntArrayList sarPred = ds.getA();
                for (int j = 0; j < sarPred.size(); j++)
                {
                    dmCausal.set(sarPred.get(j), id, 1);
                    dmEnd.set(sarPred.get(j), 0);
                }

                dmEnd.set(id, 1);
            }

            //create a totally new log relations object
            nme = nmeNew;
            relations = new LogRelationsImpl(dmCausal, dmParallel, dmEnd, dmStart, dmOneLoop, leEvents);
        }
    }

    private void RemoveDuplicates(ArrayList tuples)
    {
        int i = 0;
        while (i < tuples.size())
        {

            IntArrayList[] tuple_i = ( (IntArrayList[]) (tuples.get(i)));
            int j = -1;
            while (j < tuples.size() - 1)
            {
                j++;
                if (i == j)
                    continue;
                IntArrayList[] tuple_j = ( (IntArrayList[]) (tuples.get(j)));

                // Now check whether j is a subset of i
                if (tuple_i[0].toList().containsAll(tuple_j[0].toList()) &&
                    tuple_i[1].toList().containsAll(tuple_j[1].toList()))
                {

                    // tuple_i contains tuple_j
                    tuples.remove(j);
                    if (j < i)
                        i--;
                    j--;
                }
            }
            i++;
        }
    }

    private boolean ExpandTree(ArrayList tuples, IntArrayList A, IntArrayList B, int sA, int sB)
    {
        boolean expanded = false;

        int s = sA;
        if (sB < s)
        {
            s = sB;
            // Look for an element that can be added to A, such that
            // it has no relation with any task in A, and is a causal predecessor of all tasks in B
        }
        for (int i = s; i < nme; i++)
        {
            boolean c = (i >= sA) && !A.contains(i);
            if (c)
            {
                for (int j = 0; j < A.size(); j++)
                {
                    c = c && (relations.getCausalFollowerMatrix().get(i, A.get(j)) == 0 || relations.getCausalFollowerMatrix().get(i, A.get(j)) > 0 && relations.getOneLengthLoopsInfo().get(A.get(j)) > 0)
                        && (relations.getCausalFollowerMatrix().get(A.get(j), i) == 0 || relations.getCausalFollowerMatrix().get(A.get(j), i) > 0 && relations.getOneLengthLoopsInfo().get(i) > 0)
                        && (relations.getParallelMatrix().get(i, A.get(j)) == 0);
                    // c == i does not have a relation with any element of A
                }
            }
            if (c)
            {
                for (int j = 0; j < B.size(); j++)
                {
                    c = c && (relations.getCausalFollowerMatrix().get(i, B.get(j)) > 0);
                    // c == i is a causal predecessor of all elements of B
                }
            }
            boolean d = (i >= sB) && !B.contains(i);
            if (d)
            {
                for (int j = 0; j < B.size(); j++)
                {
                    d = d && (relations.getCausalFollowerMatrix().get(i, B.get(j)) == 0 || relations.getCausalFollowerMatrix().get(i, B.get(j)) > 0 && relations.getOneLengthLoopsInfo().get(i) > 0)
                        && (relations.getCausalFollowerMatrix().get(B.get(j), i) == 0 || relations.getCausalFollowerMatrix().get(B.get(j), i) > 0 && relations.getOneLengthLoopsInfo().get(B.get(j)) > 0)
                        && (relations.getParallelMatrix().get(i, B.get(j)) == 0);
                    // d == i does not have a relation with any element of B
                }
            }
            if (d)
            {
                for (int j = 0; j < A.size(); j++)
                {
                    d = d && (relations.getCausalFollowerMatrix().get(A.get(j), i) > 0);
                    // d == i is a causal successor of all elements of A
                }
            }
            IntArrayList tA = (IntArrayList) A.clone();
            IntArrayList tB = (IntArrayList) B.clone();

            if (c)
            {
                // i can be added to A
                A.add(i);
                expanded = ExpandTree(tuples, A, B, i + 1, sB);
                A = tA;
            }
            if (d)
            {
                // i can be added to B
                B.add(i);
                expanded = ExpandTree(tuples, A, B, sA, i + 1);
                B = tB;
            }
        }
        if (!expanded)
        {
            IntArrayList[] t = new IntArrayList[2];
            t[0] = (IntArrayList) A.clone();
            t[1] = (IntArrayList) B.clone();
            tuples.add(t);
            expanded = true;
        }
        return expanded;
    }

    public String getHtmlDescription()
    {
        return "<h1>" + getName() + "</h1>";
    }

    //get any task's predecessor
    private IntArrayList getPred(int col, boolean noLoop)
    {
        IntArrayList ialPred = new IntArrayList();
        DoubleMatrix1D dm = relations.getCausalFollowerMatrix().viewColumn(col);
        for (int i = 0; i < nme; i++)
        {
            if (noLoop && relations.getOneLengthLoopsInfo().get(i) > 0)
                continue;
            if (dm.get(i) > 0)
                ialPred.add(i);
        }
        return ialPred;
    }

    //get any task's successor
    private IntArrayList getSucc(int row, boolean noLoop)
    {
        IntArrayList ialPred = new IntArrayList();
        DoubleMatrix1D dm = relations.getCausalFollowerMatrix().viewRow(row);
        for (int i = 0; i < nme; i++)
        {
            if (noLoop && relations.getOneLengthLoopsInfo().get(i) > 0)
                continue;
            if (dm.get(i) > 0)
                ialPred.add(i);
        }
        return ialPred;
    }

    private boolean existPara(IntArrayList sarI, IntArrayList sarJ)
    {
        for (int i = 0; i < sarI.size(); i++)
            for (int j = 0; j < sarJ.size(); j++)
                if (relations.getParallelMatrix().get(sarI.get(i), sarJ.get(j)) > 0)
                    return true;

        return false;
    }

    private boolean addInvTask(ArrayList alInvTask, InvisibleTask it, ArrayList alFalseDep)
    {
        DoubleSet dsPred = it.getPred()[0];
        DoubleSet dsSucc = it.getSucc()[0];
        //can be combined into any existing invisible tasks?
        int nCombined = 0;
        for (int i = 0; i < alInvTask.size(); i++)
        {
            InvisibleTask iti = (InvisibleTask) alInvTask.get(i);
            boolean isInPred = iti.containedInPred(dsPred);
            boolean isInSucc = iti.containedInSucc(dsSucc);
            //case 1
            if (isInPred && isInSucc)
            {
                nCombined++;
            }
            else if (isInPred)
            {
                boolean isCombined = isCombinable(alFalseDep, iti.getPred(), it.getSucc(), iti.getSucc(), it.getSucc());
                //combined into this invisible task
                if (isCombined)
                {
                    iti.addSucc(dsSucc);
                    nCombined++;
                }
            }
            else if (isInSucc)
            {
                boolean isCombined = isCombinable(alFalseDep, it.getPred(), iti.getSucc(), it.getPred(), iti.getPred());
                //combined into this invisible task
                if (isCombined)
                {
                    iti.addPred(dsPred);
                    nCombined++;
                }
            }
            else
            {
                boolean isCombined = isCombinable(alFalseDep, iti.getPred(), it.getSucc(), iti.getSucc(), it.getSucc());
                isCombined = isCombined && isCombinable(alFalseDep, it.getPred(), iti.getSucc(), it.getPred(), iti.getPred());
                //combined into this invisible task
                if (isCombined)
                {
                    iti.addPred(dsPred);
                    iti.addSucc(dsSucc);
                    nCombined++;
                }
            }
        }

        if (nCombined == 0)
        {
            //add a new invisible task
            alInvTask.add(it);
            return true;
        }
        else
        {
            //combined into existing invisible tasks
            return false;
        }
    }

    private boolean isCombinable(ArrayList alFalseDep, DoubleSet[] sarIPred, DoubleSet[] sarSucc, DoubleSet[] sarISucc, DoubleSet[] sarPred)
    {
        //I's Pred'pred vs Succ'succ
        for (int j = 0; j < sarIPred.length; j++)
        {
            IntArrayList sarIPredPred = sarIPred[j].getA();
            for (int k = 0; k < sarIPredPred.size(); k++)
            {
                for(int n=0; n<sarSucc.length; n++)
                {
                    IntArrayList sarSuccSucc = sarSucc[n].getB();
                    for (int m = 0; m < sarSuccSucc.size(); m++)
                    {
                        PredSucc ps = new PredSucc(sarIPredPred.get(k), sarSuccSucc.get(m));
                        if (!alFalseDep.contains(ps))
                            return false;
                    }
                }
            }
        }
        //I's Pred'succ vs Succ'pred
        for (int j = 0; j < sarIPred.length; j++)
            for(int k=0; k<sarSucc.length; k++)
                if (existPara(sarIPred[j].getB(), sarSucc[k].getA()))
                    return false;
        //I's Succ vs Succ
        for (int j = 0; j < sarISucc.length; j++)
            for(int k=0; k<sarPred.length; k++)
                if (! (existPara(sarISucc[j].getA(), sarPred[k].getA()) ||
                       existPara(sarISucc[j].getB(), sarPred[k].getB())))
                    return false;
        return true;
    }
}

class PredSucc
{
    int pred;
    int succ;

    public PredSucc(int pred, int succ)
    {
        this.pred = pred;
        this.succ = succ;
    }

    public boolean equals(Object o)
    {
        PredSucc ps = (PredSucc) o;
        return pred == ps.getPred() && succ == ps.getSucc();
    }

    public int getSucc()
    {
        return succ;
    }

    public int getPred()
    {
        return pred;
    }

    public String toString()
    {
        return pred + "-->" + succ;
    }
}

class DoubleSet
{
    IntArrayList alA = new IntArrayList();
    IntArrayList alB = new IntArrayList();

    public DoubleSet()
    {
    }

    public boolean isEmptyAB()
    {
        return alA.isEmpty() || alB.isEmpty();
    }

    public boolean isEmptyA()
    {
        return alA.isEmpty();
    }

    public boolean isEmptyB()
    {
        return alB.isEmpty();
    }

    public void addToA(int a)
    {
        if (!alA.contains(a))
        {
            alA.add(a);
        }
    }

    public void clearA()
    {
        alA.clear();
    }

    public void clearB()
    {
        alB.clear();
    }

    public void addToA(IntArrayList alTask)
    {
        for (int i = 0; i < alTask.size(); i++)
        {
            int t = alTask.get(i);
            if (!alA.contains(t))
            {
                alA.add(t);
            }
        }
    }

    public void addToB(int b)
    {
        if (!alB.contains(b))
        {
            alB.add(b);
        }
    }

    public void removeFromA(IntArrayList alFalseA)
    {
        alA.removeAll(alFalseA);
    }

    public void removeFromB(IntArrayList alFalseB)
    {
        alB.removeAll(alFalseB);
    }

    public void addToB(IntArrayList alTask)
    {
        for (int i = 0; i < alTask.size(); i++)
        {
            int t = alTask.get(i);
            if (!alB.contains(t))
            {
                alB.add(t);
            }
        }
    }

    public IntArrayList getA()
    {
        return alA;
    }

    public IntArrayList getB()
    {
        return alB;
    }

    public boolean containedInA(int a)
    {
        return alA.contains(a);
    }

    public boolean containedInA(IntArrayList ialA)
    {
        IntArrayList alAcpy = alA.copy();
        alAcpy.removeAll(ialA);
        return alAcpy.size() == alA.size() - ialA.size();
    }

    public boolean equalsA(IntArrayList ialA)
    {
        if (alA.size() != ialA.size())
        {
            return false;
        }
        IntArrayList alCpy = alA.copy();
        alCpy.removeAll(ialA);
        return alCpy.size() == 0;
    }

    public boolean containedInB(int b)
    {
        return alB.contains(b);
    }

    public boolean containedInB(IntArrayList ialB)
    {
        IntArrayList alBcpy = alB.copy();
        alBcpy.removeAll(ialB);
        return alBcpy.size() == alB.size() - ialB.size();
    }

    public boolean equalsB(IntArrayList ialB)
    {
        if (alB.size() != ialB.size())
        {
            return false;
        }
        IntArrayList alCpy = alB.copy();
        alCpy.removeAll(ialB);
        return alCpy.size() == 0;
    }

    public boolean equals(Object o)
    {
        DoubleSet ds = (DoubleSet) o;
        return equalsA(ds.alA) && equalsB(ds.alB);
    }

    public String toString()
    {
        return "{" + alA.toString() + "->" + alB.toString() + "}";
    }
}

class InvisibleTask
{
    String t;
    int id;
    ArrayList alPred = new ArrayList();
    ArrayList alSucc = new ArrayList();

    public InvisibleTask(String t)
    {
        this.t = t;
    }

    public String getName()
    {
        return t;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void addPred(DoubleSet ds)
    {
        if (!alPred.contains(ds))
        {
            alPred.add(ds);
        }
    }

    public void addSucc(DoubleSet ds)
    {
        if (!alSucc.contains(ds))
        {
            alSucc.add(ds);
        }
    }

    public DoubleSet[] getPred()
    {
        DoubleSet[] sarPlace = new DoubleSet[alPred.size()];
        alPred.toArray(sarPlace);
        return sarPlace;
    }

    public DoubleSet[] getSucc()
    {
        DoubleSet[] sarPlace = new DoubleSet[alSucc.size()];
        alSucc.toArray(sarPlace);
        return sarPlace;
    }

    public boolean containedInPred(DoubleSet ds)
    {
        return alPred.contains(ds);
    }

    public boolean containedInSucc(DoubleSet ds)
    {
        return alSucc.contains(ds);
    }

    public IntArrayList getPredPred()
    {
        IntArrayList alTask = new IntArrayList();
        for (int i = 0; i < alPred.size(); i++)
        {
            DoubleSet ds = (DoubleSet) alPred.get(i);
            IntArrayList sarPred = ds.getA();
            for (int j = 0; j < sarPred.size(); j++)
            {
                if (!alTask.contains(sarPred.get(j)))
                {
                    alTask.add(sarPred.get(j));
                }
            }
        }

        return alTask;
    }

    public IntArrayList getSuccSucc()
    {
        IntArrayList alTask = new IntArrayList();
        for (int i = 0; i < alSucc.size(); i++)
        {
            DoubleSet ds = (DoubleSet) alSucc.get(i);
            IntArrayList sarSucc = ds.getB();
            for (int j = 0; j < sarSucc.size(); j++)
            {
                if (!alTask.contains(sarSucc.get(j)))
                {
                    alTask.add(sarSucc.get(j));
                }
            }
        }

        return alTask;
    }
}
