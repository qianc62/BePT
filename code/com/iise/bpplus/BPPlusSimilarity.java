package com.iise.bpplus;

import com.iise.shudi.bp.BehavioralProfileSimilarity;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.Place;
import org.jbpt.petri.io.PNMLSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.io.File;

/**
 * Created by little on 15-11-19.
 */
public class BPPlusSimilarity {
    //store all last transitions of each loop
    private HashMap<NetSystem, ArrayList<Transition>> htLastNodes = new HashMap();
    //store all relation matrices of each net
    private HashMap<NetSystem, HashMap<Transition, HashMap<Transition, Integer>>> htMatrices = new HashMap();
    //store all presets of each place
    private HashMap<NetSystem, HashMap<Place, Transition[]>> htPreOfPlace = new HashMap();
    //store all postsets of each place
    private HashMap<NetSystem, HashMap<Place, Transition[]>> htPostOfPlace = new HashMap();
    //store all presets of each transition
    private HashMap<NetSystem, HashMap<Transition, Place[]>> htPreOfTrans = new HashMap();
    //store all postsets of each transition
    private HashMap<NetSystem, HashMap<Transition, Place[]>> htPostOfTrans = new HashMap();
    //store all places of a net
    private HashMap<NetSystem, Place[]> htPlaces = new HashMap();
    //store all transitions of a net
    private HashMap<NetSystem, Transition[]> htTransitions = new HashMap();
    //store all preset of each node
    private HashMap<NetSystem, HashMap<Node, Node[]>> htPresets = new HashMap();
    //store all postsets of each node
    private HashMap<NetSystem, HashMap<Node, Node[]>> htPostsets = new HashMap();
    //store all -->,->> of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htCausals = new HashMap();
    //store all ||,||- of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htConcurrs = new HashMap();
    //store all # of one transition
    private HashMap<NetSystem, HashMap<Transition, ArrayList<Transition>>> htConflicts = new HashMap();

    /**
     * initialize all variables relevant to a given net
     * e.g., preset, postset of any node, relation matrix
     * @param net, a given Petri net
     */
    public void initialize(NetSystem net)
    {
        //restore all global variables
        htLastNodes.remove(net);
        htMatrices.remove(net);
        htPreOfPlace.remove(net);
        htPostOfPlace.remove(net);
        htPreOfTrans.remove(net);
        htPostOfTrans.remove(net);
        htPlaces.remove(net);
        htTransitions.remove(net);
        htPresets.remove(net);
        htPostsets.remove(net);
        htCausals.remove(net);
        htConcurrs.remove(net);
        htConflicts.remove(net);

        //initialize the array for last nodes
        ArrayList<Transition> alLastNodes = new ArrayList();
        htLastNodes.put(net, alLastNodes);

        //get all transitions
        Set<Transition> transList = net.getTransitions();
        Transition[] arTrans = new Transition[transList.size()];
        transList.toArray(arTrans);
        //store all transitions
        htTransitions.put(net, arTrans);

        //get all places
        Set<Place> placeList = net.getPlaces();
        Place[] arPlaces = new Place[placeList.size()];
        placeList.toArray(arPlaces);
        //store all places
        htPlaces.put(net, arPlaces);

        //Initialize relation matrix, causal/concurr/conflict matrix
        HashMap<Transition, HashMap<Transition, Integer>> htRM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htCausalM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htConcurrM = new HashMap();
        HashMap<Transition, ArrayList<Transition>> htConflictM = new HashMap();
        //store net's RM, CausalM, ConcurrM, ConflictM
        htMatrices.put(net, htRM);
        htCausals.put(net, htCausalM);
        htConcurrs.put(net, htConcurrM);
        htConflicts.put(net, htConflictM);
        for(Transition trans:arTrans)
        {
            HashMap<Transition, Integer> htRelations = new HashMap();
            //store one transition's all relations
            htRM.put(trans, htRelations);

            ArrayList<Transition> alCausal = new ArrayList();
            //store one transiton's all causal relations
            htCausalM.put(trans, alCausal);
            ArrayList<Transition> alConcurr = new ArrayList();
            //store one transiton's all concurrency relations
            htConcurrM.put(trans, alConcurr);
            ArrayList<Transition> alConflict = new ArrayList();
            //store one transiton's all conflict relations
            htConflictM.put(trans, alConflict);
        }

        //store all places' presets and postsets
        HashMap<Node, Node[]> htPreNode = new HashMap();
        htPresets.put(net, htPreNode);
        HashMap<Place, Transition[]> htPrePlace = new HashMap();
        htPreOfPlace.put(net, htPrePlace);
        HashMap<Node, Node[]> htPostNode = new HashMap();
        htPostsets.put(net, htPostNode);
        HashMap<Place, Transition[]> htPostPlace = new HashMap();
        htPostOfPlace.put(net, htPostPlace);
        for(Place place : arPlaces)
        {
            //restore its numbers
            place.setTag(null);
            //get input transitions
            Set<Transition> setInputs = net.getPreset(place);
            Transition[] arInput = new Transition[setInputs.size()];
            setInputs.toArray(arInput);
            //store its preset
            htPreNode.put(place, arInput);
            htPrePlace.put(place, arInput);

            //get output transitions
            Set<Transition> setOutputs = net.getPostset(place);
            Transition[] arOutput = new Transition[setOutputs.size()];
            setOutputs.toArray(arOutput);
            //store its postset
            htPostNode.put(place, arOutput);
            htPostPlace.put(place, arOutput);
        }

        //store all transitions' presets and postsets
        HashMap<Transition, Place[]> htPreTrans = new HashMap();
        htPreOfTrans.put(net, htPreTrans);
        HashMap<Transition, Place[]> htPostTrans = new HashMap();
        htPostOfTrans.put(net, htPostTrans);
        for(Transition trans: arTrans)
        {
            //restore its numbers
            trans.setTag(null);
            //get input places
            Set<Place> setInputs = net.getPreset(trans);
            Place[] arInput = new Place[setInputs.size()];
            setInputs.toArray(arInput);
            //store its preset
            htPreNode.put(trans, arInput);
            htPreTrans.put(trans, arInput);

            //get output places
            Set<Place> setOutputs = net.getPostset(trans);
            Place[] arOutput = new Place[setOutputs.size()];
            setOutputs.toArray(arOutput);
            //store its postset
            htPostNode.put(trans, arOutput);
            htPostTrans.put(trans, arOutput);
        }
    }

