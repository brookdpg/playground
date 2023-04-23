import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MyProfiler {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path recordingFile = Path.of("recording.jfr");

        // Start recording
        ProcessBuilder pb = new ProcessBuilder("java", "-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:StartFlightRecording=disk=true,filename=" + recordingFile.toAbsolutePath(), "-jar", "myapp.jar");
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();

        // Wait for some time while the application is running
        TimeUnit.SECONDS.sleep(30);

        // Stop recording
        ProcessBuilder pb2 = new ProcessBuilder("jcmd", Integer.toString(process.pid()), "JFR.stop", "filename=" + recordingFile.toAbsolutePath());
        pb2.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb2.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb2.start();

        // Print the call graph
        ProcessBuilder pb3 = new ProcessBuilder("jfr", "print", recordingFile.toAbsolutePath().toString());
        pb3.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb3.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb3.start();

        // Delete the recording file
        //Files.deleteIfExists(recordingFile);
    }
}
