package com.example.music;

import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.fingerprint.FingerprintSimilarityComputer;

public class ParallelProcess implements java.util.concurrent.Callable<Double>{
    private byte[] finger1,finger2;

    public ParallelProcess(byte[] fingerPrint1, byte[] fingerPrint2){
        this.finger1 = fingerPrint1;
        this.finger2 = fingerPrint2;
    }
    @Override
    public Double call() throws Exception {
        double sim;
        FingerprintSimilarityComputer fingerprintSimilarityComputer = new FingerprintSimilarityComputer(this.finger1,this.finger2);
        FingerprintSimilarity fingerprintSimilarity = fingerprintSimilarityComputer.getFingerprintsSimilarity();
        sim = fingerprintSimilarity.getSimilarity();
        return sim;
    }
}