    /**
     * try to find all last transitions of loops
     * using a deep first search of a directed graph
     * @param net
     */
    private void findLoops(NetSystem net)
    {
        ArrayList<Place> alPlaces = new ArrayList();
        alPlaces.addAll(net.getSourcePlaces());
        //try to find one loop
        Node start = alPlaces.get(0);
        Set<Node> global = new HashSet();
        findALoop(net, start, global);
    }

    /**
     * try to find a loop recursively
     * @param net, the traversal net
     * @param start, the current traversal node
     */
    private void findALoop(NetSystem net, Node start, Set<Node> global)
    {
        //if the node has been visited globally
        if (global.contains(start))
            return;
        //store it in the global set
        global.add(start);

        start.setTag(new Integer(1));
        Node[] arSucc = htPostsets.get(net).get(start);
        for (Node succ : arSucc)
        {
            if(succ.getTag() != null)
            {
                //find an entry place of a loop
                if(start instanceof Transition && !htLastNodes.get(net).contains(start))
                    htLastNodes.get(net).add((Transition)start);
            }
            else
            {
                //try to find a loop recursively
                findALoop(net, succ, global);
            }
        }
        start.setTag(null);
    }

    /**
     * set numbers of nodes in a breadth-first traversal order
     * consider xor-join (including loop) and and-join
     * @param net, a given Petri net, numbers will be stored in tags
     * @param isNumbering, whether the numbering procedure is called
     */
    public void numberingNodes(NetSystem net, boolean isNumbering)
    {
        //find all last nodes of loops first
        findLoops(net);

        //stop numbering if it not needed
        if(!isNumbering)
            return;

        ArrayList<Node> queue = new ArrayList();
        queue.addAll(net.getSourceNodes());
        while(queue.size()>0)
        {
            Node curr = queue.get(0);    //the curr node
            Node[] preset = htPresets.get(net).get(curr);
            //try to give this node a number
            if(preset.length == 0)  //source place
            {
                curr.setTag(new Integer(0));
                addSucc(net, queue, curr);
            }
            else if(preset.length == 1) //only one predecessor
            {
                Node pred = preset[0];
                Integer num = (Integer)pred.getTag();
                curr.setTag(new Integer(num.intValue()+1));
                addSucc(net, queue, curr);
            }
            else //has more than one predecessors
            {
                boolean allPredDone = true;
                int iNum = 0;
                for(Node pred: preset)
                {
                    Integer num = (Integer)pred.getTag();
                    //not ready for numbering this node
                    if(num == null)
                    {
                        if(!htLastNodes.get(net).contains(pred))
                            allPredDone = false;
                    }
                    else
                    {
                        //find the max number of its predecessors
                        if(iNum < num.intValue())
                            iNum = num.intValue();
                    }
                }

                //all predecessors' numbers are ready
                if(allPredDone)
                {
                    curr.setTag(new Integer(iNum+1));
                    addSucc(net, queue, curr);
                }
                else
                {
                    //push to the last in queue
                    queue.remove(curr);
                    queue.add(curr);
                }
            }
        }
    }

    protected static HashMap<Transition, HashMap<Transition, Integer>> cloneRM(HashMap<Transition, HashMap<Transition, Integer>> htRM)
    {
        //clone the hashmap itself
        HashMap<Transition, HashMap<Transition, Integer>> htRMClone = new HashMap();
        //clone each hashmap in the value set
        Set<Transition> alKeys = htRM.keySet();
        for(Transition trans: alKeys)
            htRMClone.put(trans, (HashMap)htRM.get(trans).clone());

        return htRMClone;
    }

