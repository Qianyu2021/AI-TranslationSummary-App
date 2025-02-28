from flask import Flask, request, jsonify
import whisper
import os
import yt_dlp
from transformers import pipeline, MarianMTModel, MarianTokenizer
import glob
import threading


app = Flask(__name__)
model = whisper.load_model("medium")
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")
translator_cache = {} #cache for translation models to avoid loading multiple times
JOBS = {} #store job statuses


def summarize_transcript(transcript,  max_length=300, min_length=50):

    #use the summarization pipeline to summarize 
    try:
        summary = summarizer(transcript, max_length=max_length, min_length=min_length,  do_sample=False)
        return summary[0]["summary_text"]
    except Exception as e:
        return f"Summarization error: {str(e)}"

def translate_text(text, source_language, target_language="en"):
    """Translate the given text from source_language to target language using hugging face"""
    try:
        model_name = f"Helsinki-NLP/opus-mt-{source_language}-{target_language}"
        #cache the model to avoid reloading it every time
        if model_name not in translator_cache:
            tokenizer = MarianTokenizer.from_pretrained(model_name)
            translation_model = MarianMTModel.from_pretrained(model_name)
            translator_cache[model_name] = (tokenizer, translation_model)

        tokenizer, translation_model = translator_cache[model_name]
        translated = translation_model.generate(**tokenizer(text, return_tensors="pt", padding=True, max_length=512))
        return tokenizer.decode(translated[0], skip_special_tokens=True)
    except Exception as e:
        return f"Translation error: {str(e)}"

   #this is the video processing function
   #  
def process_job(jobId, url, action, target_language=None):
    """Handles video download, trnascription, and text processing."""
    try:
        filename = f"temple_video_{jobId}.wav"
        JOBS[jobId] = {"status": "PROCESSING", "result":None}

    
        #download the video using yt-yt_dlp
        ydl_opts = {
            "outtmpl": filename, 
            "format": "bestaudio/best",
            "postprocessors": [{
                "key": "FFmpegExtractAudio",
                "preferredcodec": "wav",
                "preferredquality": "192"
            }]
        }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.extract_info(url, download=True)

        print("Files in directory:", glob.glob("*.wav"))
        wav_files = glob.glob(f"{filename}*.wav")
        if not wav_files:
            JOBS[jobId] = {"tatus": "FAILED", "error": f"File {filename} not found"}
            return
        
        actual_filename = wav_files[0]

          # Step 2: Check if the file exists
        if not wav_files:
            print(f"File {filename} not found")
            return jsonify({"error": f"File {filename} not found"}), 500

        actual_filename = wav_files[0]  
        # Step 3: Transcribe the video using Whisper
        result = model.transcribe(actual_filename, word_timestamps=True)
        detected_language = result["language"]
        transcript = result.get("text", "")     #transcribe the video using whisper

        response = {
            "jobId":jobId,
            "detected_lanaguage": detected_language,
            "transcript": transcript
        }   

        #take actions based on user request
        if action == "summarize":
            summary = summarize_transcript(transcript)
            response["summary"] = summary

        elif action == "translate":
            if not target_language:
                JOBS[jobId] = {"status": "FAILED", "error": "Target language is required for translation"}
                return
            translated_text = translate_text(transcript, detected_language, target_language)
            response["translated_text"] = translated_text

        JOBS[jobId] = {"status": "COMPLETED", "result": response}

    except yt_dlp.utils.DownloadError:    
        JOBS[jobId] = {"status": "FAILED", "error": "Invalid URL or the website is not supported."}
       # return jsonify({"error": "Invalid URL or the website is not supported."}), 400
    except Exception as e:
        import traceback
        JOBS[jobId] = {"status": "FAILED", "error": f"An error occurred: {traceback.format_exc()}"}
        #return jsonify({"error": f"An error occurred: {traceback.format_exc()}"}), 500
    finally:  #clean up the file
        if os.path.exists(filename):
            os.remove(filename)

    return response

    
@app.route("/api/jobs", methods=["POST"])
def process_transcribe():
    data = request.get_json() # transcript is obtained, it is optional to summarize it or translate it
    
    if "jobId" not in data or "url" not in data or "action" not in data:
        return jsonify({"error": "Please provide 'jobId', 'url' and 'action' ('transcribe', 'summarize', 'translate', or 'summarize_translate')."}), 400
    jobId = data["jobId"]
    action = data["action"]
    target_language = data.get("target_language")
    JOBS[jobId] = {"status": "QUEUED"}

    # start processing in a separate thread
    threading.Thread(target=process_job, args=(jobId, data["url"], action, target_language)).start()

    return jsonify({"jobId": jobId, "status": "QUEUED"}), 202
   

@app.route("/api/jobs/<jobId>", methods=["GET"])
def get_job_status(jobId):
    """Retrieves the status and result of a transcription job."""
    job = JOBS.get(jobId)
    if job is None:
        return jsonify({"error": "Job not found"}), 404
    return jsonify(job)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)


