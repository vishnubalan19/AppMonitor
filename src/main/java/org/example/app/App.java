package org.example.app;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

import java.io.BufferedReader;

import java.io.InputStreamReader;

/**
 * Hello world!
 *
 */
public class App {

    //returns the string before the white space
    public static String findValue(String value, int i){
        int temp = i;
        while(value.charAt(temp)!=' '){
            temp++;
        }
        return value.substring(i,temp);
    }

    //returns memory usage
    public static double findMemoryUsage(double totalMemory, long [] fieldValuesArray){
        double memoryUsage =0;
        memoryUsage = ((fieldValuesArray[8]+fieldValuesArray[9])*100)/(double)totalMemory;
        return memoryUsage;
    }

    //returns cpu usage
    public static double findCpuUsage(double uptime, long[] fieldValuesArray){
        double cpuUsage = 0;
        long totalTime = fieldValuesArray[0]+fieldValuesArray[1]+fieldValuesArray[2]+fieldValuesArray[3];
        double seconds = uptime - (fieldValuesArray[4]/(double)100);
        cpuUsage = 100 * ((totalTime/(double)100)/(double) seconds);
        return cpuUsage;
    }

    public static void main( String[] args )
    {
        String value;
        Process process;
        String string,statValue,statMemValue;

        //token for system's influx
        String token = "MglMIe5ROZDDmdzIZGoUUhPtsct8Zdf_YKep_M1GQjEzRS8Akx5HbS6B1Sp-YYsULb_GSSDpzaS6FAlsTxqtkg==";

        //token for docker's influx
        //String token = "eqWXHf4eBnGcaSg6x-n_JwnPM2gkL4MyOWlwVCsZzxOOhtowigqE5L4Z51IfV77eThUKuq4YLsIluhu7270xxw==";

        String bucket = "r";
        String org = "demo";
        double uptime,totalMemory;

        while(true){
            try {

                //getting pid of the process
                process = Runtime.getRuntime().exec("pgrep influxd");
                try(BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))){
                    value = bufferedReader.readLine();
                    System.out.println(value);
                }
                process.destroy();

                //getting uptime of the system
                process = Runtime.getRuntime().exec("cat /proc/uptime");
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                    string = bufferedReader.readLine();
                    String uptimeString = findValue(string,0);
                    System.out.println("uptimeString = "+uptimeString);
                    uptime = Double.parseDouble(uptimeString);
                }
                process.destroy();

                //getting memory info of the system
                process = Runtime.getRuntime().exec("cat /proc/meminfo");
                try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                    string = bufferedReader.readLine();int i=0;
                    System.out.println(string);
                    while(i<string.length()){
                        if(Character.isDigit(string.charAt(i))){
                            break;
                        }
                        i++;
                    }
                    String totalMemoryString = string.substring(i,i+8);
                    System.out.println(totalMemoryString);
                    totalMemory = Double.parseDouble(totalMemoryString);
                }
                process.destroy();

                //getting cpu status of pid
                process = Runtime.getRuntime().exec("cat /proc/"+value+"/stat");
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                    statValue = bufferedReader.readLine();
                    System.out.println(statValue);
                }
                process.destroy();

                //getting memory status of pid
                process = Runtime.getRuntime().exec("cat /proc/"+value+"/statm");
                try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                    statMemValue = bufferedReader.readLine();
                    System.out.println(statMemValue);
                }
                process.destroy();

                String [] values = statValue.split(" ");
                String [] mValues = statMemValue.split(" ");

                //client for system's influx
                InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

                //client for docker's influx
                //InfluxDBClient client = InfluxDBClientFactory.create("http://172.22.0.2:8086", token.toCharArray());

                //storing required values
                String [] fieldArray = {"user_time","system_time","child_user_time","child_system_time","start_time","priority","nice","no_of_threads",
                        "resident","data_stack","mem_usage","cpu_usage"};
                long [] fieldValuesArray = new long[10];
                double [] fieldPerArray = new double[2];
                int k=0;
                for(int i=13;i<=19;i++){
                    fieldValuesArray[k]= Long.parseLong(values[i]);
                    k++;
                }
                fieldValuesArray[k++] = Long.parseLong(values[21]);
                fieldValuesArray[k++] = Long.parseLong(mValues[1]);
                fieldValuesArray[k++] = Long.parseLong(mValues[5]);
                k=0;
                fieldPerArray[k++] = findMemoryUsage(totalMemory,fieldValuesArray);
                fieldPerArray[k++] = findCpuUsage(uptime,fieldValuesArray);

                //writing data in the bucket
                for(int i=0;i<10;i++){
                    String outputData = "procstat,host=vishnu-12378 "+fieldArray[i]+"="+fieldValuesArray[i];
                    try (WriteApi writeApi = client.getWriteApi()) {
                        writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
                    }
                }
                k=0;
                for(int i=10;i<=11;i++){
                    String outputData = "procstat,host=vishnu-12378 "+fieldArray[i]+"="+fieldPerArray[k++];
                    try (WriteApi writeApi = client.getWriteApi()) {
                        writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
                    }
                }

            } catch (Exception e) {}
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