    protected static HashMap<Transition, ArrayList<Transition>> cloneCausal(HashMap<Transition, ArrayList<Transition>> htCausalM)
    {
        //clone the hashmap itself
        HashMap<Transition, ArrayList<Transition>> htCausalMClone = new HashMap();
        //clone each hashmap in the value set
        Set<Transition> alKeys = htCausalM.keySet();
        for(Transition trans: alKeys)
            htCausalMClone.put(trans, (ArrayList)htCausalM.get(trans).clone());

        return htCausalMClone;
    }

    /**
     * deduce all task relations and store them in htMatrices
     * @param net, a given Petri net with numbers stored in tags
     */
    public void deduceRelationMatrix(NetSystem net)
    {
        HashMap<Transition, HashMap<Transition, Integer>> htRM = htMatrices.get(net);
        HashMap<Transition, ArrayList<Transition>> htCausalM = htCausals.get(net);
        HashMap<Transition, ArrayList<Transition>> htConcurrM = htConcurrs.get(net);
        HashMap<Transition, ArrayList<Transition>> htConflictM = htConflicts.get(net);

        //1: deduce -->,# from places and || from transitions

        //1.1: deduce -->,# from places
        Place[] arPlaces = htPlaces.get(net);
        for(Place p: arPlaces)
        {
            Transition[] alInput = htPreOfPlace.get(net).get(p);
            Transition[] alOutput = htPostOfPlace.get(net).get(p);

            //1.1.1 deduce # between input transitions
            for(int i=0; i<alInput.length; i++)
            {
                //must not loop back
                if(htLastNodes.get(net).contains(alInput[i]))
                    continue;
                HashMap<Transition, Integer> htRelI = htRM.get(alInput[i]);
                for (int j = i+1; j < alInput.length; j++)
                {
                    //must not loop back
                    if(htLastNodes.get(net).contains(alInput[j]))
                        continue;
                    HashMap<Transition, Integer> htRelJ = htRM.get(alInput[j]);
                    if(htRelI.get(alInput[j]) == null)
                    {
                        htRelI.put(alInput[j], TaskRelation.CONFLICT);
                        htRelJ.put(alInput[i], TaskRelation.CONFLICT);
                        //store the # relation between arInput[i] and arInput[j]
                        htConflictM.get(alInput[i]).add(alInput[j]);
                        htConflictM.get(alInput[j]).add(alInput[i]);
                    }
                }
            }

            //1.1.2 deduce # between output transitions
            for(int i=0; i<alOutput.length; i++)
            {
                HashMap<Transition, Integer> htRelI = htRM.get(alOutput[i]);
                for (int j = i+1; j < alOutput.length; j++)
                {
                    HashMap<Transition, Integer> htRelJ = htRM.get(alOutput[j]);
                    if(htRelI.get(alOutput[j]) == null)
                    {
                        htRelI.put(alOutput[j], TaskRelation.CONFLICT);
                        htRelJ.put(alOutput[i], TaskRelation.CONFLICT);
                        //store the # relation between alOutput[i] and alOutput[j]
                        htConflictM.get(alOutput[i]).add(alOutput[j]);
                        htConflictM.get(alOutput[j]).add(alOutput[i]);
                    }
                }
            }

            //1.1.3 deduce --> between input/output transitions
            for(int i=0; i<alInput.length; i++)
            {
                //must not loop back
                if (htLastNodes.get(net).contains(alInput[i]))
                    continue;
                HashMap<Transition, Integer> htRelI = htRM.get(alInput[i]);
                for (int j = 0; j < alOutput.length; j++)
                {
                    htRelI.put(alOutput[j], TaskRelation.DIRECTCAUSAL);
                    //store the --> relation here
                    htCausalM.get(alInput[i]).add(alOutput[j]);
                }
            }
        }

        //1.2: deduce || from all transitions
        Transition[] arTrans = htTransitions.get(net);
        for(Transition trans: arTrans)
        {
            //check for all the input places
            deduceConcurrs(net, htRM, htPreOfTrans, htPreOfPlace, htConcurrM, trans);

            //check for all the output places
            deduceConcurrs(net, htRM, htPostOfTrans, htPostOfPlace, htConcurrM, trans);
        }

        //2: deduce ->> according to transitive -->
        deduceIndirectCausals(htRM, htCausalM, arTrans);

        //3: deduce # according to # and (--> or ->>)
        deduceCons(htRM, htCausalM, htConflictM, arTrans, TaskRelation.CONFLICT);

        //4: deduce || according to || and (--> or ->>)
        deduceCons(htRM, htCausalM, htConcurrM, arTrans, TaskRelation.ALWAYSCONCURRENCY);

        //5: deduce -->,->>,# according to loops
        ArrayList<Transition> alLastNodes = htLastNodes.get(net);

        //5.1: add --> for last transition one by one
        ArrayList<HashMap<Transition, HashMap<Transition, Integer>>> alNewRM = new ArrayList();
        for(Transition lastNode: alLastNodes)
        {
            //clone the two relation sets for each last transition
            HashMap<Transition, HashMap<Transition, Integer>> htRMClone = cloneRM(htRM);
            HashMap<Transition, ArrayList<Transition>> htCausalMClone = cloneCausal(htCausalM);
            //store the cloned new relation sets
            alNewRM.add(htRMClone);

            //get lastNode's output places
            Place[] arOutput = htPostOfTrans.get(net).get(lastNode);
            //enumerate each output place for successors
            for(Place place: arOutput)
            {
                //get place's output transitions
                Transition[] arSucc = htPostOfPlace.get(net).get(place);
                //enumerate each successor of lastNode
                for(Transition succ: arSucc)
                {
                    if(htRMClone.get(lastNode).get(succ) != null)
                        continue;
                    //add --> between lastNode and succ
                    htRMClone.get(lastNode).put(succ, TaskRelation.DIRECTCAUSAL);
                    //store --> between lastNode and succ
                    htCausalMClone.get(lastNode).add(succ);
                }
            }

            //deduce ->> for all transitions caused by last transition
            deduceIndirectCausals(htRMClone, htCausalMClone, arTrans);
        }

        //5.2: merge all new causal relations together
        for(int i=alLastNodes.size()-1; i>=0; i--)
        {
            HashMap<Transition, HashMap<Transition, Integer>> htRMClone = alNewRM.get(i);

            for(Transition trans: arTrans)
            {
                //merge the new relations in htRMClone and htCausalMClone
                HashMap<Transition, Integer> htRelTransClone = htRMClone.get(trans);
                HashMap<Transition, Integer> htRelTrans = htRM.get(trans);
                //if they are not in the same size
                if(htRelTransClone.size() > htRelTrans.size())
                {
                    Set<Transition> alKeys = htRelTransClone.keySet();
                    for(Transition key: alKeys)
                    {
                        //merge a new relation in the clone
                        if(htRelTrans.get(key) == null)
                        {
                            htRelTrans.put(key, htRelTransClone.get(key));
                            //update the causal relations accordingly
                            htCausalM.get(trans).add(key);
                        }
                    }
                }
            }
        }

        //5.3: add # for transitions not in a loop
        for(Transition trans: arTrans)
            if(htRM.get(trans).get(trans) == null)
                htRM.get(trans).put(trans, TaskRelation.CONFLICT);

        //6: deduce <--,<<- according to --> and ->>
        for(Transition trans: arTrans)
        {
            ArrayList<Transition> arSucc = htCausalM.get(trans);
            //trans --> or ->> with succ
            for(Transition succ: arSucc)
            {
                //succ cannot reach to trans
                if(htRM.get(succ).get(trans) == null)
                {
                    Integer tr = htRM.get(trans).get(succ);
                    if(tr == TaskRelation.DIRECTCAUSAL)
                        htRM.get(succ).put(trans, TaskRelation.INVERSEDIRECTCAUSAL);
                    else
                        htRM.get(succ).put(trans, TaskRelation.INVERSEINDIRECTCAUSAL);
                }
            }
        }

        //7: deduce ||- according to || and #
        ArrayList<Transition> alSilent = new ArrayList();
        for(Transition transI: arTrans)
        {
            //transI should be a visible transition
            if(isSilentTrans(transI))
            {
                alSilent.add(transI);
                continue;
            }
            //check each || between transI and transJ
            ArrayList<Transition> alConcurr = htConcurrM.get(transI);
            for(Transition transJ: alConcurr)
            {
                //transI should also be a visible transition
                if(isSilentTrans(transJ))
                    continue;

                //find a silent transition st where and st#transJ and st||transI
                ArrayList<Transition> alConflict = htConflictM.get(transJ);
                for(Transition st: alConflict)
                {
                    //st should be a silent transition
                    if (!isSilentTrans(st))
                        continue;

                    //st must be parallel with transI
                    if(htRM.get(transI).get(st) == TaskRelation.ALWAYSCONCURRENCY)
                    {
                        //update || between transI and transJ to ||-
                        htRM.get(transI).put(transJ, TaskRelation.SOMETIMESCONCURRENCY);

                        break;
                    }
                }
            }
        }

        //8: deduce --> from ->> and <-- from <<- by silent transitions

        //8.1: generate all longest paths with only silent transitions
        ArrayList<ArrayList<Transition>> alSPTotal = new ArrayList();
        //setup a initial tag for visiting purpose
        for(Transition silentTrans: alSilent)
            silentTrans.setTag(new Integer(0));
        for(Transition silentTrans: alSilent)
        {
            //check if the silent transition has been visited
            if(silentTrans.getTag() == null)
                continue;

            //initialize silent paths for this silent transition
            ArrayList<ArrayList<Transition>> alSilentPaths = new ArrayList();
            //in default, there is only one silent path
            alSilentPaths.add(new ArrayList<Transition>());

            //try to find a silent transition backward
            backwardSilent(net, silentTrans, alSilentPaths);

            //remove the last silent transition in each path
            for(ArrayList<Transition> alSilentPath: alSilentPaths)
                alSilentPath.remove(alSilentPath.size()-1);

            //try to find a silent transition forward
            forwardSilent(net, silentTrans, alSilentPaths);

            //store silent paths for this silent transition
            alSPTotal.addAll(alSilentPaths);
        }

        //8.2: enumerating all silent paths with variant length
        HashMap<String, String> htVisitedPredSucc = new HashMap();
        for(ArrayList<Transition> alSilentPath: alSPTotal)
        {
            //the length of a silent path
            int nTotal = alSilentPath.size();
            //enumerating all subset of the silent path
            for(int len=1; len<=nTotal; len++)
            {
                int nCount = nTotal - len + 1;
                //enumerating all silent paths with length len
                for(int pos=0; pos<nCount; pos++)
                {
                    //visit the silent path from pos to pos+len-1
                    Transition pred = alSilentPath.get(pos);
                    Transition succ = alSilentPath.get(pos+len-1);

                    //check whether pred's pred ->> succ's succ
                    ArrayList<Transition> alPred = getSilentPred(net, pred);
                    ArrayList<Transition> alSucc = getSilentSucc(net, succ);

                    for(Transition transPred: alPred)
                    {
                        for(Transition transSucc: alSucc)
                        {
                            //check whether transPred ->> transSucc
                            if(htRM.get(transPred).get(transSucc) != TaskRelation.INDIRECTCAUSAL)
                                continue;

                            String strPredSucc = transPred.getId() + ":" + transSucc.getId();
                            if(htVisitedPredSucc.get(strPredSucc) == null)
                            {
                                //store the visited pair of transPred and transSucc
                                htVisitedPredSucc.put(strPredSucc, "");

                                //check whether the silent path is OK
                                boolean isDirect = backwardSilent(net, htRM, transPred, transSucc);
                                if(isDirect)
                                {
                                    //revise the relation from ->> to -->
                                    htRM.get(transPred).put(transSucc, TaskRelation.DIRECTCAUSAL);
                                    //if possible, revise the inverse relation from <<- to <--
                                    if(htRM.get(transSucc).get(transPred) == TaskRelation.INVERSEINDIRECTCAUSAL)
                                        htRM.get(transSucc).put(transPred, TaskRelation.INVERSEDIRECTCAUSAL);
                                }
                            }
                        }
                    }
                }
            }
        }

        //9: remove the row and columns of silent transitions
        for(Transition trans: arTrans)
        {
            //remove all the columns corresponding to silent transitions
            for(Transition silent: alSilent)
                htRM.get(trans).remove(silent);
        }
        //remove all the rows corresponding to silent transitions
        for(Transition silent: alSilent)
            htRM.remove(silent);
    }

