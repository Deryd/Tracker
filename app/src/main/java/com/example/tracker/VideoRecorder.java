package com.example.tracker;


import java.io.File;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface; 

public class VideoRecorder {
	
	private MediaRecorder recorder;
	
	public VideoRecorder()
    {
    }

    public void open()
    {
        recorder = new MediaRecorder();
    }
    
    public void close()
    {
		if (recorder != null)
		{
		    recorder.release();
		    recorder = null;
		}
    }
    
    public void start(String fileName)
    {
    	File saveDir = new File("/sdcard/RoadRec/");

        if (!saveDir.exists())
        {
            saveDir.mkdirs();
        }
    	recorder.setOutputFile(fileName);
	
	try
        {
	    	recorder.prepare();
            recorder.start();
        }
        catch (IllegalStateException e)
        {
        	e.printStackTrace();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }

    public void stop()
    {
    	if(recorder != null)
    	{
    		//recorder.stop();
    		recorder.reset();
    	}
    }
    
    public void setPreview(Surface surface)
    {
    	recorder.setPreviewDisplay(surface);
    }
    
    public void setCamera(Camera camera)
    {
    	recorder.setCamera(camera);	
    }
    
    public void setRecorderParams()
    {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    }

}
