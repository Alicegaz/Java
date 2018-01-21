import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.regex.Pattern;

class Summ{
    private static Double sum = 0.0;
    public static synchronized void add(Double d, String name){
        sum+=d;
        long mil = System.currentTimeMillis() - Multithreading.start;
        long second = (mil / 1000) % 60;
        long minute = (mil / (1000 * 60)) % 60;
        long hour = (mil / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, mil);
        System.out.println("thread " + name + " Time taken: " + time + " sum " + sum+" ");

    }

}
class Processor implements Runnable{
    private File file;
    volatile public double sum = 0;
    BufferedReader reader;
    private CountDownLatch latch;
    public final Object lock = new Object();
    public Processor(String file, CountDownLatch latch){
        this.file = new File("src/Resourc/"+file);
        this.latch = latch;
        reader = null;
    }
    public boolean isNumeric(String str)
    {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        } return true;
    }
    public boolean even(Double n){
        if((n%2) == 0) {
            return true;
        } else
            return false;
    }

    public void preprocess(String s, String name){
        String regex = "[0-9-\\s]+";
        Pattern t = Pattern.compile(regex);
        String error = "wrong format, unrecognized symbols in "+s;
        if (t.matcher(s).matches())
        {
        String[] tokens = s.split(" ");
        for (String number: tokens) {
            if (number.charAt(0) != '-' && isNumeric(number) && even(Double.parseDouble(number))) {
                    Multithreading.summ.add(Double.parseDouble(number), name);
                }
        }
        }
        else throw new IllegalArgumentException(error);
    }
    public void run(){
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                try {
                    preprocess(text, file.getName());
                }
                catch(IllegalArgumentException ex){
                    System.out.println(ex);
                    break;
                }
            }
            latch.countDown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();

                }
            } catch (IOException e) {
            }
            latch.countDown();
        }


    }}

public class Multithreading {
    final public static long start = System.currentTimeMillis();
    public static ArrayList<String> files = new ArrayList<String>();
    final static public Summ summ = new Summ();
    public static void listFilesForFolder(final File folder){
        for (final File fileEntry: folder.listFiles()){
            if(fileEntry.isDirectory()){
                listFilesForFolder(fileEntry);
            }
            else{
                files.add(fileEntry.getName());
            }
        }
    }

    public static void main(String[] args){
        final File folder = new File("src/Resourc");
        listFilesForFolder(folder);
        ExecutorService threadPool = Executors.newFixedThreadPool(files.size());
        //ExecutorCompletionService<Long> tasks = new ExecutorCompletionService<Long>(threadPool)
        CountDownLatch latch = new CountDownLatch(files.size());
        //System.out.println(files.size());
        for(int i = 1; i < files.size(); i++){
            threadPool.submit(new Processor(files.get(i), latch));
        }
        threadPool.shutdown();
        try{
            latch.await();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