    private boolean backwardSilent(NetSystem net, HashMap<Transition, HashMap<Transition, Integer>> htRM, Transition transPred, Transition transSucc)
    {
        //check whether transPred --> transSucc
        Place[] arInput = htPreOfTrans.get(net).get(transSucc);
        //check each input place of transSucc
        boolean isDirect = true;
        //利用哈希表记录是否变迁transPred到库所inputPlace可达，尝试加速
        for(Place placeInput: arInput)
        {
            //backward recursively from this place to meet transPred
            Transition[] arInputTrans = htPreOfPlace.get(net).get(placeInput);
            //just check whether placeInput can reach transPred
            boolean isFound = false;
            ArrayList<Transition> alTempSilent = new ArrayList();
            for(Transition inputTrans: arInputTrans)
            {
                //parallel or itself is one condition to guarantee reachability
                Integer rel = htRM.get(transPred).get(inputTrans);
                if(transPred == inputTrans || rel == TaskRelation.SOMETIMESCONCURRENCY || rel == TaskRelation.ALWAYSCONCURRENCY)
                {
                    isFound = true;
                    break;
                }
                //backup all silent transitions for backward attempt
                if(isSilentTrans(inputTrans))
                    alTempSilent.add(inputTrans);
            }

            //try to find a silent path backward
            if(!isFound && alTempSilent.size()>0)
            {
                //backward each silent transition recursively
                for(Transition transSilent: alTempSilent)
                {
                    isFound = backwardSilent(net, htRM, transPred, transSilent);

                    //have found one silent path, stop backward
                    if(isFound)
                        break;
                }
            }

            //stop backward because one place cannot be reached
            if(!isFound)
            {
                isDirect = false;
                break;
            }
        }

        return isDirect;
    }

