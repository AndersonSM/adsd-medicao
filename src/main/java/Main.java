import net.bramp.ffmpeg.*;
import com.sun.management.OperatingSystemMXBean;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.lang.management.ManagementFactory;
import java.sql.Time;
import java.util.Date;

import static java.lang.System.out;

/**
 * Created by Anderson on 12/12/2018.
 */
public class Main {

    static boolean finished = false;

    static OperatingSystemMXBean osBean;

    static double averageJvmCpu = 0;

    static double averageCpu = 0;

    static double initialFreeMemoery;

    static double finalFreeMemoery;

    public static void main(String[] args) {
        osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        printJvmCpu(osBean);
        printCpu(osBean);
        printMemory(osBean);
        initialFreeMemoery = osBean.getFreePhysicalMemorySize();
        new Thread(ffmpegThread).start();
        new Thread(infoThread).start();
    }

    private static Runnable ffmpegThread = new Runnable() {
        public void run() {
            try {
                FFmpeg ffmpeg = new FFmpeg("C:\\Users\\Anderson\\Downloads\\ffmpeg\\bin\\ffmpeg");
                FFprobe ffprobe = new FFprobe("C:\\Users\\Anderson\\Downloads\\ffmpeg\\bin\\ffprobe");

                FFmpegBuilder builder = new FFmpegBuilder()
                        .setInput("C:\\Users\\Anderson\\Documents\\Universidade\\ADSD\\ffmpeg-measurement\\src\\main\\resources\\video.mp4")     // Filename, or a FFmpegProbeResult
                        .overrideOutputFiles(true) // Override the output if it exists
                        .addOutput("output.mov")   // Filename for the destination
                        .setVideoCodec("libx264")     // Video using x264
                        .setVideoFrameRate(25, 1)     // at 25 frames per second
                        .setVideoResolution(1280, 720) // resolution
                        .done();

                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

                out.println("STARTED");
                long elapsedTime = System.currentTimeMillis();
                // Run a one-pass encode
                executor.createJob(builder).run();

                elapsedTime = System.currentTimeMillis() - elapsedTime;
                finished = true;
                out.println("FINISHED");
                out.println("Time: " + elapsedTime);
            } catch (Exception e) {
                out.print(e.getMessage());
                finished = true;
            }
        }
    };

    private static Runnable infoThread = new Runnable() {
        public void run() {
            try {
                while (!finished) {
                    if (averageCpu == 0) {
                        averageJvmCpu = osBean.getProcessCpuLoad();
                        averageCpu = osBean.getSystemCpuLoad();
                    } else {
                        averageJvmCpu = (averageJvmCpu + osBean.getProcessCpuLoad()) / 2;
                        averageCpu = (averageCpu + osBean.getSystemCpuLoad()) / 2;
                    }
                    //printJvmCpu(osBean);
                    //printCpu(osBean);
                    Thread.sleep(1000);
                }
                finalFreeMemoery = osBean.getFreePhysicalMemorySize();
                Thread.sleep(2000);
                //out.println("Average JVM CPU %: " + averageJvmCpu);
                out.println("Average CPU %: " + averageCpu);
                out.println("Memory used: " + ((initialFreeMemoery - finalFreeMemoery) / 1024 / 1024) + " MB");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public static void printJvmCpu(OperatingSystemMXBean osBean) {
        // What % CPU load this current JVM is taking, from 0.0-1.0
        out.println("JVM CPU %: " + osBean.getProcessCpuLoad());
    }

    public static void printCpu(OperatingSystemMXBean osBean) {
        // What % load the overall system is at, from 0.0-1.0
        out.println("CPU %: " + osBean.getSystemCpuLoad());
    }

    public static void printMemory(OperatingSystemMXBean osBean) {
        out.println("Memory %: " + (osBean.getFreePhysicalMemorySize() / 1024 / 1024));
    }
}
