package com.example.music;

import com.musicg.dsp.Resampler;
import com.musicg.fingerprint.PairManager;
import com.musicg.math.rank.MapRank;
import com.musicg.math.rank.MapRankInteger;
import com.musicg.processor.TopManyPointsProcessorChain;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



public class DataHelper {
    private FingerprintParameters fingerprintParameters = FingerprintParameters.getInstance();
    private int sampleSizePerFrame= fingerprintParameters.getSampleSizePerFrame();

    private int overlapFactor= fingerprintParameters.getOverlapFactor();

    private int numRobustPointsPerFrame = fingerprintParameters.getNumRobustPointsPerFrame();

    private  int numFilterBanks = fingerprintParameters.getNumFilterBanks();

    public DataHelper(){

    }

    public byte[] extractFingerPrint(Wave wave){

        int[][] coordinates;	// coordinates[x][0..3]=y0..y3
        byte[] fingerprint = new byte[0];

        // resample to target rate
        Resampler resampler=new Resampler();
        int sourceRate = wave.getWaveHeader().getSampleRate(); //8000
        int targetRate = fingerprintParameters.getSampleRate();

        byte[] resampledWaveData=resampler.reSample(wave.getBytes(), wave.getWaveHeader().getBitsPerSample(), sourceRate, targetRate);

        // update the wave header
        WaveHeader resampledWaveHeader=wave.getWaveHeader();
        resampledWaveHeader.setSampleRate(targetRate);

        // make resampled wave
        Wave resampledWave=new Wave(resampledWaveHeader,resampledWaveData);
        // end resample to target rate

        // get spectrogram's data
        Spectrogram spectrogram=resampledWave.getSpectrogram(sampleSizePerFrame, overlapFactor);
        double[][] spectorgramData=spectrogram.getNormalizedSpectrogramData();

        List<Integer>[] pointsLists=getRobustPointList(spectorgramData);
        int numFrames=pointsLists.length;

        // prepare fingerprint bytes
        coordinates=new int[numFrames][numRobustPointsPerFrame];

        for (int x=0; x<numFrames; x++){
            if (pointsLists[x].size()==numRobustPointsPerFrame){
                Iterator<Integer> pointsListsIterator=pointsLists[x].iterator();
                for (int y=0; y<numRobustPointsPerFrame; y++){
                    coordinates[x][y]=pointsListsIterator.next();
                }
            }
            else{
                // use -1 to fill the empty byte
                for (int y=0; y<numRobustPointsPerFrame; y++){
                    coordinates[x][y]=-1;
                }
            }
        }
        // end make fingerprint

        // for each valid coordinate, append with its intensity
        List<Byte> byteList=new LinkedList<Byte>();
        for (int i=0; i<numFrames; i++){
            for (int j=0; j<numRobustPointsPerFrame; j++){
                if (coordinates[i][j]!=-1){
                    // first 2 bytes is x
                    int x=i;
                    byteList.add((byte)(x>>8));
                    byteList.add((byte)x);

                    // next 2 bytes is y
                    int y=coordinates[i][j];
                    byteList.add((byte)(y>>8));
                    byteList.add((byte)y);

                    // next 4 bytes is intensity
                    int intensity=(int)(spectorgramData[x][y]*Integer.MAX_VALUE);	// spectorgramData is ranged from 0~1
                    byteList.add((byte)(intensity>>24));
                    byteList.add((byte)(intensity>>16));
                    byteList.add((byte)(intensity>>8));
                    byteList.add((byte)intensity);
                }
            }
        }
        // end for each valid coordinate, append with its intensity

        fingerprint=new byte[byteList.size()];
        Iterator<Byte> byteListIterator=byteList.iterator();
        int pointer=0;
        while(byteListIterator.hasNext()){
            fingerprint[pointer++]=byteListIterator.next();
        }

        return fingerprint;
    }