    private ArrayList<Transition> getSilentPred(NetSystem net, Transition silentTransition)
    {
        ArrayList<Transition> alPred = new ArrayList();
        Place[] arInput = htPreOfTrans.get(net).get(silentTransition);
        for(Place place: arInput)
        {
            Transition[] arPred = htPreOfPlace.get(net).get(place);
            for(Transition trans: arPred)
                if(!isSilentTrans(trans) && !alPred.contains(trans))
                    alPred.add(trans);
        }

        return alPred;
    }

    private ArrayList<Transition> getSilentSucc(NetSystem net, Transition silentTransition)
    {
        ArrayList<Transition> alSucc = new ArrayList();
        Place[] arOutput = htPostOfTrans.get(net).get(silentTransition);
        for(Place place: arOutput)
        {
            Transition[] arSucc = htPostOfPlace.get(net).get(place);
            for(Transition trans: arSucc)
                if(!isSilentTrans(trans) && !alSucc.contains(trans))
                    alSucc.add(trans);
        }

        return alSucc;
    }

    private void backwardSilent(NetSystem net, Transition silentTrans, ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        //mark this silent transition as visited
        silentTrans.setTag(null);

        //try to add a silent transition to each silent path
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentPath.add(0, silentTrans);
        //clone the current silent paths
        ArrayList<ArrayList<Transition>> alSPBackup = cloneSilentPaths(alSilentPaths);
        //cleanup the silent paths for new ones
        alSilentPaths.clear();

        //backward all places
        Place[] arInput = htPreOfTrans.get(net).get(silentTrans);
        for(Place inputPlace: arInput)
        {
            //backward all transitions
            Transition[] arPred = htPreOfPlace.get(net).get(inputPlace);
            for(Transition pred: arPred)
            {
                //check whether it is a silent transition
                if(isSilentTrans(pred))
                {
                    //clone silent paths for recursion
                    ArrayList<ArrayList<Transition>> alSPClone = cloneSilentPaths(alSPBackup);
                    //extend the silent paths backward recursively
                    backwardSilent(net, pred, alSPClone);
                    //store the extended silent paths
                    alSilentPaths.addAll(alSPClone);
                }
            }
        }

        //backward to visible transitions now
        if(alSilentPaths.size() == 0)
            alSilentPaths.addAll(alSPBackup);
    }

