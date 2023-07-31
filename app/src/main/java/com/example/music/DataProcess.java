package com.example.music;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataProcess {

    String inFileName;
    String outFileName;
    private static final int SAMPLING_RATE_IN_HZ = 11025;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;  //Single Channel
    //private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //Double Channel
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,CHANNEL_CONFIG,AUDIO_FORMAT);

    public DataProcess(String in, String out){
        this.inFileName = in;
        this.outFileName = out;
    }
    public DataProcess(){
        //default constructor
    }

    public void AddWavHeader(String inputFile, String outFile){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        long AudioLen;
        long DataLen;
        long byteRate = 16 * SAMPLING_RATE_IN_HZ * 1 / 8;
        byte[] data = new byte[BUFFER_SIZE];

        try{
            fis = new FileInputStream(inputFile);
            fos = new FileOutputStream(outFile);
            AudioLen = fis.getChannel().size();
            DataLen = AudioLen + 36;
            WriteWaveFileHeader(fos,AudioLen,DataLen,SAMPLING_RATE_IN_HZ,2,byteRate);
            while(fis.read(data)!= -1){
                fos.write(data);
            }
            fis.close();
            fos.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out,long audioLength, long dataLength, long sampleRates, long numChannels,long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (dataLength & 0xff);
        header[5] = (byte) ((dataLength >> 8) & 0xff);
        header[6] = (byte) ((dataLength >> 16) & 0xff);
        header[7] = (byte) ((dataLength >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) numChannels;
        header[23] = 0;
        header[24] = (byte) (sampleRates & 0xff);
        header[25] = (byte) ((sampleRates >> 8) & 0xff);
        header[26] = (byte) ((sampleRates >> 16) & 0xff);
        header[27] = (byte) ((sampleRates >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (audioLength& 0xff);
        header[41] = (byte) ((audioLength >> 8) & 0xff);
        header[42] = (byte) ((audioLength >> 16) & 0xff);
        header[43] = (byte) ((audioLength >> 24) & 0xff);

        out.write(header,0,44);

    }

    private void writeWaveFileHeader(OutputStream os, int audioLength, int dataLength, long sampleRates, long numChannels,int byteRate) throws IOException {
        int HEADER_SIZE = 44;
        // Calculate the total file size
        //int fileSize = HEADER_SIZE + data.length - 8; // Subtract 8 because we don't count the RIFF ID or the file size field itself

        // Write the RIFF chunk
        writeString(os, "RIFF");             // Chunk ID
        writeInt(os, dataLength);             // Chunk Size
        writeString(os, "WAVE");             // Format

        // Write the format chunk
        writeString(os, "fmt ");             // Subchunk ID
        writeInt(os, 16);                   // Subchunk Size
        writeShort(os, (short) 1);           // Audio Format (PCM = 1)
        writeShort(os, (short) 1); // Number of Channels
        writeInt(os, SAMPLING_RATE_IN_HZ);            // Sample Rate
        writeInt(os, byteRate); // Byte Rate
        writeShort(os, (short) (numChannels * 16 / 8));  // Block Align
        writeShort(os, (short) 16);                      // Bits per Sample

        // Write the data chunk
        writeString(os, "data");             // Subchunk ID
        writeInt(os, audioLength);           // Subchunk Size

        // Flush the output stream
        os.flush();
    }

    // Helper methods to write data types to an output stream
    private void writeString(OutputStream os, String s) throws IOException {
        byte[] bytes = s.getBytes("ASCII");
        os.write(bytes);
    }

    private void writeInt(OutputStream os, int n) throws IOException {
        os.write(n & 0xFF);
        os.write((n >> 8) & 0xFF);
        os.write((n >> 16) & 0xFF);
        os.write((n >> 24) & 0xFF);
    }

    private void writeShort(OutputStream os, short n) throws IOException {
        os.write(n & 0xFF);
        os.write((n >> 8) & 0xFF);
    }
}