    private List<Integer>[] getRobustPointList(double[][] spectrogramData){

        int numX=spectrogramData.length;
        int numY=spectrogramData[0].length;

        double[][] allBanksIntensities=new double[numX][numY];
        int bandwidthPerBank=numY/numFilterBanks;

        for (int b=0; b<numFilterBanks; b++){

            double[][] bankIntensities=new double[numX][bandwidthPerBank];

            for (int i=0; i<numX; i++){
                for (int j=0; j<bandwidthPerBank; j++){
                    bankIntensities[i][j]=spectrogramData[i][j+b*bandwidthPerBank];
                }
            }

            // get the most robust point in each filter bank
            TopManyPointsProcessorChain processorChain=new TopManyPointsProcessorChain(bankIntensities,1);
            double[][] processedIntensities=processorChain.getIntensities();

            for (int i=0; i<numX; i++){
                for (int j=0; j<bandwidthPerBank; j++){
                    allBanksIntensities[i][j+b*bandwidthPerBank]=processedIntensities[i][j];
                }
            }
        }

        List<int[]> robustPointList=new LinkedList<int[]>();

        // find robust points
        for (int i=0; i<allBanksIntensities.length; i++){
            for (int j=0; j<allBanksIntensities[i].length; j++){
                if (allBanksIntensities[i][j]>0){

                    int[] point=new int[]{i,j};
                    //System.out.println(i+","+frequency);
                    robustPointList.add(point);
                }
            }
        }
        // end find robust points

        List<Integer>[] robustLists=new LinkedList[spectrogramData.length];
        for (int i=0; i<robustLists.length; i++){
            robustLists[i]=new LinkedList<Integer>();
        }

        // robustLists[x]=y1,y2,y3,...
        Iterator<int[]> robustPointListIterator=robustPointList.iterator();
        while (robustPointListIterator.hasNext()){
            int[] coor=robustPointListIterator.next();
            robustLists[coor[0]].add(coor[1]);
        }

        // return the list per frame
        return robustLists;
    }

    public HashMap<Integer,List<Integer>> fingerPrintToHashMap(byte[] temp){
        PairManager pairManager=new PairManager();
        HashMap<Integer,List<Integer>> hashMapAudio;
        hashMapAudio = pairManager.getPair_PositionList_Table(temp);
        return hashMapAudio;
    }

    /*private List<int[]> getPairPositionList(byte[] fingerprint){

        int numFrames=FingerprintManager.getNumFrames(fingerprint);
        int anchorPointsIntervalLength = 4;
        int numAnchorPointsPerInterval =10;
        int maxPairs = 1;
        int maxTargetZoneDistance=4;
        int upperBoundedFrequency=1500;
        int lowerBoundedFrequency=400;
        int fps=5;
        int numFilterBanks=4;
        int numFrequencyUnits=(upperBoundedFrequency-lowerBoundedFrequency+1)/fps+1;
        int bandwidthPerBank = numFrequencyUnits / numFilterBanks;
        // table for paired frames
        byte[] pairedFrameTable=new byte[numFrames/anchorPointsIntervalLength+1];	// each second has numAnchorPointsPerSecond pairs only
        // end table for paired frames

        List<int[]> pairList=new LinkedList<int[]>();
        List<int[]> sortedCoordinateList=getSortedCoordinateList(fingerprint);

        Iterator<int[]> anchorPointListIterator=sortedCoordinateList.iterator();
        while (anchorPointListIterator.hasNext()){
            int[] anchorPoint=anchorPointListIterator.next();
            int anchorX=anchorPoint[0];
            int anchorY=anchorPoint[1];
            int numPairs=0;

            Iterator<int[]> targetPointListIterator=sortedCoordinateList.iterator();
            while (targetPointListIterator.hasNext()){

                if (numPairs>=maxPairs){
                    break;
                }

                if (isReferencePairing && pairedFrameTable[anchorX/anchorPointsIntervalLength]>=numAnchorPointsPerInterval){
                    break;
                }

                int[] targetPoint=targetPointListIterator.next();
                int targetX=targetPoint[0];
                int targetY=targetPoint[1];

                if (anchorX==targetX && anchorY==targetY){
                    continue;
                }

                // pair up the points
                int x1,y1,x2,y2;	// x2 always >= x1
                if (targetX>=anchorX){
                    x2=targetX;
                    y2=targetY;
                    x1=anchorX;
                    y1=anchorY;
                }
                else{
                    x2=anchorX;
                    y2=anchorY;
                    x1=targetX;
                    y1=targetY;
                }

                // check target zone
                if ((x2-x1)>maxTargetZoneDistance){
                    continue;
                }
                // end check target zone

                // check filter bank zone
                if (!(y1/bandwidthPerBank == y2/bandwidthPerBank)){
                    continue;	// same filter bank should have equal value
                }
                // end check filter bank zone

                int pairHashcode=(x2-x1)*numFrequencyUnits*numFrequencyUnits+y2*numFrequencyUnits+y1;

                // stop list applied on sample pairing only
                if (!isReferencePairing && stopPairTable.containsKey(pairHashcode)){
                    numPairs++;	// no reservation
                    continue;	// escape this point only
                }
                // end stop list applied on sample pairing only

                // pass all rules
                pairList.add(new int[]{pairHashcode,anchorX});
                pairedFrameTable[anchorX/anchorPointsIntervalLength]++;
                //System.out.println(anchorX+","+anchorY+"&"+targetX+","+targetY+":"+pairHashcode+" ("+pairedFrameTable[anchorX/anchorPointsIntervalLength]+")");
                numPairs++;
                // end pair up the points
            }
        }

        return pairList;
    }

    public HashMap<Integer,List<Integer>> getPair_PositionList_Table(byte[] fingerprint){

        List<int[]> pairPositionList=getPairPositionList(fingerprint);

        // table to store pair:pos,pos,pos,...;pair2:pos,pos,pos,....
        HashMap<Integer,List<Integer>> pair_positionList_table=new HashMap<Integer,List<Integer>>();

        // get all pair_positions from list, use a table to collect the data group by pair hashcode
        Iterator<int[]> pairPositionListIterator=pairPositionList.iterator();
        while (pairPositionListIterator.hasNext()){
            int[] pair_position=pairPositionListIterator.next();
            //System.out.println(pair_position[0]+","+pair_position[1]);

            // group by pair-hashcode, i.e.: <pair,List<position>>
            if (pair_positionList_table.containsKey(pair_position[0])){
                pair_positionList_table.get(pair_position[0]).add(pair_position[1]);
            }
            else{
                List<Integer> positionList=new LinkedList<Integer>();
                positionList.add(pair_position[1]);
                pair_positionList_table.put(pair_position[0], positionList);
            }
            // end group by pair-hashcode, i.e.: <pair,List<position>>
        }
        // end get all pair_positions from list, use a table to collect the data group by pair hashcode

        return pair_positionList_table;
    }*/