    private void forwardSilent(NetSystem net, Transition silentTrans, ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        //mark this silent transition as visited
        silentTrans.setTag(null);

        //try to add a silent transition to each silent path
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentPath.add(silentTrans);
        //clone the current silent paths
        ArrayList<ArrayList<Transition>> alSPBackup = cloneSilentPaths(alSilentPaths);
        //cleanup the silent paths for new ones
        alSilentPaths.clear();

        //forward all places
        Place[] arOutput = htPostOfTrans.get(net).get(silentTrans);
        for(Place outputPlace: arOutput)
        {
            //forward all transitions
            Transition[] arSucc = htPostOfPlace.get(net).get(outputPlace);
            for(Transition succ: arSucc)
            {
                //check whether it is a silent transition
                if(isSilentTrans(succ))
                {
                    //clone silent paths for recursion
                    ArrayList<ArrayList<Transition>> alSPClone = cloneSilentPaths(alSPBackup);
                    //extend the silent paths forward recursively
                    forwardSilent(net, succ, alSPClone);
                    //store the extended silent paths
                    alSilentPaths.addAll(alSPClone);
                }
            }
        }

        //forward to visible transitions now
        if(alSilentPaths.size() == 0)
            alSilentPaths.addAll(alSPBackup);
    }

    private ArrayList<ArrayList<Transition>> cloneSilentPaths(ArrayList<ArrayList<Transition>> alSilentPaths)
    {
        ArrayList<ArrayList<Transition>> alSilentClone = new ArrayList();
        for(ArrayList<Transition> alSilentPath: alSilentPaths)
            alSilentClone.add((ArrayList<Transition>)alSilentPath.clone());
        return alSilentClone;
    }

    private boolean isSilentTrans(Transition trans)
    {
        return trans.getId().toLowerCase().startsWith(TaskRelation.SLIENTTRANSITION);
    }

    private void deduceConcurrs(NetSystem net, HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<NetSystem, HashMap<Transition, Place[]>> htTrans, HashMap<NetSystem, HashMap<Place, Transition[]>> htPlace, HashMap<Transition, ArrayList<Transition>> htConcurrM, Transition trans)
    {
        //check for all the input places
        Place[] arInput = htTrans.get(net).get(trans);
        for(int i=0; i<arInput.length; i++)
        {
            //get all predecessors of arInput[i];
            Transition[] arPredI = htPlace.get(net).get(arInput[i]);
            for(int j=i+1; j<arInput.length; j++)
            {
                //get all predecessors of arInput[j]
                Transition[] arPredJ = htPlace.get(net).get(arInput[j]);
                //check if they are in parallel
                for(Transition transI: arPredI)
                {
                    HashMap<Transition, Integer> htRelI = htRM.get(transI);
                    for(Transition transJ: arPredJ)
                    {
                        HashMap<Transition, Integer> htRelJ = htRM.get(transJ);
                        //they should not be the same
                        if(transI != transJ && htRelI.get(transJ) == null)
                        {
                            htRelI.put(transJ, TaskRelation.ALWAYSCONCURRENCY);
                            htRelJ.put(transI, TaskRelation.ALWAYSCONCURRENCY);
                            //store the || relation between transI and transJ
                            htConcurrM.get(transI).add(transJ);
                            htConcurrM.get(transJ).add(transI);
                        }
                    }
                }
            }
        }
    }

    private void deduceCons(HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<Transition, ArrayList<Transition>> htCausalM, HashMap<Transition, ArrayList<Transition>> htConM, Transition[] arTrans, Integer tr)
    {
        boolean isChanged = true;
        while(isChanged)
        {
            isChanged = false;
            for (Transition pred : arTrans)
            {
                //enumerating each pred's conflicts
                HashMap<Transition, Integer> htPred = htRM.get(pred);
                //all transitions having # or || with pred
                ArrayList<Transition> alCurr = htConM.get(pred);
                for(int i=alCurr.size()-1; i>=0; i--)
                {
                    //get the curr transition
                    Transition curr = alCurr.get(i);
                    //all transitions having --> or ->> with curr
                    ArrayList<Transition> alSucc = htCausalM.get(curr);
                    for(Transition succ: alSucc)
                    {
                        //check if a new # or || is possible
                        if (htPred.get(succ) == null)
                        {
                            isChanged = true;
                            htPred.put(succ, tr);
                            htRM.get(succ).put(pred, tr);
                            //store # or || between pred and succ
                            alCurr.add(succ);
                            htConM.get(succ).add(pred);
                        }
                    }
                }
            }
        }
    }

