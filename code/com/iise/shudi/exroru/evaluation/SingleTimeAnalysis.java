package com.iise.shudi.exroru.evaluation;

import com.iise.shudi.bp.BehavioralProfileSimilarity;
import com.iise.shudi.exroru.RefinedOrderingRelation;
import com.iise.shudi.exroru.RormSimilarity;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SingleTimeAnalysis {
    public static final String ROOT_FOLDER = "/Users/little/Documents/论文指导/汪抒浩/CoopIS 2015/";

    public static void main(String[] args) throws Exception {
        RefinedOrderingRelation.SDA_WEIGHT = 0.0;
        RefinedOrderingRelation.IMPORTANCE = true;
        SingleTimeAnalysis sta = new SingleTimeAnalysis();

        BufferedWriter writer = new BufferedWriter(new FileWriter(ROOT_FOLDER +
            "ExRORU_SingleTimeAnalysis_SAP.csv"));
        writer.write(", totalTime || cpu1, lc1, causal1, concurrent1, sda1, importance1"
            + " || cpu2, lc2, causal2, concurrent2, sda2, importance2 || similarity || BP");
        writer.newLine();
        sta.analyze(writer);
        writer.close();
    }

    public void analyze(BufferedWriter writer) throws Exception {
        //File[] dgFiles = new File(ROOT_FOLDER + "DG").listFiles();
        //File[] tcFiles = new File(ROOT_FOLDER + "TC").listFiles();
        File[] sapFiles = new File(ROOT_FOLDER + "SAP").listFiles();

        // load net systems
        //List<NetSystem> dgNets = loadNets(dgFiles);
        //List<NetSystem> tcNets = loadNets(tcFiles);
        List<NetSystem> sapNets = loadNets(sapFiles);

        // compute time
        //computeTime(writer, dgNets);
        //computeTime(writer, tcNets);
        computeTime(writer, sapNets);
    }

    private List<NetSystem> loadNets(File[] files) throws Exception {
        List<NetSystem> nets = new ArrayList<>();
        PNMLSerializer pnmlSerializer = new PNMLSerializer();
        for(int i = 0; i < files.length; ++i) {
        		if(files[i].isDirectory())
        			continue;
            NetSystem net = pnmlSerializer.parse(files[i].getAbsolutePath());
            net.setName(files[i].getName());
            nets.add(net);
        }
        return nets;
    }

    private void computeTime(BufferedWriter writer, List<NetSystem> nets) throws Exception {
        int totalCount = nets.size() * (nets.size() - 1) / 2, finish = 0;
        RormSimilarity rorm = new RormSimilarity();
        for(int p = 0; p < nets.size(); ++p) {
            for(int q = p + 1; q < nets.size(); ++q) {
                System.out.println((++finish) + "/" + totalCount + " " + nets.get(p).getName() + " & " + nets.get(q).getName());
                //not include the first computation cost
                rorm.similarityWithTime(nets.get(p), nets.get(q));
                long[][] timesTotal = new long[10][];
                for(int i = 0; i<timesTotal.length; i++)
                		timesTotal[i] = rorm.similarityWithTime(nets.get(p), nets.get(q));
         
                float[] times = new float[timesTotal[0].length+1];
                for (int i = 0; i < times.length-1; i++) {
                		times[i] = 0.0f;
                		for(int j=0; j<timesTotal.length; j++)
                			times[i] += timesTotal[j][i];
                		times[i] /= timesTotal.length;
                }
                //calculate the average time cost of BP
                BehavioralProfileSimilarity bps = new BehavioralProfileSimilarity();
                long before = System.nanoTime();
                for(int i=0; i<10; i++)
                		bps.similarity(nets.get(p), nets.get(q));
                long after = System.nanoTime();
                times[times.length-1] = (after - before)/10000000.0f;
                //output the average time cost
                writer.write(nets.get(p).getName() + " & " + nets.get(q).getName());
                for (float t : times) {
                    writer.write(", " + t);
                }
                writer.newLine();
            }
        }
    }
}