    public static int getNumFrames(byte[] fingerprint){

        if (fingerprint.length<8){
            return 0;
        }

        // get the last x-coordinate (length-8&length-7)bytes from fingerprint
        int numFrames=((int)(fingerprint[fingerprint.length-8]&0xff)<<8 | (int)(fingerprint[fingerprint.length-7]&0xff))+1;
        return numFrames;
    }

    public double getSimilarFromHash(byte[] fingerprint1, HashMap<Integer,List<Integer>> data){
        HashMap<Integer,Integer> offset_Score_Table=new HashMap<Integer,Integer>();	// offset_Score_Table<offset,count>
        int numFrames= getNumFrames(fingerprint1);
        float score=0;
        int mostSimilarFramePosition=Integer.MIN_VALUE;

        // one frame may contain several points, use the shorter one be the denominator


        // get the pairs
        PairManager pairManager=new PairManager();
        HashMap<Integer,List<Integer>> this_Pair_PositionList_Table=pairManager.getPair_PositionList_Table(fingerprint1);


        for (int compareWaveHashNumber : data.keySet()) {
            // if the compareWaveHashNumber doesn't exist in both tables, no need to compare
            if (!this_Pair_PositionList_Table.containsKey(compareWaveHashNumber)
                    || !data.containsKey(compareWaveHashNumber)) {
                continue;
            }

            // for each compare hash number, get the positions
            List<Integer> wavePositionList = this_Pair_PositionList_Table.get(compareWaveHashNumber);
            List<Integer> compareWavePositionList = data.get(compareWaveHashNumber);


            for (int thisPosition : wavePositionList) {
                for (int compareWavePosition : compareWavePositionList) {
                    int offset = thisPosition - compareWavePosition;

                    if (offset_Score_Table.containsKey(offset)) {
                        offset_Score_Table.put(offset, offset_Score_Table.get(offset) + 1);
                    } else {
                        offset_Score_Table.put(offset, 1);
                    }
                }
            }
        }

        // map rank
        MapRank mapRank=new MapRankInteger(offset_Score_Table,false);

        // get the most similar positions and scores
        List<Integer> orderedKeyList=mapRank.getOrderedKeyList(100, true);
        int orderedKeyListSize = orderedKeyList.size();
        if (orderedKeyListSize > 0){
            int key=orderedKeyList.get(0);
            // get the highest score position
            mostSimilarFramePosition=key;
            score=offset_Score_Table.get(key);

            // accumulate the scores from neighbours
            if (offset_Score_Table.containsKey(key-1)){
                score+=offset_Score_Table.get(key-1)/2;
            }
            if (offset_Score_Table.containsKey(key+1)){
                score+=offset_Score_Table.get(key+1)/2;
            }
        }

		/*
		Iterator<Integer> orderedKeyListIterator=orderedKeyList.iterator();
		while (orderedKeyListIterator.hasNext()){
			int offset=orderedKeyListIterator.next();
			System.out.println(offset+": "+offset_Score_Table.get(offset));
		}
		*/

        score/=numFrames;
        float similarity=score;
        // similarity >1 means in average there is at least one match in every frame
        if (similarity>1){
            similarity=1;
        }
        return similarity;
    }


}