    private void deduceIndirectCausals(HashMap<Transition, HashMap<Transition, Integer>> htRM, HashMap<Transition, ArrayList<Transition>> htCausalM, Transition[] arTrans)
    {
        boolean isChanged = true;
        while(isChanged)
        {
            isChanged = false;
            for (Transition pred : arTrans)
            {
                //enumerating each pred's successors
                HashMap<Transition, Integer> htPred = htRM.get(pred);
                //all transitions having --> or ->> with pred
                ArrayList<Transition> alCurr = htCausalM.get(pred);
                for(int i=alCurr.size()-1; i>=0; i--)
                {
                    //get the curr transition
                    Transition curr = alCurr.get(i);
                    //all transitions having --> or ->> with curr
                    ArrayList<Transition> alSucc = htCausalM.get(curr);
                    for(Transition succ: alSucc)
                    {
                        //check if a new ->> is possible
                        if (htPred.get(succ) == null)
                        {
                            isChanged = true;
                            htPred.put(succ, TaskRelation.INDIRECTCAUSAL);
                            //store --> between pred and succ
                            alCurr.add(succ);
                        }
                    }
                }
            }
        }
    }

    private void addSucc(NetSystem net, ArrayList<Node> queue, Node curr)
    {
        queue.remove(curr);
        //add all its successors to the queue
        Node[] arPost = htPostsets.get(net).get(curr);
        for(Node succ: arPost)
        {
            //if curr is the last transition of a loop
            if(curr instanceof Transition && htLastNodes.get(net).contains(curr))
                continue;
            //add succ to queue for traversal
            if(!queue.contains(succ))
                queue.add(succ);
        }
    }

    /**
     * initialize, numberingNodes, deduceRelationMatrix must be called first
     * @param netP
     * @param netQ
     * @return the similarity betwen netP and netQ
     */
    public float similarity(NetSystem netP, NetSystem netQ)
    {
        float result = 0.0f;

        //get the relation matrix of P
        HashMap<Transition, HashMap<Transition, Integer>> htRM_P = htMatrices.get(netP);
        //get the relation matrix of Q
        HashMap<Transition, HashMap<Transition, Integer>> htRM_Q = htMatrices.get(netQ);

        //1.1: calculate the intersection of transitions of P and Q
        HashMap<String, Transition> htTransP = new HashMap();
        Transition[] arTransP = htTransitions.get(netP);
        Transition[] arTransQ = htTransitions.get(netQ);
        //store all transitions of P in a hashmap
        for(Transition transP: arTransP)
            htTransP.put(transP.getId(), transP);
        //store the mapping of the transitions from P to Q
        HashMap<Transition, Transition> hmPtoQ = new HashMap();
        for(Transition transQ: arTransQ)
        {
            //should not be a silent transition
            if(isSilentTrans(transQ))
                continue;
            Transition transP = htTransP.get(transQ.getId());
            if(transP != null)
                hmPtoQ.put(transP, transQ);
        }

        //1.2: calculate the intersection of RM_P and RM_Q
        Set<Transition> setCommon = hmPtoQ.keySet();
        for(Transition transP: setCommon)
        {
            for(Transition transQ: setCommon)
            {
                int row = htRM_P.get(transP).get(transQ);
                int col = htRM_Q.get(transP).get(transQ);
                result += TaskRelation.WEIGHTOFRELATIONS[row][col];
            }
        }

        //1.3: calculate the similarity of P and Q
        result = result/(htRM_P.size()*htRM_P.size() + htRM_Q.size()*htRM_Q.size() - result);

        return result;
    }

