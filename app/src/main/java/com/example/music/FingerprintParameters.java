package com.example.music;


public class FingerprintParameters {
    protected static FingerprintParameters instance = null;
    private final int numRobustPointsPerFrame=4;	// number of points in each frame, i.e. top 4 intensities in fingerprint
    private final int sampleSizePerFrame=2048;	// number of audio samples in a frame, it is suggested to be the FFT Size
    private final int overlapFactor=4;	// 8 means each move 1/8 nSample length. 1 means no overlap, better 1,2,4,8 ...	32
    private final int numFilterBanks=4;

    private final int upperBoundedFrequency=1500;	// low pass
    private final int lowerBoundedFrequency=400;	// high pass
    private final int fps=5;	// in order to have 5fps with 2048 sampleSizePerFrame, wave's sample rate need to be 10240 (sampleSizePerFrame*fps)
    private final int sampleRate=sampleSizePerFrame*fps;	// the audio's sample rate needed to resample to this in order to fit the sampleSizePerFrame and fps
    private final int numFramesInOneSecond=overlapFactor*fps;	// since the overlap factor affects the actual number of fps, so this value is used to evaluate how many frames in one second eventually

    private final int  refMaxActivePairs=1;	// max. active pairs per anchor point for reference songs
    private final int sampleMaxActivePairs=10;	// max. active pairs per anchor point for sample clip
    private final int numAnchorPointsPerInterval=10;
    private final int anchorPointsIntervalLength=4;	// in frames (5fps,4 overlap per second)
    private final int maxTargetZoneDistance=4;	// in frame (5fps,4 overlap per second)

    private int numFrequencyUnits=(upperBoundedFrequency-lowerBoundedFrequency+1)/fps+1;	// num frequency units

    public static FingerprintParameters getInstance() {
        if (instance == null){
            synchronized(FingerprintParameters.class){
                if(instance == null) {
                    instance = new FingerprintParameters();
                }
            }
        }
        return instance;
    }

    public int getNumRobustPointsPerFrame() {
        return numRobustPointsPerFrame;
    }

    public int getSampleSizePerFrame() {
        return sampleSizePerFrame;
    }

    public int getOverlapFactor() {
        return overlapFactor;
    }

    public int getNumFilterBanks() {
        return numFilterBanks;
    }

    public int getUpperBoundedFrequency() {
        return upperBoundedFrequency;
    }

    public int getLowerBoundedFrequency() {
        return lowerBoundedFrequency;
    }

    public int getFps() {
        return fps;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getNumFramesInOneSecond() {
        return numFramesInOneSecond;
    }

    public int getRefMaxActivePairs() {
        return refMaxActivePairs;
    }

    public int getSampleMaxActivePairs() {
        return sampleMaxActivePairs;
    }

    public int getNumAnchorPointsPerInterval() {
        return numAnchorPointsPerInterval;
    }

    public int getAnchorPointsIntervalLength() {
        return anchorPointsIntervalLength;
    }

    public int getMaxTargetZoneDistance() {
        return maxTargetZoneDistance;
    }

    public int getNumFrequencyUnits() {
        return numFrequencyUnits;
    }

    public int getMaxPossiblePairHashcode(){
        return maxTargetZoneDistance*numFrequencyUnits*numFrequencyUnits+numFrequencyUnits*numFrequencyUnits+numFrequencyUnits;
    }
}