//        System.out.println( "Hello World!" );
//        File file = new File("/proc");
//        String[] fileNames = file.list();
//        for(String fileName : fileNames){
//            System.out.println(fileName);
//        }
//            while ((s = br.readLine()) != null)
//                System.out.println("line: " + s);
//                process.waitFor();
//                System.out.println ("exit: " + process.exitValue());
//                process.destroy();

/*
* public class App {
    public static String findValue(String value, int i){
        int temp = i;
        while(value.charAt(temp)!=' '){
            temp++;
        }
        return value.substring(i,temp);
    }
    public static double findMemoryUsage(double totalMemory, long [] fieldValuesArray){
        double memoryUsage =0;
        //memoryUsage = ((data.getDataAndStack()+data.getResident())*100)/(double)totalMemory;
        memoryUsage = ((fieldValuesArray[8]+fieldValuesArray[9])*100)/(double)totalMemory;
        return memoryUsage;
    }
    public static double findCpuUsage(double uptime, long[] fieldValuesArray){
        double cpuUsage = 0;
        //long totalTime = data.getUserTime()+ data.getSystemTime()+ data.getChildUserTime()+ data.getChildSystemTime();
        long totalTime = fieldValuesArray[0]+fieldValuesArray[1]+fieldValuesArray[2]+fieldValuesArray[3];
        //double seconds = uptime - (data.getStartTime()/(double)100);
        double seconds = uptime - (fieldValuesArray[4]/(double)100);
        cpuUsage = 100 * ((totalTime/(double)100)/(double) seconds);
        return cpuUsage;
    }
    public static void main( String[] args )
    {
        String value;
        Process process;
        Data data = new Data();
        //String token = "MglMIe5ROZDDmdzIZGoUUhPtsct8Zdf_YKep_M1GQjEzRS8Akx5HbS6B1Sp-YYsULb_GSSDpzaS6FAlsTxqtkg==";
        String token = "eqWXHf4eBnGcaSg6x-n_JwnPM2gkL4MyOWlwVCsZzxOOhtowigqE5L4Z51IfV77eThUKuq4YLsIluhu7270xxw==";
        String bucket = "dbmonitor1";
        String org = "demo";
        while(true){
            try {
                process = Runtime.getRuntime().exec("pgrep influxd");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                value = bufferedReader.readLine();
                System.out.println(value);

                process = Runtime.getRuntime().exec("cat /proc/uptime");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String string = bufferedReader.readLine();
                String uptimeString = findValue(string,0);
                System.out.println("uptimeString = "+uptimeString);
                double uptime = Double.parseDouble(uptimeString);

                process = Runtime.getRuntime().exec("cat /proc/meminfo");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                string = bufferedReader.readLine();int i=0;
                System.out.println(string);
                while(i<string.length()){
                    if(Character.isDigit(string.charAt(i))){
                        break;
                    }
                    i++;
                }
                String totalMemoryString = string.substring(i,i+8);
                System.out.println(totalMemoryString);
                double totalMemory = Double.parseDouble(totalMemoryString);

                process = Runtime.getRuntime().exec("cat /proc/"+value+"/stat");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String statValue = bufferedReader.readLine();
                System.out.println(statValue);

                process = Runtime.getRuntime().exec("cat /proc/"+value+"/statm");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String statMemValue = bufferedReader.readLine();
                System.out.println(statMemValue);
//                int temp = 0;
//                int i =0;
//                long tempValue;
//                while(temp < 22){
//                    if(value.charAt(i)==' '){
//                        temp++;
//                    }
//                    else if(temp == 13 || temp ==14 || temp == 15 || temp ==16 || temp ==21){
//                        String tempString = findValue(value,i);
//                        i+=tempString.length();
//                        i--;
//                        tempValue = Long.parseLong(tempString);
//                        if(temp == 13){
//                            data.setUserTime(tempValue);
//                        }
//                        else if(temp == 14){
//                            data.setSystemTime(tempValue);
//                        }
//                        else if(temp ==15){
//                            data.setChildUserTime(tempValue);
//                        }
//                        else if(temp == 16){
//                            data.setChildSystemTime(tempValue);
//                        }
//                        else{
//                            data.setStartTime(tempValue);
//                        }
//                    }
//                    i++;
//                }
                String [] values = statValue.split(" ");
                String [] mValues = statMemValue.split(" ");
//                data.setUserTime(Long.parseLong(values[13]));
//                data.setSystemTime(Long.parseLong(values[14]));
//                data.setChildUserTime(Long.parseLong(values[15]));
//                data.setChildSystemTime(Long.parseLong(values[16]));
//                data.setPriority(Long.parseLong(values[17]));
//                data.setNice(Long.parseLong(values[18]));
//                data.setNoOfThreads(Long.parseLong(values[19]));
//                data.setStartTime(Long.parseLong(values[21]));
//                data.setResident(Long.parseLong(mValues[1]));
//                data.setDataAndStack(Long.parseLong(mValues[5]));
//                data.setMemUsage(findMemoryUsage(totalMemory,data));
//                data.setCpuUsage(findCpuUsage(uptime,data));
//                System.out.println(data.toString());
                //InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());
                InfluxDBClient client = InfluxDBClientFactory.create("http://172.22.0.2:8086", token.toCharArray());


                String [] fieldArray = {"user_time","system_time","child_user_time","child_system_time","start_time","priority","nice","no_of_threads",
                        "resident","data_stack","mem_usage","cpu_usage"};
                long [] fieldValuesArray = new long[10];
                double [] fieldPerArray = new double[2];
                int k=0;
                for(i=13;i<=19;i++){
                    fieldValuesArray[k]= Long.parseLong(values[i]);
                    k++;
                }
                fieldValuesArray[k++] = Long.parseLong(values[21]);
                fieldValuesArray[k++] = Long.parseLong(mValues[1]);
                fieldValuesArray[k++] = Long.parseLong(mValues[5]);
                k=0;
                fieldPerArray[k++] = findMemoryUsage(totalMemory,fieldValuesArray);
                fieldPerArray[k++] = findCpuUsage(uptime,fieldValuesArray);

                for(i=0;i<10;i++){
                    String outputData = "procstat,host=vishnu-12378 "+fieldArray[i]+"="+fieldValuesArray[i];
                    try (WriteApi writeApi = client.getWriteApi()) {
                        writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
                    }
                }
                k=0;
                for(i=10;i<=11;i++){
                    String outputData = "procstat,host=vishnu-12378 "+fieldArray[i]+"="+fieldPerArray[k++];
                    try (WriteApi writeApi = client.getWriteApi()) {
                        writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
                    }
                }


//                String outputData = "procstat,host=vishnu-12378 user_time="+data.getUserTime();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 system_time="+data.getSystemTime();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 child_user_time="+data.getChildUserTime();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 child_system_time="+data.getChildSystemTime();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 start_time="+data.getStartTime();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 priority="+data.getPriority();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 nice="+data.getNice();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 no_of_threads="+data.getNoOfThreads();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 resident="+data.getResident();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 data_stack="+data.getDataAndStack();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 mem_usage="+data.getMemUsage();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//                outputData = "procstat,host=vishnu-12378 cpu_usage="+data.getCpuUsage();
//                try (WriteApi writeApi = client.getWriteApi()) {
//                    writeApi.writeRecord(bucket, org, WritePrecision.NS, outputData);
//                }
//        }
//            while ((s = br.readLine()) != null)
//                System.out.println("line: " + s);
//                process.waitFor();
//                System.out.println ("exit: " + process.exitValue());
//                process.destroy();
            } catch (Exception e) {}
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
* */