    public static void main(String[] args)
    {
        PNMLSerializer pnmlSerializer = new PNMLSerializer();

        //load the models one by one in a given folder
        File folder = new File("Models/efficacy");
        File[] arModels = folder.listFiles(new FileNameSelector("pnml"));

        long nTotalCostBPP = 0;
        long nTotalCostBP = 0;
        int nTotalTrans = 0;
        int nMaxTrans = 0;
        int nMinTrans = Integer.MAX_VALUE;
        int nTotalPlace = 0;
        int nMaxPlace = 0;
        int nMinPlace = Integer.MAX_VALUE;
        int nTotalArc = 0;
        int nMaxArc = 0;
        int nMinArc = Integer.MAX_VALUE;
        for(int i=0; i<arModels.length; i++)
        {
            String fpModelP = arModels[i].getName();
            String filepathP = arModels[i].getAbsolutePath();
            NetSystem netP = pnmlSerializer.parse(filepathP);

            //move it if it is a WF-net with multiple source places
            if(netP.getSourceNodes().size() > 1)
            {
                arModels[i].renameTo(new File(folder + "/Not-WF-net/" + fpModelP));
                System.out.println(fpModelP);

                continue;
            }

            //count the features of the model sets
            int nTrans = netP.getTransitions().size();
            nTotalTrans += nTrans;
            if(nTrans < nMinTrans)
                nMinTrans = nTrans;
            if(nTrans > nMaxTrans)
                nMaxTrans = nTrans;

            int nPlace = netP.getPlaces().size();
            nTotalPlace += nPlace;
            if(nPlace < nMinPlace)
                nMinPlace = nPlace;
            if(nPlace > nMaxPlace)
                nMaxPlace = nPlace;

            int nArc = netP.getEdges().size();
            nTotalArc += nArc;
            if(nArc < nMinArc)
                nMinArc = nArc;
            if(nArc > nMaxArc)
                nMaxArc = nArc;

            for (int j = i; j < arModels.length; j++)
            {
                String fpModelQ = arModels[j].getName();
                String filepathQ = arModels[j].getAbsolutePath();
                NetSystem netQ = pnmlSerializer.parse(filepathQ);

                System.out.println("======= " + fpModelP + " : " + fpModelQ + " =======");
                BPPlusSimilarity bpp = new BPPlusSimilarity();
                BehavioralProfileSimilarity bp = new BehavioralProfileSimilarity();

                long minInit = Long.MAX_VALUE, minNumb = Long.MAX_VALUE, minDedu = Long.MAX_VALUE, minSim = Long.MAX_VALUE;
                long minBP = Long.MAX_VALUE;
                float similarityBPP = 0.0f;
                float similarityBP = 0.0f;
                for (int n = 0; n <= 20; n++)
                {
                    long before = 0, after = 0;

                    before = System.nanoTime();
                    bpp.initialize(netP);
                    bpp.initialize(netQ);
                    after = System.nanoTime();
                    if (after - before < minInit)
                        minInit = after - before;

                    before = System.nanoTime();
                    bpp.numberingNodes(netP, false);
                    bpp.numberingNodes(netQ, false);
                    after = System.nanoTime();
                    if (after - before < minNumb)
                        minNumb = after - before;

                    before = System.nanoTime();
                    bpp.deduceRelationMatrix(netP);
                    bpp.deduceRelationMatrix(netQ);
                    after = System.nanoTime();
                    if (after - before < minDedu)
                        minDedu = after - before;

                    before = System.nanoTime();
                    similarityBPP = bpp.similarity(netP, netQ);
                    after = System.nanoTime();
                    if (after - before < minSim)
                        minSim = after - before;

                    before = System.nanoTime();
                    similarityBP = bp.similarity(netP, netQ);
                    after = System.nanoTime();
                    if (after - before < minBP)
                        minBP = after - before;
                }

                System.out.println("BP+: " + similarityBPP);
                System.out.println("BP: " + similarityBP);
//                System.out.println("initializing: " + minInit);
//                System.out.println("numbering: " + minNumb);
//                System.out.println("deducing: " + minDedu);
//                System.out.println("similarity: " + minSim);
                long nOneCostBPP = minInit + minNumb + minDedu + minSim;
                System.out.println("cost of BP+: " + nOneCostBPP);
                System.out.println("cost of BP: " + minBP);
                System.out.println("nTrans: " + nTrans);
                System.out.println("nPlace: " + nPlace);
                System.out.println("nArc: " + nArc);

                //store this cost for computing the similarity of one pair
                nTotalCostBPP += nOneCostBPP;
                nTotalCostBP += minBP;
            }
        }

        System.out.println("====== Total Cost ======");
        System.out.println("Total cost of BP+: " + nTotalCostBPP);
        System.out.println("Total cost of BP: " + nTotalCostBP);
        System.out.println("nTotalTrans: " + nTotalTrans);
        System.out.println("nTotalPlace: " + nTotalPlace);
        System.out.println("nTotalArc: " + nTotalArc);

        float nAverageCostBPP = nTotalCostBPP*1.0f/arModels.length;
        float nAverageCostBP = nTotalCostBP*1.0f/arModels.length;
//        float nAverageCostBPP = nTotalCostBPP*2.0f/(arModels.length*(arModels.length+1));
//        float nAverageCostBP = nTotalCostBP*2.0f/(arModels.length*(arModels.length+1));
        System.out.println("Avg cost of BP+: " + nAverageCostBPP);
        System.out.println("Avg cost of BP: " + nAverageCostBP);
        System.out.println("nTotalModels: " + arModels.length);
        System.out.println("nMaxTrans: " + nMaxTrans);
        System.out.println("nAvgTrans: " + nTotalTrans*1.0f/arModels.length);
        System.out.println("nMinTrans: " + nMinTrans);
        System.out.println("nMaxPlace: " + nMaxPlace);
        System.out.println("nAvgPlace: " + nTotalPlace*1.0f/arModels.length);
        System.out.println("nMinPlace: " + nMinPlace);
        System.out.println("nMaxArc: " + nMaxArc);
        System.out.println("nAvgArc: " + nTotalArc*1.0f/arModels.length);
        System.out.println("nMinArc: " + nMinArc);
    }
}